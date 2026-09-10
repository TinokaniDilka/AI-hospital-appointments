package com.smartcare.service;

import com.smartcare.model.Prediction;
import com.smartcare.repository.PredictionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AIService {

    private final WebClient webClient;
    private final PredictionRepository predictionRepository;

    public AIService(
            PredictionRepository predictionRepository,
            @Value("${smartcare.ai.service-url:http://localhost:8000}") String aiServiceUrl
    ) {
        this.predictionRepository = predictionRepository;
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    @Data
    public static class AIPredictionRequest {
        private String doctorId;
        private String department;
        private int queuePosition;
        private int patientsAhead;
        private int hourOfDay = 10;
        private int dayOfWeek = 1;
        private double avgConsultationTime = 15.0;
        private int activePriorityCount = 0;
        private double historicalDelayFactor = 1.0;
        private double noShowRate = 0.1;
    }

    @Data
    public static class AIPredictionResponse {
        private double predictedWaitMinutes;
        private double minWaitMinutes;
        private double maxWaitMinutes;
        private double confidenceScore;
        private String formattedRange;
        private String status;
    }

    public AIPredictionResponse getPredictedWaitTime(
            String doctorId,
            String department,
            int queuePosition,
            int patientsAhead,
            double avgConsultationTime,
            int priorityCount
    ) {
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("doctor_id", doctorId != null ? doctorId : "DOC_101");
        reqBody.put("department", department != null ? department : "General Medicine");
        reqBody.put("queue_position", Math.max(1, queuePosition));
        reqBody.put("patients_ahead", Math.max(0, patientsAhead));
        reqBody.put("hour_of_day", 10);
        reqBody.put("day_of_week", 1);
        reqBody.put("avg_consultation_time", avgConsultationTime > 0 ? avgConsultationTime : 15.0);
        reqBody.put("active_priority_count", priorityCount);
        reqBody.put("historical_delay_factor", 1.05);
        reqBody.put("no_show_rate", 0.10);

        AIPredictionResponse response;
        String modelStatus = "SUCCESS";

        try {
            response = webClient.post()
                    .uri("/predict-wait-time")
                    .bodyValue(reqBody)
                    .retrieve()
                    .bodyToMono(AIPredictionResponse.class)
                    .block();

            if (response != null) {
                // Save prediction to database
                savePrediction(doctorId, department, response, modelStatus);
                return response;
            }
        } catch (Exception e) {
            log.warn("Python AI service call failed/offline. Falling back to heuristic estimation. Error: {}", e.getMessage());
        }

        // Fallback robust heuristic estimation if AI service is offline
        double fallbackWait = Math.max(0, patientsAhead) * (avgConsultationTime > 0 ? avgConsultationTime : 15.0) + (priorityCount * 18.0);
        double minW = Math.max(0, fallbackWait - 5);
        double maxW = fallbackWait + 10;
        
        AIPredictionResponse fallbackRes = new AIPredictionResponse();
        fallbackRes.setPredictedWaitMinutes(fallbackWait);
        fallbackRes.setMinWaitMinutes(minW);
        fallbackRes.setMaxWaitMinutes(maxW);
        fallbackRes.setConfidenceScore(0.85);
        fallbackRes.setFormattedRange(String.format("%.0f–%.0f mins", minW, maxW));
        fallbackRes.setStatus("FALLBACK_HEURISTIC");
        
        // Save fallback prediction to database
        savePrediction(doctorId, department, fallbackRes, "FALLBACK_HEURISTIC");
        
        return fallbackRes;
    }

    private void savePrediction(String doctorId, String department, AIPredictionResponse response, String modelStatus) {
        try {
            Prediction prediction = Prediction.builder()
                    .doctorId(doctorId)
                    .department(department)
                    .predictedWaitMinutes(response.getPredictedWaitMinutes())
                    .minWaitMinutes(response.getMinWaitMinutes())
                    .maxWaitMinutes(response.getMaxWaitMinutes())
                    .confidenceScore(response.getConfidenceScore())
                    .formattedRange(response.getFormattedRange())
                    .modelStatus(modelStatus)
                    .build();
            
            predictionRepository.save(prediction);
            log.debug("Saved prediction to database: doctorId={}, predictedWait={}", doctorId, response.getPredictedWaitMinutes());
        } catch (Exception e) {
            log.error("Failed to save prediction to database: {}", e.getMessage());
        }
    }
}
