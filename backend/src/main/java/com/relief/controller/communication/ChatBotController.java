package com.relief.controller.communication;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.communication.ChatBotService;
import com.relief.service.communication.ChatBotService.ChatBotResponse;
import com.relief.service.communication.ChatBotService.ChatSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Chat bot controller
 */
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Bot", description = "AI-powered chat bot APIs")
public class ChatBotController {

    private final ChatBotService chatBotService;
    private final UserRepository userRepository;

    private UUID getUserIdFromPrincipal(UserDetails principal) {
        String username = principal.getUsername();
        // Try to parse as UUID first
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            // If not UUID, treat as email/phone and lookup user
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
            return user.getId();
        }
    }

    @PostMapping("/message")
    @Operation(summary = "Send message to chat bot")
    public ResponseEntity<ChatBotResponse> sendMessage(
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        // Validate request
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        // Generate sessionId if not provided
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        ChatBotResponse response = chatBotService.processMessage(
            sessionId,
            request.getMessage().trim(),
            userId
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get user's chat sessions")
    public ResponseEntity<List<ChatSession>> getUserSessions(
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        List<ChatSession> sessions = chatBotService.getUserSessions(userId);
        
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get chat session details")
    public ResponseEntity<ChatSession> getChatSession(
            @PathVariable String sessionId) {
        
        ChatSession session = chatBotService.getChatSession(sessionId);
        
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End chat session")
    public ResponseEntity<Map<String, String>> endSession(
            @PathVariable String sessionId) {
        
        chatBotService.endSession(sessionId);
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Session ended successfully"));
    }

    // Request DTOs
    public static class SendMessageRequest {
        private String sessionId;
        private String message;

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
