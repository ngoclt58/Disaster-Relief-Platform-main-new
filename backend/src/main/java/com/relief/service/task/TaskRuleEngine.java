package com.relief.service.task;

import com.relief.entity.NeedsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for managing task creation rules and patterns
 */
@Service
@Slf4j
public class TaskRuleEngine {

    private final Map<String, TaskCreationRule> rules = new HashMap<>();

    public TaskRuleEngine() {
        initializeDefaultRules();
    }

    /**
     * Get applicable rules for a request
     */
    public List<TaskCreationRule> getApplicableRules(NeedsRequest request) {
        return rules.values().stream()
            .filter(rule -> isRuleApplicable(rule, request))
            .sorted(Comparator.comparing(TaskCreationRule::getPriority).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Check if a rule is applicable to a request
     */
    private boolean isRuleApplicable(TaskCreationRule rule, NeedsRequest request) {
        // Check request type
        if (rule.getRequestTypes() != null && !rule.getRequestTypes().contains(request.getType())) {
            return false;
        }
        
        // Check severity threshold
        if (request.getSeverity() < rule.getMinSeverity()) {
            return false;
        }
        
        // Check status
        if (rule.getRequestStatuses() != null && !rule.getRequestStatuses().contains(request.getStatus())) {
            return false;
        }
        
        // Check location requirements
        if (rule.isLocationRequired() && request.getGeomPoint() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * Initialize default task creation rules
     */
    private void initializeDefaultRules() {
        // Medical Emergency Rule
        TaskCreationRule medicalRule = new TaskCreationRule();
        medicalRule.setId("MEDICAL_EMERGENCY");
        medicalRule.setName("Medical Emergency Response");
        medicalRule.setDescription("Create medical response tasks for emergency requests");
        medicalRule.setRequestTypes(Set.of("Medical Emergency"));
        medicalRule.setMinSeverity(3);
        medicalRule.setPriority(1);
        medicalRule.setLocationRequired(true);
        medicalRule.setTaskTemplates(Arrays.asList(
            createMedicalResponseTemplate(),
            createAmbulanceCallTemplate(),
            createFirstAidTemplate()
        ));
        rules.put("MEDICAL_EMERGENCY", medicalRule);

        // Food Request Rule
        TaskCreationRule foodRule = new TaskCreationRule();
        foodRule.setId("FOOD_REQUEST");
        foodRule.setName("Food Assistance");
        foodRule.setDescription("Create food delivery tasks for food requests");
        foodRule.setRequestTypes(Set.of("Food Request"));
        foodRule.setMinSeverity(1);
        foodRule.setPriority(2);
        foodRule.setLocationRequired(true);
        foodRule.setTaskTemplates(Arrays.asList(
            createFoodDeliveryTemplate(),
            createGroceryPickupTemplate()
        ));
        rules.put("FOOD_REQUEST", foodRule);

        // Water Request Rule
        TaskCreationRule waterRule = new TaskCreationRule();
        waterRule.setId("WATER_REQUEST");
        waterRule.setName("Water Supply");
        waterRule.setDescription("Create water delivery tasks for water requests");
        waterRule.setRequestTypes(Set.of("Water Request"));
        waterRule.setMinSeverity(1);
        waterRule.setPriority(2);
        waterRule.setLocationRequired(true);
        waterRule.setTaskTemplates(Arrays.asList(
            createWaterDeliveryTemplate(),
            createWaterPurificationTemplate()
        ));
        rules.put("WATER_REQUEST", waterRule);

        // Evacuation Rule
        TaskCreationRule evacuationRule = new TaskCreationRule();
        evacuationRule.setId("EVACUATION");
        evacuationRule.setName("Evacuation Coordination");
        evacuationRule.setDescription("Create evacuation coordination tasks");
        evacuationRule.setRequestTypes(Set.of("Evacuation"));
        evacuationRule.setMinSeverity(2);
        evacuationRule.setPriority(1);
        evacuationRule.setLocationRequired(true);
        evacuationRule.setTaskTemplates(Arrays.asList(
            createEvacuationCoordinationTemplate(),
            createTransportationTemplate(),
            createSafetyAssessmentTemplate()
        ));
        rules.put("EVACUATION", evacuationRule);

        // High Priority Rule
        TaskCreationRule highPriorityRule = new TaskCreationRule();
        highPriorityRule.setId("HIGH_PRIORITY");
        highPriorityRule.setName("High Priority Response");
        highPriorityRule.setDescription("Create immediate response tasks for high priority requests");
        highPriorityRule.setMinSeverity(4);
        highPriorityRule.setPriority(1);
        highPriorityRule.setLocationRequired(false);
        highPriorityRule.setTaskTemplates(Arrays.asList(
            createImmediateResponseTemplate(),
            createEscalationTemplate()
        ));
        rules.put("HIGH_PRIORITY", highPriorityRule);
    }

    /**
     * Create medical response task template
     */
    private TaskTemplate createMedicalResponseTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("MEDICAL_RESPONSE");
        template.setTitle("Medical Emergency Response");
        template.setDescription("Respond to medical emergency at {{location}}. Request details: {{requestDescription}}");
        template.setPriority("HIGH");
        template.setEstimatedDurationMinutes(30);
        template.setRequiredSkills(Arrays.asList("FIRST_AID", "EMERGENCY_RESPONSE", "MEDICAL_KNOWLEDGE"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create ambulance call task template
     */
    private TaskTemplate createAmbulanceCallTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("AMBULANCE_CALL");
        template.setTitle("Call Emergency Services");
        template.setDescription("Call ambulance for medical emergency at {{location}}");
        template.setPriority("CRITICAL");
        template.setEstimatedDurationMinutes(5);
        template.setRequiredSkills(Arrays.asList("EMERGENCY_RESPONSE", "COMMUNICATION"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create first aid task template
     */
    private TaskTemplate createFirstAidTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("FIRST_AID");
        template.setTitle("Provide First Aid");
        template.setDescription("Provide immediate first aid assistance at {{location}}");
        template.setPriority("HIGH");
        template.setEstimatedDurationMinutes(15);
        template.setRequiredSkills(Arrays.asList("FIRST_AID", "MEDICAL_KNOWLEDGE"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList("AMBULANCE_CALL"));
        return template;
    }

    /**
     * Create food delivery task template
     */
    private TaskTemplate createFoodDeliveryTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("FOOD_DELIVERY");
        template.setTitle("Food Delivery");
        template.setDescription("Deliver food supplies to {{location}}. Request: {{requestDescription}}");
        template.setPriority("MEDIUM");
        template.setEstimatedDurationMinutes(60);
        template.setRequiredSkills(Arrays.asList("DRIVING", "DELIVERY", "CUSTOMER_SERVICE"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create grocery pickup task template
     */
    private TaskTemplate createGroceryPickupTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("GROCERY_PICKUP");
        template.setTitle("Grocery Pickup");
        template.setDescription("Pick up groceries and deliver to {{location}}");
        template.setPriority("MEDIUM");
        template.setEstimatedDurationMinutes(90);
        template.setRequiredSkills(Arrays.asList("DRIVING", "SHOPPING", "CUSTOMER_SERVICE"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create water delivery task template
     */
    private TaskTemplate createWaterDeliveryTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("WATER_DELIVERY");
        template.setTitle("Water Delivery");
        template.setDescription("Deliver clean water to {{location}}");
        template.setPriority("MEDIUM");
        template.setEstimatedDurationMinutes(45);
        template.setRequiredSkills(Arrays.asList("DRIVING", "DELIVERY", "HEAVY_LIFTING"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create water purification task template
     */
    private TaskTemplate createWaterPurificationTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("WATER_PURIFICATION");
        template.setTitle("Water Purification");
        template.setDescription("Set up water purification system at {{location}}");
        template.setPriority("MEDIUM");
        template.setEstimatedDurationMinutes(120);
        template.setRequiredSkills(Arrays.asList("TECHNICAL_SKILLS", "WATER_TREATMENT"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create evacuation coordination task template
     */
    private TaskTemplate createEvacuationCoordinationTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("EVACUATION_COORDINATION");
        template.setTitle("Evacuation Coordination");
        template.setDescription("Coordinate evacuation from {{location}}");
        template.setPriority("CRITICAL");
        template.setEstimatedDurationMinutes(180);
        template.setRequiredSkills(Arrays.asList("LEADERSHIP", "EMERGENCY_RESPONSE", "COMMUNICATION"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create transportation task template
     */
    private TaskTemplate createTransportationTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("TRANSPORTATION");
        template.setTitle("Transportation Assistance");
        template.setDescription("Provide transportation for evacuation from {{location}}");
        template.setPriority("HIGH");
        template.setEstimatedDurationMinutes(120);
        template.setRequiredSkills(Arrays.asList("DRIVING", "VEHICLE_OPERATION", "SAFETY"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList("EVACUATION_COORDINATION"));
        return template;
    }

    /**
     * Create safety assessment task template
     */
    private TaskTemplate createSafetyAssessmentTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("SAFETY_ASSESSMENT");
        template.setTitle("Safety Assessment");
        template.setDescription("Assess safety conditions at {{location}}");
        template.setPriority("HIGH");
        template.setEstimatedDurationMinutes(60);
        template.setRequiredSkills(Arrays.asList("SAFETY_INSPECTION", "RISK_ASSESSMENT"));
        template.setLocationRequired(true);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create immediate response task template
     */
    private TaskTemplate createImmediateResponseTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("IMMEDIATE_RESPONSE");
        template.setTitle("Immediate Response");
        template.setDescription("Provide immediate response to high priority request: {{requestDescription}}");
        template.setPriority("CRITICAL");
        template.setEstimatedDurationMinutes(15);
        template.setRequiredSkills(Arrays.asList("EMERGENCY_RESPONSE", "QUICK_THINKING"));
        template.setLocationRequired(false);
        template.setDependencies(Arrays.asList());
        return template;
    }

    /**
     * Create escalation task template
     */
    private TaskTemplate createEscalationTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setType("ESCALATION");
        template.setTitle("Escalate to Supervisor");
        template.setDescription("Escalate high priority request to supervisor for immediate attention");
        template.setPriority("CRITICAL");
        template.setEstimatedDurationMinutes(5);
        template.setRequiredSkills(Arrays.asList("COMMUNICATION", "LEADERSHIP"));
        template.setLocationRequired(false);
        template.setDependencies(Arrays.asList());
        return template;
    }
}


