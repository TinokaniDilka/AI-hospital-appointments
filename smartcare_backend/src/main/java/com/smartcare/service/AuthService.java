package com.smartcare.service;

import com.smartcare.model.Doctor;
import com.smartcare.model.Patient;
import com.smartcare.model.User;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.repository.UserRepository;
import com.smartcare.security.JwtTokenProvider;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Data
    public static class AuthResponse {
        private String token;
        private String userId;
        private String email;
        private String fullName;
        private String role;
        private String patientId;
        private String doctorId;
    }

    // ── Patient Registration ──────────────────────────────────────────────────

    public AuthResponse registerPatient(String email, String rawPassword, String fullName, String phone, String gender, String dob) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role("ROLE_PATIENT")
                .phoneNumber(phone)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        Patient patient = Patient.builder()
                .userId(savedUser.getId())
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .dob(dob != null ? dob : "1995-05-15")
                .gender(gender != null ? gender : "Other")
                .bloodGroup("O+")
                .emergencyContact(phone)
                .medicalNotes("New SmartCare Patient")
                .build();
        Patient savedPatient = patientRepository.save(patient);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName());

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserId(savedUser.getId());
        res.setEmail(savedUser.getEmail());
        res.setFullName(savedUser.getFullName());
        res.setRole(savedUser.getRole());
        res.setPatientId(savedPatient.getId());
        return res;
    }

    // ── Doctor Registration ───────────────────────────────────────────────────

    public AuthResponse registerDoctor(String email, String rawPassword, String fullName, String phone,
                                       String specialization, String departmentId, String departmentName,
                                       String branchId, String branchName, String roomNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role("ROLE_DOCTOR")
                .phoneNumber(phone)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .userId(savedUser.getId())
                .doctorName(fullName)
                .specialization(specialization)
                .departmentId(departmentId)
                .departmentName(departmentName)
                .branchId(branchId)
                .branchName(branchName)
                .roomNumber(roomNumber)
                .avgConsultationMinutes(15)
                .active(true)
                .build();
        Doctor savedDoctor = doctorRepository.save(doctor);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName());

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserId(savedUser.getId());
        res.setEmail(savedUser.getEmail());
        res.setFullName(savedUser.getFullName());
        res.setRole(savedUser.getRole());
        res.setDoctorId(savedDoctor.getId());
        return res;
    }

    // ── Admin Registration ────────────────────────────────────────────────────

    public AuthResponse registerAdmin(String email, String rawPassword, String fullName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role("ROLE_ADMIN")
                .phoneNumber(phone)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName());

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserId(savedUser.getId());
        res.setEmail(savedUser.getEmail());
        res.setFullName(savedUser.getFullName());
        res.setRole(savedUser.getRole());
        return res;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getFullName());

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setRole(user.getRole());

        // Attach patientId or doctorId based on role
        if ("ROLE_PATIENT".equals(user.getRole())) {
            patientRepository.findByUserId(user.getId()).ifPresent(p -> res.setPatientId(p.getId()));
        } else if ("ROLE_DOCTOR".equals(user.getRole())) {
            doctorRepository.findByUserId(user.getId()).ifPresent(d -> res.setDoctorId(d.getId()));
        }

        return res;
    }

    // ── FCM Token Update ──────────────────────────────────────────────────────

    public void updateFcmToken(String userId, String fcmToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(fcmToken);
            userRepository.save(user);
        });
    }
}
