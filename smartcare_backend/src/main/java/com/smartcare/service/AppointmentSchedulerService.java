package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.Doctor;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentSchedulerService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Schedule appointments for a specific doctor based on their daily capacity
     * This is called when:
     * 1. A new appointment is booked and paid
     * 2. A doctor changes their daily capacity
     * 3. A doctor updates their schedule
     */
    @Transactional
    public void scheduleAppointmentsForDoctor(String doctorId) {
        log.info("Scheduling appointments for doctor: {}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        // Get all waiting appointments for this doctor
        List<Appointment> waitingAppointments = appointmentRepository.findByDoctorIdAndStatus(
                doctorId, "WAITING_FOR_SCHEDULING");

        if (waitingAppointments.isEmpty()) {
            log.info("No waiting appointments to schedule for doctor: {}", doctorId);
            return;
        }

        // Sort by creation time (first come, first served)
        waitingAppointments.sort(Comparator.comparing(Appointment::getCreatedAt));

        // Schedule each appointment
        for (Appointment appointment : waitingAppointments) {
            String scheduledDate = findNextAvailableSlot(doctor, appointment.getPreferredDate());
            
            if (scheduledDate != null) {
                // Slot available - confirm appointment
                appointment.setScheduledDate(scheduledDate);
                appointment.setStatus("CONFIRMED");
                appointment.setScheduledAt(Instant.now());
                appointment.setConfirmedAt(Instant.now());
                addStatusHistory(appointment, "CONFIRMED", "Scheduled automatically based on doctor capacity", "SYSTEM");
                
                appointmentRepository.save(appointment);
                log.info("Appointment {} scheduled for {}", appointment.getAppointmentId(), scheduledDate);
                
                // Send notification
                notificationService.sendAppointmentConfirmation(appointment);
            } else {
                // No available slots - put on waitlist
                appointment.setStatus("WAITLISTED");
                appointment.setQueuePosition(calculateQueuePosition(doctorId, appointment.getCreatedAt()));
                addStatusHistory(appointment, "WAITLISTED", "No available slots, added to waitlist", "SYSTEM");
                
                appointmentRepository.save(appointment);
                log.info("Appointment {} added to waitlist at position {}", 
                        appointment.getAppointmentId(), appointment.getQueuePosition());
                
                // Send notification
                notificationService.sendWaitlistNotification(appointment);
            }
        }
    }

    /**
     * Find the next available slot for a doctor
     * @param doctor The doctor
     * @param preferredDate Patient's preferred date (can be null)
     * @return The scheduled date or null if no slots available
     */
    private String findNextAvailableSlot(Doctor doctor, String preferredDate) {
        LocalDate startDate = preferredDate != null ? 
                LocalDate.parse(preferredDate, DATE_FORMATTER) : LocalDate.now();
        
        // Look ahead up to 30 days
        LocalDate endDate = startDate.plusDays(30);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (isDoctorAvailable(doctor, date)) {
                int bookedCount = countBookedAppointments(doctor.getId(), date.format(DATE_FORMATTER));
                if (bookedCount < doctor.getMaxPatientsPerDay()) {
                    return date.format(DATE_FORMATTER);
                }
            }
        }
        
        return null;
    }

    /**
     * Check if doctor is available on a specific date
     */
    private boolean isDoctorAvailable(Doctor doctor, LocalDate date) {
        // workingDays is List<String> e.g. ["MONDAY","TUESDAY"] or ["Mon","Tue"]
        if (doctor.getWorkingDays() != null && !doctor.getWorkingDays().isEmpty()) {
            String fullDayName = date.getDayOfWeek().toString(); // "MONDAY", "TUESDAY"...
            String shortDayName = fullDayName.substring(0, 3);   // "MON", "TUE"...
            boolean isWorkingDay = doctor.getWorkingDays().stream()
                .anyMatch(d -> d.equalsIgnoreCase(fullDayName) || d.equalsIgnoreCase(shortDayName));
            if (!isWorkingDay) {
                return false;
            }
        }
        
        // Check if on leave
        if (doctor.getLeaveDates() != null) {
            String dateStr = date.format(DATE_FORMATTER);
            if (doctor.getLeaveDates().contains(dateStr)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Count confirmed appointments for a doctor on a specific date
     */
    private int countBookedAppointments(String doctorId, String date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndScheduledDate(
                doctorId, date);
        return (int) appointments.stream()
                .filter(apt -> "CONFIRMED".equals(apt.getStatus()) || 
                             "IN_PROGRESS".equals(apt.getStatus()) ||
                             "COMPLETED".equals(apt.getStatus()))
                .count();
    }

    /**
     * Calculate queue position for waitlisted appointment
     */
    private int calculateQueuePosition(String doctorId, Instant createdAt) {
        List<Appointment> waitlisted = appointmentRepository.findByDoctorIdAndStatus(
                doctorId, "WAITLISTED");
        waitlisted.sort(Comparator.comparing(Appointment::getCreatedAt));
        
        int position = 1;
        for (Appointment apt : waitlisted) {
            if (apt.getCreatedAt().isBefore(createdAt)) {
                position++;
            }
        }
        return position;
    }

    /**
     * Add status history entry
     */
    private void addStatusHistory(Appointment appointment, String status, String reason, String changedBy) {
        if (appointment.getStatusHistory() == null) {
            appointment.setStatusHistory(new ArrayList<>());
        }
        
        Appointment.StatusHistory history = new Appointment.StatusHistory();
        history.setStatus(status);
        history.setReason(reason);
        history.setTimestamp(Instant.now());
        history.setChangedBy(changedBy);
        
        appointment.getStatusHistory().add(history);
    }

    /**
     * Get available slots for a doctor on a specific date range
     */
    public Map<String, Integer> getAvailableSlots(String doctorId, String startDate, String endDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        Map<String, Integer> availableSlots = new LinkedHashMap<>();
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isDoctorAvailable(doctor, date)) {
                int booked = countBookedAppointments(doctorId, date.format(DATE_FORMATTER));
                int available = doctor.getMaxPatientsPerDay() - booked;
                availableSlots.put(date.format(DATE_FORMATTER), Math.max(0, available));
            }
        }
        
        return availableSlots;
    }

    /**
     * Reschedule appointments when doctor changes capacity
     */
    @Transactional
    public void rescheduleOnCapacityChange(String doctorId, int newCapacity) {
        log.info("Rescheduling appointments for doctor {} with new capacity: {}", doctorId, newCapacity);
        
        // First, check if we can accommodate waitlisted appointments
        List<Appointment> waitlisted = appointmentRepository.findByDoctorIdAndStatus(
                doctorId, "WAITLISTED");
        waitlisted.sort(Comparator.comparing(Appointment::getCreatedAt));
        
        for (Appointment appointment : waitlisted) {
            String scheduledDate = findNextAvailableSlot(
                    doctorRepository.findById(doctorId).orElseThrow(), 
                    appointment.getPreferredDate());
            
            if (scheduledDate != null) {
                appointment.setScheduledDate(scheduledDate);
                appointment.setStatus("CONFIRMED");
                appointment.setScheduledAt(Instant.now());
                appointment.setConfirmedAt(Instant.now());
                appointment.setQueuePosition(null);
                addStatusHistory(appointment, "CONFIRMED", 
                        "Rescheduled after doctor capacity increase", "SYSTEM");
                
                appointmentRepository.save(appointment);
                notificationService.sendAppointmentConfirmation(appointment);
            }
        }
    }

    /**
     * Get daily appointment counts for a doctor over a date range.
     * Matches by either scheduledDate or preferredDate (same logic as QueueService),
     * and counts all active statuses (not just CONFIRMED/COMPLETED).
     */
    public Map<String, Integer> getDailyAppointmentCounts(String doctorId, String startDate, String endDate) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        // Load all appointments for this doctor once (avoid per-day DB hits)
        List<Appointment> allAppointments = appointmentRepository.findByDoctorId(doctorId);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            final String dateStr = date.format(DATE_FORMATTER);
            long count = allAppointments.stream()
                .filter(apt -> {
                    // Match by scheduledDate OR preferredDate (consistent with QueueService)
                    boolean dateMatch = dateStr.equals(apt.getScheduledDate())
                            || (apt.getScheduledDate() == null && dateStr.equals(apt.getPreferredDate()));
                    if (!dateMatch) return false;

                    // Exclude only truly inactive statuses
                    String status = apt.getStatus();
                    if (status == null) return false;
                    return !"CANCELLED".equals(status)
                        && !"WAITING_FOR_SCHEDULING".equals(status)
                        && !"WAITLISTED".equals(status);
                })
                .count();
            counts.put(dateStr, (int) count);
        }

        return counts;
    }
}
