package com.relief.service.workflow;

import com.relief.entity.WorkflowTemplateEntity;
import com.relief.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing workflow templates from database
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateService.class);
    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowTemplateConverter converter;

    /**
     * Get workflow template by name
     */
    @Transactional(readOnly = true)
    public WorkflowTemplate getTemplate(String name) {
        Optional<WorkflowTemplateEntity> entity = templateRepository.findByName(name);
        if (entity.isEmpty() || !entity.get().getIsActive()) {
            log.warn("Template not found or inactive: {}", name);
            return null;
        }
        
        WorkflowTemplate template = converter.toWorkflowTemplate(entity.get().getTemplateData());
        if (template != null) {
            template.setName(entity.get().getName());
            template.setDescription(entity.get().getDescription());
        }
        return template;
    }

    /**
     * Get all available workflow templates
     */
    @Transactional(readOnly = true)
    public List<WorkflowTemplate> getAllTemplates() {
        List<WorkflowTemplateEntity> entities = templateRepository.findByIsActiveTrue();
        List<WorkflowTemplate> templates = new ArrayList<>();
        
        for (WorkflowTemplateEntity entity : entities) {
            WorkflowTemplate template = converter.toWorkflowTemplate(entity.getTemplateData());
            if (template != null) {
                template.setName(entity.getName());
                template.setDescription(entity.getDescription());
                templates.add(template);
            }
        }
        
        return templates;
    }

    /**
     * Create or update a workflow template
     */
    @Transactional
    public WorkflowTemplateEntity saveTemplate(WorkflowTemplate template) {
        Optional<WorkflowTemplateEntity> existing = templateRepository.findByName(template.getName());
        
        WorkflowTemplateEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
        } else {
            entity = WorkflowTemplateEntity.builder()
                    .name(template.getName())
                    .build();
        }
        
        entity.setDescription(template.getDescription());
        entity.setTemplateData(converter.toTemplateData(template));
        entity.setIsActive(true);
        
        return templateRepository.save(entity);
    }

    /**
     * Deactivate a workflow template
     */
    @Transactional
    public void deactivateTemplate(String name) {
        Optional<WorkflowTemplateEntity> entity = templateRepository.findByName(name);
        if (entity.isPresent()) {
            entity.get().setIsActive(false);
            templateRepository.save(entity.get());
        }
    }

    // Helper methods for creating workflow steps (used for programmatic template creation via saveTemplate)

    /**
     * Create notification step
     */
    public WorkflowStep createNotificationStep(String name, String recipientRole, String message) {
        WorkflowStep step = new WorkflowStep();
        step.setName(name);
        step.setType(WorkflowStepType.SEND_NOTIFICATION);
        step.setRequired(true);
        
        Map<String, Object> params = new HashMap<>();
        params.put("recipientRole", recipientRole);
        params.put("message", message);
        step.setParameters(params);
        
        return step;
    }

    /**
     * Create task creation step
     */
    public WorkflowStep createCreateTaskStep(String taskType, String assigneeRole) {
        WorkflowStep step = new WorkflowStep();
        step.setName("CREATE_" + taskType);
        step.setType(WorkflowStepType.CREATE_TASK);
        step.setRequired(true);
        
        Map<String, Object> params = new HashMap<>();
        params.put("taskType", taskType);
        params.put("assigneeRole", assigneeRole);
        step.setParameters(params);
        
        return step;
    }

    /**
     * Create user assignment step
     */
    public WorkflowStep createAssignUserStep(String assigneeRole) {
        WorkflowStep step = new WorkflowStep();
        step.setName("ASSIGN_USER");
        step.setType(WorkflowStepType.ASSIGN_USER);
        step.setRequired(true);
        
        Map<String, Object> params = new HashMap<>();
        params.put("assigneeRole", assigneeRole);
        step.setParameters(params);
        
        return step;
    }

    /**
     * Create wait step
     */
    public WorkflowStep createWaitStep(int waitSeconds) {
        WorkflowStep step = new WorkflowStep();
        step.setName("WAIT_" + waitSeconds);
        step.setType(WorkflowStepType.WAIT_FOR_CONDITION);
        step.setRequired(false);
        
        Map<String, Object> params = new HashMap<>();
        params.put("waitSeconds", waitSeconds);
        step.setParameters(params);
        
        return step;
    }

    /**
     * Create conditional step
     */
    public WorkflowStep createConditionalStep(WorkflowCondition condition, WorkflowStep trueStep) {
        WorkflowStep step = new WorkflowStep();
        step.setName("CONDITIONAL");
        step.setType(WorkflowStepType.CONDITIONAL_BRANCH);
        step.setRequired(false);
        step.setCondition(condition);
        step.setTrueStep(trueStep);
        
        return step;
    }

    /**
     * Create parallel execution step
     */
    public WorkflowStep createParallelStep(List<WorkflowStep> parallelSteps) {
        WorkflowStep step = new WorkflowStep();
        step.setName("PARALLEL_EXECUTION");
        step.setType(WorkflowStepType.PARALLEL_EXECUTION);
        step.setRequired(true);
        step.setParallelSteps(parallelSteps);
        
        return step;
    }
}


