package com.relief.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResidentProfileRequest {
    @NotBlank
    private String address;
    private String wardId; // UUID string
    private Double lat; // optional precise location
    private Double lng;
    private Integer householdSize;
}





