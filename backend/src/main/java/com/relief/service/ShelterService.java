package com.relief.service;

import com.relief.dto.ShelterRequest;
import com.relief.dto.ShelterResponse;
import com.relief.entity.Shelter;
import com.relief.entity.User;
import com.relief.repository.ShelterRepository;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShelterService {

    private final ShelterRepository shelterRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Page<ShelterResponse> getAllShelters(Pageable pageable) {
        log.info("Fetching all shelters with pagination: {}", pageable);
        return shelterRepository.findAll(pageable)
                .map(ShelterResponse::fromEntity);
    }

    public List<ShelterResponse> getAllShelters() {
        log.info("Fetching all shelters");
        return shelterRepository.findAll().stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ShelterResponse> getShelterById(UUID id) {
        log.info("Fetching shelter by ID: {}", id);
        return shelterRepository.findById(id)
                .map(ShelterResponse::fromEntity);
    }

    public List<ShelterResponse> getAvailableShelters() {
        log.info("Fetching available shelters");
        return shelterRepository.findAvailableShelters().stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShelterResponse> getSheltersByStatus(String status) {
        log.info("Fetching shelters by status: {}", status);
        return shelterRepository.findByStatus(status).stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShelterResponse> searchSheltersByName(String name) {
        log.info("Searching shelters by name: {}", name);
        return shelterRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShelterResponse> findNearbyShelters(Double latitude, Double longitude, Integer limit) {
        log.info("Finding nearby shelters for coordinates: {}, {} with limit: {}", latitude, longitude, limit);
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return shelterRepository.findNearbyShelters(latitude, longitude, limit).stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShelterResponse> findSheltersWithFacilities(List<String> facilities) {
        log.info("Finding shelters with facilities: {}", facilities);
        return shelterRepository.findByFacilitiesIn(facilities).stream()
                .map(ShelterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ShelterResponse createShelter(ShelterRequest request, String currentUserId) {
        log.info("Creating new shelter: {}", request.getName());
        
        Shelter shelter = Shelter.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .capacity(request.getCapacity())
                .currentOccupancy(request.getCurrentOccupancy())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .facilities(request.getFacilities())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(request.getStatus())
                .isEmergencyShelter(request.getIsEmergencyShelter())
                .accessibilityFeatures(request.getAccessibilityFeatures())
                .operatingHours(request.getOperatingHours())
                .managerName(request.getManagerName())
                .managerPhone(request.getManagerPhone())
                .notes(request.getNotes())
                .build();

        // Set geometry point if coordinates are provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point point = geometryFactory.createPoint(
                new Coordinate(request.getLongitude().doubleValue(), request.getLatitude().doubleValue())
            );
            point.setSRID(4326);
            shelter.setGeomPoint(point);
        }

        // Set created by user if provided
        if (currentUserId != null) {
            try {
                UUID userId = UUID.fromString(currentUserId);
                Optional<User> user = userRepository.findById(userId);
                user.ifPresent(shelter::setCreatedBy);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid user ID format: {}", currentUserId);
            }
        }

        Shelter savedShelter = shelterRepository.save(shelter);
        log.info("Successfully created shelter with ID: {}", savedShelter.getId());
        
        return ShelterResponse.fromEntity(savedShelter);
    }

    public Optional<ShelterResponse> updateShelter(UUID id, ShelterRequest request, String currentUserId) {
        log.info("Updating shelter with ID: {}", id);
        
        return shelterRepository.findById(id)
                .map(shelter -> {
                    shelter.setName(request.getName());
                    shelter.setDescription(request.getDescription());
                    shelter.setAddress(request.getAddress());
                    shelter.setCapacity(request.getCapacity());
                    shelter.setCurrentOccupancy(request.getCurrentOccupancy());
                    shelter.setContactPhone(request.getContactPhone());
                    shelter.setContactEmail(request.getContactEmail());
                    shelter.setFacilities(request.getFacilities());
                    shelter.setLatitude(request.getLatitude());
                    shelter.setLongitude(request.getLongitude());
                    shelter.setStatus(request.getStatus());
                    shelter.setIsEmergencyShelter(request.getIsEmergencyShelter());
                    shelter.setAccessibilityFeatures(request.getAccessibilityFeatures());
                    shelter.setOperatingHours(request.getOperatingHours());
                    shelter.setManagerName(request.getManagerName());
                    shelter.setManagerPhone(request.getManagerPhone());
                    shelter.setNotes(request.getNotes());

                    // Update geometry point if coordinates are provided
                    if (request.getLatitude() != null && request.getLongitude() != null) {
                        Point point = geometryFactory.createPoint(
                            new Coordinate(request.getLongitude().doubleValue(), request.getLatitude().doubleValue())
                        );
                        point.setSRID(4326);
                        shelter.setGeomPoint(point);
                    }

                    // Set updated by user if provided
                    if (currentUserId != null) {
                        try {
                            UUID userId = UUID.fromString(currentUserId);
                            Optional<User> user = userRepository.findById(userId);
                            user.ifPresent(shelter::setUpdatedBy);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid user ID format: {}", currentUserId);
                        }
                    }

                    Shelter updatedShelter = shelterRepository.save(shelter);
                    log.info("Successfully updated shelter with ID: {}", updatedShelter.getId());
                    
                    return ShelterResponse.fromEntity(updatedShelter);
                });
    }

    public boolean deleteShelter(UUID id) {
        log.info("Deleting shelter with ID: {}", id);
        
        if (shelterRepository.existsById(id)) {
            shelterRepository.deleteById(id);
            log.info("Successfully deleted shelter with ID: {}", id);
            return true;
        }
        
        log.warn("Shelter with ID {} not found for deletion", id);
        return false;
    }

    public Optional<ShelterResponse> updateOccupancy(UUID id, Integer newOccupancy) {
        log.info("Updating occupancy for shelter ID: {} to {}", id, newOccupancy);
        
        return shelterRepository.findById(id)
                .map(shelter -> {
                    shelter.setCurrentOccupancy(newOccupancy);
                    
                    // Auto-update status based on occupancy
                    if (newOccupancy >= shelter.getCapacity()) {
                        shelter.setStatus("FULL");
                    } else if ("FULL".equals(shelter.getStatus()) && newOccupancy < shelter.getCapacity()) {
                        shelter.setStatus("ACTIVE");
                    }
                    
                    Shelter updatedShelter = shelterRepository.save(shelter);
                    log.info("Successfully updated occupancy for shelter ID: {}", updatedShelter.getId());
                    
                    return ShelterResponse.fromEntity(updatedShelter);
                });
    }

    // Statistics methods
    public Long getTotalShelters() {
        return shelterRepository.count();
    }

    public Long getActiveShelters() {
        return shelterRepository.countByStatus("ACTIVE");
    }

    public Long getTotalCapacity() {
        Long capacity = shelterRepository.getTotalActiveCapacity();
        return capacity != null ? capacity : 0L;
    }

    public Long getTotalOccupancy() {
        Long occupancy = shelterRepository.getTotalCurrentOccupancy();
        return occupancy != null ? occupancy : 0L;
    }

    public Double getAverageOccupancyRate() {
        Double rate = shelterRepository.getAverageOccupancyRate();
        return rate != null ? rate : 0.0;
    }
}