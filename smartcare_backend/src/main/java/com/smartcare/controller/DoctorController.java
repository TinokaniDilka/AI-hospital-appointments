package com.smartcare.controller;

import com.smartcare.model.Doctor;
import com.smartcare.model.DoctorSchedule;
import com.smartcare.model.User;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.DoctorScheduleRepository;
import com.smartcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Doctor READ endpoints ─────────────────────────────────────────────────

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String branchId
    ) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return ResponseEntity.ok(doctorRepository.findByDepartmentId(departmentId));
        }
        if (branchId != null && !branchId.isEmpty()) {
            return ResponseEntity.ok(doctorRepository.findByBranchId(branchId));
        }
        return ResponseEntity.ok(doctorRepository.findByActiveTrue());
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Doctor WRITE endpoints (Admin / Staff only) ───────────────────────────

    @PostMapping("/doctors")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doc) {
        doc.setActive(true);

        if (doc.getUserId() == null || doc.getUserId().trim().isEmpty()) {
            String email = doc.getEmail();
            if (email == null || email.trim().isEmpty()) {
                String sanitized = doc.getDoctorName() != null ? doc.getDoctorName().toLowerCase().replaceAll("[^a-z0-9]", "") : "doc" + System.currentTimeMillis();
                email = sanitized + "@smartcare.com";
            }
            String rawPassword = (doc.getPassword() != null && !doc.getPassword().trim().isEmpty()) ? doc.getPassword() : "doctor123";

            final String finalEmail = email;
            User doctorUser = userRepository.findByEmail(finalEmail).orElseGet(() -> {
                User newUser = User.builder()
                        .email(finalEmail)
                        .password(passwordEncoder.encode(rawPassword))
                        .fullName(doc.getDoctorName())
                        .role("ROLE_DOCTOR")
                        .active(true)
                        .build();
                return userRepository.save(newUser);
            });

            doc.setUserId(doctorUser.getId());
            doc.setEmail(finalEmail);
        }

        return ResponseEntity.ok(doctorRepository.save(doc));
    }

    @PutMapping("/doctors/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor updated) {
        return doctorRepository.findById(id)
                .map(existing -> {
                    // Preserve immutable fields
                    updated.setId(existing.getId());
                    updated.setUserId(existing.getUserId());
                    if (!updated.isActive() == existing.isActive()) {
                        updated.setActive(existing.isActive());
                    }
                    return ResponseEntity.ok(doctorRepository.save(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/doctors/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Doctor> deactivateDoctor(@PathVariable String id) {
        return doctorRepository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    return ResponseEntity.ok(doctorRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Schedule endpoints ────────────────────────────────────────────────────

    @GetMapping("/schedules/{doctorId}")
    public ResponseEntity<DoctorSchedule> getSchedule(@PathVariable String doctorId) {
        return scheduleRepository.findByDoctorId(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<DoctorSchedule> createOrUpdateSchedule(@RequestBody DoctorSchedule sched) {
        Optional<DoctorSchedule> existing = scheduleRepository.findByDoctorId(sched.getDoctorId());
        existing.ifPresent(s -> sched.setId(s.getId()));
        return ResponseEntity.ok(scheduleRepository.save(sched));
    }

    @PutMapping("/schedules/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<DoctorSchedule> updateSchedule(@PathVariable String doctorId, @RequestBody DoctorSchedule sched) {
        sched.setDoctorId(doctorId);
        Optional<DoctorSchedule> existing = scheduleRepository.findByDoctorId(doctorId);
        existing.ifPresent(s -> sched.setId(s.getId()));
        return ResponseEntity.ok(scheduleRepository.save(sched));
    }
}
