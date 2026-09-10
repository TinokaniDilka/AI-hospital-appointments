package com.smartcare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "patients")
public class Patient {
    @Id
    private String id;
    private String userId;
    private String fullName;
    private String dob;
    private String gender;
    private String bloodGroup;
    private String emergencyContact;
    private String medicalNotes;
}
