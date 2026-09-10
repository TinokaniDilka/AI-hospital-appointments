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
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    private String actorId;
    private String actorRole;
    private String action; // SCHEDULE_UPDATE, APPOINTMENT_CANCEL, QUEUE_PRIORITY_SET, DOCTOR_DEACTIVATE
    private String entityType;
    private String entityId;
    private String details;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
