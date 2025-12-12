package com.relief.security;

import java.util.*;

public class RolePermissionMapping {
    
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();
    
    static {
        // ADMIN - Full access to everything
        ROLE_PERMISSIONS.put(Role.ADMIN, EnumSet.allOf(Permission.class));
        
        // DISPATCHER - Can manage needs, tasks, and view reports
        ROLE_PERMISSIONS.put(Role.DISPATCHER, EnumSet.of(
            Permission.USER_READ,
            Permission.NEEDS_READ,
            Permission.NEEDS_WRITE,
            Permission.NEEDS_ASSIGN,
            Permission.TASK_READ,
            Permission.TASK_WRITE,
            Permission.TASK_ASSIGN,
            Permission.INVENTORY_READ,
            Permission.INVENTORY_WRITE,
            Permission.REPORTS_READ,
            Permission.REPORTS_EXPORT,
            Permission.REALTIME_READ
        ));
        
        // HELPER - Can claim tasks and update inventory
        ROLE_PERMISSIONS.put(Role.HELPER, EnumSet.of(
            Permission.NEEDS_READ,
            Permission.TASK_READ,
            Permission.TASK_CLAIM,
            Permission.INVENTORY_READ,
            Permission.INVENTORY_WRITE,
            Permission.REALTIME_READ
        ));
        
        // RESIDENT - Can create needs and view their own data
        ROLE_PERMISSIONS.put(Role.RESIDENT, EnumSet.of(
            Permission.USER_READ,
            Permission.NEEDS_READ,
            Permission.NEEDS_WRITE,
            Permission.TASK_READ,
            Permission.REALTIME_READ
        ));
    }
    
    public static Set<Permission> getPermissionsForRole(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }
    
    public static boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = getPermissionsForRole(role);
        return permissions.contains(permission);
    }
    
    public static boolean hasAnyPermission(Role role, Permission... permissions) {
        Set<Permission> rolePermissions = getPermissionsForRole(role);
        return Arrays.stream(permissions).anyMatch(rolePermissions::contains);
    }
    
    public static boolean hasAllPermissions(Role role, Permission... permissions) {
        Set<Permission> rolePermissions = getPermissionsForRole(role);
        return Arrays.stream(permissions).allMatch(rolePermissions::contains);
    }
}



