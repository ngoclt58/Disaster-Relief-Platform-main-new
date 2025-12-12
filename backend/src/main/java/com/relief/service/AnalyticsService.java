package com.relief.service;

import com.relief.dto.AnalyticsResponse;
import com.relief.dto.TimeSeriesData;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import com.relief.repository.InventoryStockRepository;
import com.relief.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    @Autowired
    private NeedsRequestRepository needsRequestRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InventoryStockRepository inventoryStockRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public AnalyticsResponse getOverview(LocalDateTime startDate, LocalDateTime endDate) {
        long totalNeeds = needsRequestRepository.countByCreatedAtBetween(startDate, endDate);
        long activeNeeds = needsRequestRepository.countByStatusAndCreatedAtBetween("active", startDate, endDate);
        long completedTasks = taskRepository.countByStatusAndUpdatedAtBetween("delivered", startDate, endDate);
        long activeUsers = userRepository.countActiveUsers();
        
        // Calculate average response time (mock calculation)
        double averageResponseTime = calculateAverageResponseTime(startDate, endDate);
        
        Map<String, Long> needsByCategory = needsRequestRepository.countByCategoryAndCreatedAtBetween(startDate, endDate);
        Map<String, Long> tasksByStatus = taskRepository.countByStatusAndUpdatedAtBetween(startDate, endDate);
        Map<String, Long> usersByRole = userRepository.countByRole();
        
        String period = formatPeriod(startDate, endDate);
        
        return new AnalyticsResponse(
            totalNeeds, activeNeeds, completedTasks, activeUsers,
            averageResponseTime, needsByCategory, tasksByStatus, usersByRole, period
        );
    }
    
    public List<TimeSeriesData> getNeedsTrends(LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        List<TimeSeriesData> trends = new ArrayList<>();
        
        // Mock implementation - in real scenario, this would query the database
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            LocalDateTime next = getNextTimePoint(current, granularity);
            
            long count = needsRequestRepository.countByCreatedAtBetween(current, next);
            trends.add(new TimeSeriesData(current, "Needs Created", count, "needs"));
            
            current = next;
        }
        
        return trends;
    }
    
    public Map<String, Object> getTaskPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> performance = new HashMap<>();
        
        long totalTasks = taskRepository.countByCreatedAtBetween(startDate, endDate);
        long completedTasks = taskRepository.countByStatusAndUpdatedAtBetween("delivered", startDate, endDate);
        long inProgressTasks = taskRepository.countByStatusInAndUpdatedAtBetween(
            Arrays.asList("assigned", "picked_up"), startDate, endDate);
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        double averageCompletionTime = calculateAverageCompletionTime(startDate, endDate);
        
        performance.put("totalTasks", totalTasks);
        performance.put("completedTasks", completedTasks);
        performance.put("inProgressTasks", inProgressTasks);
        performance.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        performance.put("averageCompletionTime", averageCompletionTime);
        performance.put("period", formatPeriod(startDate, endDate));
        
        return performance;
    }
    
    public Map<String, Object> getUserActivity(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> activity = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long newUsers = userRepository.countByCreatedAtBetween(startDate, endDate);
        
        // Get user activity from audit logs
        long totalActions = auditLogRepository.countByTimestampBetween(startDate, endDate);
        long uniqueActiveUsers = auditLogRepository.countDistinctUserIdByTimestampBetween(startDate, endDate);
        
        activity.put("totalUsers", totalUsers);
        activity.put("activeUsers", activeUsers);
        activity.put("newUsers", newUsers);
        activity.put("totalActions", totalActions);
        activity.put("uniqueActiveUsers", uniqueActiveUsers);
        activity.put("averageActionsPerUser", uniqueActiveUsers > 0 ? totalActions / uniqueActiveUsers : 0);
        activity.put("period", formatPeriod(startDate, endDate));
        
        return activity;
    }
    
    public Map<String, Object> getInventoryStatus() {
        Map<String, Object> status = new HashMap<>();
        
        long totalItems = inventoryStockRepository.count();
        long lowStockItems = inventoryStockRepository.countByQtyAvailableLessThan(10);
        long outOfStockItems = inventoryStockRepository.countByQtyAvailableLessThan(1);
        
        // Calculate total value (mock calculation)
        double totalValue = calculateTotalInventoryValue();
        
        status.put("totalItems", totalItems);
        status.put("lowStockItems", lowStockItems);
        status.put("outOfStockItems", outOfStockItems);
        status.put("totalValue", totalValue);
        status.put("lowStockPercentage", totalItems > 0 ? (double) lowStockItems / totalItems * 100 : 0);
        status.put("lastUpdated", LocalDateTime.now());
        
        return status;
    }
    
    public Map<String, Object> getResponseTimes(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> responseTimes = new HashMap<>();
        
        double averageResponseTime = calculateAverageResponseTime(startDate, endDate);
        double medianResponseTime = calculateMedianResponseTime(startDate, endDate);
        double p95ResponseTime = calculateP95ResponseTime(startDate, endDate);
        
        responseTimes.put("average", averageResponseTime);
        responseTimes.put("median", medianResponseTime);
        responseTimes.put("p95", p95ResponseTime);
        responseTimes.put("period", formatPeriod(startDate, endDate));
        
        return responseTimes;
    }
    
    public List<Map<String, Object>> getGeographicDistribution(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        // Mock implementation - in real scenario, this would query geographic data
        Map<String, Long> regionCounts = needsRequestRepository.countByRegionAndCreatedAtBetween(startDate, endDate);
        
        for (Map.Entry<String, Long> entry : regionCounts.entrySet()) {
            Map<String, Object> region = new HashMap<>();
            region.put("region", entry.getKey());
            region.put("count", entry.getValue());
            region.put("percentage", calculatePercentage(entry.getValue(), regionCounts.values().stream().mapToLong(Long::longValue).sum()));
            distribution.add(region);
        }
        
        return distribution;
    }
    
    public Map<String, Object> getSeverityBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> breakdown = new HashMap<>();
        
        Map<Integer, Long> severityCounts = needsRequestRepository.countBySeverityAndCreatedAtBetween(startDate, endDate);
        
        breakdown.put("severity1", severityCounts.getOrDefault(1, 0L));
        breakdown.put("severity2", severityCounts.getOrDefault(2, 0L));
        breakdown.put("severity3", severityCounts.getOrDefault(3, 0L));
        breakdown.put("severity4", severityCounts.getOrDefault(4, 0L));
        breakdown.put("severity5", severityCounts.getOrDefault(5, 0L));
        breakdown.put("total", severityCounts.values().stream().mapToLong(Long::longValue).sum());
        breakdown.put("period", formatPeriod(startDate, endDate));
        
        return breakdown;
    }
    
    public byte[] exportAnalytics(String format, LocalDateTime startDate, LocalDateTime endDate) {
        // Mock implementation - in real scenario, this would generate actual export files
        String data = "Analytics data for period: " + formatPeriod(startDate, endDate);
        return data.getBytes();
    }
    
    // Helper methods
    private LocalDateTime getNextTimePoint(LocalDateTime current, String granularity) {
        switch (granularity.toLowerCase()) {
            case "hour":
                return current.plusHours(1);
            case "day":
                return current.plusDays(1);
            case "week":
                return current.plusWeeks(1);
            case "month":
                return current.plusMonths(1);
            default:
                return current.plusDays(1);
        }
    }
    
    private String formatPeriod(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return start.format(formatter) + " to " + end.format(formatter);
    }
    
    private double calculateAverageResponseTime(LocalDateTime start, LocalDateTime end) {
        // Mock calculation - in real scenario, this would calculate actual response times
        return 2.5; // hours
    }
    
    private double calculateMedianResponseTime(LocalDateTime start, LocalDateTime end) {
        // Mock calculation
        return 1.8; // hours
    }
    
    private double calculateP95ResponseTime(LocalDateTime start, LocalDateTime end) {
        // Mock calculation
        return 6.2; // hours
    }
    
    private double calculateAverageCompletionTime(LocalDateTime start, LocalDateTime end) {
        // Mock calculation
        return 4.2; // hours
    }
    
    private double calculateTotalInventoryValue() {
        // Mock calculation
        return 125000.0; // dollars
    }
    
    private double calculatePercentage(long value, long total) {
        return total > 0 ? (double) value / total * 100 : 0;
    }
}



