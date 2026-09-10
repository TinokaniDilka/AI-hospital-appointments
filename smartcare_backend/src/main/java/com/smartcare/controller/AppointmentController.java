package com.smartcare.controller;

import com.smartcare.model.Appointment;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.AppointmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;

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
            @RequestParam(required = false) String date
    ) {
        if (patientId != null && !patientId.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
        }
        if (doctorId != null && !doctorId.isEmpty() && date != null && !date.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date));
        }
        if (doctorId != null && !doctorId.isEmpty()) {
            return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
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

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable String id, @Valid @RequestBody RescheduleRequest req) {
        try {
            Appointment apt = appointmentService.rescheduleAppointment(id, req.getNewDate(), req.getNewTimeSlot());
            return ResponseEntity.ok(apt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
