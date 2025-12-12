package com.relief.util;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Test to create admin user in database
 * Run with: mvn test -Dtest=CreateAdminUserTest
 */
@SpringBootTest
@ActiveProfiles("local")
public class CreateAdminUserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    public void createAdminUser() {
        String email = "admin@relief.local";
        String password = "admin123";
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // Generate password hash
        String passwordHash = passwordEncoder.encode(password);
        System.out.println("========================================");
        System.out.println("Creating/Updating Admin User");
        System.out.println("========================================");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + passwordHash);
        System.out.println("========================================");

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            System.out.println("Updating existing user...");
            user.setPasswordHash(passwordHash);
            user.setDisabled(false);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            System.out.println("Creating new user...");
            user = new User();
            user.setId(userId);
            user.setFullName("System Administrator");
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setRole("ADMIN");
            user.setDisabled(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
        }

        user = userRepository.save(user);

        System.out.println("========================================");
        System.out.println("SUCCESS! Admin User Created/Updated");
        System.out.println("========================================");
        System.out.println("User ID: " + user.getId());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Disabled: " + user.getDisabled());
        System.out.println("========================================");
        System.out.println("You can now login with:");
        System.out.println("  Email: " + email);
        System.out.println("  Password: " + password);
        System.out.println("========================================");

        // Verify the password works
        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        System.out.println("Password verification: " + (matches ? "✓ PASSED" : "✗ FAILED"));
        System.out.println("========================================");
    }
}


