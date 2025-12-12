package com.relief.controller;

import com.relief.dto.AdminStatsResponse;
import com.relief.dto.UserManagementRequest;
import com.relief.entity.User;
import com.relief.security.RequiresRole;
import com.relief.security.Role;
import com.relief.service.AdminService;
import com.relief.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiresRole(Role.ADMIN)
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getSystemStats() {
        AdminStatsResponse stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        Page<User> users = adminService.getAllUsers(search, role, pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = adminService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserManagementRequest request) {
        User user = adminService.createUser(request);
        auditService.logAdminAction("USER_CREATED", "Created user: " + user.getEmail());
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UserManagementRequest request) {
        User user = adminService.updateUser(id, request);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.logAdminAction("USER_UPDATED", "Updated user: " + user.getEmail());
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        boolean deleted = adminService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        auditService.logAdminAction("USER_DELETED", "Deleted user with ID: " + id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/users/{id}/roles")
    public ResponseEntity<User> updateUserRole(@PathVariable String id, @RequestBody Map<String, String> request) {
        String newRole = request.get("role");
        User user = adminService.updateUserRole(id, newRole);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.logAdminAction("USER_ROLE_UPDATED", "Updated role for user: " + user.getEmail() + " to " + newRole);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable String id) {
        User user = adminService.activateUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.logAdminAction("USER_ACTIVATED", "Activated user: " + user.getEmail());
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable String id) {
        User user = adminService.deactivateUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.logAdminAction("USER_DEACTIVATED", "Deactivated user: " + user.getEmail());
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userId,
            Pageable pageable) {
        Page<Map<String, Object>> logs = auditService.getAuditLogs(action, userId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = adminService.getSystemHealth();
        return ResponseEntity.ok(health);
    }
    
    @PostMapping("/system/maintenance")
    public ResponseEntity<Map<String, String>> toggleMaintenanceMode(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.getOrDefault("enabled", false);
        adminService.setMaintenanceMode(enabled);
        auditService.logAdminAction("MAINTENANCE_MODE_TOGGLED", "Maintenance mode: " + (enabled ? "enabled" : "disabled"));
        return ResponseEntity.ok(Map.of("status", "success", "maintenance_mode", String.valueOf(enabled)));
    }
}



