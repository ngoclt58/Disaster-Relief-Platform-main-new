package com.relief.service.task;

import com.relief.entity.Task;
import com.relief.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing task dependencies and complex workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDependencyService {

    private static final Logger log = LoggerFactory.getLogger(TaskDependencyService.class);

    private final TaskRepository taskRepository;

    /**
     * Check if a task can be started based on its dependencies
     */
    public boolean canStartTask(Task task) {
        if (task.getDependencies() == null || task.getDependencies().trim().isEmpty()) {
            return true; // No dependencies
        }
        
        List<String> dependencyIds = Arrays.stream(task.getDependencies().split(","))
            .map(String::trim)
            .collect(Collectors.toList());
        
        for (String dependencyId : dependencyIds) {
            Task dependency = taskRepository.findById(UUID.fromString(dependencyId)).orElse(null);
            if (dependency == null) {
                log.warn("Dependency task {} not found for task {}", dependencyId, task.getId());
                return false;
            }
            
            if (!isTaskCompleted(dependency)) {
                log.debug("Task {} cannot start - dependency {} not completed", task.getId(), dependencyId);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Check if a task is completed
     */
    private boolean isTaskCompleted(Task task) {
        return Arrays.asList("delivered", "completed", "resolved").contains(task.getStatus());
    }

    /**
     * Get all tasks that are ready to start (dependencies satisfied)
     */
    public List<Task> getReadyToStartTasks() {
        List<Task> allTasks = taskRepository.findAll();
        
        return allTasks.stream()
            .filter(task -> "new".equals(task.getStatus()))
            .filter(this::canStartTask)
            .collect(Collectors.toList());
    }

    /**
     * Get task dependency chain for a given task
     */
    public TaskDependencyChain getTaskDependencyChain(Task task) {
        TaskDependencyChain chain = new TaskDependencyChain();
        chain.setRootTask(task);
        
        // Build dependency tree
        buildDependencyTree(task, chain, new HashSet<>());
        
        return chain;
    }

    /**
     * Build dependency tree recursively
     */
    private void buildDependencyTree(Task task, TaskDependencyChain chain, Set<UUID> visited) {
        if (visited.contains(task.getId())) {
            return; // Avoid circular dependencies
        }
        visited.add(task.getId());
        
        if (task.getDependencies() != null && !task.getDependencies().trim().isEmpty()) {
            List<String> dependencyIds = Arrays.stream(task.getDependencies().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
            
            for (String dependencyId : dependencyIds) {
                Task dependency = taskRepository.findById(UUID.fromString(dependencyId)).orElse(null);
                if (dependency != null) {
                    chain.addDependency(dependency);
                    buildDependencyTree(dependency, chain, visited);
                }
            }
        }
    }

    /**
     * Get critical path for a task workflow
     */
    public List<Task> getCriticalPath(Task rootTask) {
        List<Task> criticalPath = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        
        buildCriticalPath(rootTask, criticalPath, visited);
        
        return criticalPath;
    }

    /**
     * Build critical path recursively
     */
    private void buildCriticalPath(Task task, List<Task> criticalPath, Set<UUID> visited) {
        if (visited.contains(task.getId())) {
            return;
        }
        visited.add(task.getId());
        
        criticalPath.add(task);
        
        if (task.getDependencies() != null && !task.getDependencies().trim().isEmpty()) {
            List<String> dependencyIds = Arrays.stream(task.getDependencies().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
            
            // Find the dependency with the longest estimated duration
            Task longestDependency = null;
            long maxDuration = 0;
            
            for (String dependencyId : dependencyIds) {
                Task dependency = taskRepository.findById(UUID.fromString(dependencyId)).orElse(null);
                if (dependency != null) {
                    long duration = getTaskDuration(dependency);
                    if (duration > maxDuration) {
                        maxDuration = duration;
                        longestDependency = dependency;
                    }
                }
            }
            
            if (longestDependency != null) {
                buildCriticalPath(longestDependency, criticalPath, visited);
            }
        }
    }

    /**
     * Get estimated duration of a task in minutes
     */
    private long getTaskDuration(Task task) {
        if (task.getEta() != null && task.getCreatedAt() != null) {
            return java.time.Duration.between(task.getCreatedAt(), task.getEta()).toMinutes();
        }
        
        // Default duration based on task type
        Map<String, Long> defaultDurations = Map.of(
            "MEDICAL_RESPONSE", 30L,
            "AMBULANCE_CALL", 5L,
            "FIRST_AID", 15L,
            "FOOD_DELIVERY", 60L,
            "GROCERY_PICKUP", 90L,
            "WATER_DELIVERY", 45L,
            "EVACUATION_COORDINATION", 180L,
            "TRANSPORTATION", 120L,
            "SAFETY_ASSESSMENT", 60L
        );
        
        return defaultDurations.getOrDefault(task.getType(), 60L);
    }

    /**
     * Update task status and check if dependent tasks can start
     */
    @Transactional
    public void updateTaskStatus(Task task, String newStatus) {
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        // Check if any dependent tasks can now start
        if (isTaskCompleted(task)) {
            List<Task> dependentTasks = findDependentTasks(task);
            
            for (Task dependentTask : dependentTasks) {
                if (canStartTask(dependentTask)) {
                    log.info("Task {} is now ready to start (dependency {} completed)", 
                        dependentTask.getId(), task.getId());
                    // In real implementation, send notification or trigger workflow
                }
            }
        }
    }

    /**
     * Find all tasks that depend on the given task
     */
    private List<Task> findDependentTasks(Task task) {
        List<Task> allTasks = taskRepository.findAll();
        
        return allTasks.stream()
            .filter(t -> t.getDependencies() != null && !t.getDependencies().trim().isEmpty())
            .filter(t -> Arrays.stream(t.getDependencies().split(","))
                .map(String::trim)
                .anyMatch(depId -> depId.equals(task.getId().toString())))
            .collect(Collectors.toList());
    }

    /**
     * Validate task dependency chain for circular dependencies
     */
    public boolean validateDependencyChain(Task task) {
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();
        
        return validateDependencyChainRecursive(task, visited, recursionStack);
    }

    /**
     * Validate dependency chain recursively
     */
    private boolean validateDependencyChainRecursive(Task task, Set<UUID> visited, Set<UUID> recursionStack) {
        if (recursionStack.contains(task.getId())) {
            return false; // Circular dependency detected
        }
        
        if (visited.contains(task.getId())) {
            return true; // Already validated
        }
        
        visited.add(task.getId());
        recursionStack.add(task.getId());
        
        if (task.getDependencies() != null && !task.getDependencies().trim().isEmpty()) {
            List<String> dependencyIds = Arrays.stream(task.getDependencies().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
            
            for (String dependencyId : dependencyIds) {
                Task dependency = taskRepository.findById(UUID.fromString(dependencyId)).orElse(null);
                if (dependency != null) {
                    if (!validateDependencyChainRecursive(dependency, visited, recursionStack)) {
                        return false;
                    }
                }
            }
        }
        
        recursionStack.remove(task.getId());
        return true;
    }

    /**
     * Get task workflow visualization data
     */
    public TaskWorkflowVisualization getTaskWorkflowVisualization(Task rootTask) {
        TaskWorkflowVisualization visualization = new TaskWorkflowVisualization();
        visualization.setRootTask(rootTask);
        
        // Get all tasks in the workflow
        List<Task> allTasks = getAllTasksInWorkflow(rootTask);
        visualization.setAllTasks(allTasks);
        
        // Calculate workflow metrics
        WorkflowMetrics metrics = calculateWorkflowMetrics(allTasks);
        visualization.setMetrics(metrics);
        
        // Get critical path
        List<Task> criticalPath = getCriticalPath(rootTask);
        visualization.setCriticalPath(criticalPath);
        
        return visualization;
    }

    /**
     * Get all tasks in a workflow
     */
    private List<Task> getAllTasksInWorkflow(Task rootTask) {
        List<Task> allTasks = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        
        collectAllTasks(rootTask, allTasks, visited);
        
        return allTasks;
    }

    /**
     * Collect all tasks recursively
     */
    private void collectAllTasks(Task task, List<Task> allTasks, Set<UUID> visited) {
        if (visited.contains(task.getId())) {
            return;
        }
        visited.add(task.getId());
        allTasks.add(task);
        
        if (task.getDependencies() != null && !task.getDependencies().trim().isEmpty()) {
            List<String> dependencyIds = Arrays.stream(task.getDependencies().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
            
            for (String dependencyId : dependencyIds) {
                Task dependency = taskRepository.findById(UUID.fromString(dependencyId)).orElse(null);
                if (dependency != null) {
                    collectAllTasks(dependency, allTasks, visited);
                }
            }
        }
    }

    /**
     * Calculate workflow metrics
     */
    private WorkflowMetrics calculateWorkflowMetrics(List<Task> tasks) {
        WorkflowMetrics metrics = new WorkflowMetrics();
        
        metrics.setTotalTasks(tasks.size());
        metrics.setCompletedTasks((int) tasks.stream().filter(this::isTaskCompleted).count());
        metrics.setInProgressTasks((int) tasks.stream().filter(t -> "assigned".equals(t.getStatus()) || "picked_up".equals(t.getStatus())).count());
        metrics.setPendingTasks((int) tasks.stream().filter(t -> "new".equals(t.getStatus())).count());
        
        // Calculate total estimated duration
        long totalDuration = tasks.stream()
            .mapToLong(this::getTaskDuration)
            .sum();
        metrics.setTotalEstimatedDurationMinutes(totalDuration);
        
        // Calculate completion percentage
        if (tasks.size() > 0) {
            metrics.setCompletionPercentage((double) metrics.getCompletedTasks() / tasks.size() * 100);
        }
        
        return metrics;
    }

    /**
     * Task dependency chain data class
     */
    public static class TaskDependencyChain {
        private Task rootTask;
        private List<Task> dependencies = new ArrayList<>();

        public Task getRootTask() { return rootTask; }
        public void setRootTask(Task rootTask) { this.rootTask = rootTask; }

        public List<Task> getDependencies() { return dependencies; }
        public void setDependencies(List<Task> dependencies) { this.dependencies = dependencies; }

        public void addDependency(Task dependency) {
            dependencies.add(dependency);
        }
    }

    /**
     * Task workflow visualization data class
     */
    public static class TaskWorkflowVisualization {
        private Task rootTask;
        private List<Task> allTasks;
        private List<Task> criticalPath;
        private WorkflowMetrics metrics;

        // Getters and setters
        public Task getRootTask() { return rootTask; }
        public void setRootTask(Task rootTask) { this.rootTask = rootTask; }

        public List<Task> getAllTasks() { return allTasks; }
        public void setAllTasks(List<Task> allTasks) { this.allTasks = allTasks; }

        public List<Task> getCriticalPath() { return criticalPath; }
        public void setCriticalPath(List<Task> criticalPath) { this.criticalPath = criticalPath; }

        public WorkflowMetrics getMetrics() { return metrics; }
        public void setMetrics(WorkflowMetrics metrics) { this.metrics = metrics; }
    }

    /**
     * Workflow metrics data class
     */
    public static class WorkflowMetrics {
        private int totalTasks;
        private int completedTasks;
        private int inProgressTasks;
        private int pendingTasks;
        private long totalEstimatedDurationMinutes;
        private double completionPercentage;

        // Getters and setters
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public int getInProgressTasks() { return inProgressTasks; }
        public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

        public int getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

        public long getTotalEstimatedDurationMinutes() { return totalEstimatedDurationMinutes; }
        public void setTotalEstimatedDurationMinutes(long totalEstimatedDurationMinutes) { this.totalEstimatedDurationMinutes = totalEstimatedDurationMinutes; }

        public double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
    }
}


