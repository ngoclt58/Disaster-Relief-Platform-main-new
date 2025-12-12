package com.relief.service.task;

import com.relief.entity.NeedsRequest;
import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import com.relief.service.ai.IntelligentCategorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dynamic task creation based on patterns and rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicTaskCreationService {

    private final NeedsRequestRepository needsRequestRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskRuleEngine taskRuleEngine;
    private final IntelligentCategorizationService categorizationService;
    
    /**
     * Get a task by its ID
     * @param taskId The ID of the task to retrieve
     * @return An Optional containing the task if found, empty otherwise
     */
    public Optional<Task> getTaskById(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    /**
     * Automatically create tasks for a needs request based on patterns and rules
     */
    @Transactional
    public List<Task> createTasksForRequest(NeedsRequest request) {
        log.info("Creating dynamic tasks for request: {}", request.getId());
        
        List<Task> createdTasks = new ArrayList<>();
        
        // Get applicable rules for this request
        List<TaskCreationRule> applicableRules = taskRuleEngine.getApplicableRules(request);
        
        for (TaskCreationRule rule : applicableRules) {
            List<Task> tasks = createTasksFromRule(request, rule);
            createdTasks.addAll(tasks);
        }
        
        // Save all created tasks
        createdTasks = taskRepository.saveAll(createdTasks);
        
        log.info("Created {} dynamic tasks for request {}", createdTasks.size(), request.getId());
        return createdTasks;
    }

    /**
     * Create tasks from a specific rule
     */
    private List<Task> createTasksFromRule(NeedsRequest request, TaskCreationRule rule) {
        List<Task> tasks = new ArrayList<>();
        
        for (TaskTemplate template : rule.getTaskTemplates()) {
            Task task = createTaskFromTemplate(request, template, rule);
            if (task != null) {
                tasks.add(task);
            }
        }
        
        return tasks;
    }

    /**
     * Create a single task from a template
     */
    private Task createTaskFromTemplate(NeedsRequest request, TaskTemplate template, TaskCreationRule rule) {
        Task task = new Task();
        task.setRequestId(request.getId());
        task.setType(template.getType());
        task.setTitle(template.getTitle());
        task.setDescription(processTemplateDescription(template.getDescription(), request));
        task.setStatus("new");
        task.setPriority(template.getPriority());
        task.setCreatedAt(LocalDateTime.now());
        task.setCreatedBy(request.getCreatedBy());
        
        // Set estimated duration
        if (template.getEstimatedDurationMinutes() != null) {
            task.setEta(LocalDateTime.now().plusMinutes(template.getEstimatedDurationMinutes()));
        }
        
        // Set required skills
        if (template.getRequiredSkills() != null) {
            task.setRequiredSkills(String.join(",", template.getRequiredSkills()));
        }
        
        // Set location if needed
        if (template.isLocationRequired() && request.getGeomPoint() != null) {
            task.setLocation(request.getGeomPoint());
        }
        
        // Set dependencies
        if (template.getDependencies() != null && !template.getDependencies().isEmpty()) {
            task.setDependencies(String.join(",", template.getDependencies()));
        }
        
        return task;
    }

    /**
     * Process template description with request data
     */
    private String processTemplateDescription(String template, NeedsRequest request) {
        return template
            .replace("{{requestType}}", request.getType())
            .replace("{{requestDescription}}", request.getNotes() != null ? request.getNotes() : "")
            .replace("{{severity}}", String.valueOf(request.getSeverity()))
            .replace("{{location}}", request.getAddress() != null ? request.getAddress() : "Unknown location");
    }

    /**
     * Create tasks based on historical patterns
     */
    @Transactional
    public List<Task> createTasksFromPatterns(NeedsRequest request) {
        log.info("Creating tasks from patterns for request: {}", request.getId());
        
        List<Task> tasks = new ArrayList<>();
        
        // Analyze similar historical requests
        List<NeedsRequest> similarRequests = findSimilarHistoricalRequests(request);
        
        for (NeedsRequest similarRequest : similarRequests) {
            List<Task> historicalTasks = taskRepository.findByRequestId(similarRequest.getId());
            
            for (Task historicalTask : historicalTasks) {
                if (isTaskPatternApplicable(historicalTask, request)) {
                    Task newTask = createTaskFromPattern(historicalTask, request);
                    if (newTask != null) {
                        tasks.add(newTask);
                    }
                }
            }
        }
        
        // Remove duplicates and save
        tasks = removeDuplicateTasks(tasks);
        tasks = taskRepository.saveAll(tasks);
        
        log.info("Created {} pattern-based tasks for request {}", tasks.size(), request.getId());
        return tasks;
    }

    /**
     * Find similar historical requests
     */
    private List<NeedsRequest> findSimilarHistoricalRequests(NeedsRequest request) {
        // Use AI categorization to find similar requests
        var categorization = categorizationService.categorizeRequest(
            request.getNotes() != null ? request.getNotes() : "",
            request.getType(),
            request.getSeverity()
        );
        
        // Find requests with similar type and severity
        return needsRequestRepository.findByTypeAndSeverityAndStatusNot(
            categorization.suggestedType,
            categorization.suggestedSeverity,
            "CANCELLED"
        ).stream()
        .limit(10) // Limit to 10 similar requests
        .collect(Collectors.toList());
    }

    /**
     * Check if a task pattern is applicable to the current request
     */
    private boolean isTaskPatternApplicable(Task historicalTask, NeedsRequest currentRequest) {
        // Check if task type matches request type
        if (!isTaskTypeRelevant(historicalTask.getType(), currentRequest.getType())) {
            return false;
        }
        
        // Check if task was successful
        if (!"delivered".equals(historicalTask.getStatus())) {
            return false;
        }
        
        // Check if task was created recently (within last 30 days)
        if (historicalTask.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30))) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if task type is relevant to request type
     */
    private boolean isTaskTypeRelevant(String taskType, String requestType) {
        Map<String, List<String>> typeMapping = Map.of(
            "Medical Emergency", Arrays.asList("MEDICAL_RESPONSE", "AMBULANCE_CALL", "FIRST_AID"),
            "Food Request", Arrays.asList("FOOD_DELIVERY", "GROCERY_PICKUP", "MEAL_PREPARATION"),
            "Water Request", Arrays.asList("WATER_DELIVERY", "WATER_PURIFICATION", "SUPPLY_DELIVERY"),
            "Shelter", Arrays.asList("SHELTER_COORDINATION", "ACCOMMODATION_SETUP", "EVACUATION_ASSISTANCE"),
            "Evacuation", Arrays.asList("EVACUATION_COORDINATION", "TRANSPORTATION", "SAFETY_ASSESSMENT")
        );
        
        List<String> relevantTypes = typeMapping.get(requestType);
        return relevantTypes != null && relevantTypes.contains(taskType);
    }

    /**
     * Create task from historical pattern
     */
    private Task createTaskFromPattern(Task historicalTask, NeedsRequest request) {
        Task newTask = new Task();
        newTask.setRequestId(request.getId());
        newTask.setType(historicalTask.getType());
        newTask.setTitle(historicalTask.getTitle());
        newTask.setDescription(processTemplateDescription(historicalTask.getDescription(), request));
        newTask.setStatus("new");
        newTask.setPriority(historicalTask.getPriority());
        newTask.setCreatedAt(LocalDateTime.now());
        newTask.setCreatedBy(request.getCreatedBy());
        
        // Copy estimated duration
        if (historicalTask.getEta() != null) {
            newTask.setEta(LocalDateTime.now().plusMinutes(
                java.time.Duration.between(historicalTask.getCreatedAt(), historicalTask.getEta()).toMinutes()
            ));
        }
        
        // Copy required skills
        newTask.setRequiredSkills(historicalTask.getRequiredSkills());
        
        // Copy location if needed
        if (historicalTask.getLocation() != null) {
            newTask.setLocation(historicalTask.getLocation());
        }
        
        return newTask;
    }

    /**
     * Remove duplicate tasks based on type and description
     */
    private List<Task> removeDuplicateTasks(List<Task> tasks) {
        Map<String, Task> uniqueTasks = new HashMap<>();
        
        for (Task task : tasks) {
            String key = task.getType() + ":" + task.getTitle();
            if (!uniqueTasks.containsKey(key)) {
                uniqueTasks.put(key, task);
            }
        }
        
        return new ArrayList<>(uniqueTasks.values());
    }
}


