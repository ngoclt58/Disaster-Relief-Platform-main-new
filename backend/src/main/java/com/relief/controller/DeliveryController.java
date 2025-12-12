package com.relief.controller;

import com.relief.dto.CreateDeliveryRequest;
import com.relief.entity.Delivery;
import com.relief.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Delivery proof and tracking endpoints")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Create delivery proof")
    public ResponseEntity<Delivery> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.createDelivery(
                request.getTaskId(),
                request.getRecipientName(),
                request.getRecipientPhone(),
                request.getNotes(),
                request.getProofMediaId()
        ));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get delivery by task ID")
    public ResponseEntity<Delivery> getDeliveryByTask(@PathVariable UUID taskId) {
        Delivery delivery = deliveryService.getDeliveryByTaskId(taskId);
        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(delivery);
    }
}



