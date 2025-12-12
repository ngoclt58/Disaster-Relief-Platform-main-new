package com.relief.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class AnalyticsResponse {
    private long totalNeeds;
    private long activeNeeds;
    private long completedTasks;
    private long activeUsers;
    private double averageResponseTime;
    private Map<String, Long> needsByCategory;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> usersByRole;
    private LocalDateTime lastUpdated;
    private String period;
    
    // Constructors
    public AnalyticsResponse() {}
    
    public AnalyticsResponse(long totalNeeds, long activeNeeds, long completedTasks, 
                           long activeUsers, double averageResponseTime,
                           Map<String, Long> needsByCategory, Map<String, Long> tasksByStatus,
                           Map<String, Long> usersByRole, String period) {
        this.totalNeeds = totalNeeds;
        this.activeNeeds = activeNeeds;
        this.completedTasks = completedTasks;
        this.activeUsers = activeUsers;
        this.averageResponseTime = averageResponseTime;
        this.needsByCategory = needsByCategory;
        this.tasksByStatus = tasksByStatus;
        this.usersByRole = usersByRole;
        this.period = period;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public long getTotalNeeds() {
        return totalNeeds;
    }
    
    public void setTotalNeeds(long totalNeeds) {
        this.totalNeeds = totalNeeds;
    }
    
    public long getActiveNeeds() {
        return activeNeeds;
    }
    
    public void setActiveNeeds(long activeNeeds) {
        this.activeNeeds = activeNeeds;
    }
    
    public long getCompletedTasks() {
        return completedTasks;
    }
    
    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public double getAverageResponseTime() {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
    
    public Map<String, Long> getNeedsByCategory() {
        return needsByCategory;
    }
    
    public void setNeedsByCategory(Map<String, Long> needsByCategory) {
        this.needsByCategory = needsByCategory;
    }
    
    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }
    
    public void setTasksByStatus(Map<String, Long> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }
    
    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }
    
    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
}



