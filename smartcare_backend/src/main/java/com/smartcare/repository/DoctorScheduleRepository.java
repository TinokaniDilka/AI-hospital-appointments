package com.smartcare.repository;

import com.smartcare.model.DoctorSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends MongoRepository<DoctorSchedule, String> {
    Optional<DoctorSchedule> findByDoctorId(String doctorId);
}
