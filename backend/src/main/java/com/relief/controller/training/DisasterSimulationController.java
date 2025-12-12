package com.relief.controller.training;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.training.DisasterSimulationService;
import com.relief.service.training.DisasterSimulationService.SimulationScenario;
import com.relief.service.training.DisasterSimulationService.SimulationSession;
import com.relief.service.training.DisasterSimulationService.SimulationEvent;
import com.relief.service.training.DisasterSimulationService.SimulationResponse;
import com.relief.service.training.DisasterSimulationService.SimulationScore;
import com.relief.service.training.DisasterSimulationService.SimulationAnalytics;
import com.relief.service.training.DisasterSimulationService.SimulationTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Disaster simulation controller
 */
@RestController
@RequestMapping("/disaster-simulation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Disaster Simulation", description = "Virtual training scenarios for responders")
public class DisasterSimulationController {

    private final DisasterSimulationService disasterSimulationService;
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

    @PostMapping("/scenarios")
    @Operation(summary = "Create a simulation scenario")
    public ResponseEntity<SimulationScenario> createScenario(
            @RequestBody CreateScenarioRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        SimulationScenario scenario = disasterSimulationService.createScenario(
            request.getName(),
            request.getDescription(),
            request.getDisasterType(),
            request.getDifficulty(),
            request.getLocation(),
            request.getParameters(),
            userId
        );
        
        return ResponseEntity.ok(scenario);
    }

    @PostMapping("/sessions")
    @Operation(summary = "Start a simulation session")
    public ResponseEntity<SimulationSession> startSession(
            @RequestBody StartSessionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID instructorId = getUserIdFromPrincipal(principal);
        
        SimulationSession session = disasterSimulationService.startSession(
            request.getScenarioId(),
            request.getSessionName(),
            request.getParticipantIds(),
            instructorId
        );
        
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/join")
    @Operation(summary = "Join a simulation session")
    public ResponseEntity<SimulationSession> joinSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        
        String participantId = principal.getUsername();
        SimulationSession session = disasterSimulationService.joinSession(sessionId, participantId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/leave")
    @Operation(summary = "Leave a simulation session")
    public ResponseEntity<SimulationSession> leaveSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        
        String participantId = principal.getUsername();
        SimulationSession session = disasterSimulationService.leaveSession(sessionId, participantId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/events")
    @Operation(summary = "Trigger a simulation event")
    public ResponseEntity<SimulationEvent> triggerEvent(
            @PathVariable String sessionId,
            @RequestBody TriggerEventRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String triggeredBy = principal.getUsername();
        
        SimulationEvent event = disasterSimulationService.triggerEvent(
            sessionId,
            request.getEventType(),
            request.getEventData(),
            triggeredBy
        );
        
        return ResponseEntity.ok(event);
    }

    @PostMapping("/sessions/{sessionId}/responses")
    @Operation(summary = "Record a simulation response")
    public ResponseEntity<SimulationResponse> recordResponse(
            @PathVariable String sessionId,
            @RequestBody RecordResponseRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String participantId = principal.getUsername();
        
        SimulationResponse response = disasterSimulationService.recordResponse(
            sessionId,
            participantId,
            request.getEventId(),
            request.getResponseType(),
            request.getResponseData()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/score")
    @Operation(summary = "Calculate simulation score")
    public ResponseEntity<SimulationScore> calculateScore(
            @PathVariable String sessionId,
            @RequestParam String participantId) {
        
        SimulationScore score = disasterSimulationService.calculateScore(sessionId, participantId);
        return ResponseEntity.ok(score);
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End a simulation session")
    public ResponseEntity<SimulationSession> endSession(
            @PathVariable String sessionId,
            @RequestBody EndSessionRequest request) {
        
        SimulationSession session = disasterSimulationService.endSession(
            sessionId,
            request.getReason()
        );
        
        return ResponseEntity.ok(session);
    }

    @GetMapping("/scenarios")
    @Operation(summary = "Get simulation scenarios")
    public ResponseEntity<List<SimulationScenario>> getScenarios(
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "true") boolean isActive) {
        
        List<SimulationScenario> scenarios = disasterSimulationService.getScenarios(
            disasterType, difficulty, isActive
        );
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get simulation sessions")
    public ResponseEntity<List<SimulationSession>> getSessions(
            @RequestParam(required = false) String participantId,
            @RequestParam(required = false) String instructorId,
            @RequestParam(required = false) String status) {
        
        List<SimulationSession> sessions = disasterSimulationService.getSessions(
            participantId, instructorId, 
            status != null ? DisasterSimulationService.SessionStatus.valueOf(status) : null
        );
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get simulation session details")
    public ResponseEntity<SimulationSession> getSession(@PathVariable String sessionId) {
        SimulationSession session = disasterSimulationService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{sessionId}/events")
    @Operation(summary = "Get session events")
    public ResponseEntity<List<SimulationEvent>> getSessionEvents(@PathVariable String sessionId) {
        List<SimulationEvent> events = disasterSimulationService.getSessionEvents(sessionId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/sessions/{sessionId}/responses")
    @Operation(summary = "Get participant responses")
    public ResponseEntity<List<SimulationResponse>> getParticipantResponses(
            @PathVariable String sessionId,
            @RequestParam String participantId) {
        
        List<SimulationResponse> responses = disasterSimulationService.getParticipantResponses(
            sessionId, participantId
        );
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sessions/{sessionId}/analytics")
    @Operation(summary = "Get session analytics")
    public ResponseEntity<SimulationAnalytics> getSessionAnalytics(@PathVariable String sessionId) {
        SimulationAnalytics analytics = disasterSimulationService.getSessionAnalytics(sessionId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get simulation templates")
    public ResponseEntity<List<SimulationTemplate>> getTemplates(
            @RequestParam(required = false) String disasterType) {
        
        List<SimulationTemplate> templates = disasterSimulationService.getTemplates(disasterType);
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/templates")
    @Operation(summary = "Create simulation template")
    public ResponseEntity<SimulationTemplate> createTemplate(
            @RequestBody CreateTemplateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        SimulationTemplate template = disasterSimulationService.createTemplate(
            request.getName(),
            request.getDescription(),
            request.getDisasterType(),
            request.getConfiguration(),
            userId
        );
        
        return ResponseEntity.ok(template);
    }

    // Request DTOs
    public static class CreateScenarioRequest {
        private String name;
        private String description;
        private String disasterType;
        private String difficulty;
        private String location;
        private Map<String, Object> parameters;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class StartSessionRequest {
        private String scenarioId;
        private String sessionName;
        private List<String> participantIds;

        // Getters and setters
        public String getScenarioId() { return scenarioId; }
        public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }

        public String getSessionName() { return sessionName; }
        public void setSessionName(String sessionName) { this.sessionName = sessionName; }

        public List<String> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    }

    public static class TriggerEventRequest {
        private String eventType;
        private Map<String, Object> eventData;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Map<String, Object> getEventData() { return eventData; }
        public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }
    }

    public static class RecordResponseRequest {
        private String eventId;
        private String responseType;
        private Map<String, Object> responseData;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getResponseType() { return responseType; }
        public void setResponseType(String responseType) { this.responseType = responseType; }

        public Map<String, Object> getResponseData() { return responseData; }
        public void setResponseData(Map<String, Object> responseData) { this.responseData = responseData; }
    }

    public static class EndSessionRequest {
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CreateTemplateRequest {
        private String name;
        private String description;
        private String disasterType;
        private Map<String, Object> configuration;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    }
}


