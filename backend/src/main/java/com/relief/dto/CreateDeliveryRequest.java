package com.relief.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDeliveryRequest {
    
    @NotNull
    private UUID taskId;
    
    @NotBlank
    private String recipientName;
    
    private String recipientPhone;
    
    private String notes;
    
    private UUID proofMediaId;
}



