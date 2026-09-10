package com.smartcare.controller;

import com.smartcare.model.Patient;
import com.smartcare.repository.PatientRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;

    @Data
    public static class UpdatePatientRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;
        private String dob;
        private String gender;
        private String bloodGroup;
        private String emergencyContact;
        private String medicalNotes;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userId = userDetails.getUsername();
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        try {
            String userId = userDetails.getUsername();
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));

            if (request.getFullName() != null) patient.setFullName(request.getFullName());
            if (request.getDob() != null) patient.setDob(request.getDob());
            if (request.getGender() != null) patient.setGender(request.getGender());
            if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
            if (request.getEmergencyContact() != null) patient.setEmergencyContact(request.getEmergencyContact());
            if (request.getMedicalNotes() != null) patient.setMedicalNotes(request.getMedicalNotes());

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
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientRepository.findAll();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
