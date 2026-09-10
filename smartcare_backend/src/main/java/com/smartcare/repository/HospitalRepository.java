package com.smartcare.repository;

import com.smartcare.model.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    List<Hospital> findByActiveTrue();
}
