package com.relief.service.task;

import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for task performance analytics and efficiency tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskPerformanceAnalyticsService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Get comprehensive task performance analytics
     */
    public TaskPerformanceAnalytics getTaskPerformanceAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating task performance analytics from {} to {}", startDate, endDate);
        
        TaskPerformanceAnalytics analytics = new TaskPerformanceAnalytics();
        analytics.setStartDate(startDate);
        analytics.setEndDate(endDate);
        
        // Get all tasks in the date range
        List<Task> tasks = getTasksInDateRange(startDate, endDate);
        analytics.setTotalTasks(tasks.size());
        
        // Calculate completion metrics
        CompletionMetrics completionMetrics = calculateCompletionMetrics(tasks);
        analytics.setCompletionMetrics(completionMetrics);
        
        // Calculate efficiency metrics
        EfficiencyMetrics efficiencyMetrics = calculateEfficiencyMetrics(tasks);
        analytics.setEfficiencyMetrics(efficiencyMetrics);
        
        // Calculate helper performance
        List<HelperPerformance> helperPerformance = calculateHelperPerformance(tasks);
        analytics.setHelperPerformance(helperPerformance);
        
        // Calculate task type performance
        List<TaskTypePerformance> taskTypePerformance = calculateTaskTypePerformance(tasks);
        analytics.setTaskTypePerformance(taskTypePerformance);
        
        // Calculate trends
        List<PerformanceTrend> trends = calculatePerformanceTrends(startDate, endDate);
        analytics.setTrends(trends);
        
        return analytics;
    }

    /**
     * Get tasks in date range using a more efficient query
     */
    private List<Task> getTasksInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Fetching tasks between {} and {}", startDate, endDate);
            // Use a custom query to fetch only the tasks in the date range
            return taskRepository.findByCreatedAtBetween(startDate, endDate);
        } catch (Exception e) {
            log.error("Error fetching tasks in date range", e);
            return Collections.emptyList();
        }
    }

    /**
     * Calculate completion metrics
     */
    private CompletionMetrics calculateCompletionMetrics(List<Task> tasks) {
        CompletionMetrics metrics = new CompletionMetrics();
        
        try {
            int totalTasks = tasks.size();
            if (totalTasks == 0) {
                return metrics; // Return empty metrics for no tasks
            }
            
            // Use parallel stream for better performance with large datasets
            Map<String, Long> statusCounts = tasks.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                    Task::getStatus,
                    Collectors.counting()
                ));
            
            // Define status categories
            long completedTasks = sumStatuses(statusCounts, "delivered", "completed", "resolved");
            long cancelledTasks = sumStatuses(statusCounts, "cancelled", "failed");
            long inProgressTasks = sumStatuses(statusCounts, "assigned", "picked_up");
            
            metrics.setTotalTasks(totalTasks);
            metrics.setCompletedTasks((int) completedTasks);
            metrics.setCancelledTasks((int) cancelledTasks);
            metrics.setInProgressTasks((int) inProgressTasks);
            
            // Calculate rates
            metrics.setCompletionRate((double) completedTasks / totalTasks * 100);
            metrics.setCancellationRate((double) cancelledTasks / totalTasks * 100);
            
        } catch (Exception e) {
            log.error("Error calculating completion metrics", e);
        }
        
        return metrics;
    }
    
    /**
     * Helper method to sum counts of multiple statuses
     */
    private long sumStatuses(Map<String, Long> statusCounts, String... statuses) {
        return Arrays.stream(statuses)
            .mapToLong(status -> statusCounts.getOrDefault(status, 0L))
            .sum();
    }

    /**
     * Calculate efficiency metrics
     */
    private EfficiencyMetrics calculateEfficiencyMetrics(List<Task> tasks) {
        EfficiencyMetrics metrics = new EfficiencyMetrics();
        
        List<Task> completedTasks = tasks.stream()
            .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
            .collect(Collectors.toList());
        
        if (completedTasks.isEmpty()) {
            return metrics;
        }
        
        // Calculate average completion time
        double avgCompletionTime = completedTasks.stream()
            .mapToDouble(this::getTaskCompletionTime)
            .average()
            .orElse(0.0);
        metrics.setAverageCompletionTimeMinutes(avgCompletionTime);
        
        // Calculate on-time delivery rate
        long onTimeDeliveries = completedTasks.stream()
            .filter(this::isTaskDeliveredOnTime)
            .count();
        metrics.setOnTimeDeliveryRate((double) onTimeDeliveries / completedTasks.size() * 100);
        
        // Calculate average delay
        double avgDelay = completedTasks.stream()
            .mapToDouble(this::getTaskDelay)
            .filter(delay -> delay > 0)
            .average()
            .orElse(0.0);
        metrics.setAverageDelayMinutes(avgDelay);
        
        // Calculate throughput (tasks per hour)
        if (!completedTasks.isEmpty()) {
            LocalDateTime firstTask = completedTasks.stream()
                .map(Task::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
            LocalDateTime lastTask = completedTasks.stream()
                .map(Task::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
            
            long totalHours = java.time.Duration.between(firstTask, lastTask).toHours();
            if (totalHours > 0) {
                metrics.setThroughputTasksPerHour((double) completedTasks.size() / totalHours);
            }
        }
        
        return metrics;
    }

    /**
     * Calculate helper performance
     */
    private List<HelperPerformance> calculateHelperPerformance(List<Task> tasks) {
        Map<User, List<Task>> tasksByHelper = tasks.stream()
            .filter(t -> t.getAssignee() != null)
            .collect(Collectors.groupingBy(Task::getAssignee));
        
        return tasksByHelper.entrySet().stream()
            .map(entry -> {
                User helper = entry.getKey();
                List<Task> helperTasks = entry.getValue();
                
                HelperPerformance performance = new HelperPerformance();
                performance.setHelper(helper);
                performance.setTotalTasks(helperTasks.size());
                
                int completedTasks = (int) helperTasks.stream()
                    .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
                    .count();
                performance.setCompletedTasks(completedTasks);
                
                if (helperTasks.size() > 0) {
                    performance.setCompletionRate((double) completedTasks / helperTasks.size() * 100);
                }
                
                // Calculate average completion time
                List<Task> completedHelperTasks = helperTasks.stream()
                    .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
                    .collect(Collectors.toList());
                
                if (!completedHelperTasks.isEmpty()) {
                    double avgCompletionTime = completedHelperTasks.stream()
                        .mapToDouble(this::getTaskCompletionTime)
                        .average()
                        .orElse(0.0);
                    performance.setAverageCompletionTimeMinutes(avgCompletionTime);
                }
                
                // Calculate on-time delivery rate
                long onTimeDeliveries = completedHelperTasks.stream()
                    .filter(this::isTaskDeliveredOnTime)
                    .count();
                if (!completedHelperTasks.isEmpty()) {
                    performance.setOnTimeDeliveryRate((double) onTimeDeliveries / completedHelperTasks.size() * 100);
                }
                
                return performance;
            })
            .sorted(Comparator.comparing(HelperPerformance::getCompletionRate).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Calculate task type performance
     */
    private List<TaskTypePerformance> calculateTaskTypePerformance(List<Task> tasks) {
        Map<String, List<Task>> tasksByType = tasks.stream()
            .collect(Collectors.groupingBy(Task::getType));
        
        return tasksByType.entrySet().stream()
            .map(entry -> {
                String taskType = entry.getKey();
                List<Task> typeTasks = entry.getValue();
                
                TaskTypePerformance performance = new TaskTypePerformance();
                performance.setTaskType(taskType);
                performance.setTotalTasks(typeTasks.size());
                
                int completedTasks = (int) typeTasks.stream()
                    .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
                    .count();
                performance.setCompletedTasks(completedTasks);
                
                if (typeTasks.size() > 0) {
                    performance.setCompletionRate((double) completedTasks / typeTasks.size() * 100);
                }
                
                // Calculate average completion time
                List<Task> completedTypeTasks = typeTasks.stream()
                    .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
                    .collect(Collectors.toList());
                
                if (!completedTypeTasks.isEmpty()) {
                    double avgCompletionTime = completedTypeTasks.stream()
                        .mapToDouble(this::getTaskCompletionTime)
                        .average()
                        .orElse(0.0);
                    performance.setAverageCompletionTimeMinutes(avgCompletionTime);
                }
                
                return performance;
            })
            .sorted(Comparator.comparing(TaskTypePerformance::getCompletionRate).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Calculate performance trends
     */
    private List<PerformanceTrend> calculatePerformanceTrends(LocalDateTime startDate, LocalDateTime endDate) {
        List<PerformanceTrend> trends = new ArrayList<>();
        
        // Calculate daily trends
        LocalDateTime currentDate = startDate;
        while (currentDate.isBefore(endDate)) {
            LocalDateTime nextDate = currentDate.plusDays(1);
            
            List<Task> dailyTasks = getTasksInDateRange(currentDate, nextDate);
            
            PerformanceTrend trend = new PerformanceTrend();
            trend.setDate(currentDate.toLocalDate());
            trend.setTotalTasks(dailyTasks.size());
            
            int completedTasks = (int) dailyTasks.stream()
                .filter(t -> Arrays.asList("delivered", "completed", "resolved").contains(t.getStatus()))
                .count();
            trend.setCompletedTasks(completedTasks);
            
            if (dailyTasks.size() > 0) {
                trend.setCompletionRate((double) completedTasks / dailyTasks.size() * 100);
            }
            
            trends.add(trend);
            currentDate = nextDate;
        }
        
        return trends;
    }

    /**
     * Get task completion time in minutes
     */
    private double getTaskCompletionTime(Task task) {
        if (task.getUpdatedAt() != null && task.getCreatedAt() != null) {
            return java.time.Duration.between(task.getCreatedAt(), task.getUpdatedAt()).toMinutes();
        }
        return 0.0;
    }

    /**
     * Check if task was delivered on time
     */
    private boolean isTaskDeliveredOnTime(Task task) {
        if (task.getEta() == null || task.getUpdatedAt() == null) {
            return false;
        }
        return !task.getUpdatedAt().isAfter(task.getEta());
    }

    /**
     * Get task delay in minutes
     */
    private double getTaskDelay(Task task) {
        if (task.getEta() == null || task.getUpdatedAt() == null) {
            return 0.0;
        }
        
        if (task.getUpdatedAt().isAfter(task.getEta())) {
            return java.time.Duration.between(task.getEta(), task.getUpdatedAt()).toMinutes();
        }
        return 0.0;
    }

    /**
     * Get real-time performance dashboard data
     */
    public PerformanceDashboard getPerformanceDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);
        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime last30Days = now.minusDays(30);
        
        PerformanceDashboard dashboard = new PerformanceDashboard();
        
        // 24-hour metrics
        TaskPerformanceAnalytics last24h = getTaskPerformanceAnalytics(last24Hours, now);
        dashboard.setLast24Hours(last24h);
        
        // 7-day metrics
        TaskPerformanceAnalytics last7d = getTaskPerformanceAnalytics(last7Days, now);
        dashboard.setLast7Days(last7d);
        
        // 30-day metrics
        TaskPerformanceAnalytics last30d = getTaskPerformanceAnalytics(last30Days, now);
        dashboard.setLast30Days(last30d);
        
        // Current active tasks
        List<Task> activeTasks = taskRepository.findByStatusIn(Arrays.asList("assigned", "picked_up"));
        dashboard.setActiveTasks(activeTasks.size());
        
        // Pending tasks
        List<Task> pendingTasks = taskRepository.findByStatus("new");
        dashboard.setPendingTasks(pendingTasks.size());
        
        return dashboard;
    }

    // Data classes
    public static class TaskPerformanceAnalytics {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int totalTasks;
        private CompletionMetrics completionMetrics;
        private EfficiencyMetrics efficiencyMetrics;
        private List<HelperPerformance> helperPerformance;
        private List<TaskTypePerformance> taskTypePerformance;
        private List<PerformanceTrend> trends;

        // Getters and setters
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public CompletionMetrics getCompletionMetrics() { return completionMetrics; }
        public void setCompletionMetrics(CompletionMetrics completionMetrics) { this.completionMetrics = completionMetrics; }

        public EfficiencyMetrics getEfficiencyMetrics() { return efficiencyMetrics; }
        public void setEfficiencyMetrics(EfficiencyMetrics efficiencyMetrics) { this.efficiencyMetrics = efficiencyMetrics; }

        public List<HelperPerformance> getHelperPerformance() { return helperPerformance; }
        public void setHelperPerformance(List<HelperPerformance> helperPerformance) { this.helperPerformance = helperPerformance; }

        public List<TaskTypePerformance> getTaskTypePerformance() { return taskTypePerformance; }
        public void setTaskTypePerformance(List<TaskTypePerformance> taskTypePerformance) { this.taskTypePerformance = taskTypePerformance; }

        public List<PerformanceTrend> getTrends() { return trends; }
        public void setTrends(List<PerformanceTrend> trends) { this.trends = trends; }
    }

    public static class CompletionMetrics {
        private int totalTasks;
        private int completedTasks;
        private int cancelledTasks;
        private int inProgressTasks;
        private double completionRate;
        private double cancellationRate;

        // Getters and setters
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public int getCancelledTasks() { return cancelledTasks; }
        public void setCancelledTasks(int cancelledTasks) { this.cancelledTasks = cancelledTasks; }

        public int getInProgressTasks() { return inProgressTasks; }
        public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public double getCancellationRate() { return cancellationRate; }
        public void setCancellationRate(double cancellationRate) { this.cancellationRate = cancellationRate; }
    }

    public static class EfficiencyMetrics {
        private double averageCompletionTimeMinutes;
        private double onTimeDeliveryRate;
        private double averageDelayMinutes;
        private double throughputTasksPerHour;

        // Getters and setters
        public double getAverageCompletionTimeMinutes() { return averageCompletionTimeMinutes; }
        public void setAverageCompletionTimeMinutes(double averageCompletionTimeMinutes) { this.averageCompletionTimeMinutes = averageCompletionTimeMinutes; }

        public double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
        public void setOnTimeDeliveryRate(double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }

        public double getAverageDelayMinutes() { return averageDelayMinutes; }
        public void setAverageDelayMinutes(double averageDelayMinutes) { this.averageDelayMinutes = averageDelayMinutes; }

        public double getThroughputTasksPerHour() { return throughputTasksPerHour; }
        public void setThroughputTasksPerHour(double throughputTasksPerHour) { this.throughputTasksPerHour = throughputTasksPerHour; }
    }

    public static class HelperPerformance {
        private User helper;
        private int totalTasks;
        private int completedTasks;
        private double completionRate;
        private double averageCompletionTimeMinutes;
        private double onTimeDeliveryRate;

        // Getters and setters
        public User getHelper() { return helper; }
        public void setHelper(User helper) { this.helper = helper; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public double getAverageCompletionTimeMinutes() { return averageCompletionTimeMinutes; }
        public void setAverageCompletionTimeMinutes(double averageCompletionTimeMinutes) { this.averageCompletionTimeMinutes = averageCompletionTimeMinutes; }

        public double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
        public void setOnTimeDeliveryRate(double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }
    }

    public static class TaskTypePerformance {
        private String taskType;
        private int totalTasks;
        private int completedTasks;
        private double completionRate;
        private double averageCompletionTimeMinutes;

        // Getters and setters
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public double getAverageCompletionTimeMinutes() { return averageCompletionTimeMinutes; }
        public void setAverageCompletionTimeMinutes(double averageCompletionTimeMinutes) { this.averageCompletionTimeMinutes = averageCompletionTimeMinutes; }
    }

    public static class PerformanceTrend {
        private java.time.LocalDate date;
        private int totalTasks;
        private int completedTasks;
        private double completionRate;

        // Getters and setters
        public java.time.LocalDate getDate() { return date; }
        public void setDate(java.time.LocalDate date) { this.date = date; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }

    public static class PerformanceDashboard {
        private TaskPerformanceAnalytics last24Hours;
        private TaskPerformanceAnalytics last7Days;
        private TaskPerformanceAnalytics last30Days;
        private int activeTasks;
        private int pendingTasks;

        // Getters and setters
        public TaskPerformanceAnalytics getLast24Hours() { return last24Hours; }
        public void setLast24Hours(TaskPerformanceAnalytics last24Hours) { this.last24Hours = last24Hours; }

        public TaskPerformanceAnalytics getLast7Days() { return last7Days; }
        public void setLast7Days(TaskPerformanceAnalytics last7Days) { this.last7Days = last7Days; }

        public TaskPerformanceAnalytics getLast30Days() { return last30Days; }
        public void setLast30Days(TaskPerformanceAnalytics last30Days) { this.last30Days = last30Days; }

        public int getActiveTasks() { return activeTasks; }
        public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }

        public int getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
    }
}


