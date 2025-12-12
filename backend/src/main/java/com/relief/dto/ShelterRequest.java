package com.relief.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShelterRequest {
    
    @NotBlank(message = "Shelter name is required")
    @Size(max = 255, message = "Shelter name must not exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity must not exceed 10,000")
    private Integer capacity;

    @Min(value = 0, message = "Current occupancy cannot be negative")
    private Integer currentOccupancy = 0;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Invalid phone number format")
    private String contactPhone;

    @Email(message = "Invalid email format")
    private String contactEmail;

    private List<String> facilities;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Pattern(regexp = "^(ACTIVE|FULL|CLOSED|MAINTENANCE)$", message = "Status must be ACTIVE, FULL, CLOSED, or MAINTENANCE")
    private String status = "ACTIVE";

    private Boolean isEmergencyShelter = true;

    private List<String> accessibilityFeatures;

    private String operatingHours = "24/7";

    @Size(max = 255, message = "Manager name must not exceed 255 characters")
    private String managerName;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Invalid manager phone number format")
    private String managerPhone;

    private String notes;
}