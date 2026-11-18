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

    private final java.util.concurrent.ConcurrentHashMap<String, String> otpStore = new java.util.concurrent.ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

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

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName(), savedPatient.getId(), null);

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

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName(), null, savedDoctor.getId());

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

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getFullName(), null, null);

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

        // Fetch patientId or doctorId based on role
String patientId = null;
String doctorId = null;

if ("ROLE_PATIENT".equals(user.getRole())) {
    var patient = patientRepository.findByUserId(user.getId());
    if (patient.isPresent()) {
        patientId = patient.get().getId();
    }
} else if ("ROLE_DOCTOR".equals(user.getRole())) {
    var doctor = doctorRepository.findByUserId(user.getId());
    if (doctor.isPresent()) {
        doctorId = doctor.get().getId();
    }
}

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getFullName(), patientId, doctorId);

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setRole(user.getRole());
        res.setPatientId(patientId);
        res.setDoctorId(doctorId);

        return res;
    }

    // ── FCM Token Update ──────────────────────────────────────────────────────

    public void updateFcmToken(String userId, String fcmToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(fcmToken);
            userRepository.save(user);
        });
    }

    // ── Password Reset OTP ────────────────────────────────────────────────────

    public String generatePasswordResetOtp(String email) {
        userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));
        String otp = String.format("%06d", (int)(Math.random() * 1000000));
        otpStore.put(email, otp);
        
        // Send OTP via email
        try {
            emailService.sendOtpEmail(email, otp);
            System.out.println("OTP sent successfully to email: " + email);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            System.err.println("Email configuration may be incorrect. Check application.properties");
            System.out.println("DEV MODE - OTP for " + email + ": " + otp);
        }
        
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String stored = otpStore.get(email);
        if (stored == null || !stored.equals(otp)) {
            return false;
        }
        return true;
    }

    public void resetPassword(String email, String otp, String newPassword) {
        String stored = otpStore.get(email);
        if (stored == null || !stored.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
        otpStore.remove(email);
    }
}
