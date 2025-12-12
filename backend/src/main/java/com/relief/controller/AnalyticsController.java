package com.relief.controller;

import com.relief.dto.AnalyticsResponse;
import com.relief.dto.TimeSeriesData;
import com.relief.security.RequiresPermission;
import com.relief.security.Permission;
import com.relief.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/overview")
    @Operation(summary = "Get analytics overview")
    public ResponseEntity<AnalyticsResponse> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        AnalyticsResponse overview = analyticsService.getOverview(start, end);
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/needs/trends")
    @Operation(summary = "Get needs request trends")
    public ResponseEntity<List<TimeSeriesData>> getNeedsTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        List<TimeSeriesData> trends = analyticsService.getNeedsTrends(start, end, granularity);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/tasks/performance")
    @Operation(summary = "Get task performance metrics")
    public ResponseEntity<Map<String, Object>> getTaskPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> performance = analyticsService.getTaskPerformance(start, end);
        return ResponseEntity.ok(performance);
    }
    
    @GetMapping("/users/activity")
    @Operation(summary = "Get user activity metrics")
    public ResponseEntity<Map<String, Object>> getUserActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> activity = analyticsService.getUserActivity(start, end);
        return ResponseEntity.ok(activity);
    }
    
    @GetMapping("/inventory/status")
    @Operation(summary = "Get inventory status and alerts")
    @RequiresPermission(Permission.REPORTS_READ)
    public ResponseEntity<Map<String, Object>> getInventoryStatus() {
        Map<String, Object> status = analyticsService.getInventoryStatus();
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/response-times")
    @Operation(summary = "Get response time analytics")
    public ResponseEntity<Map<String, Object>> getResponseTimes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> responseTimes = analyticsService.getResponseTimes(start, end);
        return ResponseEntity.ok(responseTimes);
    }
    
    @GetMapping("/geographic/distribution")
    @Operation(summary = "Get geographic distribution of needs")
    public ResponseEntity<List<Map<String, Object>>> getGeographicDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        List<Map<String, Object>> distribution = analyticsService.getGeographicDistribution(start, end);
        return ResponseEntity.ok(distribution);
    }
    
    @GetMapping("/severity/breakdown")
    @Operation(summary = "Get severity level breakdown")
    public ResponseEntity<Map<String, Object>> getSeverityBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> breakdown = analyticsService.getSeverityBreakdown(start, end);
        return ResponseEntity.ok(breakdown);
    }
    
    @GetMapping("/export")
    @Operation(summary = "Export analytics data")
    @RequiresPermission(Permission.REPORTS_EXPORT)
    public ResponseEntity<byte[]> exportAnalytics(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        
        byte[] data = analyticsService.exportAnalytics(format, start, end);
        return ResponseEntity.ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=analytics." + format)
                .body(data);
    }
}



