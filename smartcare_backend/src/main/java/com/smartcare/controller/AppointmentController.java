package com.smartcare.controller;

import com.smartcare.model.Appointment;
import com.smartcare.model.AppointmentStatus;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.AutomatedSchedulingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final AutomatedSchedulingService automatedSchedulingService;

    @Data
    public static class BookingRequest {
        @NotBlank(message = "Patient ID is required")
        private String patientId;
        @NotBlank(message = "Patient name is required")
        private String patientName;
        @NotBlank(message = "Doctor ID is required")
        private String doctorId;
        @NotBlank(message = "Date is required")
        private String date;
        @NotBlank(message = "Time slot is required")
        private String timeSlot;
    }

    @Data
    public static class CancelRequest {
        private String reason;
    }

    @Data
    public static class RescheduleRequest {
        @NotBlank(message = "New date is required")
        private String newDate;
        @NotBlank(message = "New time slot is required")
        private String newTimeSlot;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String date,
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
            return ResponseEntity.ok(appointmentRepository.findByAppointmentDate(date));
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
            Appointment apt = appointmentService.bookAppointment(
                    req.getPatientId(), req.getPatientName(), req.getDoctorId(), req.getDate(), req.getTimeSlot());
            return ResponseEntity.ok(apt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable String id, @RequestBody(required = false) CancelRequest req) {
        try {
            String reason = req != null ? req.getReason() : "Patient requested cancellation";
            Appointment apt = appointmentService.cancelAppointment(id, reason);
            return ResponseEntity.ok(apt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> processPayment(@PathVariable String id) {
        try {
            automatedSchedulingService.processAfterPayment(id);
            Appointment apt = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            return ResponseEntity.ok(apt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable String id) {
        try {
            Appointment apt = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            if (apt.getStatus() != AppointmentStatus.IN_PROGRESS) {
                return ResponseEntity.badRequest().body("Appointment must be IN_PROGRESS to complete");
            }
            
            apt.setStatus(AppointmentStatus.COMPLETED);
            Appointment saved = appointmentRepository.save(apt);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable String id, @RequestBody StatusUpdateRequest req) {
        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        String newStatus = req.getStatus().trim().toUpperCase();
        return appointmentRepository.findById(id)
                .or(() -> appointmentRepository.findAll().stream().filter(a -> id.equals(a.getAppointmentId())).findFirst())
                .map(apt -> {
                    try {
                        apt.setStatus(AppointmentStatus.valueOf(newStatus));
                    } catch (IllegalArgumentException e) {
                        apt.setStatus(AppointmentStatus.WAITING_FOR_SCHEDULING);
                    }
                    Appointment saved = appointmentRepository.save(apt);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class StatusUpdateRequest {
        private String status;
    }
}
