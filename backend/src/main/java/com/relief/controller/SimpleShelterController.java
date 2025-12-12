package com.relief.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/simple-shelters")
@RequiredArgsConstructor
@Slf4j
public class SimpleShelterController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllShelters() {
        log.info("GET /simple-shelters");
        
        // Return simple mock data for testing
        List<Map<String, Object>> shelters = Arrays.asList(
            createMockShelter("1", "Test Shelter 1", "123 Main St", 100, 20),
            createMockShelter("2", "Test Shelter 2", "456 Oak Ave", 200, 50),
            createMockShelter("3", "Test Shelter 3", "789 Pine Rd", 150, 75)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", shelters);
        response.put("totalElements", shelters.size());
        response.put("totalPages", 1);
        response.put("size", 20);
        response.put("number", 0);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShelter(@RequestBody Map<String, Object> request) {
        log.info("POST /simple-shelters - Creating shelter: {}", request.get("name"));
        
        // Create a simple response
        Map<String, Object> shelter = createMockShelter(
            UUID.randomUUID().toString(),
            (String) request.get("name"),
            (String) request.get("address"),
            (Integer) request.get("capacity"),
            0
        );
        
        return ResponseEntity.ok(shelter);
    }
    
    private Map<String, Object> createMockShelter(String id, String name, String address, int capacity, int occupancy) {
        Map<String, Object> shelter = new HashMap<>();
        shelter.put("id", id);
        shelter.put("name", name);
        shelter.put("address", address);
        shelter.put("capacity", capacity);
        shelter.put("currentOccupancy", occupancy);
        shelter.put("status", "ACTIVE");
        shelter.put("isEmergencyShelter", true);
        shelter.put("operatingHours", "24/7");
        shelter.put("availableCapacity", capacity - occupancy);
        shelter.put("occupancyRate", capacity > 0 ? (double) occupancy / capacity * 100.0 : 0.0);
        shelter.put("isAvailable", true);
        shelter.put("isFull", occupancy >= capacity);
        shelter.put("facilities", Arrays.asList("wifi", "kitchen", "parking"));
        shelter.put("createdAt", new Date().toString());
        shelter.put("updatedAt", new Date().toString());
        return shelter;
    }
}