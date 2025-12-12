package com.relief.security;

public enum Role {
    ADMIN("ADMIN", "System Administrator"),
    DISPATCHER("DISPATCHER", "Emergency Dispatcher"),
    HELPER("HELPER", "Volunteer Helper"),
    RESIDENT("RESIDENT", "Affected Resident");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromCode(String code) {
        for (Role role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}



