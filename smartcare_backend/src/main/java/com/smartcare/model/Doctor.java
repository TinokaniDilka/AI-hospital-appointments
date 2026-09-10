package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctors")
public class Doctor {
    @Id
    private String id;
    private String userId;
    private String doctorName;
    private String specialization;
    private String departmentId;
    private String departmentName;
    private String branchId;
    private String branchName;
    private String roomNumber;
    private double consultationFee;
    @Builder.Default
    private int avgConsultationMinutes = 15;
    private String qualification;
    private String experienceYears;
    private boolean active;
}
