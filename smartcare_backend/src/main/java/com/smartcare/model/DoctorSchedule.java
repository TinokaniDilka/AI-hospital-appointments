package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedules")
public class DoctorSchedule {
    @Id
    private String id;
    private String doctorId;
    private List<String> workingDays; // e.g. ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
    private String startTime; // "09:00"
    private String endTime;   // "17:00"
    @Builder.Default
    private int slotDurationMinutes = 20;
    @Builder.Default
    private int maxPatientsPerDay = 25;
    private int dailyCapacity; // Maximum patients per day for automated scheduling
    private String breakStartTime; // "13:00"
    private String breakEndTime;   // "14:00"
    private List<String> leaveDates; // ISO format "2026-09-15"
}
