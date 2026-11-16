package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

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
    private String email;
    @org.springframework.data.annotation.Transient
    private String password;
    private String specialization;
    private String departmentId;
    private String departmentName;
    private String branchId;
    private String branchName;
    private String roomNumber;
    private double consultationFee;
    @Builder.Default
    private int avgConsultationMinutes = 15;
    
    // Daily patient capacity (set by doctor)
    @Builder.Default
    private int maxPatientsPerDay = 25;
    
    // Working schedule
    private java.util.List<String> workingDays; // ["MONDAY", "TUESDAY", ...]
    private String startTime; // "09:00"
    private String endTime; // "17:00"
    
    // Leave dates
    private java.util.List<String> leaveDates; // ["2026-09-15", "2026-09-16"]
    
    private String qualification;
    private String experienceYears;
    @Builder.Default
    private boolean active = true;
}
