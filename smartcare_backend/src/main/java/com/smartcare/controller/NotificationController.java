package com.smartcare.controller;

import com.smartcare.model.NotificationLog;
import com.smartcare.repository.NotificationLogRepository;
import com.smartcare.service.FirebaseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationRepository;
    private final FirebaseService firebaseService;

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

    @PostMapping
    public ResponseEntity<?> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        NotificationLog notification = new NotificationLog();
        notification.setPatientId(request.getPatientId());
        notification.setTitle(request.getTitle());
        notification.setBody(request.getMessage());
        notification.setType(request.getType());
        notification.setRead(false);
        
        NotificationLog saved = notificationRepository.save(notification);
        
        if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
            firebaseService.sendPushNotification(request.getFcmToken(), request.getTitle(), request.getMessage());
        }
        
        return ResponseEntity.ok(saved);
    }

    @Data
    public static class SendNotificationRequest {
        @NotBlank(message = "Patient ID is required")
        private String patientId;
        @NotBlank(message = "Title is required")
        private String title;
        @NotBlank(message = "Message is required")
        private String message;
        private String type;
        private String fcmToken;
    }
}
