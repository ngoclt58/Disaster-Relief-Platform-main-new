package com.relief.controller.ai;

import com.relief.service.ai.IntelligentCategorizationService;
import com.relief.service.ai.IntelligentCategorizationService.CategorizationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for AI-powered intelligent categorization
 */
@RestController
@RequestMapping("/ai/categorization")
@RequiredArgsConstructor
@Tag(name = "AI Categorization", description = "AI-powered request categorization and analysis")
public class AICategorizationController {

    private final IntelligentCategorizationService categorizationService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze and categorize a request description")
    public ResponseEntity<CategorizationResult> categorizeRequest(
            @RequestBody CategorizationRequest request) {
        
        CategorizationResult result = categorizationService.categorizeRequest(
            request.getDescription(),
            request.getCurrentType(),
            request.getCurrentSeverity()
        );
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch-analyze")
    @Operation(summary = "Batch analyze multiple request descriptions")
    public ResponseEntity<BatchCategorizationResult> batchCategorize(
            @RequestBody BatchCategorizationRequest request) {
        
        BatchCategorizationResult result = new BatchCategorizationResult();
        
        for (CategorizationRequest req : request.getRequests()) {
            CategorizationResult categorization = categorizationService.categorizeRequest(
                req.getDescription(),
                req.getCurrentType(),
                req.getCurrentSeverity()
            );
            result.addResult(req.getId(), categorization);
        }
        
        return ResponseEntity.ok(result);
    }

    // Request/Response DTOs
    public static class CategorizationRequest {
        private String id;
        private String description;
        private String currentType;
        private Integer currentSeverity;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCurrentType() { return currentType; }
        public void setCurrentType(String currentType) { this.currentType = currentType; }
        public Integer getCurrentSeverity() { return currentSeverity; }
        public void setCurrentSeverity(Integer currentSeverity) { this.currentSeverity = currentSeverity; }
    }

    public static class BatchCategorizationRequest {
        private java.util.List<CategorizationRequest> requests;

        public java.util.List<CategorizationRequest> getRequests() { return requests; }
        public void setRequests(java.util.List<CategorizationRequest> requests) { this.requests = requests; }
    }

    public static class BatchCategorizationResult {
        private java.util.Map<String, CategorizationResult> results = new java.util.HashMap<>();

        public void addResult(String id, CategorizationResult result) {
            results.put(id, result);
        }

        public java.util.Map<String, CategorizationResult> getResults() { return results; }
        public void setResults(java.util.Map<String, CategorizationResult> results) { this.results = results; }
    }
}


