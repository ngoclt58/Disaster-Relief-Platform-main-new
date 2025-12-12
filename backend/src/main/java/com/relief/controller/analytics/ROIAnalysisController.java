package com.relief.controller.analytics;

import com.relief.service.analytics.ROIAnalysisService;
import com.relief.service.analytics.ROIAnalysisService.ROIAnalysis;
import com.relief.service.analytics.ROIAnalysisService.ROIMetrics;
import com.relief.service.analytics.ROIAnalysisService.CostBenefitAnalysis;
import com.relief.service.analytics.ROIAnalysisService.CostItem;
import com.relief.service.analytics.ROIAnalysisService.BenefitItem;
import com.relief.service.analytics.ROIAnalysisService.EffectivenessMetrics;
import com.relief.service.analytics.ROIAnalysisService.ImpactAssessment;
import com.relief.service.analytics.ROIAnalysisService.PerformanceBenchmark;
import com.relief.service.analytics.ROIAnalysisService.ValueForMoneyAnalysis;
import com.relief.service.analytics.ROIAnalysisService.ROIComparison;
import com.relief.service.analytics.ROIAnalysisService.ROITrend;
import com.relief.service.analytics.ROIAnalysisService.ROIAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ROI analysis controller for measuring effectiveness and return on investment of relief efforts
 */
@RestController
@RequestMapping("/analytics/roi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ROI Analysis", description = "Measure effectiveness and return on investment of relief efforts")
public class ROIAnalysisController {

    private final ROIAnalysisService roiAnalysisService;

    @lombok.Data
    public static class CreateAnalysisRequest {
        private String name;
        private String description;
        private String analysisType;
        private String projectId;
        private java.util.Map<String, Object> parameters;
        private String userId;
    }

    @PostMapping("/analyses")
    @Operation(summary = "Create ROI analysis")
    public ResponseEntity<ROIAnalysis> createAnalysis(@RequestBody CreateAnalysisRequest request) {
        ROIAnalysis analysis = roiAnalysisService.createAnalysis(
                request.getName(),
                request.getDescription(),
                request.getAnalysisType(),
                request.getProjectId(),
                request.getParameters() != null ? request.getParameters() : java.util.Map.of(),
                request.getUserId()
        );
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/analyses/{analysisId}/execute")
    @Operation(summary = "Execute ROI analysis")
    public ResponseEntity<ROIAnalysis> executeAnalysis(@PathVariable String analysisId) {
        ROIAnalysis analysis = roiAnalysisService.executeAnalysis(analysisId);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/analyses/{analysisId}")
    @Operation(summary = "Get ROI analysis")
    public ResponseEntity<ROIAnalysis> getAnalysis(@PathVariable String analysisId) {
        ROIAnalysis analysis = roiAnalysisService.getAnalysis(analysisId);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/analyses")
    @Operation(summary = "Get user ROI analyses")
    public ResponseEntity<List<ROIAnalysis>> getUserAnalyses(@RequestParam String userId) {
        List<ROIAnalysis> analyses = roiAnalysisService.getUserAnalyses(userId);
        return ResponseEntity.ok(analyses);
    }

    @PostMapping("/metrics/calculate")
    @Operation(summary = "Calculate ROI metrics")
    public ResponseEntity<ROIMetrics> calculateROI(
            @RequestParam String projectId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestBody Map<String, Object> parameters) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        ROIMetrics metrics = roiAnalysisService.calculateROI(projectId, start, end, parameters);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/cost-benefit")
    @Operation(summary = "Perform cost-benefit analysis")
    public ResponseEntity<CostBenefitAnalysis> performCostBenefitAnalysis(
            @RequestParam String projectId,
            @RequestBody List<CostItem> costs,
            @RequestBody List<BenefitItem> benefits) {
        
        CostBenefitAnalysis analysis = roiAnalysisService.performCostBenefitAnalysis(projectId, costs, benefits);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/effectiveness/measure")
    @Operation(summary = "Measure effectiveness")
    public ResponseEntity<EffectivenessMetrics> measureEffectiveness(
            @RequestParam String projectId,
            @RequestParam String metricType,
            @RequestBody Map<String, Object> parameters) {
        
        EffectivenessMetrics metrics = roiAnalysisService.measureEffectiveness(projectId, metricType, parameters);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/impact/assess")
    @Operation(summary = "Assess impact")
    public ResponseEntity<ImpactAssessment> assessImpact(
            @RequestParam String projectId,
            @RequestParam String impactType,
            @RequestBody Map<String, Object> criteria) {
        
        ImpactAssessment assessment = roiAnalysisService.assessImpact(projectId, impactType, criteria);
        return ResponseEntity.ok(assessment);
    }

    @PostMapping("/benchmark")
    @Operation(summary = "Benchmark performance")
    public ResponseEntity<PerformanceBenchmark> benchmarkPerformance(
            @RequestParam String projectId,
            @RequestParam String benchmarkType,
            @RequestBody Map<String, Object> parameters) {
        
        PerformanceBenchmark benchmark = roiAnalysisService.benchmarkPerformance(projectId, benchmarkType, parameters);
        return ResponseEntity.ok(benchmark);
    }

    @PostMapping("/value-for-money")
    @Operation(summary = "Analyze value for money")
    public ResponseEntity<ValueForMoneyAnalysis> analyzeValueForMoney(
            @RequestParam String projectId,
            @RequestBody Map<String, Object> parameters) {
        
        ValueForMoneyAnalysis analysis = roiAnalysisService.analyzeValueForMoney(projectId, parameters);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare ROI across projects")
    public ResponseEntity<ROIComparison> compareROI(
            @RequestBody List<String> projectIds,
            @RequestParam String comparisonType,
            @RequestBody Map<String, Object> parameters) {
        
        ROIComparison comparison = roiAnalysisService.compareROI(projectIds, comparisonType, parameters);
        return ResponseEntity.ok(comparison);
    }

    @PostMapping("/trends/analyze")
    @Operation(summary = "Analyze ROI trends")
    public ResponseEntity<ROITrend> analyzeTrends(
            @RequestParam String projectId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        ROITrend trend = roiAnalysisService.analyzeTrends(projectId, start, end);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get ROI analytics")
    public ResponseEntity<ROIAnalytics> getROIAnalytics(@RequestParam String projectId) {
        ROIAnalytics analytics = roiAnalysisService.getROIAnalytics(projectId);
        return ResponseEntity.ok(analytics);
    }

    @DeleteMapping("/analyses/{analysisId}")
    @Operation(summary = "Delete ROI analysis")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable String analysisId) {
        roiAnalysisService.deleteAnalysis(analysisId);
        return ResponseEntity.ok().build();
    }
}


