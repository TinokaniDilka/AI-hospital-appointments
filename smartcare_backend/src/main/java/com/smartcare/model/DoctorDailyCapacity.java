package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctor_daily_capacity")
public class DoctorDailyCapacity {
    @Id
    private String id;
    
    @Indexed
    private String doctorId;
    
    @Indexed
    private LocalDate date;
    
    private int dailyCapacity; // Maximum patients per day
    private int bookedSlots; // Number of confirmed appointments
    private int waitlistedCount; // Number of waitlisted appointments
    
    @Builder.Default
    private boolean isActive = true;
    
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    private Instant updatedAt = Instant.now();
    
    public int getRemainingSlots() {
        return Math.max(0, dailyCapacity - bookedSlots);
    }
    
    public boolean isFull() {
        return bookedSlots >= dailyCapacity;
    }
}
