package com.smartcare.controller;

import com.smartcare.model.Appointment;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.security.JwtTokenProvider;
import com.smartcare.service.AppointmentSchedulerService;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentSchedulerService appointmentSchedulerService;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Data
    public static class BookingRequest {
        @NotBlank(message = "Patient ID is required")
        private String patientId;
        @NotBlank(message = "Patient name is required")
        private String patientName;
        @NotBlank(message = "Doctor ID is required")
        private String doctorId;
        @NotBlank(message = "Preferred date is required")
        private String preferredDate;
        @NotBlank(message = "Time slot is required")
        private String timeSlot;
        private String paymentStatus = "PENDING";
        private double totalAmount = 100.0;
    }

    @Data
    public static class PaymentConfirmationRequest {
        @NotBlank(message = "Appointment ID is required")
        private String appointmentId;
        private String paymentMethod;
    }

    @Data
    public static class CancelRequest {
        private String reason;
    }

    @Data
    public static class CompleteConsultationRequest {
        private String notes;
    }

    @Data
    public static class StatusUpdateRequest {
        private String status;
    }

    @Data
    public static class RescheduleRequest {
        @NotBlank(message = "New date is required")
        private String newDate;
        @NotBlank(message = "New time slot is required")
        private String newTimeSlot;
    }

    private String extractPatientId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return jwtTokenProvider.getClaimsFromToken(token).get("patientId", String.class);
            } catch (Exception e) {
                // Token parsing failed
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String resolvedPatientId = patientId;
        if ((resolvedPatientId == null || resolvedPatientId.isEmpty()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            String tokenPatientId = extractPatientId(authHeader);
            if (tokenPatientId != null) {
                resolvedPatientId = tokenPatientId;
            }
        }
        
        if (resolvedPatientId != null && !resolvedPatientId.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByPatientId(resolvedPatientId));
        }
        if (doctorId != null && !doctorId.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
        }
        if (date != null && !date.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByScheduledDate(date));
        }
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByStatus(status));
        }
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam String doctorId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody BookingRequest req) {
        try {
            Appointment apt = Appointment.builder()
                    .appointmentId("APT-" + System.currentTimeMillis())
                    .patientId(req.getPatientId())
                    .patientName(req.getPatientName())
                    .doctorId(req.getDoctorId())
                    .preferredDate(req.getPreferredDate())
                    .timeSlot(req.getTimeSlot())
                    .status("WAITING_FOR_SCHEDULING")
                    .paymentStatus(req.getPaymentStatus())
                    .totalAmount(req.getTotalAmount())
                    .amountPaid(0.0)
                    .createdAt(Instant.now())
                    .build();
            
            List<Appointment.StatusHistory> history = new java.util.ArrayList<>();
            history.add(new Appointment.StatusHistory(
                    "WAITING_FOR_SCHEDULING", 
                    "Appointment booked, waiting for payment confirmation", 
                    Instant.now(), 
                    "PATIENT"
            ));
            apt.setStatusHistory(history);
            
            Appointment saved = appointmentRepository.save(apt);
            appointmentSchedulerService.scheduleAppointmentsForDoctor(req.getDoctorId());
            
            Appointment updated = appointmentRepository.findById(saved.getId())
                    .orElse(saved);
            
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentConfirmationRequest req) {
        Appointment apt = appointmentRepository.findById(req.getAppointmentId()).orElse(null);
        if (apt == null) {
            apt = appointmentRepository.findAll().stream()
                    .filter(a -> req.getAppointmentId().equals(a.getAppointmentId()))
                    .findFirst()
                    .orElse(null);
        }
        if (apt == null) {
            return ResponseEntity.notFound().build();
        }

        apt.setPaymentStatus("PAID");
        apt.setAmountPaid(apt.getTotalAmount());
        
        List<Appointment.StatusHistory> history = apt.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        history.add(new Appointment.StatusHistory(
                "PAYMENT_CONFIRMED", 
                "Payment confirmed by admin", 
                Instant.now(), 
                "ADMIN"
        ));
        apt.setStatusHistory(history);
        
        return ResponseEntity.ok(appointmentRepository.save(apt));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable String id, @RequestBody(required = false) CancelRequest req) {
        try {
            String reason = req != null ? req.getReason() : "Patient requested cancellation";
            Appointment apt = appointmentService.cancelAppointment(id, reason);
            
            apt.setStatus("CANCELLED");
            apt.setCancellationReason(reason);
            
            List<Appointment.StatusHistory> history = apt.getStatusHistory();
            if (history == null) {
                history = new java.util.ArrayList<>();
            }
            history.add(new Appointment.StatusHistory(
                    "CANCELLED", 
                    reason, 
                    Instant.now(), 
                    "PATIENT"
            ));
            apt.setStatusHistory(history);
            
            Appointment saved = appointmentRepository.save(apt);
            notificationService.sendAppointmentCancelled(saved, reason);
            
            if (apt.getScheduledDate() != null) {
                appointmentSchedulerService.scheduleAppointmentsForDoctor(apt.getDoctorId());
            }
            
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeConsultation(
            @PathVariable String id,
            @RequestBody(required = false) CompleteConsultationRequest req
    ) {
        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) {
            apt = appointmentRepository.findAll().stream()
                    .filter(a -> id.equals(a.getAppointmentId()))
                    .findFirst()
                    .orElse(null);
        }
        if (apt == null) {
            return ResponseEntity.notFound().build();
        }

        apt.setStatus("COMPLETED");
        apt.setCompletedAt(Instant.now());
        
        List<Appointment.StatusHistory> history = apt.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        history.add(new Appointment.StatusHistory(
                "COMPLETED", 
                req != null && req.getNotes() != null ? req.getNotes() : "Consultation completed", 
                Instant.now(), 
                "DOCTOR"
        ));
        apt.setStatusHistory(history);
        
        Appointment saved = appointmentRepository.save(apt);
        notificationService.sendAppointmentCompleted(saved);
        appointmentSchedulerService.scheduleAppointmentsForDoctor(apt.getDoctorId());
        
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable String id, @RequestBody StatusUpdateRequest req) {
        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        String newStatus = req.getStatus().trim().toUpperCase();
        
        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) {
            apt = appointmentRepository.findAll().stream()
                    .filter(a -> id.equals(a.getAppointmentId()))
                    .findFirst()
                    .orElse(null);
        }
        if (apt == null) {
            return ResponseEntity.notFound().build();
        }

        apt.setStatus(newStatus);
        
        List<Appointment.StatusHistory> history = apt.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        history.add(new Appointment.StatusHistory(
                newStatus, 
                "Status updated by system", 
                Instant.now(), 
                "SYSTEM"
        ));
        apt.setStatusHistory(history);
        
        Appointment saved = appointmentRepository.save(apt);
        return ResponseEntity.ok(saved);
    }

   @GetMapping("/doctors/{id}/daily-counts")
public ResponseEntity<Map<String, Integer>> getDailyAppointmentCounts(
        @PathVariable String id,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
) {
        if (startDate == null || startDate.isEmpty()) {
            startDate = java.time.LocalDate.now().toString();
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = java.time.LocalDate.now().plusDays(6).toString();
        }
        
       Map<String, Integer> counts =
        appointmentSchedulerService.getDailyAppointmentCounts(
                id, startDate, endDate
        );

return ResponseEntity.ok(counts);
}

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable String id,
            @Valid @RequestBody RescheduleRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        final String tokenPatientId = extractPatientId(authHeader);

        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) {
            apt = appointmentRepository.findAll().stream()
                    .filter(a -> id.equals(a.getAppointmentId()))
                    .findFirst()
                    .orElse(null);
        }
        if (apt == null) {
            return ResponseEntity.notFound().build();
        }

        if (tokenPatientId != null && !tokenPatientId.equals(apt.getPatientId())) {
            return ResponseEntity.status(403).body("You can only reschedule your own appointments");
        }

        if ("COMPLETED".equals(apt.getStatus()) || "CANCELLED".equals(apt.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot reschedule completed or cancelled appointments");
        }

        String oldDate = apt.getScheduledDate() != null ? apt.getScheduledDate() : apt.getPreferredDate();
        String oldTime = apt.getTimeSlot();
        
        apt.setPreferredDate(req.getNewDate());
        apt.setScheduledDate(req.getNewDate());
        apt.setTimeSlot(req.getNewTimeSlot());
        apt.setStatus("RESCHEDULED");
        
        List<Appointment.StatusHistory> history = apt.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        history.add(new Appointment.StatusHistory(
                "RESCHEDULED", 
                String.format("Rescheduled from %s %s to %s %s", oldDate, oldTime, req.getNewDate(), req.getNewTimeSlot()), 
                Instant.now(), 
                "PATIENT"
        ));
        apt.setStatusHistory(history);
        
        Appointment saved = appointmentRepository.save(apt);
        appointmentSchedulerService.scheduleAppointmentsForDoctor(apt.getDoctorId());
        notificationService.sendRescheduledNotification(saved, oldDate, req.getNewDate());
        
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkInAppointment(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        final String tokenPatientId = extractPatientId(authHeader);

        Appointment apt = appointmentRepository.findById(id).orElse(null);
        if (apt == null) {
            apt = appointmentRepository.findAll().stream()
                    .filter(a -> id.equals(a.getAppointmentId()))
                    .findFirst()
                    .orElse(null);
        }
        if (apt == null) {
            return ResponseEntity.notFound().build();
        }

        if (tokenPatientId != null && !tokenPatientId.equals(apt.getPatientId())) {
            return ResponseEntity.status(403).body("You can only check in to your own appointments");
        }

        if (!"CONFIRMED".equals(apt.getStatus()) && !"RESCHEDULED".equals(apt.getStatus())) {
            return ResponseEntity.badRequest().body("Appointment must be confirmed to check in");
        }

        String today = java.time.LocalDate.now().toString();
        String appointmentDate = apt.getScheduledDate() != null ? apt.getScheduledDate() : apt.getPreferredDate();
        if (!today.equals(appointmentDate)) {
            return ResponseEntity.badRequest().body("You can only check in on the day of your appointment");
        }

        apt.setStatus("CHECKED_IN");
        
        List<Appointment.StatusHistory> history = apt.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        history.add(new Appointment.StatusHistory(
                "CHECKED_IN", 
                "Patient checked in at the hospital", 
                Instant.now(), 
                "PATIENT"
        ));
        apt.setStatusHistory(history);
        
        Appointment saved = appointmentRepository.save(apt);
        appointmentSchedulerService.scheduleAppointmentsForDoctor(apt.getDoctorId());
        
        return ResponseEntity.ok(saved);
    }
}