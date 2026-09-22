package com.smartcare.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfig {

    @Value("${firebase.config.file:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                    new ClassPathResource(firebaseConfigPath).getInputStream());
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.warn("Firebase configuration file not found. Firebase features will be disabled.");
            log.warn("To enable Firebase, place firebase-service-account.json in src/main/resources/");
        }
    }
}
