package com.smartcare.scheduler;

import com.smartcare.model.Appointment;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentStatusScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Runs every hour to update appointment statuses based on dates
     * - CONFIRMED appointments on today's date -> IN_PROGRESS
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    public void updateAppointmentStatuses() {
        log.info("Running appointment status update scheduler");
        
        String today = LocalDate.now().format(DATE_FORMATTER);
        
        // Update CONFIRMED appointments scheduled for today to IN_PROGRESS
        List<Appointment> confirmedToday = appointmentRepository.findByScheduledDate(today);
        confirmedToday.stream()
                .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                .forEach(apt -> {
                    apt.setStatus("IN_PROGRESS");
                    
                    if (apt.getStatusHistory() == null) {
                        apt.setStatusHistory(new java.util.ArrayList<>());
                    }
                    apt.getStatusHistory().add(new Appointment.StatusHistory(
                            "IN_PROGRESS", 
                            "Appointment date arrived, status updated automatically", 
                            Instant.now(), 
                            "SYSTEM"
                    ));
                    
                    appointmentRepository.save(apt);
                    notificationService.sendAppointmentInProgress(apt);
                    log.info("Updated appointment {} to IN_PROGRESS", apt.getAppointmentId());
                });
        
        log.info("Appointment status update scheduler completed");
    }

    /**
     * Runs daily at midnight to clean up old completed appointments
     * This is optional - can be used for archiving or cleanup
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void cleanupOldAppointments() {
        log.info("Running daily appointment cleanup");
        
        // Example: Archive appointments older than 90 days
        // This can be implemented based on requirements
        
        log.info("Daily appointment cleanup completed");
    }
}
