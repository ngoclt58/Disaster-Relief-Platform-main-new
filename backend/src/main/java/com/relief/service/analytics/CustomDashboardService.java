package com.relief.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Custom dashboard service for drag-and-drop dashboard builder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomDashboardService {

    public Dashboard createDashboard(String name, String description, String userId, String userRole, 
                                  boolean isPublic, Map<String, Object> layout) {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(UUID.randomUUID().toString());
        dashboard.setName(name);
        dashboard.setDescription(description);
        dashboard.setUserId(userId);
        dashboard.setUserRole(userRole);
        dashboard.setIsPublic(isPublic);
        dashboard.setLayout(layout);
        dashboard.setCreatedAt(LocalDateTime.now());
        dashboard.setUpdatedAt(LocalDateTime.now());
        dashboard.setIsActive(true);
        
        log.info("Created custom dashboard: {} for user: {}", dashboard.getId(), userId);
        return dashboard;
    }

    public Dashboard updateDashboard(String dashboardId, String name, String description, 
                                  Map<String, Object> layout, boolean isPublic) {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(dashboardId);
        dashboard.setName(name);
        dashboard.setDescription(description);
        dashboard.setLayout(layout);
        dashboard.setIsPublic(isPublic);
        dashboard.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updated dashboard: {}", dashboardId);
        return dashboard;
    }

    public DashboardWidget addWidget(String dashboardId, String widgetType, String title, 
                                  Map<String, Object> configuration, Map<String, Object> position) {
        DashboardWidget widget = new DashboardWidget();
        widget.setId(UUID.randomUUID().toString());
        widget.setDashboardId(dashboardId);
        widget.setWidgetType(widgetType);
        widget.setTitle(title);
        widget.setConfiguration(configuration);
        widget.setPosition(position);
        widget.setCreatedAt(LocalDateTime.now());
        widget.setIsVisible(true);
        
        log.info("Added widget to dashboard: {} - type: {}", dashboardId, widgetType);
        return widget;
    }

    public DashboardWidget updateWidget(String widgetId, String title, Map<String, Object> configuration, 
                                     Map<String, Object> position, boolean isVisible) {
        DashboardWidget widget = new DashboardWidget();
        widget.setId(widgetId);
        widget.setTitle(title);
        widget.setConfiguration(configuration);
        widget.setPosition(position);
        widget.setIsVisible(isVisible);
        widget.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updated widget: {}", widgetId);
        return widget;
    }

    public void removeWidget(String widgetId) {
        log.info("Removed widget: {}", widgetId);
    }

    public Dashboard getDashboard(String dashboardId) {
        // Implementation for getting dashboard
        Dashboard dashboard = new Dashboard();
        dashboard.setId(dashboardId);
        dashboard.setName("Operations Overview");
        dashboard.setDescription("Key KPIs for current relief operations");
        dashboard.setUserId("user-123");
        dashboard.setUserRole("ADMIN");
        dashboard.setIsPublic(true);
        Map<String, Object> layout = new HashMap<>();
        layout.put("widgets", Arrays.asList(
            Map.of("id", "w-requests", "type", "kpi", "title", "Active Requests"),
            Map.of("id", "w-responseTime", "type", "kpi", "title", "Avg Response Time"),
            Map.of("id", "w-trends", "type", "chart", "title", "Needs Trends (7 days)")
        ));
        dashboard.setLayout(layout);
        dashboard.setCreatedAt(LocalDateTime.now());
        dashboard.setUpdatedAt(LocalDateTime.now());
        dashboard.setIsActive(true);
        
        return dashboard;
    }

    public List<Dashboard> getUserDashboards(String userId, String userRole) {
        // Return a small set of demo dashboards for the current user
        Dashboard main = getDashboard("dashboard-main");
        main.setUserId(userId);
        main.setUserRole(userRole);

        Dashboard finance = new Dashboard();
        finance.setId("dashboard-finance");
        finance.setName("Funding & ROI");
        finance.setDescription("Donations, spending, and ROI");
        finance.setUserId(userId);
        finance.setUserRole(userRole);
        finance.setIsPublic(false);
        finance.setLayout(Map.of(
            "widgets", Arrays.asList(
                Map.of("id", "w-donations", "type", "kpi", "title", "Total Donations"),
                Map.of("id", "w-roi", "type", "chart", "title", "ROI by Campaign")
            )
        ));
        finance.setCreatedAt(LocalDateTime.now().minusDays(5));
        finance.setUpdatedAt(LocalDateTime.now().minusDays(1));
        finance.setIsActive(true);

        return Arrays.asList(main, finance);
    }

    public List<Dashboard> getPublicDashboards() {
        Dashboard overview = getDashboard("dashboard-public-overview");
        overview.setUserId("public");
        overview.setUserRole("VIEWER");
        return List.of(overview);
    }

    public List<DashboardWidget> getDashboardWidgets(String dashboardId) {
        List<DashboardWidget> widgets = new ArrayList<>();

        if ("dashboard-finance".equals(dashboardId)) {
          DashboardWidget donations = new DashboardWidget();
          donations.setId("w-donations");
          donations.setDashboardId(dashboardId);
          donations.setWidgetType("kpi");
          donations.setTitle("Total Donations");
          donations.setConfiguration(Map.of("metric", "total_donations", "currency", "USD"));
          donations.setPosition(Map.of("row", 0, "col", 0, "width", 3, "height", 1));
          donations.setCreatedAt(LocalDateTime.now().minusDays(5));
          donations.setIsVisible(true);
          widgets.add(donations);

          DashboardWidget roi = new DashboardWidget();
          roi.setId("w-roi");
          roi.setDashboardId(dashboardId);
          roi.setWidgetType("chart");
          roi.setTitle("ROI by Campaign");
          roi.setConfiguration(Map.of("type", "bar", "metric", "roi_percentage"));
          roi.setPosition(Map.of("row", 1, "col", 0, "width", 6, "height", 3));
          roi.setCreatedAt(LocalDateTime.now().minusDays(5));
          roi.setIsVisible(true);
          widgets.add(roi);
        } else {
          DashboardWidget requests = new DashboardWidget();
          requests.setId("w-requests");
          requests.setDashboardId(dashboardId);
          requests.setWidgetType("kpi");
          requests.setTitle("Active Requests");
          requests.setConfiguration(Map.of("metric", "active_requests"));
          requests.setPosition(Map.of("row", 0, "col", 0, "width", 3, "height", 1));
          requests.setCreatedAt(LocalDateTime.now().minusDays(10));
          requests.setIsVisible(true);
          widgets.add(requests);

          DashboardWidget responseTime = new DashboardWidget();
          responseTime.setId("w-responseTime");
          responseTime.setDashboardId(dashboardId);
          responseTime.setWidgetType("kpi");
          responseTime.setTitle("Avg Response Time");
          responseTime.setConfiguration(Map.of("metric", "avg_response_time_hours"));
          responseTime.setPosition(Map.of("row", 0, "col", 3, "width", 3, "height", 1));
          responseTime.setCreatedAt(LocalDateTime.now().minusDays(9));
          responseTime.setIsVisible(true);
          widgets.add(responseTime);
        }

        return widgets;
    }

    public DashboardWidget getWidget(String widgetId) {
        // Implementation for getting widget
        DashboardWidget widget = new DashboardWidget();
        widget.setId(widgetId);
        widget.setDashboardId("dashboard-123");
        widget.setWidgetType("chart");
        widget.setTitle("Sample Widget");
        widget.setConfiguration(Collections.emptyMap());
        widget.setPosition(Collections.emptyMap());
        widget.setCreatedAt(LocalDateTime.now());
        widget.setIsVisible(true);
        
        return widget;
    }

    public WidgetData getWidgetData(String widgetId, Map<String, Object> filters) {
        WidgetData data = new WidgetData();
        data.setWidgetId(widgetId);
        data.setData(Collections.emptyList());
        data.setMetadata(Collections.emptyMap());
        data.setGeneratedAt(LocalDateTime.now());
        
        log.info("Generated widget data for: {}", widgetId);
        return data;
    }

    public DashboardTemplate createTemplate(String name, String description, String userRole, 
                                         Map<String, Object> template, String createdBy) {
        DashboardTemplate templateObj = new DashboardTemplate();
        templateObj.setId(UUID.randomUUID().toString());
        templateObj.setName(name);
        templateObj.setDescription(description);
        templateObj.setUserRole(userRole);
        templateObj.setTemplate(template);
        templateObj.setCreatedBy(createdBy);
        templateObj.setCreatedAt(LocalDateTime.now());
        templateObj.setIsPublic(false);
        
        log.info("Created dashboard template: {}", templateObj.getId());
        return templateObj;
    }

    public List<DashboardTemplate> getTemplates(String userRole) {
        DashboardTemplate opsTemplate = new DashboardTemplate();
        opsTemplate.setId("template-ops");
        opsTemplate.setName("Operations Template");
        opsTemplate.setDescription("Layout for operations KPIs and trends");
        opsTemplate.setUserRole(userRole);
        opsTemplate.setTemplate(Map.of(
            "sections", List.of("KPIs", "Trends", "Maps")
        ));
        opsTemplate.setCreatedBy("system");
        opsTemplate.setCreatedAt(LocalDateTime.now().minusDays(30));
        opsTemplate.setIsPublic(true);

        return List.of(opsTemplate);
    }

    public Dashboard cloneDashboard(String dashboardId, String newName, String userId) {
        Dashboard original = getDashboard(dashboardId);
        Dashboard cloned = new Dashboard();
        cloned.setId(UUID.randomUUID().toString());
        cloned.setName(newName);
        cloned.setDescription(original.getDescription() + " (Copy)");
        cloned.setUserId(userId);
        cloned.setUserRole(original.getUserRole());
        cloned.setIsPublic(false);
        cloned.setLayout(original.getLayout());
        cloned.setCreatedAt(LocalDateTime.now());
        cloned.setUpdatedAt(LocalDateTime.now());
        cloned.setIsActive(true);
        
        log.info("Cloned dashboard: {} to {}", dashboardId, cloned.getId());
        return cloned;
    }

    public void shareDashboard(String dashboardId, String userId, String permission) {
        log.info("Shared dashboard: {} with user: {} permission: {}", dashboardId, userId, permission);
    }

    public void deleteDashboard(String dashboardId) {
        log.info("Deleted dashboard: {}", dashboardId);
    }

    public DashboardAnalytics getDashboardAnalytics(String dashboardId) {
        DashboardAnalytics analytics = new DashboardAnalytics();
        analytics.setDashboardId(dashboardId);
        analytics.setViewCount(124);
        analytics.setUniqueViewers(37);
        analytics.setLastViewed(LocalDateTime.now().minusHours(2));
        analytics.setPopularWidgets(Arrays.asList("w-requests", "w-trends"));
        analytics.setUserEngagement(Map.of(
            "clicks", 420,
            "shares", 12,
            "downloads", 7
        ));
        
        return analytics;
    }

    // Data classes
    public static class Dashboard {
        private String id;
        private String name;
        private String description;
        private String userId;
        private String userRole;
        private boolean isPublic;
        private Map<String, Object> layout;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
        public void setIsPublic(boolean aPublic) { isPublic = aPublic; }

        public Map<String, Object> getLayout() { return layout; }
        public void setLayout(Map<String, Object> layout) { this.layout = layout; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public void setIsActive(boolean active) { isActive = active; }
    }

    public static class DashboardWidget {
        private String id;
        private String dashboardId;
        private String widgetType;
        private String title;
        private Map<String, Object> configuration;
        private Map<String, Object> position;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isVisible;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDashboardId() { return dashboardId; }
        public void setDashboardId(String dashboardId) { this.dashboardId = dashboardId; }

        public String getWidgetType() { return widgetType; }
        public void setWidgetType(String widgetType) { this.widgetType = widgetType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

        public Map<String, Object> getPosition() { return position; }
        public void setPosition(Map<String, Object> position) { this.position = position; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isVisible() { return isVisible; }
        public void setVisible(boolean visible) { isVisible = visible; }
        public void setIsVisible(boolean visible) { isVisible = visible; }
    }

    public static class WidgetData {
        private String widgetId;
        private List<Object> data;
        private Map<String, Object> metadata;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getWidgetId() { return widgetId; }
        public void setWidgetId(String widgetId) { this.widgetId = widgetId; }

        public List<Object> getData() { return data; }
        public void setData(List<Object> data) { this.data = data; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class DashboardTemplate {
        private String id;
        private String name;
        private String description;
        private String userRole;
        private Map<String, Object> template;
        private String createdBy;
        private LocalDateTime createdAt;
        private boolean isPublic;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }

        public Map<String, Object> getTemplate() { return template; }
        public void setTemplate(Map<String, Object> template) { this.template = template; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
        public void setIsPublic(boolean aPublic) { isPublic = aPublic; }
    }

    public static class DashboardAnalytics {
        private String dashboardId;
        private int viewCount;
        private int uniqueViewers;
        private LocalDateTime lastViewed;
        private List<String> popularWidgets;
        private Map<String, Object> userEngagement;

        // Getters and setters
        public String getDashboardId() { return dashboardId; }
        public void setDashboardId(String dashboardId) { this.dashboardId = dashboardId; }

        public int getViewCount() { return viewCount; }
        public void setViewCount(int viewCount) { this.viewCount = viewCount; }

        public int getUniqueViewers() { return uniqueViewers; }
        public void setUniqueViewers(int uniqueViewers) { this.uniqueViewers = uniqueViewers; }

        public LocalDateTime getLastViewed() { return lastViewed; }
        public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }

        public List<String> getPopularWidgets() { return popularWidgets; }
        public void setPopularWidgets(List<String> popularWidgets) { this.popularWidgets = popularWidgets; }

        public Map<String, Object> getUserEngagement() { return userEngagement; }
        public void setUserEngagement(Map<String, Object> userEngagement) { this.userEngagement = userEngagement; }
    }
}


