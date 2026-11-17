package com.smartcare.controller;

import com.smartcare.model.Doctor;
import com.smartcare.model.QueueState;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.service.QueueService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final DoctorRepository doctorRepository;

    @Data
    public static class StatusUpdateRequest {
        private String appointmentId;
        private String status; // WAITING, APPROACHING, NOW_SERVING, COMPLETED, CANCELLED, NO_SHOW
    }

    @Data
    public static class PriorityRequest {
        private String appointmentId;
        private boolean isPriority;
        private String reason;
    }

    @GetMapping("/{doctorId}/today")
    public ResponseEntity<QueueState> getTodayQueue(
            @PathVariable String doctorId,
            @RequestParam(required = false) String date
    ) {
        return ResponseEntity.ok(queueService.getOrCreateQueueForDoctor(doctorId, date));
    }

    @PostMapping("/{doctorId}/call-next")
    public ResponseEntity<QueueState> callNext(
            @PathVariable String doctorId,
            @RequestParam(required = false) String date
    ) {
        return ResponseEntity.ok(queueService.callNextPatient(doctorId, date));
    }

    @PostMapping("/{doctorId}/status")
    public ResponseEntity<QueueState> updateStatus(
            @PathVariable String doctorId,
            @RequestParam(required = false) String date,
            @RequestBody StatusUpdateRequest req
    ) {
        return ResponseEntity.ok(queueService.setPatientStatus(doctorId, date, req.getAppointmentId(), req.getStatus()));
    }

    @PostMapping("/{doctorId}/priority")
    public ResponseEntity<QueueState> setPriority(
            @PathVariable String doctorId,
            @RequestParam(required = false) String date,
            @RequestBody PriorityRequest req
    ) {
        return ResponseEntity.ok(queueService.togglePriority(doctorId, date, req.getAppointmentId(), req.isPriority(), req.getReason()));
    }

    @PostMapping("/{doctorId}/session")
    public ResponseEntity<?> toggleSession(@PathVariable String doctorId) {
        System.out.println("Toggle session request for doctor: " + doctorId);
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> {
                        System.err.println("Doctor not found: " + doctorId);
                        return new IllegalArgumentException("Doctor not found with ID: " + doctorId);
                    });
            
            boolean newStatus = !doctor.isSessionActive();
            doctor.setSessionActive(newStatus);
            Doctor saved = doctorRepository.save(doctor);
            
            System.out.println("Session toggled for doctor " + doctorId + ". New status: " + saved.isSessionActive());
            
            return ResponseEntity.ok(Map.of(
                "sessionActive", saved.isSessionActive(),
                "message", saved.isSessionActive() ? "Session started" : "Session ended"
            ));
        } catch (Exception e) {
            System.err.println("Error toggling session: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to toggle session",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{doctorId}/session")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@PathVariable String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        
        return ResponseEntity.ok(Map.of(
            "sessionActive", doctor.isSessionActive()
        ));
    }
}
