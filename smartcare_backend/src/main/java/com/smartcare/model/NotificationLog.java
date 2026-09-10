package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationLog {
    @Id
    private String id;
    private String patientId;
    private String title;
    private String body;
    private String type; // APPOINTMENT_BOOKED, QUEUE_APPROACHING, QUEUE_DELAYED, CANCELLED
    @Builder.Default
    private boolean isRead = false;
    @Builder.Default
    private Instant sentAt = Instant.now();
}
