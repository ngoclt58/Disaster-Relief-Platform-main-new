package com.relief.service.task;

import java.util.List;
import java.util.Set;

/**
 * Rule for creating tasks based on request characteristics
 */
public class TaskCreationRule {
    private String id;
    private String name;
    private String description;
    private Set<String> requestTypes;
    private int minSeverity;
    private Set<String> requestStatuses;
    private int priority;
    private boolean locationRequired;
    private List<TaskTemplate> taskTemplates;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<String> getRequestTypes() { return requestTypes; }
    public void setRequestTypes(Set<String> requestTypes) { this.requestTypes = requestTypes; }

    public int getMinSeverity() { return minSeverity; }
    public void setMinSeverity(int minSeverity) { this.minSeverity = minSeverity; }

    public Set<String> getRequestStatuses() { return requestStatuses; }
    public void setRequestStatuses(Set<String> requestStatuses) { this.requestStatuses = requestStatuses; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isLocationRequired() { return locationRequired; }
    public void setLocationRequired(boolean locationRequired) { this.locationRequired = locationRequired; }

    public List<TaskTemplate> getTaskTemplates() { return taskTemplates; }
    public void setTaskTemplates(List<TaskTemplate> taskTemplates) { this.taskTemplates = taskTemplates; }
}


