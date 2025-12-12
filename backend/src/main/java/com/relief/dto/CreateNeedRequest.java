package com.relief.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateNeedRequest {
    @NotBlank
    private String type; // food, water, medical, evacuation, sos, other

    @NotNull
    @Min(1)
    @Max(5)
    private Integer severity;

    private String notes;

    private Double lat;
    private Double lng;

    private List<UUID> mediaIds; // optional
}





