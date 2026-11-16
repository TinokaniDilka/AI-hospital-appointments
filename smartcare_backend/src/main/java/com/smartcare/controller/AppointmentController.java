package com.smartcare.controller;

import com.smartcare.model.Appointment;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.AppointmentSchedulerService;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (patientId != null && !patientId.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
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
            // Create appointment with WAITING_FOR_SCHEDULING status
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
            
            // Add initial status history
            apt.setStatusHistory(new java.util.ArrayList<>());
            apt.getStatusHistory().add(new Appointment.StatusHistory(
                    "WAITING_FOR_SCHEDULING", 
                    "Appointment booked, waiting for payment confirmation", 
                    Instant.now(), 
                    "PATIENT"
            ));
            
            Appointment saved = appointmentRepository.save(apt);
            
            // Trigger scheduling immediately to assign time slot and queue number
            appointmentSchedulerService.scheduleAppointmentsForDoctor(req.getDoctorId());
            
            // Fetch the updated appointment with assigned time slot and queue number
            Appointment updated = appointmentRepository.findById(saved.getId())
                    .orElse(saved);
            
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentConfirmationRequest req) {
        return appointmentRepository.findById(req.getAppointmentId())
                .or(() -> appointmentRepository.findAll().stream()
                        .filter(a -> req.getAppointmentId().equals(a.getAppointmentId()))
                        .findFirst())
                .map(apt -> {
                    apt.setPaymentStatus("PAID");
                    apt.setAmountPaid(apt.getTotalAmount());
                    // Keep appointment status as WAITING_FOR_SCHEDULING, don't change it
                    // Don't trigger scheduling - admin will trigger it separately
                    
                    // Add status history
                    if (apt.getStatusHistory() == null) {
                        apt.setStatusHistory(new java.util.ArrayList<>());
                    }
                    apt.getStatusHistory().add(new Appointment.StatusHistory(
                            "PAYMENT_CONFIRMED", 
                            "Payment confirmed by admin", 
                            Instant.now(), 
                            "ADMIN"
                    ));
                    
                    return ResponseEntity.ok(appointmentRepository.save(apt));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable String id, @RequestBody(required = false) CancelRequest req) {
        try {
            String reason = req != null ? req.getReason() : "Patient requested cancellation";
            Appointment apt = appointmentService.cancelAppointment(id, reason);
            
            // Update status to CANCELLED
            apt.setStatus("CANCELLED");
            apt.setCancellationReason(reason);
            
            if (apt.getStatusHistory() == null) {
                apt.setStatusHistory(new java.util.ArrayList<>());
            }
            apt.getStatusHistory().add(new Appointment.StatusHistory(
                    "CANCELLED", 
                    reason, 
                    Instant.now(), 
                    "PATIENT"
            ));
            
            Appointment saved = appointmentRepository.save(apt);
            notificationService.sendAppointmentCancelled(saved, reason);
            
            // If a confirmed appointment was cancelled, reschedule waitlisted appointments
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
        return appointmentRepository.findById(id)
                .or(() -> appointmentRepository.findAll().stream()
                        .filter(a -> id.equals(a.getAppointmentId()))
                        .findFirst())
                .map(apt -> {
                    apt.setStatus("COMPLETED");
                    apt.setCompletedAt(Instant.now());
                    
                    if (apt.getStatusHistory() == null) {
                        apt.setStatusHistory(new java.util.ArrayList<>());
                    }
                    apt.getStatusHistory().add(new Appointment.StatusHistory(
                            "COMPLETED", 
                            req != null && req.getNotes() != null ? req.getNotes() : "Consultation completed", 
                            Instant.now(), 
                            "DOCTOR"
                    ));
                    
                    Appointment saved = appointmentRepository.save(apt);
                    notificationService.sendAppointmentCompleted(saved);
                    
                    // Reschedule waitlisted appointments
                    appointmentSchedulerService.scheduleAppointmentsForDoctor(apt.getDoctorId());
                    
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class StatusUpdateRequest {
        private String status;
    }

    // Manual status update - restricted to SYSTEM only (for scheduled jobs)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable String id, @RequestBody StatusUpdateRequest req) {
        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        String newStatus = req.getStatus().trim().toUpperCase();
        return appointmentRepository.findById(id)
                .or(() -> appointmentRepository.findAll().stream().filter(a -> id.equals(a.getAppointmentId())).findFirst())
                .map(apt -> {
                    apt.setStatus(newStatus);
                    
                    if (apt.getStatusHistory() == null) {
                        apt.setStatusHistory(new java.util.ArrayList<>());
                    }
                    apt.getStatusHistory().add(new Appointment.StatusHistory(
                            newStatus, 
                            "Status updated by system", 
                            Instant.now(), 
                            "SYSTEM"
                    ));
                    
                    Appointment saved = appointmentRepository.save(apt);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
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
        
        Map<String, Integer> counts = appointmentSchedulerService.getDailyAppointmentCounts(id, startDate, endDate);
        return ResponseEntity.ok(counts);
    }
}
