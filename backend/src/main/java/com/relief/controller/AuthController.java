package com.relief.controller;

import com.relief.dto.LoginRequest;
import com.relief.dto.LoginResponse;
import com.relief.dto.RegisterRequest;
import com.relief.entity.User;
import com.relief.security.JwtTokenProvider;
import com.relief.exception.ConflictException;
import com.relief.exception.NotFoundException;
import com.relief.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmailOrPhone(),
                        loginRequest.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userService.findByEmailOrPhone(loginRequest.getEmailOrPhone(), loginRequest.getEmailOrPhone())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(User.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build())
                .build());
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email is already taken!");
        }

        if (userService.existsByPhone(registerRequest.getPhone())) {
            throw new ConflictException("Phone number is already taken!");
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .passwordHash(registerRequest.getPassword())
                .role(registerRequest.getRole())
                .build();

        User savedUser = userService.createUser(user);
        
        // Remove password from response
        savedUser.setPasswordHash(null);
        
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<LoginResponse> refresh() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = tokenProvider.generateToken(authentication);
        
        String username = authentication.getName();
        User user = userService.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(User.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build())
                .build());
    }
}
