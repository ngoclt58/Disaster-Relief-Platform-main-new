package com.relief.controller.notification;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.notification.SmartNotificationService;
import com.relief.service.notification.UserPreferenceService;
import com.relief.service.notification.UserPreferenceService.UserPreferences;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for smart notifications and user preferences
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Smart Notifications", description = "Notification management and user preferences")
public class SmartNotificationController {

    private final SmartNotificationService smartNotificationService;
    private final UserPreferenceService userPreferenceService;
    private final UserRepository userRepository;

    private UUID getUserIdFromPrincipal(UserDetails principal) {
        String username = principal.getUsername();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
            return user.getId();
        }
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get user notification preferences")
    public ResponseEntity<UserPreferences> getUserPreferences(
            @AuthenticationPrincipal UserDetails principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        UserPreferences preferences = userPreferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user notification preferences")
    public ResponseEntity<UserPreferences> updateUserPreferences(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody UserPreferences preferences) {
        UUID userId = getUserIdFromPrincipal(principal);
        userPreferenceService.updateUserPreferences(userId, preferences);
        return ResponseEntity.ok(preferences);
    }

    @PostMapping("/send")
    @Operation(summary = "Send smart notification")
    public ResponseEntity<Map<String, String>> sendNotification(
            @RequestBody SendNotificationRequest request) {
        smartNotificationService.sendContextualNotification(
            request.getRequestId(),
            request.getEventType(),
            request.getMessage()
        );
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get user notification history")
    public ResponseEntity<Map<String, Object>> getNotificationHistory(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "24") int hours) {
        UUID userId = getUserIdFromPrincipal(principal);
        Map<String, Object> history = new java.util.HashMap<>();
        history.put("totalNotifications", userPreferenceService.getNotificationCount(userId, "ALL", hours));
        history.put("last24Hours", userPreferenceService.getNotificationCount(userId, "ALL", 24));
        return ResponseEntity.ok(history);
    }

    public static class SendNotificationRequest {
        private UUID requestId;
        private String eventType;
        private String message;

        public UUID getRequestId() { return requestId; }
        public void setRequestId(UUID requestId) { this.requestId = requestId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

