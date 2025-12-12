package com.relief.service.workflow;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to convert between WorkflowTemplate and Map (for JSONB storage)
 */
@Component
public class WorkflowTemplateConverter {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateConverter.class);

    /**
     * Convert Map (from JSONB) to WorkflowTemplate
     */
    public WorkflowTemplate toWorkflowTemplate(Map<String, Object> templateData) {
        try {
            WorkflowTemplate template = new WorkflowTemplate();
            
            // Extract basic fields
            Object nameObj = templateData.get("name");
            if (nameObj != null) {
                template.setName(nameObj.toString());
            }
            
            Object descObj = templateData.get("description");
            if (descObj != null) {
                template.setDescription(descObj.toString());
            }
            
            // Convert steps
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepsData = (List<Map<String, Object>>) templateData.get("steps");
            if (stepsData != null) {
                List<WorkflowStep> steps = new ArrayList<>();
                for (Map<String, Object> stepData : stepsData) {
                    WorkflowStep step = convertStep(stepData);
                    if (step != null) {
                        steps.add(step);
                    }
                }
                template.setSteps(steps);
            }
            
            return template;
        } catch (Exception e) {
            log.error("Failed to convert template data to WorkflowTemplate", e);
            return null;
        }
    }

    /**
     * Convert WorkflowTemplate to Map (for JSONB storage)
     */
    public Map<String, Object> toTemplateData(WorkflowTemplate template) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", template.getName());
        templateData.put("description", template.getDescription());
        
        if (template.getSteps() != null) {
            List<Map<String, Object>> stepsData = new ArrayList<>();
            for (WorkflowStep step : template.getSteps()) {
                Map<String, Object> stepData = convertStepToMap(step);
                if (stepData != null) {
                    stepsData.add(stepData);
                }
            }
            templateData.put("steps", stepsData);
        }
        
        return templateData;
    }

    /**
     * Convert a single step from Map to WorkflowStep
     */
    @SuppressWarnings("unchecked")
    private WorkflowStep convertStep(Map<String, Object> stepData) {
        try {
            WorkflowStep step = new WorkflowStep();
            
            Object nameObj = stepData.get("name");
            if (nameObj != null) {
                step.setName(nameObj.toString());
            }
            
            Object typeObj = stepData.get("type");
            if (typeObj != null) {
                try {
                    step.setType(WorkflowStepType.valueOf(typeObj.toString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown workflow step type: {}", typeObj);
                }
            }
            
            Object requiredObj = stepData.get("required");
            if (requiredObj != null) {
                step.setRequired(Boolean.parseBoolean(requiredObj.toString()));
            }
            
            Object paramsObj = stepData.get("parameters");
            if (paramsObj instanceof Map) {
                step.setParameters((Map<String, Object>) paramsObj);
            }
            
            Object conditionObj = stepData.get("condition");
            if (conditionObj instanceof Map) {
                step.setCondition(convertCondition((Map<String, Object>) conditionObj));
            }
            
            Object trueStepObj = stepData.get("trueStep");
            if (trueStepObj instanceof Map) {
                step.setTrueStep(convertStep((Map<String, Object>) trueStepObj));
            }
            
            Object parallelStepsObj = stepData.get("parallelSteps");
            if (parallelStepsObj instanceof List) {
                List<WorkflowStep> parallelSteps = new ArrayList<>();
                for (Object parallelStepObj : (List<?>) parallelStepsObj) {
                    if (parallelStepObj instanceof Map) {
                        WorkflowStep parallelStep = convertStep((Map<String, Object>) parallelStepObj);
                        if (parallelStep != null) {
                            parallelSteps.add(parallelStep);
                        }
                    }
                }
                step.setParallelSteps(parallelSteps);
            }
            
            return step;
        } catch (Exception e) {
            log.error("Failed to convert step data", e);
            return null;
        }
    }

    /**
     * Convert a single step from WorkflowStep to Map
     */
    private Map<String, Object> convertStepToMap(WorkflowStep step) {
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("name", step.getName());
        stepData.put("type", step.getType() != null ? step.getType().name() : null);
        stepData.put("required", step.isRequired());
        stepData.put("parameters", step.getParameters());
        
        if (step.getCondition() != null) {
            stepData.put("condition", convertConditionToMap(step.getCondition()));
        }
        
        if (step.getTrueStep() != null) {
            stepData.put("trueStep", convertStepToMap(step.getTrueStep()));
        }
        
        if (step.getParallelSteps() != null) {
            List<Map<String, Object>> parallelStepsData = new ArrayList<>();
            for (WorkflowStep parallelStep : step.getParallelSteps()) {
                parallelStepsData.add(convertStepToMap(parallelStep));
            }
            stepData.put("parallelSteps", parallelStepsData);
        }
        
        return stepData;
    }

    /**
     * Convert condition from Map to WorkflowCondition
     */
    private WorkflowCondition convertCondition(Map<String, Object> conditionData) {
        WorkflowCondition condition = new WorkflowCondition();
        Object variableObj = conditionData.get("variable");
        if (variableObj != null) {
            condition.setVariable(variableObj.toString());
        }
        
        Object operatorObj = conditionData.get("operator");
        if (operatorObj != null) {
            condition.setOperator(operatorObj.toString());
        }
        
        condition.setExpectedValue(conditionData.get("expectedValue"));
        return condition;
    }

    /**
     * Convert condition from WorkflowCondition to Map
     */
    private Map<String, Object> convertConditionToMap(WorkflowCondition condition) {
        Map<String, Object> conditionData = new HashMap<>();
        conditionData.put("variable", condition.getVariable());
        conditionData.put("operator", condition.getOperator());
        conditionData.put("expectedValue", condition.getExpectedValue());
        return conditionData;
    }
}

