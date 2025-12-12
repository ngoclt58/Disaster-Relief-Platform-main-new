package com.relief.controller.communication;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.communication.VideoConferencingService;
import com.relief.service.communication.VideoConferencingService.VideoConference;
import com.relief.service.communication.VideoConferencingService.ConferenceJoinResult;
import com.relief.service.communication.VideoConferencingService.ConferenceSettings;
import com.relief.service.communication.VideoConferencingService.ConferenceType;
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
 * Video conferencing controller
 */
@RestController
@RequestMapping("/video-conference")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Conferencing", description = "Video conferencing and remote coordination APIs")
public class VideoConferencingController {

    private final VideoConferencingService videoConferencingService;
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

    @PostMapping("/create")
    @Operation(summary = "Create a new video conference")
    public ResponseEntity<VideoConference> createConference(
            @RequestBody CreateConferenceRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        VideoConference conference = videoConferencingService.createConference(
            request.getTitle(),
            request.getDescription(),
            userId,
            request.getType()
        );
        
        return ResponseEntity.ok(conference);
    }

    @PostMapping("/{conferenceId}/join")
    @Operation(summary = "Join a video conference")
    public ResponseEntity<ConferenceJoinResult> joinConference(
            @PathVariable String conferenceId,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        ConferenceJoinResult result = videoConferencingService.joinConference(conferenceId, userId);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{conferenceId}/leave")
    @Operation(summary = "Leave a video conference")
    public ResponseEntity<Map<String, String>> leaveConference(
            @PathVariable String conferenceId,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        videoConferencingService.leaveConference(conferenceId, userId);
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Left conference successfully"));
    }

    @GetMapping("/my-conferences")
    @Operation(summary = "Get user's active conferences")
    public ResponseEntity<List<VideoConference>> getUserConferences(
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        List<VideoConference> conferences = videoConferencingService.getUserConferences(userId);
        
        return ResponseEntity.ok(conferences);
    }

    @GetMapping("/{conferenceId}/participants")
    @Operation(summary = "Get conference participants")
    public ResponseEntity<List<VideoConferencingService.ConferenceParticipant>> getConferenceParticipants(
            @PathVariable String conferenceId) {
        
        List<VideoConferencingService.ConferenceParticipant> participants = 
            videoConferencingService.getConferenceParticipants(conferenceId);
        
        return ResponseEntity.ok(participants);
    }

    @PutMapping("/{conferenceId}/settings")
    @Operation(summary = "Update conference settings")
    public ResponseEntity<VideoConference> updateConferenceSettings(
            @PathVariable String conferenceId,
            @RequestBody ConferenceSettings settings,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        VideoConference conference = videoConferencingService.updateConferenceSettings(conferenceId, settings);
        
        return ResponseEntity.ok(conference);
    }

    @PostMapping("/{conferenceId}/end")
    @Operation(summary = "End a video conference")
    public ResponseEntity<Map<String, String>> endConference(
            @PathVariable String conferenceId,
            @AuthenticationPrincipal UserDetails principal) {
        
        videoConferencingService.endConference(conferenceId, getUserIdFromPrincipal(principal));
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Conference ended successfully"));
    }

    // Request DTOs
    public static class CreateConferenceRequest {
        private String title;
        private String description;
        private ConferenceType type;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ConferenceType getType() { return type; }
        public void setType(ConferenceType type) { this.type = type; }
    }
}
