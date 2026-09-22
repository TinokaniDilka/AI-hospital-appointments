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
    private String appointmentDate; // YYYY-MM-DD
    private String timeSlot; // "10:20 - 10:40"
    private AppointmentStatus status;
    private int queuePosition; // Position in waitlist
    private int scheduledSlotNumber; // Assigned slot number for confirmed appointments
    @Builder.Default
    private boolean isPriority = false;
    private String priorityReason;
    private String cancellationReason;
    @Builder.Default
    private String paymentStatus = "UNPAID";
    @Builder.Default
    private double amountPaid = 0.0;
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant scheduledAt = null;
    @Builder.Default
    private Instant confirmedAt = null;
    @Builder.Default
    private Instant completedAt = null;
}
