package com.relief.security;

public enum Permission {
    // User Management
    USER_READ("user:read", "Read user information"),
    USER_WRITE("user:write", "Create/update user information"),
    USER_DELETE("user:delete", "Delete user accounts"),
    USER_MANAGE_ROLES("user:manage_roles", "Assign/remove user roles"),

    // Needs Management
    NEEDS_READ("needs:read", "Read needs requests"),
    NEEDS_WRITE("needs:write", "Create/update needs requests"),
    NEEDS_DELETE("needs:delete", "Delete needs requests"),
    NEEDS_ASSIGN("needs:assign", "Assign needs to helpers"),

    // Task Management
    TASK_READ("task:read", "Read task information"),
    TASK_WRITE("task:write", "Create/update tasks"),
    TASK_ASSIGN("task:assign", "Assign tasks to helpers"),
    TASK_CLAIM("task:claim", "Claim available tasks"),

    // Inventory Management
    INVENTORY_READ("inventory:read", "Read inventory information"),
    INVENTORY_WRITE("inventory:write", "Update inventory levels"),
    INVENTORY_MANAGE("inventory:manage", "Manage inventory hubs and items"),

    // System Administration
    SYSTEM_MONITOR("system:monitor", "Monitor system health and metrics"),
    SYSTEM_CONFIG("system:config", "Configure system settings"),
    AUDIT_READ("audit:read", "Read audit logs"),

    // Reports and Analytics
    REPORTS_READ("reports:read", "Read reports and analytics"),
    REPORTS_EXPORT("reports:export", "Export reports and data"),

    // Real-time Data
    REALTIME_READ("realtime:read", "Access real-time data streams"),

    // Media Management
    MEDIA_READ("media:read", "Read media files"),
    MEDIA_UPLOAD("media:upload", "Upload media files"),
    MEDIA_DELETE("media:delete", "Delete media files"),

    // AI & Machine Learning
    AI_READ("ai:read", "Access AI and ML features"),
    AI_WRITE("ai:write", "Create and manage AI models"),
    AI_ADMIN("ai:admin", "Administer AI systems"),

    // Optimization
    OPTIMIZATION_READ("optimization:read", "Access optimization features"),
    OPTIMIZATION_WRITE("optimization:write", "Configure optimization settings"),
    OPTIMIZATION_ADMIN("optimization:admin", "Administer optimization systems"),

    // Security & Privacy
    SECURITY_READ("security:read", "Read security features"),
    SECURITY_WRITE("security:write", "Manage security settings"),
    SECURITY_ANALYTICS("security:analytics", "Access security analytics"),

    // Composite / high-level permissions
    ADMIN_USERS("admin:users", "Full access to user and dedupe management");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.code.equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
}

