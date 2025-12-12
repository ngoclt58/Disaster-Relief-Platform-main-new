package com.relief.controller;

import com.relief.dto.ShelterRequest;
import com.relief.dto.ShelterResponse;
import com.relief.service.ShelterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/shelters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shelters", description = "Emergency shelter management APIs")
public class ShelterController {

    private final ShelterService shelterService;

    @GetMapping
    @Operation(summary = "Get all shelters with optional pagination and filtering")
    public ResponseEntity<Page<ShelterResponse>> getAllShelters(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Search by name") @RequestParam(required = false) String search
    ) {
        log.info("GET /shelters - page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, search: {}", 
                page, size, sortBy, sortDir, status, search);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ShelterResponse> shelters;
        
        if (search != null && !search.trim().isEmpty()) {
            // For search, we'll get all matching results and manually paginate
            List<ShelterResponse> searchResults = shelterService.searchSheltersByName(search.trim());
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            List<ShelterResponse> pageContent = searchResults.subList(start, end);
            shelters = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, searchResults.size());
        } else {
            shelters = shelterService.getAllShelters(pageable);
        }

        log.info("Found {} shelters", shelters.getTotalElements());
        return ResponseEntity.ok(shelters);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shelter by ID")
    public ResponseEntity<ShelterResponse> getShelterById(
            @Parameter(description = "Shelter ID") @PathVariable UUID id
    ) {
        log.info("GET /shelters/{}", id);
        
        return shelterService.getShelterById(id)
                .map(shelter -> {
                    log.info("Found shelter: {}", shelter.getName());
                    return ResponseEntity.ok(shelter);
                })
                .orElseGet(() -> {
                    log.warn("Shelter not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available shelters (ACTIVE status and not full)")
    public ResponseEntity<List<ShelterResponse>> getAvailableShelters() {
        log.info("GET /shelters/available");
        
        List<ShelterResponse> availableShelters = shelterService.getAvailableShelters();
        log.info("Found {} available shelters", availableShelters.size());
        
        return ResponseEntity.ok(availableShelters);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby shelters")
    public ResponseEntity<List<ShelterResponse>> findNearbyShelters(
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("GET /shelters/nearby - lat: {}, lng: {}, limit: {}", latitude, longitude, limit);
        
        List<ShelterResponse> nearbyShelters = shelterService.findNearbyShelters(latitude, longitude, limit);
        log.info("Found {} nearby shelters", nearbyShelters.size());
        
        return ResponseEntity.ok(nearbyShelters);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get shelters by status")
    public ResponseEntity<List<ShelterResponse>> getSheltersByStatus(
            @Parameter(description = "Shelter status") @PathVariable String status
    ) {
        log.info("GET /shelters/status/{}", status);
        
        List<ShelterResponse> shelters = shelterService.getSheltersByStatus(status.toUpperCase());
        log.info("Found {} shelters with status: {}", shelters.size(), status);
        
        return ResponseEntity.ok(shelters);
    }

    @PostMapping
    @Operation(summary = "Create a new shelter")
    public ResponseEntity<ShelterResponse> createShelter(
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication
    ) {
        log.info("POST /shelters - Creating shelter: {}", request.getName());
        
        String currentUserId = authentication != null ? authentication.getName() : null;
        ShelterResponse createdShelter = shelterService.createShelter(request, currentUserId);
        
        log.info("Successfully created shelter with ID: {}", createdShelter.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShelter);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing shelter")
    public ResponseEntity<ShelterResponse> updateShelter(
            @Parameter(description = "Shelter ID") @PathVariable UUID id,
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication
    ) {
        log.info("PUT /shelters/{} - Updating shelter: {}", id, request.getName());
        
        String currentUserId = authentication != null ? authentication.getName() : null;
        
        return shelterService.updateShelter(id, request, currentUserId)
                .map(updatedShelter -> {
                    log.info("Successfully updated shelter with ID: {}", updatedShelter.getId());
                    return ResponseEntity.ok(updatedShelter);
                })
                .orElseGet(() -> {
                    log.warn("Shelter not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PatchMapping("/{id}/occupancy")
    @Operation(summary = "Update shelter occupancy")
    public ResponseEntity<ShelterResponse> updateOccupancy(
            @Parameter(description = "Shelter ID") @PathVariable UUID id,
            @Parameter(description = "New occupancy count") @RequestParam Integer occupancy
    ) {
        log.info("PATCH /shelters/{}/occupancy - New occupancy: {}", id, occupancy);
        
        return shelterService.updateOccupancy(id, occupancy)
                .map(updatedShelter -> {
                    log.info("Successfully updated occupancy for shelter ID: {}", updatedShelter.getId());
                    return ResponseEntity.ok(updatedShelter);
                })
                .orElseGet(() -> {
                    log.warn("Shelter not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shelter")
    public ResponseEntity<Void> deleteShelter(
            @Parameter(description = "Shelter ID") @PathVariable UUID id
    ) {
        log.info("DELETE /shelters/{}", id);
        
        boolean deleted = shelterService.deleteShelter(id);
        
        if (deleted) {
            log.info("Successfully deleted shelter with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Shelter not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get shelter statistics")
    public ResponseEntity<Map<String, Object>> getShelterStatistics() {
        log.info("GET /shelters/statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShelters", shelterService.getTotalShelters());
        stats.put("activeShelters", shelterService.getActiveShelters());
        stats.put("totalCapacity", shelterService.getTotalCapacity());
        stats.put("totalOccupancy", shelterService.getTotalOccupancy());
        stats.put("averageOccupancyRate", shelterService.getAverageOccupancyRate());
        
        log.info("Shelter statistics: {}", stats);
        return ResponseEntity.ok(stats);
    }
}