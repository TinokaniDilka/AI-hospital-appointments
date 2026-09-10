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
    private String status; // BOOKED, IN_QUEUE, NOW_SERVING, COMPLETED, CANCELLED, NO_SHOW
    private int queueNumber;
    @Builder.Default
    private boolean isPriority = false;
    private String priorityReason;
    private String cancellationReason;
    @Builder.Default
    private Instant createdAt = Instant.now();
}
