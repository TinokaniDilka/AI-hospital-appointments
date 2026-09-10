package com.smartcare.repository;

import com.smartcare.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByPatientIdOrderBySentAtDesc(String patientId);
}
