package com.smartcare.controller;

import com.smartcare.model.Branch;
import com.smartcare.model.Department;
import com.smartcare.model.Hospital;
import com.smartcare.repository.BranchRepository;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalRepository hospitalRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;

    // ── Hospital endpoints ────────────────────────────────────────────────────

    @GetMapping("/hospitals")
    public ResponseEntity<List<Hospital>> getHospitals() {
        return ResponseEntity.ok(hospitalRepository.findByActiveTrue());
    }

    @GetMapping("/hospitals/{id}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable String id) {
        return hospitalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hospitals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
        hospital.setActive(true);
        return ResponseEntity.ok(hospitalRepository.save(hospital));
    }

    @PutMapping("/hospitals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hospital> updateHospital(@PathVariable String id, @RequestBody Hospital updated) {
        return hospitalRepository.findById(id)
                .map(existing -> {
                    updated.setId(existing.getId());
                    updated.setActive(existing.isActive());
                    return ResponseEntity.ok(hospitalRepository.save(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/hospitals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hospital> deactivateHospital(@PathVariable String id) {
        return hospitalRepository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    return ResponseEntity.ok(hospitalRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Branch endpoints ──────────────────────────────────────────────────────

    @GetMapping("/branches")
    public ResponseEntity<List<Branch>> getBranches(@RequestParam(required = false) String hospitalId) {
        if (hospitalId != null && !hospitalId.isEmpty()) {
            return ResponseEntity.ok(branchRepository.findByHospitalId(hospitalId));
        }
        return ResponseEntity.ok(branchRepository.findByActiveTrue());
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<Branch> getBranchById(@PathVariable String id) {
        return branchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        branch.setActive(true);
        return ResponseEntity.ok(branchRepository.save(branch));
    }

    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Branch> updateBranch(@PathVariable String id, @RequestBody Branch updated) {
        return branchRepository.findById(id)
                .map(existing -> {
                    updated.setId(existing.getId());
                    updated.setActive(existing.isActive());
                    return ResponseEntity.ok(branchRepository.save(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Branch> deactivateBranch(@PathVariable String id) {
        return branchRepository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    return ResponseEntity.ok(branchRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Department endpoints ──────────────────────────────────────────────────

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments(@RequestParam(required = false) String branchId) {
        if (branchId != null && !branchId.isEmpty()) {
            return ResponseEntity.ok(departmentRepository.findByBranchId(branchId));
        }
        return ResponseEntity.ok(departmentRepository.findByActiveTrue());
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable String id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(@RequestBody Department dept) {
        dept.setActive(true);
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> updateDepartment(@PathVariable String id, @RequestBody Department updated) {
        return departmentRepository.findById(id)
                .map(existing -> {
                    updated.setId(existing.getId());
                    updated.setActive(existing.isActive());
                    return ResponseEntity.ok(departmentRepository.save(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> deactivateDepartment(@PathVariable String id) {
        return departmentRepository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    return ResponseEntity.ok(departmentRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
