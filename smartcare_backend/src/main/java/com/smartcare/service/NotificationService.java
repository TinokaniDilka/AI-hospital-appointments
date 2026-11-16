package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.NotificationLog;
import com.smartcare.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final FirebaseService firebaseService;

    public void sendAppointmentConfirmation(Appointment appointment) {
        String title = "Appointment Confirmed";
        String body = String.format("Your appointment with Dr. %s is confirmed for %s", 
                appointment.getDoctorName(), appointment.getScheduledDate());
        
        createNotification(appointment.getPatientId(), title, body, "APPOINTMENT_CONFIRMED");
        log.info("Appointment confirmation sent to patient: {}", appointment.getPatientId());
    }

    public void sendWaitlistNotification(Appointment appointment) {
        String title = "Added to Waitlist";
        String body = String.format("Your appointment has been added to the waitlist. Position: %d", 
                appointment.getQueuePosition());
        
        createNotification(appointment.getPatientId(), title, body, "WAITLISTED");
        log.info("Waitlist notification sent to patient: {}", appointment.getPatientId());
    }

    public void sendAppointmentInProgress(Appointment appointment) {
        String title = "Appointment In Progress";
        String body = String.format("Your appointment with Dr. %s is now in progress", 
                appointment.getDoctorName());
        
        createNotification(appointment.getPatientId(), title, body, "IN_PROGRESS");
        log.info("In-progress notification sent to patient: {}", appointment.getPatientId());
    }

    public void sendAppointmentCompleted(Appointment appointment) {
        String title = "Appointment Completed";
        String body = String.format("Your appointment with Dr. %s has been completed", 
                appointment.getDoctorName());
        
        createNotification(appointment.getPatientId(), title, body, "COMPLETED");
        log.info("Completion notification sent to patient: {}", appointment.getPatientId());
    }

    public void sendAppointmentCancelled(Appointment appointment, String reason) {
        String title = "Appointment Cancelled";
        String body = String.format("Your appointment has been cancelled. Reason: %s", 
                reason != null ? reason : "Not specified");
        
        createNotification(appointment.getPatientId(), title, body, "CANCELLED");
        log.info("Cancellation notification sent to patient: {}", appointment.getPatientId());
    }

    public void sendRescheduledNotification(Appointment appointment, String oldDate, String newDate) {
        String title = "Appointment Rescheduled";
        String body = String.format("Your appointment has been rescheduled from %s to %s", 
                oldDate, newDate);
        
        createNotification(appointment.getPatientId(), title, body, "RESCHEDULED");
        log.info("Reschedule notification sent to patient: {}", appointment.getPatientId());
    }

    private void createNotification(String patientId, String title, String body, String type) {
        try {
            NotificationLog notification = NotificationLog.builder()
                    .patientId(patientId)
                    .title(title)
                    .body(body)
                    .type(type)
                    .isRead(false)
                    .sentAt(Instant.now())
                    .build();
            
            notificationLogRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to create notification", e);
        }
    }
}
