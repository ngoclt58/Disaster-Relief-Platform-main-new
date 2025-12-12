package com.relief.controller.workflow;

import com.relief.service.workflow.WorkflowOrchestrationService;
import com.relief.service.workflow.WorkflowOrchestrationService.WorkflowExecutionResult;
import com.relief.service.workflow.WorkflowTemplateService;
import com.relief.service.workflow.WorkflowTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for workflow orchestration
 */
@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow Orchestration", description = "Workflow management and execution APIs")
public class WorkflowController {

    private final WorkflowOrchestrationService workflowService;
    private final WorkflowTemplateService templateService;

    @PostMapping("/execute")
    @Operation(summary = "Execute a workflow for a request")
    public ResponseEntity<WorkflowExecutionResult> executeWorkflow(
            @RequestBody WorkflowExecutionRequest request) {
        
        WorkflowExecutionResult result = workflowService.executeWorkflow(
            request.getRequestId(),
            request.getWorkflowType()
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get available workflow templates")
    public ResponseEntity<List<WorkflowTemplate>> getTemplates() {
        List<WorkflowTemplate> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{templateName}")
    @Operation(summary = "Get specific workflow template")
    public ResponseEntity<WorkflowTemplate> getTemplate(@PathVariable String templateName) {
        WorkflowTemplate template = templateService.getTemplate(templateName);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get workflow execution status")
    public ResponseEntity<WorkflowExecutionResult> getExecutionStatus(@PathVariable String executionId) {
        WorkflowExecutionResult result = workflowService.getExecutionResult(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    // Request DTOs
    public static class WorkflowExecutionRequest {
        private UUID requestId;
        private String workflowType;

        public UUID getRequestId() { return requestId; }
        public void setRequestId(UUID requestId) { this.requestId = requestId; }
        public String getWorkflowType() { return workflowType; }
        public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
    }
}


