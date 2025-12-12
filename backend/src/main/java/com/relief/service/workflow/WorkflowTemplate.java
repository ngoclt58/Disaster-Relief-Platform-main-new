package com.relief.service.workflow;

import java.util.List;

/**
 * Workflow template definition
 */
public class WorkflowTemplate {
    private String name;
    private String description;
    private List<WorkflowStep> steps;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<WorkflowStep> getSteps() { return steps; }
    public void setSteps(List<WorkflowStep> steps) { this.steps = steps; }
}


