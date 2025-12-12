package com.relief.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalNeeds;
    private long activeNeeds;
    private long completedTasks;
    private long pendingTasks;
    private long totalInventoryItems;
    private long lowStockItems;
    private Map<String, Long> usersByRole;
    private Map<String, Long> needsByCategory;
    private Map<String, Long> tasksByStatus;
    private LocalDateTime lastUpdated;
    
    // Constructors
    public AdminStatsResponse() {}
    
    public AdminStatsResponse(long totalUsers, long activeUsers, long totalNeeds, long activeNeeds,
                            long completedTasks, long pendingTasks, long totalInventoryItems,
                            long lowStockItems, Map<String, Long> usersByRole,
                            Map<String, Long> needsByCategory, Map<String, Long> tasksByStatus) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalNeeds = totalNeeds;
        this.activeNeeds = activeNeeds;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.totalInventoryItems = totalInventoryItems;
        this.lowStockItems = lowStockItems;
        this.usersByRole = usersByRole;
        this.needsByCategory = needsByCategory;
        this.tasksByStatus = tasksByStatus;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
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
    
    public long getPendingTasks() {
        return pendingTasks;
    }
    
    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }
    
    public long getTotalInventoryItems() {
        return totalInventoryItems;
    }
    
    public void setTotalInventoryItems(long totalInventoryItems) {
        this.totalInventoryItems = totalInventoryItems;
    }
    
    public long getLowStockItems() {
        return lowStockItems;
    }
    
    public void setLowStockItems(long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
    
    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }
    
    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
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
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}



