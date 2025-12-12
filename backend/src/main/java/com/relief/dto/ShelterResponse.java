package com.relief.dto;

import com.relief.entity.Shelter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShelterResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private Integer capacity;
    private Integer currentOccupancy;
    private String contactPhone;
    private String contactEmail;
    private List<String> facilities;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Boolean isEmergencyShelter;
    private List<String> accessibilityFeatures;
    private String operatingHours;
    private String managerName;
    private String managerPhone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Integer availableCapacity;
    private Double occupancyRate;
    private Boolean isAvailable;
    private Boolean isFull;

    public static ShelterResponse fromEntity(Shelter shelter) {
        return ShelterResponse.builder()
                .id(shelter.getId())
                .name(shelter.getName())
                .description(shelter.getDescription())
                .address(shelter.getAddress())
                .capacity(shelter.getCapacity())
                .currentOccupancy(shelter.getCurrentOccupancy())
                .contactPhone(shelter.getContactPhone())
                .contactEmail(shelter.getContactEmail())
                .facilities(shelter.getFacilities())
                .latitude(shelter.getLatitude())
                .longitude(shelter.getLongitude())
                .status(shelter.getStatus())
                .isEmergencyShelter(shelter.getIsEmergencyShelter())
                .accessibilityFeatures(shelter.getAccessibilityFeatures())
                .operatingHours(shelter.getOperatingHours())
                .managerName(shelter.getManagerName())
                .managerPhone(shelter.getManagerPhone())
                .notes(shelter.getNotes())
                .createdAt(shelter.getCreatedAt())
                .updatedAt(shelter.getUpdatedAt())
                .availableCapacity(shelter.getAvailableCapacity())
                .occupancyRate(shelter.getOccupancyRate())
                .isAvailable(shelter.isAvailable())
                .isFull(shelter.isFull())
                .build();
    }
}