package com.relief.service.workflow;

import java.util.List;
import java.util.Map;

/**
 * Individual workflow step definition
 */
public class WorkflowStep {
    private String name;
    private WorkflowStepType type;
    private boolean required;
    private Map<String, Object> parameters;
    private WorkflowCondition condition;
    private WorkflowStep trueStep;
    private WorkflowStep falseStep;
    private List<WorkflowStep> parallelSteps;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WorkflowStepType getType() { return type; }
    public void setType(WorkflowStepType type) { this.type = type; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public WorkflowCondition getCondition() { return condition; }
    public void setCondition(WorkflowCondition condition) { this.condition = condition; }

    public WorkflowStep getTrueStep() { return trueStep; }
    public void setTrueStep(WorkflowStep trueStep) { this.trueStep = trueStep; }

    public WorkflowStep getFalseStep() { return falseStep; }
    public void setFalseStep(WorkflowStep falseStep) { this.falseStep = falseStep; }

    public List<WorkflowStep> getParallelSteps() { return parallelSteps; }
    public void setParallelSteps(List<WorkflowStep> parallelSteps) { this.parallelSteps = parallelSteps; }
}


