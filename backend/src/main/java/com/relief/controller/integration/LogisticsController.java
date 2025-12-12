package com.relief.controller.integration;

import com.relief.service.integration.LogisticsService;
import com.relief.service.integration.LogisticsService.LogisticsItem;
import com.relief.service.integration.LogisticsService.LogisticsQuote;
import com.relief.service.integration.LogisticsService.LogisticsShipment;
import com.relief.service.integration.LogisticsService.LogisticsTracking;
import com.relief.service.integration.LogisticsService.LogisticsProvider;
import com.relief.service.integration.LogisticsService.LogisticsRoute;
import com.relief.service.integration.LogisticsService.LogisticsInventory;
import com.relief.service.integration.LogisticsService.LogisticsDeliverySchedule;
import com.relief.service.integration.LogisticsService.LogisticsAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Third-party logistics integration controller
 */
@RestController
@RequestMapping("/integration/logistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Logistics Integration", description = "Integration with delivery and logistics providers")
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/quotes")
    @Operation(summary = "Get logistics quote")
    public ResponseEntity<LogisticsQuote> getQuote(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestBody List<LogisticsItem> items,
            @RequestParam String serviceType) {
        
        LogisticsQuote quote = logisticsService.getQuote(origin, destination, items, serviceType);
        return ResponseEntity.ok(quote);
    }

    @PostMapping("/shipments")
    @Operation(summary = "Create logistics shipment")
    public ResponseEntity<LogisticsShipment> createShipment(
            @RequestParam String quoteId,
            @RequestParam String recipientName,
            @RequestParam String recipientAddress,
            @RequestParam String recipientPhone,
            @RequestBody Map<String, Object> specialInstructions) {
        
        LogisticsShipment shipment = logisticsService.createShipment(
            quoteId, recipientName, recipientAddress, recipientPhone, specialInstructions
        );
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/shipments/{shipmentId}/tracking")
    @Operation(summary = "Get shipment tracking info")
    public ResponseEntity<LogisticsTracking> getTrackingInfo(@PathVariable String shipmentId) {
        LogisticsTracking tracking = logisticsService.getTrackingInfo(shipmentId);
        return ResponseEntity.ok(tracking);
    }

    @GetMapping("/providers")
    @Operation(summary = "Get logistics providers")
    public ResponseEntity<List<LogisticsProvider>> getProviders(
            @RequestParam String serviceType,
            @RequestParam String region) {
        
        List<LogisticsProvider> providers = logisticsService.getProviders(serviceType, region);
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/routes/optimize")
    @Operation(summary = "Get optimal route")
    public ResponseEntity<LogisticsRoute> getOptimalRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestBody List<String> waypoints,
            @RequestParam String priority) {
        
        LogisticsRoute route = logisticsService.getOptimalRoute(origin, destination, waypoints, priority);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/providers/{providerId}/inventory")
    @Operation(summary = "Get provider inventory")
    public ResponseEntity<LogisticsInventory> getInventory(
            @PathVariable String providerId,
            @RequestParam String itemType) {
        
        LogisticsInventory inventory = logisticsService.getInventory(providerId, itemType);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/delivery-schedule")
    @Operation(summary = "Get delivery schedule")
    public ResponseEntity<LogisticsDeliverySchedule> getDeliverySchedule(
            @RequestParam String region,
            @RequestParam String serviceType) {
        
        LogisticsDeliverySchedule schedule = logisticsService.getDeliverySchedule(region, serviceType);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/shipments/{shipmentId}/status")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<Boolean> updateShipmentStatus(
            @PathVariable String shipmentId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        
        boolean success = logisticsService.updateShipmentStatus(shipmentId, status, notes);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get logistics analytics")
    public ResponseEntity<LogisticsAnalytics> getAnalytics(
            @RequestParam String providerId,
            @RequestParam String timeRange) {
        
        LogisticsAnalytics analytics = logisticsService.getAnalytics(providerId, timeRange);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to shipment updates")
    public ResponseEntity<Void> subscribeToShipmentUpdates(
            @RequestParam String shipmentId,
            @RequestParam String callbackUrl) {
        
        logisticsService.subscribeToShipmentUpdates(shipmentId, callbackUrl);
        return ResponseEntity.ok().build();
    }
}


