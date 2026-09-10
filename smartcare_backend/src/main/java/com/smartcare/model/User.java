package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String role; // ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN, ROLE_STAFF
    private String phoneNumber;
    private boolean active;
    private String fcmToken; // Firebase Cloud Messaging device token for push notifications
    @Builder.Default
    private Instant createdAt = Instant.now();
}
