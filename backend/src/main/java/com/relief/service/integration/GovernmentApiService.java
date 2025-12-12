package com.relief.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Government API integration service for official disaster management systems
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GovernmentApiService {

    private static final Logger log = LoggerFactory.getLogger(GovernmentApiService.class);

    private final RestTemplate restTemplate;
    private final IntegrationConfigService integrationConfigService;

    public GovernmentDisasterData getDisasterData(String region, String disasterType) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/disasters?region=%s&type=%s", apiUrl, region, disasterType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentDisasterData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentDisasterData.class
            );
            
            log.info("Retrieved disaster data from government API for region: {}", region);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving disaster data from government API", e);
            return new GovernmentDisasterData();
        }
    }

    public List<GovernmentAlert> getActiveAlerts(String region) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/alerts?region=%s&status=active", apiUrl, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentAlert[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentAlert[].class
            );
            
            log.info("Retrieved {} active alerts from government API", response.getBody().length);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving alerts from government API", e);
            return Collections.emptyList();
        }
    }

    public GovernmentResourceData getResourceAvailability(String region, String resourceType) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/resources?region=%s&type=%s", apiUrl, region, resourceType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentResourceData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentResourceData.class
            );
            
            log.info("Retrieved resource availability from government API");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving resource data from government API", e);
            return new GovernmentResourceData();
        }
    }

    public GovernmentEvacuationData getEvacuationRoutes(String region) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/evacuation-routes?region=%s", apiUrl, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentEvacuationData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentEvacuationData.class
            );
            
            log.info("Retrieved evacuation routes from government API");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving evacuation routes from government API", e);
            return new GovernmentEvacuationData();
        }
    }

    public GovernmentShelterData getShelterInformation(String region) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/shelters?region=%s", apiUrl, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentShelterData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentShelterData.class
            );
            
            log.info("Retrieved shelter information from government API");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving shelter data from government API", e);
            return new GovernmentShelterData();
        }
    }

    public boolean reportIncident(GovernmentIncidentReport report) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = apiUrl + "/incidents";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<GovernmentIncidentReport> entity = new HttpEntity<>(report, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
            );
            
            log.info("Reported incident to government API: {}", report.getIncidentId());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error reporting incident to government API", e);
            return false;
        }
    }

    public List<GovernmentEmergencyContact> getEmergencyContacts(String region) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/emergency-contacts?region=%s", apiUrl, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentEmergencyContact[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentEmergencyContact[].class
            );
            
            log.info("Retrieved emergency contacts from government API");
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving emergency contacts from government API", e);
            return Collections.emptyList();
        }
    }

    public GovernmentComplianceStatus checkCompliance(String organizationId) {
        try {
            String apiUrl = integrationConfigService.getGovernmentApiUrl();
            String endpoint = String.format("%s/compliance?organizationId=%s", apiUrl, organizationId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getGovernmentApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GovernmentComplianceStatus> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, GovernmentComplianceStatus.class
            );
            
            log.info("Checked compliance status for organization: {}", organizationId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error checking compliance status", e);
            return new GovernmentComplianceStatus();
        }
    }

    public void syncData() {
        log.info("Starting government API data synchronization");
        // Implementation for periodic data synchronization
    }

    // Data classes
    public static class GovernmentDisasterData {
        private String region;
        private String disasterType;
        private String severity;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
        private List<String> affectedAreas;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getAffectedAreas() { return affectedAreas; }
        public void setAffectedAreas(List<String> affectedAreas) { this.affectedAreas = affectedAreas; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class GovernmentAlert {
        private String id;
        private String region;
        private String alertType;
        private String severity;
        private String message;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
        private String source;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getIssuedAt() { return issuedAt; }
        public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    public static class GovernmentResourceData {
        private String region;
        private String resourceType;
        private int availableQuantity;
        private int totalQuantity;
        private List<String> locations;
        private Map<String, Object> specifications;

        // Getters and setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

        public int getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

        public List<String> getLocations() { return locations; }
        public void setLocations(List<String> locations) { this.locations = locations; }

        public Map<String, Object> getSpecifications() { return specifications; }
        public void setSpecifications(Map<String, Object> specifications) { this.specifications = specifications; }
    }

    public static class GovernmentEvacuationData {
        private String region;
        private List<EvacuationRoute> routes;
        private List<String> assemblyPoints;
        private Map<String, Object> instructions;

        // Getters and setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public List<EvacuationRoute> getRoutes() { return routes; }
        public void setRoutes(List<EvacuationRoute> routes) { this.routes = routes; }

        public List<String> getAssemblyPoints() { return assemblyPoints; }
        public void setAssemblyPoints(List<String> assemblyPoints) { this.assemblyPoints = assemblyPoints; }

        public Map<String, Object> getInstructions() { return instructions; }
        public void setInstructions(Map<String, Object> instructions) { this.instructions = instructions; }
    }

    public static class EvacuationRoute {
        private String id;
        private String name;
        private String fromLocation;
        private String toLocation;
        private List<String> waypoints;
        private String status;
        private int capacity;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getFromLocation() { return fromLocation; }
        public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

        public String getToLocation() { return toLocation; }
        public void setToLocation(String toLocation) { this.toLocation = toLocation; }

        public List<String> getWaypoints() { return waypoints; }
        public void setWaypoints(List<String> waypoints) { this.waypoints = waypoints; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
    }

    public static class GovernmentShelterData {
        private String region;
        private List<Shelter> shelters;
        private int totalCapacity;
        private int currentOccupancy;

        // Getters and setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public List<Shelter> getShelters() { return shelters; }
        public void setShelters(List<Shelter> shelters) { this.shelters = shelters; }

        public int getTotalCapacity() { return totalCapacity; }
        public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

        public int getCurrentOccupancy() { return currentOccupancy; }
        public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    }

    public static class Shelter {
        private String id;
        private String name;
        private String address;
        private int capacity;
        private int currentOccupancy;
        private List<String> amenities;
        private String status;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getCurrentOccupancy() { return currentOccupancy; }
        public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }

        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class GovernmentIncidentReport {
        private String incidentId;
        private String incidentType;
        private String location;
        private String description;
        private String severity;
        private LocalDateTime reportedAt;
        private String reporterId;
        private Map<String, Object> details;

        // Getters and setters
        public String getIncidentId() { return incidentId; }
        public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

        public String getIncidentType() { return incidentType; }
        public void setIncidentType(String incidentType) { this.incidentType = incidentType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getReportedAt() { return reportedAt; }
        public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

        public String getReporterId() { return reporterId; }
        public void setReporterId(String reporterId) { this.reporterId = reporterId; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    public static class GovernmentEmergencyContact {
        private String id;
        private String name;
        private String department;
        private String phone;
        private String email;
        private String region;
        private String role;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class GovernmentComplianceStatus {
        private String organizationId;
        private boolean isCompliant;
        private List<String> complianceIssues;
        private LocalDateTime lastChecked;
        private String complianceLevel;

        // Getters and setters
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

        public boolean isCompliant() { return isCompliant; }
        public void setCompliant(boolean compliant) { isCompliant = compliant; }

        public List<String> getComplianceIssues() { return complianceIssues; }
        public void setComplianceIssues(List<String> complianceIssues) { this.complianceIssues = complianceIssues; }

        public LocalDateTime getLastChecked() { return lastChecked; }
        public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

        public String getComplianceLevel() { return complianceLevel; }
        public void setComplianceLevel(String complianceLevel) { this.complianceLevel = complianceLevel; }
    }
}


