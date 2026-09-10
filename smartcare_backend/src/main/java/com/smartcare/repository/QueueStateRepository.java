package com.smartcare.repository;

import com.smartcare.model.QueueState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueueStateRepository extends MongoRepository<QueueState, String> {
    Optional<QueueState> findByDoctorIdAndDate(String doctorId, String date);
}
