package com.relief.controller.ai;

import com.relief.service.ai.EarlyWarningService;
import com.relief.service.ai.EarlyWarningService.WarningRule;
import com.relief.service.ai.EarlyWarningService.EarlyWarning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/early-warnings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Early Warning Systems", description = "Real-time alerts based on predictive models")
public class EarlyWarningController {

    private final EarlyWarningService earlyWarningService;

    @PostMapping("/rules")
    @Operation(summary = "Create warning rule")
    public ResponseEntity<WarningRule> createRule(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String triggerCondition,
            @RequestBody Map<String, Object> thresholds,
            @RequestParam String action) {
        
        WarningRule rule = earlyWarningService.createRule(name, description, triggerCondition, thresholds, action);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active warnings")
    public ResponseEntity<List<EarlyWarning>> getActiveWarnings() {
        List<EarlyWarning> warnings = earlyWarningService.getActiveWarnings();
        return ResponseEntity.ok(warnings);
    }

    @GetMapping("/warnings")
    @Operation(summary = "Get warnings by rule")
    public ResponseEntity<List<EarlyWarning>> getWarnings(
            @RequestParam String ruleId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<EarlyWarning> warnings = earlyWarningService.getWarningsByRule(ruleId, start, end);
        return ResponseEntity.ok(warnings);
    }

    @PostMapping("/warnings/{warningId}/acknowledge")
    @Operation(summary = "Acknowledge warning")
    public ResponseEntity<Void> acknowledgeWarning(
            @PathVariable String warningId,
            @RequestParam String acknowledgedBy) {
        
        earlyWarningService.acknowledgeWarning(warningId, acknowledgedBy);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "Get warning rule")
    public ResponseEntity<WarningRule> getRule(@PathVariable String ruleId) {
        WarningRule rule = earlyWarningService.getRule(ruleId);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all rules")
    public ResponseEntity<List<WarningRule>> getRules() {
        List<WarningRule> rules = earlyWarningService.getRules();
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "Update warning rule")
    public ResponseEntity<Void> updateRule(
            @PathVariable String ruleId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String triggerCondition,
            @RequestBody Map<String, Object> thresholds,
            @RequestParam String action,
            @RequestParam String severity) {
        
        earlyWarningService.updateRule(ruleId, name, description, triggerCondition, thresholds, action, severity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete warning rule")
    public ResponseEntity<Void> deleteRule(@PathVariable String ruleId) {
        earlyWarningService.deleteRule(ruleId);
        return ResponseEntity.ok().build();
    }
}



