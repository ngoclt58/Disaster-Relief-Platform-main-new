package com.relief.service.escalation;

import com.relief.entity.NeedsRequest;
import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import com.relief.service.notification.SmartNotificationService;
import com.relief.service.escalation.EscalationRuleService.EscalationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Auto-escalation service for high-priority and unresolved issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoEscalationService {

    private final NeedsRequestRepository needsRequestRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SmartNotificationService smartNotificationService;
    private final EscalationRuleService escalationRuleService;

    /**
     * Scheduled task to check for escalations every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void checkAndEscalate() {
        log.info("Running auto-escalation check");
        
        // Check high-priority requests
        checkHighPriorityRequests();
        
        // Check unresolved requests
        checkUnresolvedRequests();
        
        // Check overdue tasks
        checkOverdueTasks();
        
        log.info("Auto-escalation check completed");
    }

    /**
     * Check high-priority requests for escalation
     */
    private void checkHighPriorityRequests() {
        List<NeedsRequest> highPriorityRequests = needsRequestRepository.findHighPriorityRequests();
        
        for (NeedsRequest request : highPriorityRequests) {
            EscalationRule rule = escalationRuleService.getRuleForRequest(request);
            
            if (shouldEscalate(request, rule)) {
                escalateRequest(request, rule);
            }
        }
    }

    /**
     * Check unresolved requests for escalation
     */
    private void checkUnresolvedRequests() {
        List<NeedsRequest> unresolvedRequests = needsRequestRepository.findUnresolvedRequests();
        
        for (NeedsRequest request : unresolvedRequests) {
            EscalationRule rule = escalationRuleService.getRuleForRequest(request);
            
            if (shouldEscalate(request, rule)) {
                escalateRequest(request, rule);
            }
        }
    }

    /**
     * Check overdue tasks for escalation
     */
    private void checkOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks();
        
        for (Task task : overdueTasks) {
            if (task.getRequest() != null) {
                EscalationRule rule = escalationRuleService.getRuleForRequest(task.getRequest());
                
                if (shouldEscalateTask(task, rule)) {
                    escalateTask(task, rule);
                }
            }
        }
    }

    /**
     * Check if request should be escalated
     */
    private boolean shouldEscalate(NeedsRequest request, EscalationRule rule) {
        if (rule == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCreated = ChronoUnit.MINUTES.between(request.getCreatedAt(), now);
        
        // Check time-based escalation
        if (minutesSinceCreated >= rule.getEscalationTimeMinutes()) {
            return true;
        }
        
        // Check if already escalated recently
        if (wasRecentlyEscalated(request)) {
            return false;
        }
        
        // Check severity-based escalation
        if (request.getSeverity() >= rule.getSeverityThreshold()) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if task should be escalated
     */
    private boolean shouldEscalateTask(Task task, EscalationRule rule) {
        if (rule == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check if task is overdue
        if (task.getEta() != null && now.isAfter(task.getEta())) {
            return true;
        }
        
        // Check if task has been in same status too long
        if (task.getUpdatedAt() != null) {
            long minutesSinceUpdate = ChronoUnit.MINUTES.between(task.getUpdatedAt(), now);
            if (minutesSinceUpdate >= rule.getTaskEscalationTimeMinutes()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Escalate a request
     */
    private void escalateRequest(NeedsRequest request, EscalationRule rule) {
        log.info("Escalating request {} to level {}", request.getId(), rule.getEscalationLevel());
        
        // Update request status
        request.setStatus("ESCALATED");
        request.setUpdatedAt(LocalDateTime.now());
        needsRequestRepository.save(request);
        
        // Get escalation targets
        List<User> escalationTargets = getEscalationTargets(rule);
        
        // Send escalation notifications
        for (User target : escalationTargets) {
            String message = buildEscalationMessage(request, rule, target);
            smartNotificationService.sendContextualNotification(
                request, 
                "ESCALATION", 
                message
            );
        }
        
        // Log escalation
        logEscalation(request, rule, "REQUEST_ESCALATED");
    }

    /**
     * Escalate a task
     */
    private void escalateTask(Task task, EscalationRule rule) {
        log.info("Escalating task {} to level {}", task.getId(), rule.getEscalationLevel());
        
        // Update task status
        task.setStatus("ESCALATED");
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        // Get escalation targets
        List<User> escalationTargets = getEscalationTargets(rule);
        
        // Send escalation notifications
        if (task.getRequest() != null) {
            for (User target : escalationTargets) {
                String message = buildTaskEscalationMessage(task, rule, target);
                smartNotificationService.sendContextualNotification(
                    task.getRequest(), 
                    "TASK_ESCALATION", 
                    message
                );
            }
        }
        
        // Log escalation
        logEscalation(task.getRequest(), rule, "TASK_ESCALATED");
    }

    /**
     * Get escalation targets based on rule
     */
    private List<User> getEscalationTargets(EscalationRule rule) {
        switch (rule.getEscalationLevel()) {
            case 1:
                return userRepository.findByRoleAndNotDisabled("DISPATCHER");
            case 2:
                return userRepository.findByRoleAndNotDisabled("ADMIN");
            case 3:
                return userRepository.findByRoleAndNotDisabled("SUPERVISOR");
            default:
                return userRepository.findByRoleAndNotDisabled("ADMIN");
        }
    }

    /**
     * Build escalation message for request
     */
    private String buildEscalationMessage(NeedsRequest request, EscalationRule rule, User target) {
        StringBuilder message = new StringBuilder();
        
        message.append("🚨 ESCALATION ALERT - Level ").append(rule.getEscalationLevel()).append("\n\n");
        message.append("Request ID: ").append(request.getId()).append("\n");
        message.append("Type: ").append(request.getType()).append("\n");
        message.append("Severity: ").append(request.getSeverity()).append("/5\n");
        message.append("Status: ").append(request.getStatus()).append("\n");
        message.append("Created: ").append(request.getCreatedAt()).append("\n");
        
        if (request.getNotes() != null) {
            message.append("Details: ").append(request.getNotes()).append("\n");
        }
        
        message.append("\nThis request has been escalated due to:\n");
        message.append("- ").append(rule.getEscalationReason()).append("\n");
        message.append("- Time since creation: ").append(ChronoUnit.MINUTES.between(request.getCreatedAt(), LocalDateTime.now())).append(" minutes\n");
        
        message.append("\nPlease take immediate action to resolve this issue.");
        
        return message.toString();
    }

    /**
     * Build escalation message for task
     */
    private String buildTaskEscalationMessage(Task task, EscalationRule rule, User target) {
        StringBuilder message = new StringBuilder();
        
        message.append("🚨 TASK ESCALATION ALERT - Level ").append(rule.getEscalationLevel()).append("\n\n");
        message.append("Task ID: ").append(task.getId()).append("\n");
        message.append("Type: ").append(task.getType()).append("\n");
        message.append("Status: ").append(task.getStatus()).append("\n");
        message.append("Assignee: ").append(task.getAssignee() != null ? task.getAssignee().getFullName() : "Unassigned").append("\n");
        message.append("Created: ").append(task.getCreatedAt()).append("\n");
        
        if (task.getEta() != null) {
            message.append("ETA: ").append(task.getEta()).append("\n");
        }
        
        message.append("\nThis task has been escalated due to:\n");
        message.append("- ").append(rule.getEscalationReason()).append("\n");
        
        if (task.getEta() != null && LocalDateTime.now().isAfter(task.getEta())) {
            message.append("- Task is overdue by ").append(ChronoUnit.MINUTES.between(task.getEta(), LocalDateTime.now())).append(" minutes\n");
        }
        
        message.append("\nPlease take immediate action to resolve this task.");
        
        return message.toString();
    }

    /**
     * Check if request was recently escalated
     */
    private boolean wasRecentlyEscalated(NeedsRequest request) {
        // Simple check - in real implementation, check escalation history
        return "ESCALATED".equals(request.getStatus());
    }

    /**
     * Log escalation event
     */
    private void logEscalation(NeedsRequest request, EscalationRule rule, String eventType) {
        // In real implementation, log to audit system
        log.info("Escalation logged: {} for request {} with rule {}", eventType, request.getId(), rule.getId());
    }
}


