package com.relief.controller.realtime;

import com.relief.service.realtime.EventCorrelationService;
import com.relief.service.realtime.EventCorrelationService.CorrelationRule;
import com.relief.service.realtime.EventCorrelationService.Event;
import com.relief.service.realtime.EventCorrelationService.CorrelationResult;
import com.relief.service.realtime.EventCorrelationService.EventPattern;
import com.relief.service.realtime.EventCorrelationService.CorrelationAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Event correlation controller to connect related events across different data sources
 */
@RestController
@RequestMapping("/api/realtime/event-correlation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Correlation", description = "Connect related events across different data sources")
public class EventCorrelationController {

    private final EventCorrelationService eventCorrelationService;

    @PostMapping("/rules")
    @Operation(summary = "Create correlation rule")
    public ResponseEntity<CorrelationRule> createRule(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String pattern,
            @RequestBody Map<String, Object> conditions,
            @RequestParam String action) {
        
        CorrelationRule rule = eventCorrelationService.createRule(name, description, pattern, conditions, action);
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/events")
    @Operation(summary = "Process event")
    public ResponseEntity<Void> processEvent(@RequestBody Event event) {
        eventCorrelationService.processEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/correlate")
    @Operation(summary = "Correlate events")
    public ResponseEntity<CorrelationResult> correlateEvents(
            @RequestParam String ruleId,
            @RequestBody List<String> eventIds) {
        
        CorrelationResult result = eventCorrelationService.correlateEvents(ruleId, eventIds);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/correlations")
    @Operation(summary = "Find correlations")
    public ResponseEntity<List<CorrelationResult>> findCorrelations(
            @RequestParam(required = false) String source,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<CorrelationResult> correlations = eventCorrelationService.findCorrelations(source, start, end);
        return ResponseEntity.ok(correlations);
    }

    @PostMapping("/patterns/detect")
    @Operation(summary = "Detect event pattern")
    public ResponseEntity<EventPattern> detectPattern(
            @RequestParam String source,
            @RequestParam String eventType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        EventPattern pattern = eventCorrelationService.detectPattern(source, eventType, start, end);
        return ResponseEntity.ok(pattern);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get correlation analytics")
    public ResponseEntity<CorrelationAnalytics> getAnalytics(@RequestParam String source) {
        CorrelationAnalytics analytics = eventCorrelationService.getAnalytics(source);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "Get correlation rule")
    public ResponseEntity<CorrelationRule> getRule(@PathVariable String ruleId) {
        CorrelationRule rule = eventCorrelationService.getRule(ruleId);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all correlation rules")
    public ResponseEntity<List<CorrelationRule>> getRules() {
        List<CorrelationRule> rules = eventCorrelationService.getRules();
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "Update correlation rule")
    public ResponseEntity<Void> updateRule(
            @PathVariable String ruleId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String pattern,
            @RequestBody Map<String, Object> conditions,
            @RequestParam String action) {
        
        eventCorrelationService.updateRule(ruleId, name, description, pattern, conditions, action);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete correlation rule")
    public ResponseEntity<Void> deleteRule(@PathVariable String ruleId) {
        eventCorrelationService.deleteRule(ruleId);
        return ResponseEntity.ok().build();
    }
}


