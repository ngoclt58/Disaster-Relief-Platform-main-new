package com.relief.service.workflow;

import com.relief.entity.NeedsRequest;
import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.entity.WorkflowExecutionEntity;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import com.relief.repository.WorkflowExecutionRepository;
import com.relief.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Advanced workflow orchestration service with conditional logic and parallel execution
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowOrchestrationService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NeedsRequestRepository needsRequestRepository;
    private final WorkflowTemplateService workflowTemplateService;
    private final NotificationService notificationService;
    private final WorkflowExecutionRepository executionRepository;

    /**
     * Execute a workflow for a needs request by ID
     */
    @Transactional
    public WorkflowExecutionResult executeWorkflow(UUID requestId, String workflowType) {
        NeedsRequest request = needsRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Needs request not found: " + requestId));
        return executeWorkflow(request, workflowType);
    }

    /**
     * Execute a workflow for a needs request
     */
    @Transactional
    public WorkflowExecutionResult executeWorkflow(NeedsRequest request, String workflowType) {
        log.info("Executing workflow '{}' for request {}", workflowType, request.getId());
        
        WorkflowTemplate template = workflowTemplateService.getTemplate(workflowType);
        if (template == null) {
            throw new IllegalArgumentException("Workflow template not found: " + workflowType);
        }

        WorkflowContext context = new WorkflowContext(request);
        WorkflowExecutionResult result = new WorkflowExecutionResult();
        result.setExecutionId(UUID.randomUUID().toString());
        result.setRequestId(request.getId().toString());
        result.setWorkflowType(workflowType);
        
        try {
            // Execute workflow steps
            for (WorkflowStep step : template.getSteps()) {
                StepExecutionResult stepResult = executeStep(step, context);
                result.addStepResult(stepResult);
                
                // Check if workflow should continue
                if (!stepResult.isSuccess() && step.isRequired()) {
                    result.setStatus("FAILED");
                    result.setErrorMessage("Required step failed: " + step.getName());
                    break;
                }
                
                // Check conditional logic
                if (step.getCondition() != null && !evaluateCondition(step.getCondition(), context)) {
                    log.info("Skipping step {} due to condition", step.getName());
                    continue;
                }
            }
            
            if (result.getStatus() == null) {
                result.setStatus("COMPLETED");
            }
            
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            result.setStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }
        
        // Store execution result in database
        saveExecutionResult(request, workflowType, result);
        
        return result;
    }

    /**
     * Save workflow execution result to database
     */
    @Transactional
    private void saveExecutionResult(NeedsRequest request, String workflowType, WorkflowExecutionResult result) {
        try {
            WorkflowExecutionEntity entity = WorkflowExecutionEntity.builder()
                    .executionId(result.getExecutionId())
                    .request(request)
                    .workflowType(workflowType)
                    .status(result.getStatus())
                    .errorMessage(result.getErrorMessage())
                    .startTime(result.getStartTime())
                    .endTime(result.getEndTime())
                    .build();
            
            // Convert step results to JSON
            Map<String, Object> executionData = new HashMap<>();
            List<Map<String, Object>> stepResultsData = new ArrayList<>();
            for (StepExecutionResult stepResult : result.getStepResults()) {
                Map<String, Object> stepData = new HashMap<>();
                stepData.put("stepName", stepResult.getStepName());
                stepData.put("success", stepResult.isSuccess());
                stepData.put("message", stepResult.getMessage());
                stepResultsData.add(stepData);
            }
            executionData.put("stepResults", stepResultsData);
            executionData.put("requestId", request.getId().toString());
            executionData.put("workflowType", workflowType);
            
            entity.setExecutionData(executionData);
            executionRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to save workflow execution result to database", e);
        }
    }

    /**
     * Get workflow execution result by execution ID
     */
    @Transactional(readOnly = true)
    public WorkflowExecutionResult getExecutionResult(String executionId) {
        Optional<WorkflowExecutionEntity> entityOpt = executionRepository.findByExecutionId(executionId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        
        WorkflowExecutionEntity entity = entityOpt.get();
        return convertToExecutionResult(entity);
    }

    /**
     * Convert WorkflowExecutionEntity to WorkflowExecutionResult
     */
    private WorkflowExecutionResult convertToExecutionResult(WorkflowExecutionEntity entity) {
        WorkflowExecutionResult result = new WorkflowExecutionResult();
        result.setExecutionId(entity.getExecutionId());
        result.setRequestId(entity.getRequest() != null ? entity.getRequest().getId().toString() : null);
        result.setWorkflowType(entity.getWorkflowType());
        result.setStatus(entity.getStatus());
        result.setErrorMessage(entity.getErrorMessage());
        
        // Convert step results from JSON
        if (entity.getExecutionData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepResultsData = (List<Map<String, Object>>) entity.getExecutionData().get("stepResults");
            if (stepResultsData != null) {
                for (Map<String, Object> stepData : stepResultsData) {
                    StepExecutionResult stepResult = new StepExecutionResult(
                            (String) stepData.get("stepName"),
                            (Boolean) stepData.getOrDefault("success", false),
                            (String) stepData.get("message")
                    );
                    result.addStepResult(stepResult);
                }
            }
        }
        
        // Set timestamps (note: we don't store these in the result class as LocalDateTime, but they're in entity)
        // We'd need to update WorkflowExecutionResult to store timestamps if needed
        
        return result;
    }

    /**
     * Execute a single workflow step
     */
    private StepExecutionResult executeStep(WorkflowStep step, WorkflowContext context) {
        log.info("Executing step: {}", step.getName());
        
        try {
            switch (step.getType()) {
                case CREATE_TASK:
                    return executeCreateTaskStep(step, context);
                case SEND_NOTIFICATION:
                    return executeNotificationStep(step, context);
                case ASSIGN_USER:
                    return executeAssignUserStep(step, context);
                case WAIT_FOR_CONDITION:
                    return executeWaitStep(step, context);
                case PARALLEL_EXECUTION:
                    return executeParallelStep(step, context);
                case CONDITIONAL_BRANCH:
                    return executeConditionalStep(step, context);
                default:
                    return new StepExecutionResult(step.getName(), false, "Unknown step type: " + step.getType());
            }
        } catch (Exception e) {
            log.error("Step execution failed: {}", step.getName(), e);
            return new StepExecutionResult(step.getName(), false, e.getMessage());
        }
    }

    /**
     * Execute create task step
     */
    private StepExecutionResult executeCreateTaskStep(WorkflowStep step, WorkflowContext context) {
        Map<String, Object> params = step.getParameters();
        String taskType = (String) params.get("taskType");
        String assigneeId = (String) params.get("assigneeId");
        
        Task task = Task.builder()
                .request(context.getRequest())
                .status("new")
                .build();
        
        if (assigneeId != null) {
            User assignee = userRepository.findById(UUID.fromString(assigneeId)).orElse(null);
            if (assignee != null) {
                task.setAssignee(assignee);
                task.setStatus("assigned");
            }
        }
        
        task = taskRepository.save(task);
        context.addVariable("taskId", task.getId().toString());
        context.addVariable("taskType", taskType);
        
        return new StepExecutionResult(step.getName(), true, "Task created: " + task.getId());
    }

    /**
     * Execute notification step
     */
    private StepExecutionResult executeNotificationStep(WorkflowStep step, WorkflowContext context) {
        Map<String, Object> params = step.getParameters();
        String message = (String) params.get("message");
        String recipientRole = (String) params.get("recipientRole");
        
        // Send notification based on role
        List<User> recipients = getUsersByRole(recipientRole);
        for (User recipient : recipients) {
            notificationService.sendNotification(recipient, message, context.getRequest());
        }
        
        return new StepExecutionResult(step.getName(), true, "Notifications sent to " + recipients.size() + " users");
    }

    /**
     * Execute assign user step
     */
    private StepExecutionResult executeAssignUserStep(WorkflowStep step, WorkflowContext context) {
        Map<String, Object> params = step.getParameters();
        String assigneeRole = (String) params.get("assigneeRole");
        String taskId = (String) context.getVariable("taskId");
        
        if (taskId == null) {
            return new StepExecutionResult(step.getName(), false, "No task ID available");
        }
        
        Task task = taskRepository.findById(UUID.fromString(taskId)).orElse(null);
        if (task == null) {
            return new StepExecutionResult(step.getName(), false, "Task not found");
        }
        
        // Find best assignee based on role and availability
        User assignee = findBestAssignee(assigneeRole, context.getRequest());
        if (assignee != null) {
            task.setAssignee(assignee);
            task.setStatus("assigned");
            taskRepository.save(task);
            context.addVariable("assigneeId", assignee.getId().toString());
        }
        
        return new StepExecutionResult(step.getName(), true, "User assigned: " + (assignee != null ? assignee.getId() : "none"));
    }

    /**
     * Execute wait step
     */
    private StepExecutionResult executeWaitStep(WorkflowStep step, WorkflowContext context) {
        Map<String, Object> params = step.getParameters();
        int waitSeconds = (Integer) params.getOrDefault("waitSeconds", 0);
        
        try {
            Thread.sleep(waitSeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new StepExecutionResult(step.getName(), false, "Wait interrupted");
        }
        
        return new StepExecutionResult(step.getName(), true, "Waited " + waitSeconds + " seconds");
    }

    /**
     * Execute parallel step
     */
    private StepExecutionResult executeParallelStep(WorkflowStep step, WorkflowContext context) {
        List<WorkflowStep> parallelSteps = step.getParallelSteps();
        List<CompletableFuture<StepExecutionResult>> futures = new ArrayList<>();
        
        for (WorkflowStep parallelStep : parallelSteps) {
            CompletableFuture<StepExecutionResult> future = CompletableFuture.supplyAsync(() -> 
                executeStep(parallelStep, context)
            );
            futures.add(future);
        }
        
        // Wait for all parallel steps to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get();
        } catch (Exception e) {
            return new StepExecutionResult(step.getName(), false, "Parallel execution failed: " + e.getMessage());
        }
        
        // Collect results
        List<StepExecutionResult> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        boolean allSuccess = results.stream().allMatch(StepExecutionResult::isSuccess);
        String message = "Parallel execution completed: " + 
            results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum() + 
            "/" + results.size() + " successful";
        
        return new StepExecutionResult(step.getName(), allSuccess, message);
    }

    /**
     * Execute conditional step
     */
    private StepExecutionResult executeConditionalStep(WorkflowStep step, WorkflowContext context) {
        if (evaluateCondition(step.getCondition(), context)) {
            return executeStep(step.getTrueStep(), context);
        } else if (step.getFalseStep() != null) {
            return executeStep(step.getFalseStep(), context);
        }
        
        return new StepExecutionResult(step.getName(), true, "Conditional step skipped");
    }

    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(WorkflowCondition condition, WorkflowContext context) {
        if (condition == null) return true;
        
        Object value = context.getVariable(condition.getVariable());
        String operator = condition.getOperator();
        Object expectedValue = condition.getExpectedValue();
        
        switch (operator) {
            case "equals":
                return Objects.equals(value, expectedValue);
            case "not_equals":
                return !Objects.equals(value, expectedValue);
            case "greater_than":
                return compareNumbers(value, expectedValue) > 0;
            case "less_than":
                return compareNumbers(value, expectedValue) < 0;
            case "contains":
                return value != null && value.toString().contains(expectedValue.toString());
            case "not_contains":
                return value == null || !value.toString().contains(expectedValue.toString());
            default:
                return false;
        }
    }

    /**
     * Compare numbers for conditions
     */
    private int compareNumbers(Object value1, Object value2) {
        if (value1 instanceof Number && value2 instanceof Number) {
            return Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
        }
        return 0;
    }

    /**
     * Find best assignee based on role and availability
     */
    private User findBestAssignee(String role, NeedsRequest request) {
        List<User> users = userRepository.findByRoleAndNotDisabled(role);
        
        // Simple assignment logic - in real implementation, use more sophisticated algorithms
        return users.stream()
            .filter(user -> isUserAvailable(user))
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if user is available for assignment
     */
    private boolean isUserAvailable(User user) {
        // Check if user has too many active tasks
        long activeTaskCount = taskRepository.countByAssigneeAndStatusIn(
            user, 
            Arrays.asList("assigned", "picked_up")
        );
        
        return activeTaskCount < 5; // Max 5 active tasks per user
    }

    /**
     * Get users by role
     */
    private List<User> getUsersByRole(String role) {
        return userRepository.findByRoleAndNotDisabled(role);
    }

    // Data classes
    public static class WorkflowContext {
        private final NeedsRequest request;
        private final Map<String, Object> variables = new HashMap<>();

        public WorkflowContext(NeedsRequest request) {
            this.request = request;
        }

        public NeedsRequest getRequest() {
            return request;
        }

        public Object getVariable(String name) {
            return variables.get(name);
        }

        public void addVariable(String name, Object value) {
            variables.put(name, value);
        }
    }

    public static class WorkflowExecutionResult {
        private String executionId;
        private String requestId;
        private String workflowType;
        private String status;
        private String errorMessage;
        private List<StepExecutionResult> stepResults = new ArrayList<>();
        private LocalDateTime startTime = LocalDateTime.now();
        private LocalDateTime endTime;

        public void addStepResult(StepExecutionResult result) {
            stepResults.add(result);
        }

        public void setStatus(String status) {
            this.status = status;
            this.endTime = LocalDateTime.now();
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getWorkflowType() { return workflowType; }
        public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
        
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public List<StepExecutionResult> getStepResults() { return stepResults; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
    }

    public static class StepExecutionResult {
        private final String stepName;
        private final boolean success;
        private final String message;

        public StepExecutionResult(String stepName, boolean success, String message) {
            this.stepName = stepName;
            this.success = success;
            this.message = message;
        }

        public String getStepName() { return stepName; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}


