package com.relief.controller.integration;

import com.relief.service.integration.GovernmentApiService;
import com.relief.service.integration.GovernmentApiService.GovernmentDisasterData;
import com.relief.service.integration.GovernmentApiService.GovernmentAlert;
import com.relief.service.integration.GovernmentApiService.GovernmentResourceData;
import com.relief.service.integration.GovernmentApiService.GovernmentEvacuationData;
import com.relief.service.integration.GovernmentApiService.GovernmentShelterData;
import com.relief.service.integration.GovernmentApiService.GovernmentIncidentReport;
import com.relief.service.integration.GovernmentApiService.GovernmentEmergencyContact;
import com.relief.service.integration.GovernmentApiService.GovernmentComplianceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Government API integration controller
 */
@RestController
@RequestMapping("/integration/government")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Government API Integration", description = "Integration with official disaster management systems")
public class GovernmentApiController {

    private final GovernmentApiService governmentApiService;

    @GetMapping("/disasters")
    @Operation(summary = "Get disaster data from government API")
    public ResponseEntity<GovernmentDisasterData> getDisasterData(
            @RequestParam String region,
            @RequestParam String disasterType) {
        
        GovernmentDisasterData data = governmentApiService.getDisasterData(region, disasterType);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active alerts from government API")
    public ResponseEntity<List<GovernmentAlert>> getActiveAlerts(
            @RequestParam String region) {
        
        List<GovernmentAlert> alerts = governmentApiService.getActiveAlerts(region);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/resources")
    @Operation(summary = "Get resource availability from government API")
    public ResponseEntity<GovernmentResourceData> getResourceAvailability(
            @RequestParam String region,
            @RequestParam String resourceType) {
        
        GovernmentResourceData data = governmentApiService.getResourceAvailability(region, resourceType);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/evacuation-routes")
    @Operation(summary = "Get evacuation routes from government API")
    public ResponseEntity<GovernmentEvacuationData> getEvacuationRoutes(
            @RequestParam String region) {
        
        GovernmentEvacuationData data = governmentApiService.getEvacuationRoutes(region);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/shelters")
    @Operation(summary = "Get shelter information from government API")
    public ResponseEntity<GovernmentShelterData> getShelterInformation(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        // If location provided, use it; otherwise use region
        if (latitude != null && longitude != null) {
            // For now, use region-based lookup (can be enhanced with location-based search)
            GovernmentShelterData data = governmentApiService.getShelterInformation(region != null ? region : "default");
            return ResponseEntity.ok(data);
        } else if (region != null) {
            GovernmentShelterData data = governmentApiService.getShelterInformation(region);
            return ResponseEntity.ok(data);
        } else {
            // Default region if nothing provided
            GovernmentShelterData data = governmentApiService.getShelterInformation("default");
            return ResponseEntity.ok(data);
        }
    }

    @PostMapping("/incidents")
    @Operation(summary = "Report incident to government API")
    public ResponseEntity<Boolean> reportIncident(
            @RequestBody GovernmentIncidentReport report) {
        
        boolean success = governmentApiService.reportIncident(report);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/emergency-contacts")
    @Operation(summary = "Get emergency contacts from government API")
    public ResponseEntity<List<GovernmentEmergencyContact>> getEmergencyContacts(
            @RequestParam String region) {
        
        List<GovernmentEmergencyContact> contacts = governmentApiService.getEmergencyContacts(region);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/compliance")
    @Operation(summary = "Check compliance status with government API")
    public ResponseEntity<GovernmentComplianceStatus> checkCompliance(
            @RequestParam String organizationId) {
        
        GovernmentComplianceStatus status = governmentApiService.checkCompliance(organizationId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync data with government API")
    public ResponseEntity<Void> syncData() {
        governmentApiService.syncData();
        return ResponseEntity.ok().build();
    }
}


