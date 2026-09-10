package com.smartcare.controller;

import com.smartcare.model.QueueState;
import com.smartcare.service.QueueService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

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
}
