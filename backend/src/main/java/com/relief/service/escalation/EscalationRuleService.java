package com.relief.service.escalation;

import com.relief.entity.NeedsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing escalation rules
 */
@Service
@Slf4j
public class EscalationRuleService {

    private final Map<String, EscalationRule> rules = new HashMap<>();

    public EscalationRuleService() {
        initializeDefaultRules();
    }

    /**
     * Get escalation rule for a request
     */
    public EscalationRule getRuleForRequest(NeedsRequest request) {
        // Find matching rule based on request characteristics
        for (EscalationRule rule : rules.values()) {
            if (matchesRule(request, rule)) {
                return rule;
            }
        }
        
        // Return default rule if no specific match
        return rules.get("DEFAULT");
    }

    /**
     * Check if request matches rule criteria
     */
    private boolean matchesRule(NeedsRequest request, EscalationRule rule) {
        // Check request type
        if (rule.getRequestTypes() != null && !rule.getRequestTypes().contains(request.getType())) {
            return false;
        }
        
        // Check severity threshold
        if (request.getSeverity() < rule.getSeverityThreshold()) {
            return false;
        }
        
        // Check status
        if (rule.getStatuses() != null && !rule.getStatuses().contains(request.getStatus())) {
            return false;
        }
        
        return true;
    }

    /**
     * Initialize default escalation rules
     */
    private void initializeDefaultRules() {
        // Medical Emergency Rule
        EscalationRule medicalRule = new EscalationRule();
        medicalRule.setId("MEDICAL_EMERGENCY");
        medicalRule.setName("Medical Emergency Escalation");
        medicalRule.setRequestTypes(Set.of("Medical Emergency"));
        medicalRule.setSeverityThreshold(4);
        medicalRule.setEscalationTimeMinutes(15); // 15 minutes
        medicalRule.setTaskEscalationTimeMinutes(10); // 10 minutes
        medicalRule.setEscalationLevel(1);
        medicalRule.setEscalationReason("Medical emergency requires immediate response");
        rules.put("MEDICAL_EMERGENCY", medicalRule);

        // High Priority Rule
        EscalationRule highPriorityRule = new EscalationRule();
        highPriorityRule.setId("HIGH_PRIORITY");
        highPriorityRule.setName("High Priority Escalation");
        highPriorityRule.setSeverityThreshold(4);
        highPriorityRule.setEscalationTimeMinutes(30); // 30 minutes
        highPriorityRule.setTaskEscalationTimeMinutes(20); // 20 minutes
        highPriorityRule.setEscalationLevel(1);
        highPriorityRule.setEscalationReason("High priority request requires prompt attention");
        rules.put("HIGH_PRIORITY", highPriorityRule);

        // Evacuation Rule
        EscalationRule evacuationRule = new EscalationRule();
        evacuationRule.setId("EVACUATION");
        evacuationRule.setName("Evacuation Escalation");
        evacuationRule.setRequestTypes(Set.of("Evacuation"));
        evacuationRule.setSeverityThreshold(3);
        evacuationRule.setEscalationTimeMinutes(45); // 45 minutes
        evacuationRule.setTaskEscalationTimeMinutes(30); // 30 minutes
        evacuationRule.setEscalationLevel(2);
        evacuationRule.setEscalationReason("Evacuation requires coordinated response");
        rules.put("EVACUATION", evacuationRule);

        // Medium Priority Rule
        EscalationRule mediumPriorityRule = new EscalationRule();
        mediumPriorityRule.setId("MEDIUM_PRIORITY");
        mediumPriorityRule.setName("Medium Priority Escalation");
        mediumPriorityRule.setSeverityThreshold(3);
        mediumPriorityRule.setEscalationTimeMinutes(120); // 2 hours
        mediumPriorityRule.setTaskEscalationTimeMinutes(60); // 1 hour
        mediumPriorityRule.setEscalationLevel(1);
        mediumPriorityRule.setEscalationReason("Medium priority request needs attention");
        rules.put("MEDIUM_PRIORITY", mediumPriorityRule);

        // Default Rule
        EscalationRule defaultRule = new EscalationRule();
        defaultRule.setId("DEFAULT");
        defaultRule.setName("Default Escalation");
        defaultRule.setSeverityThreshold(2);
        defaultRule.setEscalationTimeMinutes(240); // 4 hours
        defaultRule.setTaskEscalationTimeMinutes(120); // 2 hours
        defaultRule.setEscalationLevel(1);
        defaultRule.setEscalationReason("Request has been pending for too long");
        rules.put("DEFAULT", defaultRule);

        // Critical Overdue Rule
        EscalationRule criticalOverdueRule = new EscalationRule();
        criticalOverdueRule.setId("CRITICAL_OVERDUE");
        criticalOverdueRule.setName("Critical Overdue Escalation");
        criticalOverdueRule.setStatuses(Set.of("ESCALATED"));
        criticalOverdueRule.setEscalationTimeMinutes(60); // 1 hour after escalation
        criticalOverdueRule.setTaskEscalationTimeMinutes(30); // 30 minutes
        criticalOverdueRule.setEscalationLevel(3);
        criticalOverdueRule.setEscalationReason("Escalated request still unresolved - critical intervention required");
        rules.put("CRITICAL_OVERDUE", criticalOverdueRule);
    }

    /**
     * Escalation rule data class
     */
    public static class EscalationRule {
        private String id;
        private String name;
        private Set<String> requestTypes;
        private Set<String> statuses;
        private int severityThreshold;
        private int escalationTimeMinutes;
        private int taskEscalationTimeMinutes;
        private int escalationLevel;
        private String escalationReason;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Set<String> getRequestTypes() { return requestTypes; }
        public void setRequestTypes(Set<String> requestTypes) { this.requestTypes = requestTypes; }

        public Set<String> getStatuses() { return statuses; }
        public void setStatuses(Set<String> statuses) { this.statuses = statuses; }

        public int getSeverityThreshold() { return severityThreshold; }
        public void setSeverityThreshold(int severityThreshold) { this.severityThreshold = severityThreshold; }

        public int getEscalationTimeMinutes() { return escalationTimeMinutes; }
        public void setEscalationTimeMinutes(int escalationTimeMinutes) { this.escalationTimeMinutes = escalationTimeMinutes; }

        public int getTaskEscalationTimeMinutes() { return taskEscalationTimeMinutes; }
        public void setTaskEscalationTimeMinutes(int taskEscalationTimeMinutes) { this.taskEscalationTimeMinutes = taskEscalationTimeMinutes; }

        public int getEscalationLevel() { return escalationLevel; }
        public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }

        public String getEscalationReason() { return escalationReason; }
        public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
    }
}


