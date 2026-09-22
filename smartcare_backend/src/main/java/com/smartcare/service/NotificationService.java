package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.AppointmentStatus;
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

    /**
     * Send notification when appointment status changes automatically
     */
    public void notifyStatusChange(Appointment appointment, AppointmentStatus oldStatus, AppointmentStatus newStatus) {
        String title = buildNotificationTitle(newStatus);
        String body = buildNotificationBody(appointment, newStatus);
        String type = newStatus.name();

        log.info("Status change notification: Appointment {} changed from {} to {}", 
                appointment.getId(), oldStatus, newStatus);

        // Log the notification (can be extended to send push notifications)
        try {
            com.smartcare.model.NotificationLog notification = com.smartcare.model.NotificationLog.builder()
                    .patientId(appointment.getPatientId())
                    .title(title)
                    .body(body)
                    .type(type)
                    .isRead(false)
                    .sentAt(Instant.now())
                    .build();
            
            notificationLogRepository.save(notification);
            log.info("Notification logged for patient {}", appointment.getPatientId());
        } catch (Exception e) {
            log.error("Failed to log notification", e);
        }
    }

    private String buildNotificationTitle(AppointmentStatus status) {
        return switch (status) {
            case PAID -> "Payment Received";
            case CONFIRMED -> "Appointment Confirmed";
            case IN_PROGRESS -> "Appointment In Progress";
            case COMPLETED -> "Appointment Completed";
            case WAITLISTED -> "Added to Waitlist";
            case CANCELLED -> "Appointment Cancelled";
            default -> "Appointment Status Updated";
        };
    }

    private String buildNotificationBody(Appointment appointment, AppointmentStatus status) {
        return switch (status) {
            case PAID -> String.format("Payment of $%.2f received for your appointment with %s on %s", 
                    appointment.getAmountPaid(), appointment.getDoctorName(), appointment.getAppointmentDate());
            case CONFIRMED -> String.format("Your appointment with %s on %s at %s has been confirmed. Slot #%d", 
                    appointment.getDoctorName(), appointment.getAppointmentDate(), appointment.getTimeSlot(), appointment.getScheduledSlotNumber());
            case IN_PROGRESS -> String.format("Your appointment with %s is now in progress", appointment.getDoctorName());
            case COMPLETED -> String.format("Your consultation with %s has been completed", appointment.getDoctorName());
            case WAITLISTED -> String.format("You are on waitlist position #%d for appointment with %s on %s", 
                    appointment.getQueuePosition(), appointment.getDoctorName(), appointment.getAppointmentDate());
            case CANCELLED -> String.format("Your appointment with %s on %s has been cancelled", 
                    appointment.getDoctorName(), appointment.getAppointmentDate());
            default -> String.format("Appointment status updated to %s", status);
        };
    }

    /**
     * Send notification when waitlist position changes
     */
    public void notifyWaitlistUpdate(Appointment appointment, int newPosition) {
        String title = "Waitlist Position Updated";
        String body = String.format("Your waitlist position for appointment with %s on %s is now #%d", 
                appointment.getDoctorName(), appointment.getAppointmentDate(), newPosition);

        log.info("Waitlist notification: Appointment {} position changed to {}", 
                appointment.getId(), newPosition);

        try {
            com.smartcare.model.NotificationLog notification = com.smartcare.model.NotificationLog.builder()
                    .patientId(appointment.getPatientId())
                    .title(title)
                    .body(body)
                    .type("WAITLIST_UPDATE")
                    .isRead(false)
                    .sentAt(Instant.now())
                    .build();
            
            notificationLogRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to log waitlist notification", e);
        }
    }
}
