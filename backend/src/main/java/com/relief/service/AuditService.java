package com.relief.service;

import com.relief.entity.AuditLog;
import com.relief.repository.AuditLogRepository;
import com.relief.security.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private RbacService rbacService;
    
    public void logAdminAction(String action, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setUserId(rbacService.getCurrentUser() != null ? rbacService.getCurrentUser().getId().toString() : "system");
        auditLog.setUserRole(rbacService.getCurrentUserRole() != null ? rbacService.getCurrentUserRole().getCode() : "SYSTEM");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress(getCurrentIpAddress());
        auditLog.setUserAgent(getCurrentUserAgent());
        
        auditLogRepository.save(auditLog);
    }
    
    public void logUserAction(String action, String description, String targetUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setUserId(rbacService.getCurrentUser() != null ? rbacService.getCurrentUser().getId().toString() : "system");
        auditLog.setUserRole(rbacService.getCurrentUserRole() != null ? rbacService.getCurrentUserRole().getCode() : "SYSTEM");
        auditLog.setTargetUserId(targetUserId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress(getCurrentIpAddress());
        auditLog.setUserAgent(getCurrentUserAgent());
        
        auditLogRepository.save(auditLog);
    }
    
    public void logSystemEvent(String action, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setUserId("system");
        auditLog.setUserRole("SYSTEM");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress("127.0.0.1");
        auditLog.setUserAgent("System");
        
        auditLogRepository.save(auditLog);
    }
    
    public Page<Map<String, Object>> getAuditLogs(String action, String userId, Pageable pageable) {
        Page<AuditLog> logs;
        
        if (action != null && userId != null) {
            logs = auditLogRepository.findByActionAndUserId(action, userId, pageable);
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action, pageable);
        } else if (userId != null) {
            logs = auditLogRepository.findByUserId(userId, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }
        
        return logs.map(this::convertToMap);
    }
    
    private Map<String, Object> convertToMap(AuditLog auditLog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", auditLog.getId());
        map.put("action", auditLog.getAction());
        map.put("description", auditLog.getDescription());
        map.put("userId", auditLog.getUserId());
        map.put("userRole", auditLog.getUserRole());
        map.put("targetUserId", auditLog.getTargetUserId());
        map.put("timestamp", auditLog.getTimestamp());
        map.put("ipAddress", auditLog.getIpAddress());
        map.put("userAgent", auditLog.getUserAgent());
        return map;
    }
    
    private String getCurrentIpAddress() {
        // In a real application, this would be extracted from the HTTP request
        return "127.0.0.1";
    }
    
    private String getCurrentUserAgent() {
        // In a real application, this would be extracted from the HTTP request
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    }
}



