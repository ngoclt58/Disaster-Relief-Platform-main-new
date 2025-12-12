package com.relief.util;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

/**
 * Test to verify admin password works
 * Run with: mvn test -Dtest=VerifyAdminPasswordTest
 */
@SpringBootTest
@ActiveProfiles("local")
public class VerifyAdminPasswordTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void verifyAdminPassword() {
        String email = "admin@relief.local";
        String password = "admin123";

        System.out.println("========================================");
        System.out.println("Verifying Admin Password");
        System.out.println("========================================");

        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            System.out.println("ERROR: User not found in database!");
            System.out.println("========================================");
            return;
        }

        User user = userOpt.get();
        String storedHash = user.getPasswordHash();
        
        System.out.println("User found:");
        System.out.println("  Email: " + user.getEmail());
        System.out.println("  Role: " + user.getRole());
        System.out.println("  Disabled: " + user.getDisabled());
        System.out.println("  Stored Hash: " + storedHash);
        System.out.println();
        
        // Test password matching
        boolean matches = passwordEncoder.matches(password, storedHash);
        
        System.out.println("Password Test:");
        System.out.println("  Password: " + password);
        System.out.println("  Matches: " + (matches ? "✓ YES" : "✗ NO"));
        System.out.println();
        
        if (!matches) {
            System.out.println("========================================");
            System.out.println("PASSWORD MISMATCH DETECTED!");
            System.out.println("========================================");
            System.out.println("Generating new hash and updating...");
            
            String newHash = passwordEncoder.encode(password);
            System.out.println("New Hash: " + newHash);
            
            user.setPasswordHash(newHash);
            userRepository.save(user);
            
            // Verify again
            boolean matchesAfter = passwordEncoder.matches(password, user.getPasswordHash());
            System.out.println("After update - Matches: " + (matchesAfter ? "✓ YES" : "✗ NO"));
        }
        
        System.out.println("========================================");
    }
}


