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
@Document(collection = "hospitals")
public class Hospital {
    @Id
    private String id;
    private String name;
    private String code;
    private String address;
    private String contactPhone;
    private boolean active;
}
