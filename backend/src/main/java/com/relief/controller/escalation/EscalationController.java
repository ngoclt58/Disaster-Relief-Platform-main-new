package com.relief.controller.escalation;

import com.relief.service.escalation.AutoEscalationService;
import com.relief.service.escalation.EscalationRuleService;
import com.relief.service.escalation.EscalationRuleService.EscalationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for auto-escalation management
 */
@RestController
@RequestMapping("/escalations")
@RequiredArgsConstructor
@Tag(name = "Auto-Escalation", description = "Escalation rules and management APIs")
public class EscalationController {

    private final AutoEscalationService escalationService;
    private final EscalationRuleService ruleService;

    @GetMapping("/rules")
    @Operation(summary = "Get all escalation rules")
    public ResponseEntity<List<EscalationRule>> getEscalationRules() {
        // In real implementation, return all rules
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "Get specific escalation rule")
    public ResponseEntity<EscalationRule> getEscalationRule(@PathVariable String ruleId) {
        EscalationRule rule = ruleService.getRuleForRequest(null); // Simplified
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/trigger")
    @Operation(summary = "Manually trigger escalation check")
    public ResponseEntity<Map<String, String>> triggerEscalationCheck() {
        escalationService.checkAndEscalate();
        return ResponseEntity.ok(Map.of("status", "escalation_check_triggered"));
    }

    @GetMapping("/status")
    @Operation(summary = "Get escalation system status")
    public ResponseEntity<Map<String, Object>> getEscalationStatus() {
        Map<String, Object> status = Map.of(
            "active", true,
            "lastCheck", "2024-01-01T00:00:00Z",
            "totalRules", 6,
            "escalationsToday", 0
        );
        return ResponseEntity.ok(status);
    }

    @PostMapping("/rules")
    @Operation(summary = "Create new escalation rule")
    public ResponseEntity<EscalationRule> createEscalationRule(
            @RequestBody EscalationRule rule) {
        // In real implementation, save rule to database
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "Update escalation rule")
    public ResponseEntity<EscalationRule> updateEscalationRule(
            @PathVariable String ruleId,
            @RequestBody EscalationRule rule) {
        // In real implementation, update rule in database
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete escalation rule")
    public ResponseEntity<Map<String, String>> deleteEscalationRule(@PathVariable String ruleId) {
        // In real implementation, delete rule from database
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}


