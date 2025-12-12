package com.relief.controller.financial;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.financial.FinancialReportingService;
import com.relief.service.financial.FinancialReportingService.FinancialReport;
import com.relief.service.financial.FinancialReportingService.ReportType;
import com.relief.service.financial.FinancialReportingService.ReportFormat;
import com.relief.service.financial.FinancialReportingService.ReportSchedule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Financial reporting controller
 */
@RestController
@RequestMapping("/financial-reporting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Financial Reporting", description = "Financial reporting and analytics APIs")
public class FinancialReportingController {

    private final FinancialReportingService financialReportingService;
    private final UserRepository userRepository;

    private UUID getUserIdFromPrincipal(UserDetails principal) {
        String username = principal.getUsername();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
            return user.getId();
        }
    }

    @PostMapping("/reports")
    @Operation(summary = "Generate financial report")
    public ResponseEntity<FinancialReport> generateReport(
            @RequestBody GenerateReportRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        FinancialReport report = financialReportingService.generateReport(
            request.getType(),
            request.getFormat(),
            request.getStartDate(),
            request.getEndDate(),
            request.getFilters(),
            userId
        );
        
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "Get financial report")
    public ResponseEntity<FinancialReport> getReport(@PathVariable String reportId) {
        FinancialReport report = financialReportingService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports")
    @Operation(summary = "Get financial reports")
    public ResponseEntity<List<FinancialReport>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<FinancialReport> reports = financialReportingService.getReports(
            type, startDate, endDate, limit
        );
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reports/{reportId}/schedule")
    @Operation(summary = "Schedule recurring report")
    public ResponseEntity<Map<String, Object>> scheduleReport(
            @PathVariable String reportId,
            @RequestBody ScheduleReportRequest request) {
        
        Map<String, Object> schedule = financialReportingService.scheduleReport(
            reportId,
            request.getSchedule(),
            request.getRecipients(),
            request.isEnabled()
        );
        
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/reports/{reportId}/schedule")
    @Operation(summary = "Cancel scheduled report")
    public ResponseEntity<Map<String, Object>> cancelScheduledReport(@PathVariable String reportId) {
        Map<String, Object> result = financialReportingService.cancelScheduledReport(reportId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reports/{reportId}/download")
    @Operation(summary = "Download report file")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable String reportId,
            @RequestParam ReportFormat format) {
        
        byte[] fileContent = financialReportingService.downloadReport(reportId, format);
        return ResponseEntity.ok(fileContent);
    }

    @PostMapping("/reports/{reportId}/share")
    @Operation(summary = "Share report")
    public ResponseEntity<Map<String, Object>> shareReport(
            @PathVariable String reportId,
            @RequestBody ShareReportRequest request) {
        
        Map<String, Object> result = financialReportingService.shareReport(
            reportId,
            request.getRecipients(),
            request.getExpiryDate(),
            request.getPermissions()
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reports/{reportId}/analytics")
    @Operation(summary = "Get report analytics")
    public ResponseEntity<Map<String, Object>> getReportAnalytics(@PathVariable String reportId) {
        Map<String, Object> analytics = financialReportingService.getReportAnalytics(reportId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get report templates")
    public ResponseEntity<List<Map<String, Object>>> getReportTemplates() {
        List<Map<String, Object>> templates = financialReportingService.getReportTemplates();
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/templates")
    @Operation(summary = "Create report template")
    public ResponseEntity<Map<String, Object>> createReportTemplate(
            @RequestBody CreateTemplateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        Map<String, Object> template = financialReportingService.createReportTemplate(
            request.getName(),
            request.getDescription(),
            request.getType(),
            request.getConfiguration(),
            userId
        );
        
        return ResponseEntity.ok(template);
    }

    @PutMapping("/templates/{templateId}")
    @Operation(summary = "Update report template")
    public ResponseEntity<Map<String, Object>> updateReportTemplate(
            @PathVariable String templateId,
            @RequestBody UpdateTemplateRequest request) {
        
        Map<String, Object> template = financialReportingService.updateReportTemplate(
            templateId,
            request.getName(),
            request.getDescription(),
            request.getConfiguration()
        );
        
        return ResponseEntity.ok(template);
    }

    @DeleteMapping("/templates/{templateId}")
    @Operation(summary = "Delete report template")
    public ResponseEntity<Map<String, Object>> deleteReportTemplate(@PathVariable String templateId) {
        Map<String, Object> result = financialReportingService.deleteReportTemplate(templateId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get financial dashboard data")
    public ResponseEntity<Map<String, Object>> getFinancialDashboard(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> dashboard = financialReportingService.getFinancialDashboard(
            startDate, endDate
        );
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get financial KPIs")
    public ResponseEntity<Map<String, Object>> getFinancialKPIs(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> kpis = financialReportingService.getFinancialKPIs(
            startDate, endDate
        );
        
        return ResponseEntity.ok(kpis);
    }

    // Request DTOs
    public static class GenerateReportRequest {
        private ReportType type;
        private ReportFormat format;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Map<String, Object> filters;

        // Getters and setters
        public ReportType getType() { return type; }
        public void setType(ReportType type) { this.type = type; }

        public ReportFormat getFormat() { return format; }
        public void setFormat(ReportFormat format) { this.format = format; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    }

    public static class ScheduleReportRequest {
        private ReportSchedule schedule;
        private List<String> recipients;
        private boolean enabled;

        // Getters and setters
        public ReportSchedule getSchedule() { return schedule; }
        public void setSchedule(ReportSchedule schedule) { this.schedule = schedule; }

        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class ShareReportRequest {
        private List<String> recipients;
        private LocalDateTime expiryDate;
        private Map<String, Object> permissions;

        // Getters and setters
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }

        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

        public Map<String, Object> getPermissions() { return permissions; }
        public void setPermissions(Map<String, Object> permissions) { this.permissions = permissions; }
    }

    public static class CreateTemplateRequest {
        private String name;
        private String description;
        private ReportType type;
        private Map<String, Object> configuration;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ReportType getType() { return type; }
        public void setType(ReportType type) { this.type = type; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    }

    public static class UpdateTemplateRequest {
        private String name;
        private String description;
        private Map<String, Object> configuration;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    }
}


