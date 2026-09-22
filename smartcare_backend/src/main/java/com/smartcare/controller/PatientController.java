package com.smartcare.controller;

import com.smartcare.model.Patient;
import com.smartcare.model.User;
import com.smartcare.repository.PatientRepository;
import com.smartcare.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Data
    public static class UpdatePatientRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;
        private String email;
        private String phoneNumber;
        private String dob;
        private String gender;
        private String bloodGroup;
        private String emergencyContact;
        private String medicalNotes;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            String userId = authentication.getName();
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseGet(() -> patientRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Patient profile not found")));
            if (patient.getEmail() == null || patient.getEmail().isEmpty()) {
                userRepository.findById(userId).ifPresent(u -> {
                    patient.setEmail(u.getEmail());
                    patient.setPhoneNumber(u.getPhoneNumber());
                });
            }
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            org.springframework.security.core.Authentication authentication,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            String userId = authentication.getName();
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseGet(() -> patientRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Patient profile not found")));

            if (request.getFullName() != null) patient.setFullName(request.getFullName());
            if (request.getEmail() != null) patient.setEmail(request.getEmail());
            if (request.getPhoneNumber() != null) patient.setPhoneNumber(request.getPhoneNumber());
            if (request.getDob() != null) patient.setDob(request.getDob());
            if (request.getGender() != null) patient.setGender(request.getGender());
            if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
            if (request.getEmergencyContact() != null) patient.setEmergencyContact(request.getEmergencyContact());
            if (request.getMedicalNotes() != null) patient.setMedicalNotes(request.getMedicalNotes());

            // Sync with User entity if userId exists
            if (patient.getUserId() != null) {
                userRepository.findById(patient.getUserId()).ifPresent(user -> {
                    if (request.getFullName() != null) user.setFullName(request.getFullName());
                    if (request.getEmail() != null && !request.getEmail().isEmpty()) user.setEmail(request.getEmail());
                    if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
                    userRepository.save(user);
                });
            }

            Patient updated = patientRepository.save(patient);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable String id) {
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<Patient> patients = patientRepository.findAll();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
