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
@Document(collection = "branches")
public class Branch {
    @Id
    private String id;
    private String hospitalId;
    private String branchName;
    private String city;
    private String address;
    private String phone;
    private boolean active;
}
