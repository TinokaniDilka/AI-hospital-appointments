package com.smartcare.service;

import com.smartcare.model.*;
import com.smartcare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final QueueService queueService;
    private final NotificationLogRepository notificationRepository;

    public List<String> getAvailableSlots(String doctorId, String dateStr) {
        Optional<DoctorSchedule> schedOpt = scheduleRepository.findByDoctorId(doctorId);
        List<String> slots = new ArrayList<>();

        if (schedOpt.isEmpty()) {
            // Default 9:00 to 17:00 fallback slots if schedule not explicitly configured
            return generateDefaultSlots("09:00", "17:00", 20, "13:00", "14:00");
        }

        DoctorSchedule sched = schedOpt.get();

        // Check leave dates
        if (sched.getLeaveDates() != null && sched.getLeaveDates().contains(dateStr)) {
            return slots; // Empty list: Doctor on leave
        }

        // FIX: Check working days — prevent booking on non-working days (e.g. Sunday for a Mon-Fri doctor)
        if (sched.getWorkingDays() != null && !sched.getWorkingDays().isEmpty()) {
            LocalDate requestedDate = LocalDate.parse(dateStr);
            String dayName = requestedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            if (!sched.getWorkingDays().contains(dayName)) {
                return slots; // Empty list: Not a working day for this doctor
            }
        }

        // Generate base time slots
        List<String> generated = generateDefaultSlots(
                sched.getStartTime() != null ? sched.getStartTime() : "09:00",
                sched.getEndTime() != null ? sched.getEndTime() : "17:00",
                sched.getSlotDurationMinutes() > 0 ? sched.getSlotDurationMinutes() : 20,
                sched.getBreakStartTime(),
                sched.getBreakEndTime()
        );

        // Filter out already booked slots and check doctor's daily patient limit
        int maxLimit = 25;
        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isPresent() && docOpt.get().getMaxPatientsPerDay() > 0) {
            maxLimit = docOpt.get().getMaxPatientsPerDay();
        }

        List<Appointment> existingBookings = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, dateStr);
        long activeCount = existingBookings.stream().filter(a -> !"CANCELLED".equals(a.getStatus())).count();
        if (activeCount >= maxLimit) {
            return new ArrayList<>(); // Doctor's daily capacity limit reached
        }

        List<String> bookedSlots = existingBookings.stream()
                .filter(a -> !"CANCELLED".equals(a.getStatus()))
                .map(Appointment::getTimeSlot)
                .toList();

        return generated.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .toList();
    }

    private List<String> generateDefaultSlots(String start, String end, int durationMins, String breakStart, String breakEnd) {
        List<String> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime cur = LocalTime.parse(start, fmt);
        LocalTime endTime = LocalTime.parse(end, fmt);
        LocalTime bStart = (breakStart != null && !breakStart.isEmpty()) ? LocalTime.parse(breakStart, fmt) : null;
        LocalTime bEnd = (breakEnd != null && !breakEnd.isEmpty()) ? LocalTime.parse(breakEnd, fmt) : null;

        while (cur.plusMinutes(durationMins).isBefore(endTime) || cur.plusMinutes(durationMins).equals(endTime)) {
            LocalTime next = cur.plusMinutes(durationMins);
            if (bStart != null && bEnd != null && (cur.isAfter(bStart.minusMinutes(1)) && cur.isBefore(bEnd))) {
                cur = bEnd;
                continue;
            }
            result.add(String.format("%s - %s", cur.format(fmt), next.format(fmt)));
            cur = next;
        }

        return result;
    }

    public Appointment bookAppointment(String patientId, String patientName, String doctorId, String dateStr, String timeSlot) {
        Doctor doc = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        int maxLimit = doc.getMaxPatientsPerDay() > 0 ? doc.getMaxPatientsPerDay() : 25;

        // Enforce maximum daily capacity chosen by the doctor
        List<Appointment> existingBookings = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, dateStr);
        long activeCount = existingBookings.stream().filter(a -> !"CANCELLED".equals(a.getStatus())).count();
        if (activeCount >= maxLimit) {
            throw new IllegalArgumentException("Daily capacity reached (doctor's daily max limit of " + maxLimit + " patients) for " + doc.getDoctorName() + " on " + dateStr + ". Please select another date.");
        }

        // Validate slot availability
        List<Appointment> existing = appointmentRepository.findByDoctorIdAndAppointmentDateAndTimeSlot(doctorId, dateStr, timeSlot);
        boolean isAlreadyBooked = existing.stream().anyMatch(a -> !"CANCELLED".equals(a.getStatus()));
        if (isAlreadyBooked) {
            throw new IllegalArgumentException("The selected time slot " + timeSlot + " is already booked.");
        }

        int queueNumber = (int) activeCount + 1;
        String aptId = "APT-" + (10000 + (int)(Math.random() * 89999));

        Appointment appointment = Appointment.builder()
                .appointmentId(aptId)
                .patientId(patientId)
                .patientName(patientName)
                .doctorId(doc.getId())
                .doctorName(doc.getDoctorName())
                .branchId(doc.getBranchId())
                .branchName(doc.getBranchName())
                .departmentId(doc.getDepartmentId())
                .departmentName(doc.getDepartmentName())
                .appointmentDate(dateStr)
                .timeSlot(timeSlot)
                .status(AppointmentStatus.WAITING_FOR_SCHEDULING)
                .queuePosition(queueNumber)
                .isPriority(false)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Update real-time Queue state
        queueService.getOrCreateQueueForDoctor(doctorId, dateStr);

        // Dispatch Notification
        NotificationLog notif = NotificationLog.builder()
                .patientId(patientId)
                .title("Appointment Confirmed!")
                .body("Your appointment with " + doc.getDoctorName() + " on " + dateStr + " at " + timeSlot + " (Queue #" + queueNumber + ") is confirmed.")
                .type("APPOINTMENT_BOOKED")
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        return saved;
    }

    public Appointment cancelAppointment(String id, String reason) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        apt.setStatus(AppointmentStatus.CANCELLED);
        apt.setCancellationReason(reason);
        Appointment saved = appointmentRepository.save(apt);
        queueService.setPatientStatus(apt.getDoctorId(), apt.getAppointmentDate(), apt.getAppointmentId(), "CANCELLED");
        return saved;
    }

    public Appointment rescheduleAppointment(String id, String newDate, String newSlot) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        cancelAppointment(id, "Rescheduled to " + newDate + " " + newSlot);
        return bookAppointment(apt.getPatientId(), apt.getPatientName(), apt.getDoctorId(), newDate, newSlot);
    }
}
