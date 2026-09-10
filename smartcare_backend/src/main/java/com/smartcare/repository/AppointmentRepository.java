package com.smartcare.repository;

import com.smartcare.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientId(String patientId);
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDate(String doctorId, String appointmentDate);
    List<Appointment> findByDoctorIdAndAppointmentDateAndTimeSlot(String doctorId, String appointmentDate, String timeSlot);
    long countByDoctorIdAndAppointmentDate(String doctorId, String appointmentDate);
}
