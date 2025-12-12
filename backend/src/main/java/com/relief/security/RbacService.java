package com.relief.security;

import com.relief.entity.User;
import com.relief.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Service
public class RbacService {
    
    @Autowired
    private UserService userService;
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String emailOrPhone = authentication.getName();
        return userService.findByEmailOrPhone(emailOrPhone, emailOrPhone).orElse(null);
    }
    
    public Role getCurrentUserRole() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return Role.fromCode(user.getRole());
    }
    
    public boolean hasPermission(Permission permission) {
        Role role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return RolePermissionMapping.hasPermission(role, permission);
    }
    
    public boolean hasAnyPermission(Permission... permissions) {
        Role role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return RolePermissionMapping.hasAnyPermission(role, permissions);
    }
    
    public boolean hasAllPermissions(Permission... permissions) {
        Role role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return RolePermissionMapping.hasAllPermissions(role, permissions);
    }
    
    public boolean hasRole(Role role) {
        Role currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }
    
    public boolean hasAnyRole(Role... roles) {
        Role currentRole = getCurrentUserRole();
        if (currentRole == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(role -> role.equals(currentRole));
    }
    
    public Set<Permission> getCurrentUserPermissions() {
        Role role = getCurrentUserRole();
        if (role == null) {
            return Collections.emptySet();
        }
        return RolePermissionMapping.getPermissionsForRole(role);
    }
    
    public boolean canAccessUser(String targetUserId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Admins can access any user
        if (hasRole(Role.ADMIN)) {
            return true;
        }
        
        // Users can only access their own data
        return currentUser.getId().toString().equals(targetUserId);
    }
    
    public boolean canManageUser(String targetUserId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Only admins can manage other users
        if (!hasRole(Role.ADMIN)) {
            return false;
        }
        
        // Admins cannot manage themselves
        return !currentUser.getId().toString().equals(targetUserId);
    }
}
