package com.smartcare.repository;

import com.smartcare.model.Prediction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends MongoRepository<Prediction, String> {
    List<Prediction> findByDoctorId(String doctorId);
    List<Prediction> findByPatientId(String patientId);
    List<Prediction> findByAppointmentId(String appointmentId);
    Optional<Prediction> findFirstByDoctorIdOrderByCreatedAtDesc(String doctorId);
}
