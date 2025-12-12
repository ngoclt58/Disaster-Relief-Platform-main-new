package com.relief.service.task;

import java.util.List;

/**
 * Template for creating tasks
 */
public class TaskTemplate {
    private String type;
    private String title;
    private String description;
    private String priority;
    private Integer estimatedDurationMinutes;
    private List<String> requiredSkills;
    private boolean locationRequired;
    private List<String> dependencies;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public boolean isLocationRequired() { return locationRequired; }
    public void setLocationRequired(boolean locationRequired) { this.locationRequired = locationRequired; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
}


