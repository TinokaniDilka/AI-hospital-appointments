package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;
    private String appointmentId; // e.g. "APT-10024"
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String branchId;
    private String branchName;
    private String departmentId;
    private String departmentName;
    
    // Preferred date by patient
    private String preferredDate; // YYYY-MM-DD
    private String timeSlot; // "10:20 - 10:40"
    
    // Scheduled date (assigned by system)
    private String scheduledDate; // YYYY-MM-DD
    
    // Status: WAITING_FOR_SCHEDULING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, WAITLISTED
    private String status;
    
    // Queue position if waitlisted
    private Integer queuePosition;
    
    @Builder.Default
    private boolean isPriority = false;
    private String priorityReason;
    private String cancellationReason;
    
    // Payment
    @Builder.Default
    private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED, REFUNDED
    @Builder.Default
    private double amountPaid = 0.0;
    @Builder.Default
    private double totalAmount = 100.0;
    
    // Timestamps
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant scheduledAt;
    private Instant confirmedAt;
    private Instant completedAt;
    
    // Status history for audit trail
    private java.util.List<StatusHistory> statusHistory;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private String status;
        private String reason;
        private Instant timestamp;
        private String changedBy; // SYSTEM, DOCTOR, PATIENT, ADMIN
    }
}
