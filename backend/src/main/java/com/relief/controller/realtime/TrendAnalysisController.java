package com.relief.controller.realtime;

import com.relief.service.realtime.TrendAnalysisService;
import com.relief.service.realtime.TrendAnalysisService.TrendAnalyzer;
import com.relief.service.realtime.TrendAnalysisService.DataPoint;
import com.relief.service.realtime.TrendAnalysisService.TrendResult;
import com.relief.service.realtime.TrendAnalysisService.TrendAlert;
import com.relief.service.realtime.TrendAnalysisService.TrendAnalytics;
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
 * Trend analysis controller to identify emerging patterns and trends in real-time data
 */
@RestController
@RequestMapping("/api/realtime/trend-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trend Analysis", description = "Identify emerging patterns and trends in real-time data")
public class TrendAnalysisController {

    private final TrendAnalysisService trendAnalysisService;

    @PostMapping("/analyzers")
    @Operation(summary = "Create trend analyzer")
    public ResponseEntity<TrendAnalyzer> createAnalyzer(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String dataSource,
            @RequestParam String metric,
            @RequestBody Map<String, Object> configuration) {
        
        TrendAnalyzer analyzer = trendAnalysisService.createAnalyzer(name, description, dataSource, metric, configuration);
        return ResponseEntity.ok(analyzer);
    }

    @PostMapping("/analyzers/{analyzerId}/data")
    @Operation(summary = "Add data point")
    public ResponseEntity<Void> addDataPoint(
            @PathVariable String analyzerId,
            @RequestBody DataPoint dataPoint) {
        
        trendAnalysisService.addDataPoint(analyzerId, dataPoint);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/analyzers/{analyzerId}/analyze")
    @Operation(summary = "Analyze trend")
    public ResponseEntity<TrendResult> analyzeTrend(@PathVariable String analyzerId) {
        TrendResult result = trendAnalysisService.analyzeTrend(analyzerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analyzers/{analyzerId}/trends")
    @Operation(summary = "Get trends")
    public ResponseEntity<List<TrendResult>> getTrends(
            @PathVariable String analyzerId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<TrendResult> trends = trendAnalysisService.getTrends(analyzerId, start, end);
        return ResponseEntity.ok(trends);
    }

    @PostMapping("/alerts")
    @Operation(summary = "Create trend alert")
    public ResponseEntity<TrendAlert> createAlert(
            @RequestParam String analyzerId,
            @RequestParam String name,
            @RequestParam String condition,
            @RequestParam String threshold,
            @RequestParam String action) {
        
        TrendAlert alert = trendAnalysisService.createAlert(analyzerId, name, condition, threshold, action);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/analyzers/{analyzerId}/alerts")
    @Operation(summary = "Check alerts")
    public ResponseEntity<List<TrendAlert>> checkAlerts(@PathVariable String analyzerId) {
        List<TrendAlert> alerts = trendAnalysisService.checkAlerts(analyzerId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/analyzers/{analyzerId}/analytics")
    @Operation(summary = "Get trend analytics")
    public ResponseEntity<TrendAnalytics> getAnalytics(@PathVariable String analyzerId) {
        TrendAnalytics analytics = trendAnalysisService.getAnalytics(analyzerId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analyzers/{analyzerId}")
    @Operation(summary = "Get trend analyzer")
    public ResponseEntity<TrendAnalyzer> getAnalyzer(@PathVariable String analyzerId) {
        TrendAnalyzer analyzer = trendAnalysisService.getAnalyzer(analyzerId);
        return ResponseEntity.ok(analyzer);
    }

    @GetMapping("/analyzers")
    @Operation(summary = "Get all trend analyzers")
    public ResponseEntity<List<TrendAnalyzer>> getAnalyzers() {
        List<TrendAnalyzer> analyzers = trendAnalysisService.getAnalyzers();
        return ResponseEntity.ok(analyzers);
    }

    @PutMapping("/analyzers/{analyzerId}")
    @Operation(summary = "Update trend analyzer")
    public ResponseEntity<Void> updateAnalyzer(
            @PathVariable String analyzerId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestBody Map<String, Object> configuration) {
        
        trendAnalysisService.updateAnalyzer(analyzerId, name, description, configuration);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/analyzers/{analyzerId}")
    @Operation(summary = "Delete trend analyzer")
    public ResponseEntity<Void> deleteAnalyzer(@PathVariable String analyzerId) {
        trendAnalysisService.deleteAnalyzer(analyzerId);
        return ResponseEntity.ok().build();
    }
}


