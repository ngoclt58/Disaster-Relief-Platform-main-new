package com.relief.controller.training;

import com.relief.service.training.CertificationTrackingService;
import com.relief.service.training.CertificationTrackingService.Certification;
import com.relief.service.training.CertificationTrackingService.UserCertification;
import com.relief.service.training.CertificationTrackingService.CertificationRequirement;
import com.relief.service.training.CertificationTrackingService.CertificationProgress;
import com.relief.service.training.CertificationTrackingService.CertificationReport;
import com.relief.service.training.CertificationTrackingService.CertificationAlert;
import com.relief.service.training.CertificationTrackingService.CertificationAnalytics;
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
 * Certification tracking controller
 */
@RestController
@RequestMapping("/certification-tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Certification Tracking", description = "Track training and certification requirements")
public class CertificationTrackingController {

    private final CertificationTrackingService certificationTrackingService;

    @PostMapping("/certifications")
    @Operation(summary = "Create a certification")
    public ResponseEntity<Certification> createCertification(
            @RequestBody CreateCertificationRequest request) {
        
        Certification certification = certificationTrackingService.createCertification(
            request.getName(),
            request.getDescription(),
            request.getCategory(),
            request.getIssuingOrganization(),
            request.getValidityMonths(),
            request.getRequiredSkills(),
            request.getRequirements()
        );
        
        return ResponseEntity.ok(certification);
    }

    @PostMapping("/user-certifications")
    @Operation(summary = "Assign certification to user")
    public ResponseEntity<UserCertification> assignCertification(
            @RequestBody AssignCertificationRequest request) {
        
        UserCertification userCert = certificationTrackingService.assignCertification(
            request.getUserId(),
            request.getCertificationId(),
            request.getAssignedDate(),
            request.getExpiryDate()
        );
        
        return ResponseEntity.ok(userCert);
    }

    @PostMapping("/user-certifications/{userCertificationId}/complete")
    @Operation(summary = "Complete certification")
    public ResponseEntity<UserCertification> completeCertification(
            @PathVariable String userCertificationId,
            @RequestBody CompleteCertificationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String verifiedBy = principal.getUsername();
        
        UserCertification userCert = certificationTrackingService.completeCertification(
            userCertificationId,
            request.getCompletionMethod(),
            request.getCompletionNotes(),
            verifiedBy
        );
        
        return ResponseEntity.ok(userCert);
    }

    @PostMapping("/user-certifications/{userCertificationId}/renew")
    @Operation(summary = "Renew certification")
    public ResponseEntity<UserCertification> renewCertification(
            @PathVariable String userCertificationId,
            @RequestBody RenewCertificationRequest request) {
        
        UserCertification userCert = certificationTrackingService.renewCertification(
            userCertificationId,
            request.getNewExpiryDate(),
            request.getRenewalNotes()
        );
        
        return ResponseEntity.ok(userCert);
    }

    @PostMapping("/user-certifications/{userCertificationId}/revoke")
    @Operation(summary = "Revoke certification")
    public ResponseEntity<UserCertification> revokeCertification(
            @PathVariable String userCertificationId,
            @RequestBody RevokeCertificationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String revokedBy = principal.getUsername();
        
        UserCertification userCert = certificationTrackingService.revokeCertification(
            userCertificationId,
            request.getReason(),
            revokedBy
        );
        
        return ResponseEntity.ok(userCert);
    }

    @GetMapping("/user-certifications")
    @Operation(summary = "Get user certifications")
    public ResponseEntity<List<UserCertification>> getUserCertifications(
            @RequestParam String userId,
            @RequestParam(required = false) String status) {
        
        List<UserCertification> certifications = certificationTrackingService.getUserCertifications(
            userId,
            status != null ? CertificationTrackingService.CertificationStatus.valueOf(status) : null
        );
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications/expiring")
    @Operation(summary = "Get expiring certifications")
    public ResponseEntity<List<UserCertification>> getExpiringCertifications(
            @RequestParam(defaultValue = "30") int daysBeforeExpiry) {
        
        List<UserCertification> certifications = certificationTrackingService.getExpiringCertifications(daysBeforeExpiry);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications/expired")
    @Operation(summary = "Get expired certifications")
    public ResponseEntity<List<UserCertification>> getExpiredCertifications() {
        List<UserCertification> certifications = certificationTrackingService.getExpiredCertifications();
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications")
    @Operation(summary = "Get certifications")
    public ResponseEntity<List<Certification>> getCertifications(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "true") boolean isActive) {
        
        List<Certification> certifications = certificationTrackingService.getCertifications(category, isActive);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/certifications/{certificationId}")
    @Operation(summary = "Get certification details")
    public ResponseEntity<Certification> getCertification(@PathVariable String certificationId) {
        Certification certification = certificationTrackingService.getCertification(certificationId);
        return ResponseEntity.ok(certification);
    }

    @GetMapping("/user-certifications/{userCertificationId}")
    @Operation(summary = "Get user certification details")
    public ResponseEntity<UserCertification> getUserCertification(@PathVariable String userCertificationId) {
        UserCertification userCert = certificationTrackingService.getUserCertification(userCertificationId);
        return ResponseEntity.ok(userCert);
    }

    @PostMapping("/requirements")
    @Operation(summary = "Create certification requirement")
    public ResponseEntity<CertificationRequirement> createRequirement(
            @RequestBody CreateRequirementRequest request) {
        
        CertificationRequirement requirement = certificationTrackingService.createRequirement(
            request.getCertificationId(),
            request.getRequirementType(),
            request.getDescription(),
            request.isMandatory(),
            request.getCriteria()
        );
        
        return ResponseEntity.ok(requirement);
    }

    @GetMapping("/certifications/{certificationId}/requirements")
    @Operation(summary = "Get certification requirements")
    public ResponseEntity<List<CertificationRequirement>> getCertificationRequirements(
            @PathVariable String certificationId) {
        
        List<CertificationRequirement> requirements = certificationTrackingService.getCertificationRequirements(certificationId);
        return ResponseEntity.ok(requirements);
    }

    @PostMapping("/progress")
    @Operation(summary = "Track certification progress")
    public ResponseEntity<CertificationProgress> trackProgress(
            @RequestBody TrackProgressRequest request) {
        
        CertificationProgress progress = certificationTrackingService.trackProgress(
            request.getUserCertificationId(),
            request.getRequirementId(),
            request.getStatus(),
            request.getNotes()
        );
        
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user-certifications/{userCertificationId}/progress")
    @Operation(summary = "Get certification progress")
    public ResponseEntity<List<CertificationProgress>> getCertificationProgress(
            @PathVariable String userCertificationId) {
        
        List<CertificationProgress> progress = certificationTrackingService.getCertificationProgress(userCertificationId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/reports")
    @Operation(summary = "Generate certification report")
    public ResponseEntity<CertificationReport> generateReport(
            @RequestBody GenerateReportRequest request) {
        
        CertificationReport report = certificationTrackingService.generateReport(
            request.getUserId(),
            request.getCertificationId(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        return ResponseEntity.ok(report);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get certification alerts")
    public ResponseEntity<List<CertificationAlert>> getCertificationAlerts(
            @RequestParam String userId) {
        
        List<CertificationAlert> alerts = certificationTrackingService.getCertificationAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get certification analytics")
    public ResponseEntity<CertificationAnalytics> getCertificationAnalytics(
            @RequestParam String organizationId) {
        
        CertificationAnalytics analytics = certificationTrackingService.getCertificationAnalytics(organizationId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/notifications/expiry")
    @Operation(summary = "Send expiry notifications")
    public ResponseEntity<Void> sendExpiryNotifications() {
        certificationTrackingService.sendExpiryNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/renewal")
    @Operation(summary = "Send renewal reminders")
    public ResponseEntity<Void> sendRenewalReminders() {
        certificationTrackingService.sendRenewalReminders();
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    public static class CreateCertificationRequest {
        private String name;
        private String description;
        private String category;
        private String issuingOrganization;
        private int validityMonths;
        private List<String> requiredSkills;
        private Map<String, Object> requirements;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getIssuingOrganization() { return issuingOrganization; }
        public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

        public int getValidityMonths() { return validityMonths; }
        public void setValidityMonths(int validityMonths) { this.validityMonths = validityMonths; }

        public List<String> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

        public Map<String, Object> getRequirements() { return requirements; }
        public void setRequirements(Map<String, Object> requirements) { this.requirements = requirements; }
    }

    public static class AssignCertificationRequest {
        private String userId;
        private String certificationId;
        private LocalDateTime assignedDate;
        private LocalDateTime expiryDate;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCertificationId() { return certificationId; }
        public void setCertificationId(String certificationId) { this.certificationId = certificationId; }

        public LocalDateTime getAssignedDate() { return assignedDate; }
        public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    }

    public static class CompleteCertificationRequest {
        private String completionMethod;
        private String completionNotes;

        // Getters and setters
        public String getCompletionMethod() { return completionMethod; }
        public void setCompletionMethod(String completionMethod) { this.completionMethod = completionMethod; }

        public String getCompletionNotes() { return completionNotes; }
        public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    }

    public static class RenewCertificationRequest {
        private LocalDateTime newExpiryDate;
        private String renewalNotes;

        // Getters and setters
        public LocalDateTime getNewExpiryDate() { return newExpiryDate; }
        public void setNewExpiryDate(LocalDateTime newExpiryDate) { this.newExpiryDate = newExpiryDate; }

        public String getRenewalNotes() { return renewalNotes; }
        public void setRenewalNotes(String renewalNotes) { this.renewalNotes = renewalNotes; }
    }

    public static class RevokeCertificationRequest {
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CreateRequirementRequest {
        private String certificationId;
        private String requirementType;
        private String description;
        private boolean isMandatory;
        private Map<String, Object> criteria;

        // Getters and setters
        public String getCertificationId() { return certificationId; }
        public void setCertificationId(String certificationId) { this.certificationId = certificationId; }

        public String getRequirementType() { return requirementType; }
        public void setRequirementType(String requirementType) { this.requirementType = requirementType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isMandatory() { return isMandatory; }
        public void setMandatory(boolean mandatory) { isMandatory = mandatory; }

        public Map<String, Object> getCriteria() { return criteria; }
        public void setCriteria(Map<String, Object> criteria) { this.criteria = criteria; }
    }

    public static class TrackProgressRequest {
        private String userCertificationId;
        private String requirementId;
        private String status;
        private String notes;

        // Getters and setters
        public String getUserCertificationId() { return userCertificationId; }
        public void setUserCertificationId(String userCertificationId) { this.userCertificationId = userCertificationId; }

        public String getRequirementId() { return requirementId; }
        public void setRequirementId(String requirementId) { this.requirementId = requirementId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class GenerateReportRequest {
        private String userId;
        private String certificationId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCertificationId() { return certificationId; }
        public void setCertificationId(String certificationId) { this.certificationId = certificationId; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }
}


