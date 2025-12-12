package com.relief.controller;

import com.relief.dto.UserUpdateRequest;
import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.security.RequiresPermission;
import com.relief.security.Permission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management endpoints")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @RequiresPermission(Permission.USER_READ)
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        
        // Remove sensitive information
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<User> updateMyProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserUpdateRequest request) {
        
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        
        // Update user fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        User updatedUser = userRepository.save(user);
        updatedUser.setPasswordHash(null); // Remove sensitive information
        
        return ResponseEntity.ok(updatedUser);
    }
}


