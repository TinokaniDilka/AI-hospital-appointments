package com.smartcare.controller;

import com.smartcare.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private String gender;
        private String dob;
    }

    @Data
    public static class RegisterDoctorRequest {
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private String specialization;
        private String departmentId;
        private String departmentName;
        private String branchId;
        private String branchName;
        private String roomNumber;
    }

    @Data
    public static class RegisterAdminRequest {
        private String email;
        private String password;
        private String fullName;
        private String phone;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class FcmTokenRequest {
        private String fcmToken;
    }

    // ── Patient Registration ──────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            AuthService.AuthResponse res = authService.registerPatient(
                    req.getEmail(), req.getPassword(), req.getFullName(), req.getPhone(), req.getGender(), req.getDob());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Doctor Registration ───────────────────────────────────────────────────

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody RegisterDoctorRequest req) {
        try {
            AuthService.AuthResponse res = authService.registerDoctor(
                    req.getEmail(), req.getPassword(), req.getFullName(), req.getPhone(),
                    req.getSpecialization(), req.getDepartmentId(), req.getDepartmentName(),
                    req.getBranchId(), req.getBranchName(), req.getRoomNumber());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Admin Registration (Admin only) ───────────────────────────────────────

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterAdminRequest req) {
        try {
            AuthService.AuthResponse res = authService.registerAdmin(
                    req.getEmail(), req.getPassword(), req.getFullName(), req.getPhone());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            System.out.println("Login attempt for email: " + req.getEmail());
            AuthService.AuthResponse res = authService.login(req.getEmail(), req.getPassword());
            System.out.println("Login successful for: " + req.getEmail());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            System.out.println("Login failed for " + req.getEmail() + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── FCM Token Registration ─────────────────────────────────────────────────

    @PostMapping("/fcm-token")
    public ResponseEntity<?> registerFcmToken(@RequestBody FcmTokenRequest req, Authentication authentication) {
        try {
            String userId = authentication.getName(); // JWT subject = userId
            authService.updateFcmToken(userId, req.getFcmToken());
            return ResponseEntity.ok().body(Map.of("status", "FCM_TOKEN_UPDATED"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}