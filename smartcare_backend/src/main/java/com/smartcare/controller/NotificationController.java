package com.smartcare.controller;

import com.smartcare.model.NotificationLog;
import com.smartcare.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationRepository;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<NotificationLog>> getPatientNotifications(@PathVariable String patientId) {
        return ResponseEntity.ok(notificationRepository.findByPatientIdOrderBySentAtDesc(patientId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }
}
