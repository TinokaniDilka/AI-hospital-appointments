package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.AppointmentStatus;
import com.smartcare.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentStatusScheduler {

    private final AppointmentRepository appointmentRepository;

    /**
     * Runs every hour to update appointment statuses based on date
     * - CONFIRMED appointments on today's date -> IN_PROGRESS
     * - IN_PROGRESS appointments from past dates -> COMPLETED (auto-complete if not manually completed)
     * - WAITLISTED appointments past their date -> CANCELLED
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    @Transactional
    public void updateAppointmentStatuses() {
        LocalDate today = LocalDate.now();
        log.info("Running scheduled appointment status update for date: {}", today);

        // Update CONFIRMED appointments for today to IN_PROGRESS
        List<Appointment> confirmedToday = appointmentRepository.findByAppointmentDate(today.toString())
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();

        for (Appointment apt : confirmedToday) {
            apt.setStatus(AppointmentStatus.IN_PROGRESS);
            appointmentRepository.save(apt);
            log.info("Updated appointment {} to IN_PROGRESS (appointment date: {})", 
                    apt.getId(), apt.getAppointmentDate());
        }

        // Auto-complete IN_PROGRESS appointments from past dates (if not already completed)
        LocalDate yesterday = today.minusDays(1);
        List<Appointment> inProgressPast = appointmentRepository.findByAppointmentDate(yesterday.toString())
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.IN_PROGRESS)
                .toList();

        for (Appointment apt : inProgressPast) {
            apt.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(apt);
            log.info("Auto-completed appointment {} (past date: {})", 
                    apt.getId(), apt.getAppointmentDate());
        }

        // Cancel WAITLISTED appointments from past dates
        List<Appointment> waitlistedPast = appointmentRepository.findByAppointmentDate(yesterday.toString())
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.WAITLISTED)
                .toList();

        for (Appointment apt : waitlistedPast) {
            apt.setStatus(AppointmentStatus.CANCELLED);
            apt.setCancellationReason("Auto-cancelled: Appointment date passed while on waitlist");
            appointmentRepository.save(apt);
            log.info("Auto-cancelled waitlisted appointment {} (past date: {})", 
                    apt.getId(), apt.getAppointmentDate());
        }

        log.info("Scheduled status update completed. Updated {} CONFIRMED->IN_PROGRESS, {} IN_PROGRESS->COMPLETED, {} WAITLISTED->CANCELLED",
                confirmedToday.size(), inProgressPast.size(), waitlistedPast.size());
    }

    /**
     * Runs every 5 minutes to check for appointments that should be IN_PROGRESS
     * This is a more frequent check for real-time updates
     */
    @Scheduled(cron = "0 */5 * * * ?") // Every 5 minutes
    @Transactional
    public void checkForRealtimeUpdates() {
        LocalDate today = LocalDate.now();
        
        List<Appointment> confirmedToday = appointmentRepository.findByAppointmentDate(today.toString())
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();

        if (!confirmedToday.isEmpty()) {
            log.info("Found {} CONFIRMED appointments for today, updating to IN_PROGRESS", confirmedToday.size());
            for (Appointment apt : confirmedToday) {
                apt.setStatus(AppointmentStatus.IN_PROGRESS);
                appointmentRepository.save(apt);
            }
        }
    }
}
