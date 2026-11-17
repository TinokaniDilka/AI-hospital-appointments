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

    @Data
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;
    }

    @Data
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
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

    // ── Forgot / Reset Password ───────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            String otp = authService.generatePasswordResetOtp(req.getEmail());
            // Return OTP in response for development/testing
            return ResponseEntity.ok(Map.of(
                "message", "OTP sent to your email. For development: " + otp,
                "otp", otp,
                "email", req.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            boolean isValid = authService.verifyOtp(req.getEmail(), req.getOtp());
            if (isValid) {
                return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            authService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}