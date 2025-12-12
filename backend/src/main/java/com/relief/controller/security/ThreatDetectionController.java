package com.relief.controller.security;

import com.relief.service.security.ThreatDetectionService;
import com.relief.service.security.ThreatDetectionService.SecurityEvent;
import com.relief.service.security.ThreatDetectionService.ThreatAlert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/security/threats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Threat Detection", description = "ML-based anomaly detection for security threats")
public class ThreatDetectionController {

    private final ThreatDetectionService threatDetectionService;

    @PostMapping("/events")
    @Operation(summary = "Record security event")
    public ResponseEntity<SecurityEvent> recordEvent(
            @RequestParam(required = false) String userId,
            @RequestParam String ip,
            @RequestParam String action,
            @RequestBody(required = false) Map<String, Object> context) {
        SecurityEvent e = threatDetectionService.recordEvent(userId, ip, action, context);
        return ResponseEntity.ok(e);
    }

    @GetMapping("/events")
    @Operation(summary = "Get recent events")
    public ResponseEntity<List<SecurityEvent>> getRecent(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(threatDetectionService.getRecentEvents(limit));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active threat alerts")
    public ResponseEntity<List<ThreatAlert>> alerts() {
        return ResponseEntity.ok(threatDetectionService.getActiveAlerts());
    }

    @PostMapping("/alerts/{alertId}/ack")
    @Operation(summary = "Acknowledge alert")
    public ResponseEntity<Void> acknowledge(@PathVariable String alertId, @RequestParam String userId) {
        threatDetectionService.acknowledgeAlert(alertId, userId);
        return ResponseEntity.ok().build();
    }
}




