package com.smartcare.repository;

import com.smartcare.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientId(String patientId);
    List<Appointment> findByDoctorId(String doctorId);
    
    @Query("{'doctorId': ?0, 'appointmentDate': ?1}")
    List<Appointment> findByDoctorIdAndAppointmentDate(String doctorId, String appointmentDate);
    
    @Query("{'doctorId': ?0, 'appointmentDate': ?1, 'timeSlot': ?2}")
    List<Appointment> findByDoctorIdAndAppointmentDateAndTimeSlot(String doctorId, String appointmentDate, String timeSlot);
    
    @Query("{'appointmentDate': ?0}")
    List<Appointment> findByAppointmentDate(String appointmentDate);
    
    @Query("{'doctorId': ?0, 'appointmentDate': ?1}")
    long countByDoctorIdAndAppointmentDate(String doctorId, String appointmentDate);
    
    // New methods for automated scheduling
    @Query("{'doctorId': ?0, 'status': ?1}")
    List<Appointment> findByDoctorIdAndStatus(String doctorId, String status);
    
    @Query("{'doctorId': ?0, 'scheduledDate': ?1}")
    List<Appointment> findByDoctorIdAndScheduledDate(String doctorId, String scheduledDate);
    
    @Query("{'status': ?0}")
    List<Appointment> findByStatus(String status);
    
    @Query("{'scheduledDate': ?0}")
    List<Appointment> findByScheduledDate(String scheduledDate);
}
