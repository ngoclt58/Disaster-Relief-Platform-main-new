package com.relief.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt hash for admin password
 * Run this main method to get the hash, then use it in SQL
 */
public class CreateAdminUser {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("BCrypt Hash Generator");
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("========================================");
        System.out.println();
        System.out.println("Use this SQL to update the user:");
        System.out.println();
        System.out.println("UPDATE users");
        System.out.println("SET password_hash = '" + hash + "',");
        System.out.println("    disabled = false,");
        System.out.println("    updated_at = NOW()");
        System.out.println("WHERE email = 'admin@relief.local';");
        System.out.println();
        System.out.println("Or if user doesn't exist:");
        System.out.println();
        System.out.println("INSERT INTO users (id, full_name, email, password_hash, role, disabled, created_at, updated_at)");
        System.out.println("VALUES (");
        System.out.println("    '550e8400-e29b-41d4-a716-446655440000'::uuid,");
        System.out.println("    'System Administrator',");
        System.out.println("    'admin@relief.local',");
        System.out.println("    '" + hash + "',");
        System.out.println("    'ADMIN',");
        System.out.println("    false,");
        System.out.println("    NOW(),");
        System.out.println("    NOW()");
        System.out.println(");");
        System.out.println("========================================");
    }
}

