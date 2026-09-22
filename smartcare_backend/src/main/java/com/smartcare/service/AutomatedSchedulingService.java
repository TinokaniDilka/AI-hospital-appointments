package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.AppointmentStatus;
import com.smartcare.model.DoctorDailyCapacity;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorDailyCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomatedSchedulingService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorDailyCapacityRepository capacityRepository;
    private final NotificationService notificationService;

    /**
     * Process appointment after payment is completed
     * Status flow: WAITING_FOR_SCHEDULING -> PAID -> (CONFIRMED or WAITLISTED)
     */
    @Transactional
    public void processAfterPayment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.WAITING_FOR_SCHEDULING) {
            log.warn("Appointment {} is not in WAITING_FOR_SCHEDULING status", appointmentId);
            return;
        }

        // Update to PAID status
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.PAID);
        appointment.setPaymentStatus("PAID");
        appointment.setAmountPaid(100.0); // Default amount
        appointmentRepository.save(appointment);

        // Send notification
        notificationService.notifyStatusChange(appointment, oldStatus, AppointmentStatus.PAID);

        // Attempt to schedule the appointment
        scheduleAppointment(appointment);
    }

    /**
     * Schedule an appointment based on doctor capacity
     */
    @Transactional
    public void scheduleAppointment(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            log.warn("Appointment date is null for appointment {}", appointment.getId());
            return;
        }

        LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate());
        String doctorId = appointment.getDoctorId();

        // Get or create daily capacity for the doctor
        DoctorDailyCapacity capacity = capacityRepository.findByDoctorIdAndDate(doctorId, appointmentDate)
                .orElseGet(() -> createDefaultCapacity(doctorId, appointmentDate));

        // Check if there's available capacity
        if (!capacity.isFull()) {
            // Confirm the appointment
            confirmAppointment(appointment, capacity);
        } else {
            // Add to waitlist
            addToWaitlist(appointment, capacity);
        }
    }

    /**
     * Confirm appointment and assign slot
     */
    private void confirmAppointment(Appointment appointment, DoctorDailyCapacity capacity) {
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(Instant.now());
        appointment.setScheduledSlotNumber(capacity.getBookedSlots() + 1);
        appointment.setQueuePosition(0);

        // Update capacity
        capacity.setBookedSlots(capacity.getBookedSlots() + 1);
        capacity.setUpdatedAt(Instant.now());

        appointmentRepository.save(appointment);
        capacityRepository.save(capacity);

        // Send notification
        notificationService.notifyStatusChange(appointment, oldStatus, AppointmentStatus.CONFIRMED);

        log.info("Appointment {} confirmed for doctor {} on date {} with slot number {}",
                appointment.getId(), appointment.getDoctorId(), appointment.getAppointmentDate(),
                appointment.getScheduledSlotNumber());
    }

    /**
     * Add appointment to waitlist
     */
    private void addToWaitlist(Appointment appointment, DoctorDailyCapacity capacity) {
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.WAITLISTED);
        appointment.setQueuePosition(capacity.getWaitlistedCount() + 1);
        appointment.setScheduledSlotNumber(0);

        // Update capacity
        capacity.setWaitlistedCount(capacity.getWaitlistedCount() + 1);
        capacity.setUpdatedAt(Instant.now());

        appointmentRepository.save(appointment);
        capacityRepository.save(capacity);

        // Send notification
        notificationService.notifyStatusChange(appointment, oldStatus, AppointmentStatus.WAITLISTED);

        log.info("Appointment {} waitlisted for doctor {} on date {} at position {}",
                appointment.getId(), appointment.getDoctorId(), appointment.getAppointmentDate(),
                appointment.getQueuePosition());
    }

    /**
     * Recalculate schedule when doctor capacity changes
     */
    @Transactional
    public void recalculateSchedule(String doctorId, LocalDate date, int newCapacity) {
        DoctorDailyCapacity capacity = capacityRepository.findByDoctorIdAndDate(doctorId, date)
                .orElseGet(() -> createDefaultCapacity(doctorId, date));

        int oldCapacity = capacity.getDailyCapacity();
        capacity.setDailyCapacity(newCapacity);
        capacity.setUpdatedAt(Instant.now());

        // Get all appointments for this doctor and date
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date.toString());

        // Separate confirmed and waitlisted
        List<Appointment> confirmed = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .sorted(Comparator.comparing(Appointment::getConfirmedAt))
                .toList();

        List<Appointment> waitlisted = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.WAITLISTED)
                .sorted(Comparator.comparing(Appointment::getCreatedAt))
                .toList();

        // If capacity increased, move waitlisted to confirmed
        if (newCapacity > oldCapacity) {
            int slotsAvailable = newCapacity - confirmed.size();
            int toPromote = Math.min(slotsAvailable, waitlisted.size());

            for (int i = 0; i < toPromote; i++) {
                Appointment apt = waitlisted.get(i);
                AppointmentStatus oldStatus = apt.getStatus();
                apt.setStatus(AppointmentStatus.CONFIRMED);
                apt.setConfirmedAt(Instant.now());
                apt.setScheduledSlotNumber(confirmed.size() + i + 1);
                apt.setQueuePosition(0);
                appointmentRepository.save(apt);
                
                // Send notification for promotion
                notificationService.notifyStatusChange(apt, oldStatus, AppointmentStatus.CONFIRMED);
            }

            // Update remaining waitlist positions and send notifications
            for (int i = toPromote; i < waitlisted.size(); i++) {
                Appointment apt = waitlisted.get(i);
                int newPosition = i - toPromote + 1;
                apt.setQueuePosition(newPosition);
                appointmentRepository.save(apt);
                
                // Send waitlist position update notification
                notificationService.notifyWaitlistUpdate(apt, newPosition);
            }

            capacity.setBookedSlots(confirmed.size() + toPromote);
            capacity.setWaitlistedCount(waitlisted.size() - toPromote);
        }
        // If capacity decreased, move confirmed to waitlist
        else if (newCapacity < oldCapacity) {
            int toDemote = Math.max(0, confirmed.size() - newCapacity);

            for (int i = 0; i < toDemote; i++) {
                int index = confirmed.size() - 1 - i;
                Appointment apt = confirmed.get(index);
                AppointmentStatus oldStatus = apt.getStatus();
                apt.setStatus(AppointmentStatus.WAITLISTED);
                apt.setConfirmedAt(null);
                apt.setScheduledSlotNumber(0);
                apt.setQueuePosition(i + 1);
                appointmentRepository.save(apt);
                
                // Send notification for demotion
                notificationService.notifyStatusChange(apt, oldStatus, AppointmentStatus.WAITLISTED);
            }

            capacity.setBookedSlots(newCapacity);
            capacity.setWaitlistedCount(waitlisted.size() + toDemote);
        }

        capacityRepository.save(capacity);
        log.info("Recalculated schedule for doctor {} on date {} with new capacity {}",
                doctorId, date, newCapacity);
    }

    /**
     * Create default daily capacity for a doctor
     */
    private DoctorDailyCapacity createDefaultCapacity(String doctorId, LocalDate date) {
        return DoctorDailyCapacity.builder()
                .doctorId(doctorId)
                .date(date)
                .dailyCapacity(20) // Default capacity
                .bookedSlots(0)
                .waitlistedCount(0)
                .isActive(true)
                .build();
    }

    /**
     * Get remaining slots for a doctor on a specific date
     */
    public int getRemainingSlots(String doctorId, LocalDate date) {
        Optional<DoctorDailyCapacity> capacity = capacityRepository.findByDoctorIdAndDate(doctorId, date);
        return capacity.map(DoctorDailyCapacity::getRemainingSlots).orElse(20);
    }

    /**
     * Get queue position for a waitlisted appointment
     */
    public int getQueuePosition(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return appointment.getQueuePosition();
    }
}
