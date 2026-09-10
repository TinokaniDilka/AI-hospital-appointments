package com.smartcare.controller;

import com.smartcare.service.AIService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AIProxyController {

    private final AIService aiService;
    private final WebClient webClient;

    public AIProxyController(
            AIService aiService,
            @Value("${smartcare.ai.service-url:http://localhost:8000}") String aiServiceUrl
    ) {
        this.aiService = aiService;
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    @Data
    public static class PredictWaitTimeRequest {
        private String doctorId;
        private String department;
        private int queuePosition;
        private int patientsAhead;
        private double avgConsultationTime = 15.0;
        private int priorityCount = 0;
    }

    @PostMapping("/predict-wait-time")
    public ResponseEntity<AIService.AIPredictionResponse> predictWaitTime(@RequestBody PredictWaitTimeRequest req) {
        AIService.AIPredictionResponse res = aiService.getPredictedWaitTime(
                req.getDoctorId(),
                req.getDepartment(),
                req.getQueuePosition(),
                req.getPatientsAhead(),
                req.getAvgConsultationTime(),
                req.getPriorityCount()
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/retrain")
    public ResponseEntity<Map<String, String>> retrainModel() {
        Map<String, String> response = new HashMap<>();
        try {
            // Trigger retraining on the Python AI service
            webClient.post()
                    .uri("/retrain")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            response.put("status", "success");
            response.put("message", "Model retraining initiated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to initiate model retraining: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getModelMetrics() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Fetch metrics from the Python AI service
            Map<String, Object> metrics = webClient.get()
                    .uri("/metrics")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (metrics != null) {
                response.put("status", "success");
                response.put("metrics", metrics);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No metrics available");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch model metrics: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
