package com.relief.service.optimization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for AI-driven task assignment to optimize dispatcher/helper workloads
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancingService {

    private final Map<String, WorkloadAssignment> assignments = new ConcurrentHashMap<>();

    /**
     * Balance workload across available workers
     */
    public WorkloadAssignment balanceWorkload(
            List<Worker> availableWorkers,
            List<TaskItem> pendingTasks) {

        WorkloadAssignment assignment = new WorkloadAssignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setAssignedAt(LocalDateTime.now());

        // Calculate current workloads
        Map<String, Double> workloads = availableWorkers.stream()
                .collect(Collectors.toMap(
                        Worker::getId,
                        this::calculateCurrentWorkload
                ));

        List<TaskAssignment> taskAssignments = new ArrayList<>();

        // Sort tasks by priority
        List<TaskItem> sortedTasks = pendingTasks.stream()
                .sorted(Comparator
                        .comparing(TaskItem::getPriority).reversed()
                        .thenComparing(TaskItem::getEstimatedDuration))
                .collect(Collectors.toList());

        // Assign tasks using load balancing algorithm
        for (TaskItem task : sortedTasks) {
            String assignedWorker = findBestWorker(availableWorkers, task, workloads);
            
            if (assignedWorker != null) {
                TaskAssignment taskAssignment = new TaskAssignment();
                taskAssignment.setTaskId(task.getId());
                taskAssignment.setWorkerId(assignedWorker);
                taskAssignment.setTaskName(task.getName());
                taskAssignment.setTaskType(task.getType());
                taskAssignment.setEstimatedDuration(task.getEstimatedDuration());
                taskAssignment.setPriority(task.getPriority());
                taskAssignment.setLocation(task.getLocation());
                
                taskAssignments.add(taskAssignment);
                
                // Update workload
                workloads.put(assignedWorker, workloads.get(assignedWorker) + task.getEstimatedDuration());
            }
        }

        assignment.setTaskAssignments(taskAssignments);
        assignment.setWorkloadDistribution(calculateDistribution(workloads, availableWorkers));
        assignment.setBalanceScore(calculateBalanceScore(workloads));
        assignment.setTotalTasksAssigned(taskAssignments.size());

        assignments.put(assignment.getId(), assignment);

        log.info("Balanced workload: {} tasks assigned with balance score: {}", 
                taskAssignments.size(), assignment.getBalanceScore());

        return assignment;
    }

    private double calculateCurrentWorkload(Worker worker) {
        // Base workload calculation
        double baseWorkload = worker.getCurrentTasks().size() * 60.0; // Each task = 60 minutes
        
        // Skill-based multiplier
        double skillMultiplier = 1.0;
        if (worker.getSkills() != null && worker.getSkills().isEmpty()) {
            skillMultiplier = 0.9; // Skilled workers handle tasks faster
        }

        return baseWorkload * skillMultiplier;
    }

    private String findBestWorker(
            List<Worker> availableWorkers,
            TaskItem task,
            Map<String, Double> workloads) {

        if (availableWorkers.isEmpty()) {
            return null;
        }

        // Find worker with lowest workload who has required skills
        return availableWorkers.stream()
                .filter(w -> hasRequiredSkills(w, task))
                .min(Comparator
                        .comparing((Worker w) -> workloads.get(w.getId()))
                        .thenComparing(this::calculateWorkerEfficiency))
                .map(Worker::getId)
                .orElse(null);
    }

    private boolean hasRequiredSkills(Worker worker, TaskItem task) {
        if (task.getRequiredSkills() == null || task.getRequiredSkills().isEmpty()) {
            return true; // No specific skills required
        }

        return worker.getSkills() != null && 
               worker.getSkills().containsAll(task.getRequiredSkills());
    }

    private double calculateWorkerEfficiency(Worker worker) {
        // Efficiency based on workload and skills
        double workloadFactor = worker.getCurrentTasks().size() * 0.2;
        double skillFactor = worker.getSkills() != null ? worker.getSkills().size() * 0.1 : 0.0;
        
        return workloadFactor - skillFactor; // Lower is better
    }

    private Map<String, Double> calculateDistribution(Map<String, Double> workloads, List<Worker> workers) {
        Map<String, Double> distribution = new HashMap<>();
        
        double totalWorkload = workloads.values().stream().mapToDouble(Double::doubleValue).sum();
        int workerCount = workers.size();

        for (Worker worker : workers) {
            double workload = workloads.get(worker.getId());
            double percentage = workerCount > 0 ? (workload / totalWorkload) * 100 : 0.0;
            distribution.put(worker.getId(), percentage);
        }

        return distribution;
    }

    private double calculateBalanceScore(Map<String, Double> workloads) {
        if (workloads.isEmpty()) return 100.0;

        double average = workloads.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = workloads.values().stream()
                .mapToDouble(w -> Math.pow(w - average, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Score based on coefficient of variation (lower is better)
        double cv = average > 0 ? stdDev / average : 0.0;
        return Math.max(0.0, Math.min(100.0, 100 - (cv * 100)));
    }

    public WorkloadAssignment getAssignment(String assignmentId) {
        return assignments.get(assignmentId);
    }

    public List<WorkloadAssignment> getAssignments() {
        return new ArrayList<>(assignments.values());
    }

    // Inner classes
    @lombok.Data
    public static class WorkloadAssignment {
        private String id;
        private LocalDateTime assignedAt;
        private List<TaskAssignment> taskAssignments;
        private Map<String, Double> workloadDistribution;
        private double balanceScore;
        private int totalTasksAssigned;
    }

    @lombok.Data
    public static class TaskAssignment {
        private String taskId;
        private String workerId;
        private String taskName;
        private String taskType;
        private double estimatedDuration;
        private int priority;
        private String location;
    }

    @lombok.Data
    public static class Worker {
        private String id;
        private String name;
        private List<String> skills;
        private List<String> currentTasks;
        private String location;

        public Worker(String id, String name, List<String> skills, List<String> currentTasks, String location) {
            this.id = id;
            this.name = name;
            this.skills = skills != null ? skills : new ArrayList<>();
            this.currentTasks = currentTasks != null ? currentTasks : new ArrayList<>();
            this.location = location;
        }
    }

    @lombok.Data
    public static class TaskItem {
        private String id;
        private String name;
        private String type;
        private double estimatedDuration;
        private int priority;
        private List<String> requiredSkills;
        private String location;

        public TaskItem(String id, String name, String type, double estimatedDuration, 
                       int priority, List<String> requiredSkills, String location) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.estimatedDuration = estimatedDuration;
            this.priority = priority;
            this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
            this.location = location;
        }
    }
}

