package com.relief.service.notification;

import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.UserRepository;
import com.relief.service.LocationService;
import com.relief.service.NotificationService;
import com.relief.service.notification.UserPreferenceService.UserPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart notification service with context-aware delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmartNotificationService.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserPreferenceService userPreferenceService;
    private final LocationService locationService;
    private final NeedsRequestRepository needsRequestRepository;

    /**
     * Send context-aware notification to appropriate users (by UUID)
     */
    public void sendContextualNotification(UUID requestId, String eventType, String baseMessage) {
        NeedsRequest request = needsRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("NeedsRequest not found: " + requestId));
        sendContextualNotification(request, eventType, baseMessage);
    }

    /**
     * Send context-aware notification to appropriate users
     */
    public void sendContextualNotification(NeedsRequest request, String eventType, String baseMessage) {
        log.info("Sending contextual notification for event: {} on request: {}", eventType, request.getId());
        
        // Determine notification strategy based on event type and request context
        NotificationStrategy strategy = determineNotificationStrategy(request, eventType);
        
        // Get target users based on context
        List<User> targetUsers = getTargetUsers(request, eventType, strategy);
        
        // Send personalized notifications
        for (User user : targetUsers) {
            if (shouldNotifyUser(user, request, eventType)) {
                String personalizedMessage = personalizeMessage(baseMessage, user, request, eventType);
                String preferredChannel = getUserPreferredChannel(user, eventType);
                
                notificationService.sendNotification(user, personalizedMessage, preferredChannel, request);
                
                // Update notification history for rate limiting
                updateNotificationHistory(user, eventType);
            }
        }
    }

    /**
     * Determine notification strategy based on context
     */
    private NotificationStrategy determineNotificationStrategy(NeedsRequest request, String eventType) {
        // High priority events get immediate notification
        if (isHighPriorityEvent(eventType) || request.getSeverity() >= 4) {
            return NotificationStrategy.IMMEDIATE;
        }
        
        // Medical emergencies get immediate notification to all available helpers
        if ("MEDICAL_EMERGENCY".equals(request.getType())) {
            return NotificationStrategy.EMERGENCY_BROADCAST;
        }
        
        // Evacuation events get broadcast to all users in area
        if ("EVACUATION".equals(request.getType())) {
            return NotificationStrategy.AREA_BROADCAST;
        }
        
        // Regular events use smart assignment
        return NotificationStrategy.SMART_ASSIGNMENT;
    }

    /**
     * Get target users based on strategy and context
     */
    private List<User> getTargetUsers(NeedsRequest request, String eventType, NotificationStrategy strategy) {
        switch (strategy) {
            case IMMEDIATE:
                return getImmediateResponseUsers(request);
            case EMERGENCY_BROADCAST:
                return getEmergencyResponseUsers(request);
            case AREA_BROADCAST:
                return getAreaUsers(request);
            case SMART_ASSIGNMENT:
                return getSmartAssignedUsers(request, eventType);
            default:
                return getDefaultUsers(request);
        }
    }

    /**
     * Get users for immediate response
     */
    private List<User> getImmediateResponseUsers(NeedsRequest request) {
        return userRepository.findByRoleAndNotDisabled("DISPATCHER").stream()
            .filter(user -> isUserAvailable(user))
            .collect(Collectors.toList());
    }

    /**
     * Get users for emergency response
     */
    private List<User> getEmergencyResponseUsers(NeedsRequest request) {
        List<User> users = new ArrayList<>();
        
        // Add dispatchers
        users.addAll(userRepository.findByRoleAndNotDisabled("DISPATCHER"));
        
        // Add available helpers
        users.addAll(userRepository.findByRoleAndNotDisabled("HELPER").stream()
            .filter(user -> isUserAvailable(user))
            .collect(Collectors.toList()));
            
        return users;
    }

    /**
     * Get users in the same area
     */
    private List<User> getAreaUsers(NeedsRequest request) {
        if (request.getGeomPoint() == null) {
            return getDefaultUsers(request);
        }
        
        // Find users within 10km radius
        return userRepository.findUsersInRadius(
            request.getGeomPoint(), 
            10000 // 10km
        );
    }

    /**
     * Get smart assigned users based on context
     */
    private List<User> getSmartAssignedUsers(NeedsRequest request, String eventType) {
        List<User> candidates = new ArrayList<>();
        
        // Add users based on request type
        switch (request.getType()) {
            case "Medical Emergency":
                candidates.addAll(userRepository.findByRoleAndNotDisabled("HELPER"));
                break;
            case "Food Request":
            case "Water Request":
                candidates.addAll(userRepository.findByRoleAndNotDisabled("HELPER"));
                break;
            case "Evacuation":
                candidates.addAll(userRepository.findByRoleAndNotDisabled("DISPATCHER"));
                break;
            default:
                candidates.addAll(userRepository.findByRoleAndNotDisabled("HELPER"));
        }
        
        // Filter by availability and preferences
        return candidates.stream()
            .filter(user -> isUserAvailable(user))
            .filter(user -> userPrefersEventType(user, eventType))
            .collect(Collectors.toList());
    }

    /**
     * Get default users for notification
     */
    private List<User> getDefaultUsers(NeedsRequest request) {
        return userRepository.findByRoleAndNotDisabled("DISPATCHER");
    }

    /**
     * Check if user should be notified based on preferences and context
     */
    private boolean shouldNotifyUser(User user, NeedsRequest request, String eventType) {
        // Check user preferences
        if (!userPrefersEventType(user, eventType)) {
            return false;
        }
        
        // Check notification frequency limits
        if (isNotificationRateLimited(user, eventType)) {
            return false;
        }
        
        // Check user availability
        if (!isUserAvailable(user)) {
            return false;
        }
        
        // Check location relevance for area-based events
        if (isLocationRelevant(user, request)) {
            return true;
        }
        
        return true;
    }

    /**
     * Personalize message for user
     */
    private String personalizeMessage(String baseMessage, User user, NeedsRequest request, String eventType) {
        StringBuilder personalized = new StringBuilder();
        
        // Add greeting
        personalized.append("Hello ").append(user.getFullName()).append(",\n\n");
        
        // Add context-specific information
        if (request.getGeomPoint() != null) {
            String location = locationService.getLocationDescription(request.getGeomPoint());
            personalized.append("Location: ").append(location).append("\n");
        }
        
        personalized.append("Request Type: ").append(request.getType()).append("\n");
        personalized.append("Severity: ").append(request.getSeverity()).append("/5\n");
        
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            personalized.append("Details: ").append(request.getNotes()).append("\n");
        }
        
        personalized.append("\n").append(baseMessage);
        
        // Add urgency indicators
        if (request.getSeverity() >= 4) {
            personalized.append("\n\n⚠️ URGENT: High priority request requiring immediate attention!");
        }
        
        return personalized.toString();
    }

    /**
     * Get user's preferred notification channel
     */
    private String getUserPreferredChannel(User user, String eventType) {
        UserPreferences preferences = userPreferenceService.getUserPreferences(user.getId());
        
        // Emergency events override preferences
        if (isHighPriorityEvent(eventType)) {
            return "PUSH"; // Always push for emergencies
        }
        
        return preferences.getPreferredChannel(eventType);
    }

    /**
     * Check if event is high priority
     */
    private boolean isHighPriorityEvent(String eventType) {
        return Arrays.asList("MEDICAL_EMERGENCY", "EVACUATION", "CRITICAL_ALERT")
            .contains(eventType);
    }

    /**
     * Check if user is available
     */
    private boolean isUserAvailable(User user) {
        // Check if user is online (simplified)
        return !user.getDisabled();
    }

    /**
     * Check if user prefers this event type
     */
    private boolean userPrefersEventType(User user, String eventType) {
        UserPreferences preferences = userPreferenceService.getUserPreferences(user.getId());
        return preferences.isEventTypeEnabled(eventType);
    }

    /**
     * Check if notification is rate limited
     */
    private boolean isNotificationRateLimited(User user, String eventType) {
        // Simple rate limiting - max 5 notifications per hour per user
        return userPreferenceService.getNotificationCount(user.getId(), eventType, 1) >= 5;
    }

    /**
     * Check if location is relevant for user
     */
    private boolean isLocationRelevant(User user, NeedsRequest request) {
        // Simplified - in real implementation, check user's work area, home, etc.
        return true;
    }

    /**
     * Update notification history for rate limiting
     */
    private void updateNotificationHistory(User user, String eventType) {
        userPreferenceService.recordNotification(user.getId(), eventType);
    }

    // Enums
    public enum NotificationStrategy {
        IMMEDIATE,           // Immediate notification to dispatchers
        EMERGENCY_BROADCAST, // Broadcast to all available helpers
        AREA_BROADCAST,      // Broadcast to users in area
        SMART_ASSIGNMENT,    // Smart assignment based on context
        TARGETED            // Targeted notification to specific users
    }
}


