package com.smartcare.repository;

import com.smartcare.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    List<Department> findByBranchId(String branchId);
    List<Department> findByActiveTrue();
}
