package com.smartcare.repository;

import com.smartcare.model.DoctorDailyCapacity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorDailyCapacityRepository extends MongoRepository<DoctorDailyCapacity, String> {
    Optional<DoctorDailyCapacity> findByDoctorIdAndDate(String doctorId, LocalDate date);
    List<DoctorDailyCapacity> findByDoctorIdAndDateBetween(String doctorId, LocalDate startDate, LocalDate endDate);
    List<DoctorDailyCapacity> findByDate(LocalDate date);
    void deleteByDoctorIdAndDate(String doctorId, LocalDate date);
}
