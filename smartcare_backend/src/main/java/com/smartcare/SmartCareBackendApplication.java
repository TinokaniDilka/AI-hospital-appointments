package com.smartcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartCareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCareBackendApplication.class, args);
    }
}
