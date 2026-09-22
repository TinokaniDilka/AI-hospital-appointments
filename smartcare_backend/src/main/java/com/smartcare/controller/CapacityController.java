package com.smartcare.controller;

import com.smartcare.service.AutomatedSchedulingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/capacity")
@RequiredArgsConstructor
public class CapacityController {

    private final AutomatedSchedulingService automatedSchedulingService;

    @GetMapping("/remaining")
    public ResponseEntity<?> getRemainingSlots(
            @RequestParam String doctorId,
            @RequestParam String date
    ) {
        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            int remainingSlots = automatedSchedulingService.getRemainingSlots(doctorId, appointmentDate);
            return ResponseEntity.ok(new RemainingSlotsResponse(remainingSlots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class RemainingSlotsResponse {
        private int remainingSlots;

        public RemainingSlotsResponse(int remainingSlots) {
            this.remainingSlots = remainingSlots;
        }
    }
}
