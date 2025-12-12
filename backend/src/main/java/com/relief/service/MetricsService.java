package com.relief.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters
    private final Counter needsCreatedCounter;
    private final Counter tasksCompletedCounter;
    private final Counter userActionsCounter;
    private final Counter deliveriesCompletedCounter;
    
    // Timers
    private final Timer responseTimeTimer;
    private final Timer taskCompletionTimer;
    
    // Gauges
    private final AtomicLong activeNeedsGauge = new AtomicLong(0);
    private final AtomicLong activeTasksGauge = new AtomicLong(0);
    private final AtomicLong activeUsersGauge = new AtomicLong(0);
    private final AtomicLong lowStockItemsGauge = new AtomicLong(0);

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.needsCreatedCounter = Counter.builder("disaster_relief_needs_created_total")
                .description("Total number of needs requests created")
                .register(meterRegistry);
                
        this.tasksCompletedCounter = Counter.builder("disaster_relief_tasks_completed_total")
                .description("Total number of tasks completed")
                .register(meterRegistry);
                
        this.userActionsCounter = Counter.builder("disaster_relief_user_actions_total")
                .description("Total number of user actions")
                .register(meterRegistry);
                
        this.deliveriesCompletedCounter = Counter.builder("disaster_relief_deliveries_completed_total")
                .description("Total number of deliveries completed")
                .register(meterRegistry);
        
        // Initialize timers
        this.responseTimeTimer = Timer.builder("disaster_relief_response_time_seconds")
                .description("Response time for API requests")
                .register(meterRegistry);
                
        this.taskCompletionTimer = Timer.builder("disaster_relief_task_completion_seconds")
                .description("Time taken to complete tasks")
                .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("disaster_relief_needs_active_total", activeNeedsGauge, AtomicLong::get)
                .description("Number of active needs requests")
                .register(meterRegistry);
                
        Gauge.builder("disaster_relief_tasks_active_total", activeTasksGauge, AtomicLong::get)
                .description("Number of active tasks")
                .register(meterRegistry);
                
        Gauge.builder("disaster_relief_users_active_total", activeUsersGauge, AtomicLong::get)
                .description("Number of active users")
                .register(meterRegistry);
                
        Gauge.builder("disaster_relief_inventory_low_stock_total", lowStockItemsGauge, AtomicLong::get)
                .description("Number of items with low stock")
                .register(meterRegistry);
    }

    // Counter methods
    public void incrementNeedsCreated() {
        needsCreatedCounter.increment();
    }

    public void incrementNeedsCreated(String category, String severity) {
        Counter.builder("disaster_relief_needs_created_total")
                .tag("category", category)
                .tag("severity", severity)
                .register(meterRegistry)
                .increment();
    }

    public void incrementTasksCompleted() {
        tasksCompletedCounter.increment();
    }

    public void incrementTasksCompleted(String status) {
        Counter.builder("disaster_relief_tasks_completed_total")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void incrementUserActions() {
        userActionsCounter.increment();
    }

    public void incrementUserActions(String action, String role) {
        Counter.builder("disaster_relief_user_actions_total")
                .tag("action", action)
                .tag("role", role)
                .register(meterRegistry)
                .increment();
    }

    public void incrementDeliveriesCompleted() {
        deliveriesCompletedCounter.increment();
    }

    public void incrementDeliveriesCompleted(String status) {
        Counter.builder("disaster_relief_deliveries_completed_total")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    // Timer methods
    public void recordResponseTime(Duration duration) {
        responseTimeTimer.record(duration);
    }

    public void recordResponseTime(Duration duration, String endpoint, String method) {
        Timer.builder("disaster_relief_response_time_seconds")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
                .record(duration);
    }

    public void recordTaskCompletion(Duration duration) {
        taskCompletionTimer.record(duration);
    }

    public void recordTaskCompletion(Duration duration, String taskType) {
        Timer.builder("disaster_relief_task_completion_seconds")
                .tag("task_type", taskType)
                .register(meterRegistry)
                .record(duration);
    }

    // Gauge methods
    public void setActiveNeeds(long count) {
        activeNeedsGauge.set(count);
    }

    public void setActiveTasks(long count) {
        activeTasksGauge.set(count);
    }

    public void setActiveUsers(long count) {
        activeUsersGauge.set(count);
    }

    public void setLowStockItems(long count) {
        lowStockItemsGauge.set(count);
    }

    // Business metrics
    public void recordNeedsByCategory(String category, long count) {
        Gauge.builder("disaster_relief_needs_total", () -> count)
                .tag("category", category)
                .register(meterRegistry);
    }

    public void recordTasksByStatus(String status, long count) {
        Gauge.builder("disaster_relief_tasks_total", () -> count)
                .tag("status", status)
                .register(meterRegistry);
    }

    public void recordUsersByRole(String role, long count) {
        Gauge.builder("disaster_relief_users_total", () -> count)
                .tag("role", role)
                .register(meterRegistry);
    }

    public void recordInventoryByHub(String hub, long count) {
        Gauge.builder("disaster_relief_inventory_total", () -> count)
                .tag("hub", hub)
                .register(meterRegistry);
    }

    public void recordResponseTimeBySeverity(String severity, Duration duration) {
        Timer.builder("disaster_relief_response_time_seconds")
                .tag("severity", severity)
                .register(meterRegistry)
                .record(duration);
    }

    public void recordResponseTimeByRegion(String region, Duration duration) {
        Timer.builder("disaster_relief_response_time_seconds")
                .tag("region", region)
                .register(meterRegistry)
                .record(duration);
    }
}



