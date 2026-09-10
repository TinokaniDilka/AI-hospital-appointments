package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "queues")
public class QueueState {
    @Id
    private String id;
    private String doctorId;
    private String doctorName;
    private String departmentName;
    private String date; // YYYY-MM-DD
    @Builder.Default
    private int currentServingNumber = 0;
    @Builder.Default
    private List<QueueItem> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueItem {
        private String appointmentId;
        private String patientId;
        private String patientName;
        private int queueNumber;
        private int position;
        private String status; // WAITING, APPROACHING, NOW_SERVING, COMPLETED, CANCELLED, NO_SHOW
        private boolean isPriority;
        private double predictedWaitMinutes;
        private String formattedWaitRange;
    }
}
