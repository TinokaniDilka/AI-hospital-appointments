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
@Document(collection = "predictions")
public class Prediction {
    @Id
    private String id;
    private String doctorId;
    private String patientId;
    private String appointmentId;
    private String department;
    private double predictedWaitMinutes;
    private double minWaitMinutes;
    private double maxWaitMinutes;
    private double confidenceScore;
    private String formattedRange;
    private String modelStatus; // "SUCCESS" or "FALLBACK_HEURISTIC"
    @Builder.Default
    private Instant createdAt = Instant.now();
}
