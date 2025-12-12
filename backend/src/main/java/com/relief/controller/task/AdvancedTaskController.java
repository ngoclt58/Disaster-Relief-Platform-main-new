package com.relief.controller.task;

import com.relief.entity.NeedsRequest;
import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.service.task.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced task management controller
 */
@RestController
@RequestMapping("/tasks/advanced")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advanced Task Management", description = "Advanced task management with dynamic creation, skill matching, and analytics")
public class AdvancedTaskController {

    private final DynamicTaskCreationService dynamicTaskCreationService;
    private final SkillBasedMatchingService skillBasedMatchingService;
    private final TaskDependencyService taskDependencyService;
    private final TaskPerformanceAnalyticsService performanceAnalyticsService;
    private final NeedsRequestRepository needsRequestRepository;

    @PostMapping("/create-dynamic")
    @Operation(summary = "Create dynamic tasks for a request")
    public ResponseEntity<Map<String, Object>> createDynamicTasks(
            @RequestParam UUID requestId,
            @AuthenticationPrincipal UserDetails principal) {
        
        NeedsRequest request = needsRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        List<Task> tasks = dynamicTaskCreationService.createTasksForRequest(request);
        
        return ResponseEntity.ok(Map.of(
            "requestId", requestId,
            "tasksCreated", tasks.size(),
            "tasks", tasks
        ));
    }

    @PostMapping("/create-from-patterns")
    @Operation(summary = "Create tasks from historical patterns")
    public ResponseEntity<Map<String, Object>> createTasksFromPatterns(
            @RequestParam UUID requestId,
            @AuthenticationPrincipal UserDetails principal) {
        
        NeedsRequest request = needsRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        List<Task> tasks = dynamicTaskCreationService.createTasksFromPatterns(request);
        
        return ResponseEntity.ok(Map.of(
            "requestId", requestId,
            "tasksCreated", tasks.size(),
            "tasks", tasks
        ));
    }

    @PostMapping("/match-skills")
    @Operation(summary = "Find best skill match for a task")
    public ResponseEntity<Map<String, Object>> findSkillMatch(
            @RequestParam UUID taskId,
            @AuthenticationPrincipal UserDetails principal) {
        
        Task task = dynamicTaskCreationService.getTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        User bestMatch = skillBasedMatchingService.findBestMatch(task);
        
        if (bestMatch != null) {
            return ResponseEntity.ok(Map.of(
                "taskId", taskId,
                "bestMatch", Map.of(
                    "id", bestMatch.getId(),
                    "email", bestMatch.getEmail(),
                    "fullName", bestMatch.getFullName()
                ),
                "matchFound", true
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "taskId", taskId,
                "matchFound", false,
                "message", "No suitable helper found"
            ));
        }
    }

    @PostMapping("/auto-assign")
    @Operation(summary = "Auto-assign all unassigned tasks")
    public ResponseEntity<Map<String, Object>> autoAssignTasks(
            @AuthenticationPrincipal UserDetails principal) {
        
        skillBasedMatchingService.autoAssignTasks();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Auto-assignment completed"
        ));
    }

    @GetMapping("/dependencies/{taskId}")
    @Operation(summary = "Get task dependency chain")
    public ResponseEntity<TaskDependencyService.TaskDependencyChain> getTaskDependencies(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails principal) {
        
        Task task = dynamicTaskCreationService.getTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        TaskDependencyService.TaskDependencyChain chain = taskDependencyService.getTaskDependencyChain(task);
        
        return ResponseEntity.ok(chain);
    }

    @GetMapping("/dependencies/{taskId}/critical-path")
    @Operation(summary = "Get critical path for a task")
    public ResponseEntity<List<Task>> getCriticalPath(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails principal) {
        
        Task task = dynamicTaskCreationService.getTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        List<Task> criticalPath = taskDependencyService.getCriticalPath(task);
        
        return ResponseEntity.ok(criticalPath);
    }

    @GetMapping("/dependencies/ready-to-start")
    @Operation(summary = "Get tasks ready to start")
    public ResponseEntity<List<Task>> getReadyToStartTasks(
            @AuthenticationPrincipal UserDetails principal) {
        
        List<Task> readyTasks = taskDependencyService.getReadyToStartTasks();
        
        return ResponseEntity.ok(readyTasks);
    }

    @PostMapping("/dependencies/{taskId}/validate")
    @Operation(summary = "Validate task dependency chain")
    public ResponseEntity<Map<String, Object>> validateDependencies(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails principal) {
        
        Task task = dynamicTaskCreationService.getTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        boolean isValid = taskDependencyService.validateDependencyChain(task);
        
        return ResponseEntity.ok(Map.of(
            "taskId", taskId,
            "isValid", isValid,
            "message", isValid ? "Dependency chain is valid" : "Circular dependency detected"
        ));
    }

    @GetMapping("/workflow/{taskId}")
    @Operation(summary = "Get task workflow visualization")
    public ResponseEntity<TaskDependencyService.TaskWorkflowVisualization> getTaskWorkflow(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails principal) {
        
        Task task = dynamicTaskCreationService.getTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        TaskDependencyService.TaskWorkflowVisualization workflow = taskDependencyService.getTaskWorkflowVisualization(task);
        
        return ResponseEntity.ok(workflow);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get task performance analytics")
    public ResponseEntity<TaskPerformanceAnalyticsService.TaskPerformanceAnalytics> getPerformanceAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails principal) {
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        TaskPerformanceAnalyticsService.TaskPerformanceAnalytics analytics = 
            performanceAnalyticsService.getTaskPerformanceAnalytics(start, end);
        
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/dashboard")
    @Operation(summary = "Get performance dashboard")
    public ResponseEntity<TaskPerformanceAnalyticsService.PerformanceDashboard> getPerformanceDashboard(
            @AuthenticationPrincipal UserDetails principal) {
        
        TaskPerformanceAnalyticsService.PerformanceDashboard dashboard = 
            performanceAnalyticsService.getPerformanceDashboard();
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/analytics/helpers")
    @Operation(summary = "Get helper performance ranking")
    public ResponseEntity<List<TaskPerformanceAnalyticsService.HelperPerformance>> getHelperPerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails principal) {
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        TaskPerformanceAnalyticsService.TaskPerformanceAnalytics analytics = 
            performanceAnalyticsService.getTaskPerformanceAnalytics(start, end);
        
        return ResponseEntity.ok(analytics.getHelperPerformance());
    }

    @GetMapping("/analytics/task-types")
    @Operation(summary = "Get task type performance")
    public ResponseEntity<List<TaskPerformanceAnalyticsService.TaskTypePerformance>> getTaskTypePerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails principal) {
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        TaskPerformanceAnalyticsService.TaskPerformanceAnalytics analytics = 
            performanceAnalyticsService.getTaskPerformanceAnalytics(start, end);
        
        return ResponseEntity.ok(analytics.getTaskTypePerformance());
    }

    @GetMapping("/analytics/trends")
    @Operation(summary = "Get performance trends")
    public ResponseEntity<List<TaskPerformanceAnalyticsService.PerformanceTrend>> getPerformanceTrends(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails principal) {
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        TaskPerformanceAnalyticsService.TaskPerformanceAnalytics analytics = 
            performanceAnalyticsService.getTaskPerformanceAnalytics(start, end);
        
        return ResponseEntity.ok(analytics.getTrends());
    }
}


