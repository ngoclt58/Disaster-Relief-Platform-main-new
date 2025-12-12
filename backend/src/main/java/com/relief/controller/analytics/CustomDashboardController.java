package com.relief.controller.analytics;

import com.relief.service.analytics.CustomDashboardService;
import com.relief.service.analytics.CustomDashboardService.Dashboard;
import com.relief.service.analytics.CustomDashboardService.DashboardWidget;
import com.relief.service.analytics.CustomDashboardService.WidgetData;
import com.relief.service.analytics.CustomDashboardService.DashboardTemplate;
import com.relief.service.analytics.CustomDashboardService.DashboardAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Custom dashboard controller for drag-and-drop dashboard builder
 */
@RestController
@RequestMapping("/analytics/dashboards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Custom Dashboards", description = "Drag-and-drop dashboard builder for different user roles")
public class CustomDashboardController {

    private final CustomDashboardService customDashboardService;

    @PostMapping
    @Operation(summary = "Create custom dashboard")
    public ResponseEntity<Dashboard> createDashboard(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String userId,
            @RequestParam String userRole,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestBody Map<String, Object> layout) {
        
        Dashboard dashboard = customDashboardService.createDashboard(name, description, userId, userRole, isPublic, layout);
        return ResponseEntity.ok(dashboard);
    }

    @PutMapping("/{dashboardId}")
    @Operation(summary = "Update custom dashboard")
    public ResponseEntity<Dashboard> updateDashboard(
            @PathVariable String dashboardId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestBody Map<String, Object> layout,
            @RequestParam(defaultValue = "false") boolean isPublic) {
        
        Dashboard dashboard = customDashboardService.updateDashboard(dashboardId, name, description, layout, isPublic);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{dashboardId}")
    @Operation(summary = "Get custom dashboard")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable String dashboardId) {
        Dashboard dashboard = customDashboardService.getDashboard(dashboardId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping
    @Operation(summary = "Get user dashboards")
    public ResponseEntity<List<Dashboard>> getUserDashboards(
            @RequestParam String userId,
            @RequestParam String userRole) {
        
        List<Dashboard> dashboards = customDashboardService.getUserDashboards(userId, userRole);
        return ResponseEntity.ok(dashboards);
    }

    @GetMapping("/public")
    @Operation(summary = "Get public dashboards")
    public ResponseEntity<List<Dashboard>> getPublicDashboards() {
        List<Dashboard> dashboards = customDashboardService.getPublicDashboards();
        return ResponseEntity.ok(dashboards);
    }

    @PostMapping("/{dashboardId}/widgets")
    @Operation(summary = "Add widget to dashboard")
    public ResponseEntity<DashboardWidget> addWidget(
            @PathVariable String dashboardId,
            @RequestParam String widgetType,
            @RequestParam String title,
            @RequestBody Map<String, Object> configuration,
            @RequestBody Map<String, Object> position) {
        
        DashboardWidget widget = customDashboardService.addWidget(dashboardId, widgetType, title, configuration, position);
        return ResponseEntity.ok(widget);
    }

    @PutMapping("/widgets/{widgetId}")
    @Operation(summary = "Update dashboard widget")
    public ResponseEntity<DashboardWidget> updateWidget(
            @PathVariable String widgetId,
            @RequestParam String title,
            @RequestBody Map<String, Object> configuration,
            @RequestBody Map<String, Object> position,
            @RequestParam(defaultValue = "true") boolean isVisible) {
        
        DashboardWidget widget = customDashboardService.updateWidget(widgetId, title, configuration, position, isVisible);
        return ResponseEntity.ok(widget);
    }

    @GetMapping("/{dashboardId}/widgets")
    @Operation(summary = "Get dashboard widgets")
    public ResponseEntity<List<DashboardWidget>> getDashboardWidgets(@PathVariable String dashboardId) {
        List<DashboardWidget> widgets = customDashboardService.getDashboardWidgets(dashboardId);
        return ResponseEntity.ok(widgets);
    }

    @GetMapping("/widgets/{widgetId}")
    @Operation(summary = "Get widget")
    public ResponseEntity<DashboardWidget> getWidget(@PathVariable String widgetId) {
        DashboardWidget widget = customDashboardService.getWidget(widgetId);
        return ResponseEntity.ok(widget);
    }

    @GetMapping("/widgets/{widgetId}/data")
    @Operation(summary = "Get widget data")
    public ResponseEntity<WidgetData> getWidgetData(
            @PathVariable String widgetId,
            @RequestBody Map<String, Object> filters) {
        
        WidgetData data = customDashboardService.getWidgetData(widgetId, filters);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/widgets/{widgetId}")
    @Operation(summary = "Remove widget from dashboard")
    public ResponseEntity<Void> removeWidget(@PathVariable String widgetId) {
        customDashboardService.removeWidget(widgetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/templates")
    @Operation(summary = "Create dashboard template")
    public ResponseEntity<DashboardTemplate> createTemplate(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> template,
            @RequestParam String createdBy) {
        
        DashboardTemplate templateObj = customDashboardService.createTemplate(name, description, userRole, template, createdBy);
        return ResponseEntity.ok(templateObj);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get dashboard templates")
    public ResponseEntity<List<DashboardTemplate>> getTemplates(@RequestParam String userRole) {
        List<DashboardTemplate> templates = customDashboardService.getTemplates(userRole);
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/{dashboardId}/clone")
    @Operation(summary = "Clone dashboard")
    public ResponseEntity<Dashboard> cloneDashboard(
            @PathVariable String dashboardId,
            @RequestParam String newName,
            @RequestParam String userId) {
        
        Dashboard cloned = customDashboardService.cloneDashboard(dashboardId, newName, userId);
        return ResponseEntity.ok(cloned);
    }

    @PostMapping("/{dashboardId}/share")
    @Operation(summary = "Share dashboard")
    public ResponseEntity<Void> shareDashboard(
            @PathVariable String dashboardId,
            @RequestParam String userId,
            @RequestParam String permission) {
        
        customDashboardService.shareDashboard(dashboardId, userId, permission);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{dashboardId}/analytics")
    @Operation(summary = "Get dashboard analytics")
    public ResponseEntity<DashboardAnalytics> getDashboardAnalytics(@PathVariable String dashboardId) {
        DashboardAnalytics analytics = customDashboardService.getDashboardAnalytics(dashboardId);
        return ResponseEntity.ok(analytics);
    }

    @DeleteMapping("/{dashboardId}")
    @Operation(summary = "Delete dashboard")
    public ResponseEntity<Void> deleteDashboard(@PathVariable String dashboardId) {
        customDashboardService.deleteDashboard(dashboardId);
        return ResponseEntity.ok().build();
    }
}


