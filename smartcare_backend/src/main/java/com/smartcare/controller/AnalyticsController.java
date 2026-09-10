package com.smartcare.controller;

import com.smartcare.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsService.AnalyticsSummary> getDashboardAnalytics() {
        return ResponseEntity.ok(analyticsService.getHospitalAnalytics());
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsService.AnalyticsSummary> getSummaryAnalytics() {
        return ResponseEntity.ok(analyticsService.getHospitalAnalytics());
    }

    @GetMapping("/trends")
    public ResponseEntity<?> getAppointmentTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "daily") String bucket // 'daily' or 'weekly'
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return ResponseEntity.ok(analyticsService.getAppointmentTrends(startDate, endDate, bucket));
    }
}
