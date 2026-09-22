package com.smartcare.service;

import com.smartcare.model.Appointment;
import com.smartcare.model.AppointmentStatus;
import com.smartcare.model.Doctor;
import com.smartcare.model.NotificationLog;
import com.smartcare.model.QueueState;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.NotificationLogRepository;
import com.smartcare.repository.QueueStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueStateRepository queueStateRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final AIService aiService;

    public QueueState getOrCreateQueueForDoctor(String doctorId, String date) {
        String queryDate = (date != null && !date.isEmpty()) ? date : LocalDate.now().toString();
        Optional<QueueState> existing = queueStateRepository.findByDoctorIdAndDate(doctorId, queryDate);
        if (existing.isPresent()) {
            return refreshQueueCalculations(existing.get());
        }

        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        String docName = docOpt.map(Doctor::getDoctorName).orElse("Doctor");
        String deptName = docOpt.map(Doctor::getDepartmentName).orElse("General");

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, queryDate);

        QueueState queueState = QueueState.builder()
                .doctorId(doctorId)
                .doctorName(docName)
                .departmentName(deptName)
                .date(queryDate)
                .currentServingNumber(0)
                .items(new ArrayList<>())
                .build();

        for (Appointment apt : appointments) {
            QueueState.QueueItem item = QueueState.QueueItem.builder()
                    .appointmentId(apt.getAppointmentId())
                    .patientId(apt.getPatientId())
                    .patientName(apt.getPatientName())
                    .queueNumber(apt.getQueuePosition())
                    .position(apt.getQueuePosition())
                    .status(apt.getStatus() != null ? apt.getStatus().name() : "WAITING")
                    .isPriority(apt.isPriority())
                    .build();
            queueState.getItems().add(item);
        }

        return refreshQueueCalculations(queueStateRepository.save(queueState));
    }

    public QueueState refreshQueueCalculations(QueueState queue) {
        if (queue.getItems() == null) queue.setItems(new ArrayList<>());

        // Sort items: Priority cases first (if WAITING/APPROACHING), then by Queue Number
        queue.getItems().sort((a, b) -> {
            boolean aActive = isWaitingOrApproaching(a.getStatus());
            boolean bActive = isWaitingOrApproaching(b.getStatus());

            if (aActive && bActive) {
                if (a.isPriority() != b.isPriority()) {
                    return a.isPriority() ? -1 : 1;
                }
            }
            return Integer.compare(a.getQueueNumber(), b.getQueueNumber());
        });

        int waitingCounter = 0;
        int activePriorityCount = (int) queue.getItems().stream()
                .filter(i -> isWaitingOrApproaching(i.getStatus()) && i.isPriority())
                .count();

        Doctor doctor = doctorRepository.findById(queue.getDoctorId()).orElse(null);
        double avgConsult = doctor != null ? doctor.getAvgConsultationMinutes() : 15.0;
        String dept = doctor != null ? doctor.getDepartmentName() : "General Medicine";

        for (int idx = 0; idx < queue.getItems().size(); idx++) {
            QueueState.QueueItem item = queue.getItems().get(idx);
            if (isWaitingOrApproaching(item.getStatus())) {
                waitingCounter++;
                item.setPosition(waitingCounter);

                // Check approaching notification status (2 patients ahead)
                if (waitingCounter <= 2 && "WAITING".equals(item.getStatus())) {
                    item.setStatus("APPROACHING");
                    sendNotification(item.getPatientId(), "Consultation Approaching!",
                            "Your Queue #" + item.getQueueNumber() + " is approaching. Only " + (waitingCounter - 1) + " patient(s) ahead.",
                            "QUEUE_APPROACHING");
                }

                // Query Python AI model for wait time estimation
                AIService.AIPredictionResponse aiRes = aiService.getPredictedWaitTime(
                        queue.getDoctorId(),
                        dept,
                        item.getPosition(),
                        waitingCounter - 1,
                        avgConsult,
                        activePriorityCount
                );
                item.setPredictedWaitMinutes(aiRes.getPredictedWaitMinutes());
                item.setFormattedWaitRange(aiRes.getFormattedRange());
            } else if ("NOW_SERVING".equals(item.getStatus())) {
                item.setPosition(0);
                item.setPredictedWaitMinutes(0.0);
                item.setFormattedWaitRange("Now Serving in Room");
            } else {
                item.setPosition(-1);
                item.setPredictedWaitMinutes(0.0);
                item.setFormattedWaitRange("—");
            }
        }

        return queueStateRepository.save(queue);
    }

    public QueueState callNextPatient(String doctorId, String date) {
        QueueState queue = getOrCreateQueueForDoctor(doctorId, date);
        
        // Complete current serving if exists
        for (QueueState.QueueItem item : queue.getItems()) {
            if ("NOW_SERVING".equals(item.getStatus())) {
                item.setStatus("COMPLETED");
                updateAppointmentStatus(item.getAppointmentId(), "COMPLETED");
            }
        }

        // Find next patient waiting or approaching
        Optional<QueueState.QueueItem> nextOpt = queue.getItems().stream()
                .filter(i -> isWaitingOrApproaching(i.getStatus()))
                .findFirst();

        if (nextOpt.isPresent()) {
            QueueState.QueueItem nextItem = nextOpt.get();
            nextItem.setStatus("NOW_SERVING");
            queue.setCurrentServingNumber(nextItem.getQueueNumber());
            updateAppointmentStatus(nextItem.getAppointmentId(), "NOW_SERVING");

            sendNotification(nextItem.getPatientId(), "It's Your Turn!",
                    "Queue #" + nextItem.getQueueNumber() + " is now being served. Please proceed to doctor consultation room.",
                    "NOW_SERVING");
        } else {
            log.info("No more patients waiting in queue for doctor {}", doctorId);
        }

        return refreshQueueCalculations(queue);
    }

    public QueueState setPatientStatus(String doctorId, String date, String appointmentId, String newStatus) {
        QueueState queue = getOrCreateQueueForDoctor(doctorId, date);
        for (QueueState.QueueItem item : queue.getItems()) {
            if (item.getAppointmentId().equals(appointmentId)) {
                item.setStatus(newStatus);
                updateAppointmentStatus(appointmentId, newStatus);
                if ("NO_SHOW".equals(newStatus)) {
                    sendNotification(item.getPatientId(), "Appointment Marked No-Show",
                            "Your appointment #" + appointmentId + " was marked as No-Show by hospital staff.",
                            "NO_SHOW");
                }
            }
        }
        return refreshQueueCalculations(queue);
    }

    public QueueState togglePriority(String doctorId, String date, String appointmentId, boolean isPriority, String reason) {
        QueueState queue = getOrCreateQueueForDoctor(doctorId, date);
        for (QueueState.QueueItem item : queue.getItems()) {
            if (item.getAppointmentId().equals(appointmentId)) {
                item.setPriority(isPriority);
                Optional<Appointment> aptOpt = appointmentRepository.findAll().stream()
                        .filter(a -> a.getAppointmentId().equals(appointmentId))
                        .findFirst();
                if (aptOpt.isPresent()) {
                    Appointment apt = aptOpt.get();
                    apt.setPriority(isPriority);
                    apt.setPriorityReason(reason);
                    appointmentRepository.save(apt);
                }
            }
        }
        return refreshQueueCalculations(queue);
    }

    private void updateAppointmentStatus(String appointmentId, String status) {
        appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentId().equals(appointmentId))
                .findFirst()
                .ifPresent(apt -> {
                    try {
                        apt.setStatus(AppointmentStatus.valueOf(status));
                    } catch (IllegalArgumentException e) {
                        // Fallback for old string statuses
                        apt.setStatus(AppointmentStatus.WAITING_FOR_SCHEDULING);
                    }
                    appointmentRepository.save(apt);
                });
    }

    private boolean isWaitingOrApproaching(String status) {
        return "WAITING".equals(status) || "APPROACHING".equals(status) || "BOOKED".equals(status) || "IN_QUEUE".equals(status);
    }

    private void sendNotification(String patientId, String title, String body, String type) {
        NotificationLog notif = NotificationLog.builder()
                .patientId(patientId)
                .title(title)
                .body(body)
                .type(type)
                .isRead(false)
                .build();
        notificationLogRepository.save(notif);
    }
}
