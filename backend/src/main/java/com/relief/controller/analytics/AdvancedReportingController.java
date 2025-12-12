package com.relief.controller.analytics;

import com.relief.service.analytics.AdvancedReportingService;
import com.relief.service.analytics.AdvancedReportingService.Report;
import com.relief.service.analytics.AdvancedReportingService.ReportDataSource;
import com.relief.service.analytics.AdvancedReportingService.ReportExecution;
import com.relief.service.analytics.AdvancedReportingService.ReportResult;
import com.relief.service.analytics.AdvancedReportingService.ReportSchedule;
import com.relief.service.analytics.AdvancedReportingService.ReportTemplate;
import com.relief.service.analytics.AdvancedReportingService.ReportAnalytics;
import com.relief.service.analytics.AdvancedReportingService.ReportExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Advanced reporting controller for complex reports with multiple data sources and visualizations
 */
@RestController
@RequestMapping("/analytics/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advanced Reporting", description = "Complex reports with multiple data sources and visualizations")
public class AdvancedReportingController {

    private final AdvancedReportingService advancedReportingService;

    // DTOs to match JSON payloads from frontend
    @lombok.Data
    public static class CreateReportRequest {
        private String name;
        private String description;
        private String reportType;
        private String userId;
        private java.util.List<AdvancedReportingService.ReportDataSource> dataSources;
        private java.util.Map<String, Object> configuration;
    }

    @lombok.Data
    public static class UpdateReportRequest {
        private String name;
        private String description;
        private java.util.List<AdvancedReportingService.ReportDataSource> dataSources;
        private java.util.Map<String, Object> configuration;
    }

    @PostMapping
    @Operation(summary = "Create advanced report")
    public ResponseEntity<Report> createReport(@RequestBody CreateReportRequest request) {
        Report report = advancedReportingService.createReport(
                request.getName(),
                request.getDescription(),
                request.getReportType(),
                request.getUserId(),
                request.getDataSources(),
                request.getConfiguration() != null ? request.getConfiguration() : Map.of()
        );
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update advanced report")
    public ResponseEntity<Report> updateReport(
            @PathVariable String reportId,
            @RequestBody UpdateReportRequest request) {

        Report report = advancedReportingService.updateReport(
                reportId,
                request.getName(),
                request.getDescription(),
                request.getDataSources(),
                request.getConfiguration() != null ? request.getConfiguration() : Map.of()
        );
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get advanced report")
    public ResponseEntity<Report> getReport(@PathVariable String reportId) {
        Report report = advancedReportingService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    @GetMapping
    @Operation(summary = "Get user reports")
    public ResponseEntity<List<Report>> getUserReports(
            @RequestParam String userId,
            @RequestParam(required = false) String reportType) {
        
        List<Report> reports = advancedReportingService.getUserReports(userId, reportType);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/public")
    @Operation(summary = "Get public reports")
    public ResponseEntity<List<Report>> getPublicReports(@RequestParam(required = false) String reportType) {
        List<Report> reports = advancedReportingService.getPublicReports(reportType);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/{reportId}/execute")
    @Operation(summary = "Execute report")
    public ResponseEntity<ReportExecution> executeReport(
            @PathVariable String reportId,
            @RequestBody Map<String, Object> parameters,
            @RequestParam String userId) {
        
        ReportExecution execution = advancedReportingService.executeReport(reportId, parameters, userId);
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get report execution")
    public ResponseEntity<ReportExecution> getExecution(@PathVariable String executionId) {
        ReportExecution execution = advancedReportingService.getExecution(executionId);
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/executions/{executionId}/result")
    @Operation(summary = "Get report result")
    public ResponseEntity<ReportResult> getReportResult(@PathVariable String executionId) {
        ReportResult result = advancedReportingService.getReportResult(executionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{reportId}/executions")
    @Operation(summary = "Get report executions")
    public ResponseEntity<List<ReportExecution>> getReportExecutions(@PathVariable String reportId) {
        List<ReportExecution> executions = advancedReportingService.getReportExecutions(reportId);
        return ResponseEntity.ok(executions);
    }

    @PostMapping("/{reportId}/schedule")
    @Operation(summary = "Schedule report")
    public ResponseEntity<ReportSchedule> scheduleReport(
            @PathVariable String reportId,
            @RequestParam String scheduleType,
            @RequestParam String cronExpression,
            @RequestParam String userId,
            @RequestBody Map<String, Object> parameters) {
        
        ReportSchedule schedule = advancedReportingService.scheduleReport(reportId, scheduleType, cronExpression, userId, parameters);
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @Operation(summary = "Cancel scheduled report")
    public ResponseEntity<Void> cancelSchedule(@PathVariable String scheduleId) {
        advancedReportingService.cancelSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/templates")
    @Operation(summary = "Create report template")
    public ResponseEntity<ReportTemplate> createTemplate(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String reportType,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> template,
            @RequestParam String createdBy) {
        
        ReportTemplate templateObj = advancedReportingService.createTemplate(name, description, reportType, userRole, template, createdBy);
        return ResponseEntity.ok(templateObj);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get report templates")
    public ResponseEntity<List<ReportTemplate>> getTemplates(
            @RequestParam(required = false) String reportType,
            @RequestParam String userRole) {
        
        List<ReportTemplate> templates = advancedReportingService.getTemplates(reportType, userRole);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{reportId}/analytics")
    @Operation(summary = "Get report analytics")
    public ResponseEntity<ReportAnalytics> getReportAnalytics(@PathVariable String reportId) {
        ReportAnalytics analytics = advancedReportingService.getReportAnalytics(reportId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/{reportId}/share")
    @Operation(summary = "Share report")
    public ResponseEntity<Void> shareReport(
            @PathVariable String reportId,
            @RequestParam String userId,
            @RequestParam String permission) {
        
        advancedReportingService.shareReport(reportId, userId, permission);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/executions/{executionId}/export")
    @Operation(summary = "Export report")
    public ResponseEntity<ReportExport> exportReport(
            @PathVariable String executionId,
            @RequestParam String format) {
        
        ReportExport export = advancedReportingService.exportReport(executionId, format);
        return ResponseEntity.ok(export);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete report")
    public ResponseEntity<Void> deleteReport(@PathVariable String reportId) {
        advancedReportingService.deleteReport(reportId);
        return ResponseEntity.ok().build();
    }
}


