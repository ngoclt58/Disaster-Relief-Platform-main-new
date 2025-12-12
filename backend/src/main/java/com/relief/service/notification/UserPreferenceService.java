package com.relief.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing user notification preferences
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(UserPreferenceService.class);
    private final Map<UUID, UserPreferences> userPreferences = new ConcurrentHashMap<>();
    private final Map<UUID, List<NotificationRecord>> notificationHistory = new ConcurrentHashMap<>();

    /**
     * Get user preferences
     */
    public UserPreferences getUserPreferences(UUID userId) {
        return userPreferences.computeIfAbsent(userId, id -> createDefaultPreferences());
    }

    /**
     * Update user preferences
     */
    public void updateUserPreferences(UUID userId, UserPreferences preferences) {
        userPreferences.put(userId, preferences);
        log.info("Updated preferences for user: {}", userId);
    }

    /**
     * Record notification for rate limiting
     */
    public void recordNotification(UUID userId, String eventType) {
        List<NotificationRecord> history = notificationHistory.computeIfAbsent(userId, id -> new ArrayList<>());
        history.add(new NotificationRecord(eventType, LocalDateTime.now()));
        
        // Clean up old records (keep only last 24 hours)
        history.removeIf(record -> record.getTimestamp().isBefore(LocalDateTime.now().minusHours(24)));
    }

    /**
     * Get notification count for rate limiting
     */
    public int getNotificationCount(UUID userId, String eventType, int hours) {
        List<NotificationRecord> history = notificationHistory.getOrDefault(userId, new ArrayList<>());
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        
        return (int) history.stream()
            .filter(record -> record.getEventType().equals(eventType))
            .filter(record -> record.getTimestamp().isAfter(cutoff))
            .count();
    }

    /**
     * Create default user preferences
     */
    private UserPreferences createDefaultPreferences() {
        UserPreferences preferences = new UserPreferences();
        
        // Default enabled event types
        preferences.setEnabledEventTypes(new HashSet<>(Arrays.asList(
            "MEDICAL_EMERGENCY", "EVACUATION", "TASK_ASSIGNED", "TASK_UPDATED"
        )));
        
        // Default channel preferences
        Map<String, String> channelPreferences = new HashMap<>();
        channelPreferences.put("MEDICAL_EMERGENCY", "PUSH");
        channelPreferences.put("EVACUATION", "PUSH");
        channelPreferences.put("TASK_ASSIGNED", "EMAIL");
        channelPreferences.put("TASK_UPDATED", "IN_APP");
        preferences.setChannelPreferences(channelPreferences);
        
        // Default settings
        preferences.setMaxNotificationsPerHour(10);
        preferences.setQuietHoursStart(22); // 10 PM
        preferences.setQuietHoursEnd(7);    // 7 AM
        preferences.setLocationBasedNotifications(true);
        
        return preferences;
    }

    /**
     * User preferences data class
     */
    public static class UserPreferences {
        private Set<String> enabledEventTypes = new HashSet<>();
        private Map<String, String> channelPreferences = new HashMap<>();
        private int maxNotificationsPerHour = 10;
        private int quietHoursStart = 22;
        private int quietHoursEnd = 7;
        private boolean locationBasedNotifications = true;
        private boolean emailNotifications = true;
        private boolean pushNotifications = true;
        private boolean smsNotifications = false;

        // Getters and setters
        public Set<String> getEnabledEventTypes() { return enabledEventTypes; }
        public void setEnabledEventTypes(Set<String> enabledEventTypes) { this.enabledEventTypes = enabledEventTypes; }

        public Map<String, String> getChannelPreferences() { return channelPreferences; }
        public void setChannelPreferences(Map<String, String> channelPreferences) { this.channelPreferences = channelPreferences; }

        public int getMaxNotificationsPerHour() { return maxNotificationsPerHour; }
        public void setMaxNotificationsPerHour(int maxNotificationsPerHour) { this.maxNotificationsPerHour = maxNotificationsPerHour; }

        public int getQuietHoursStart() { return quietHoursStart; }
        public void setQuietHoursStart(int quietHoursStart) { this.quietHoursStart = quietHoursStart; }

        public int getQuietHoursEnd() { return quietHoursEnd; }
        public void setQuietHoursEnd(int quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

        public boolean isLocationBasedNotifications() { return locationBasedNotifications; }
        public void setLocationBasedNotifications(boolean locationBasedNotifications) { this.locationBasedNotifications = locationBasedNotifications; }

        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

        public boolean isPushNotifications() { return pushNotifications; }
        public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }

        public boolean isSmsNotifications() { return smsNotifications; }
        public void setSmsNotifications(boolean smsNotifications) { this.smsNotifications = smsNotifications; }

        // Helper methods
        public String getPreferredChannel(String eventType) {
            return channelPreferences.getOrDefault(eventType, "EMAIL");
        }

        public boolean isEventTypeEnabled(String eventType) {
            return enabledEventTypes.contains(eventType);
        }

        public boolean isInQuietHours() {
            int currentHour = LocalDateTime.now().getHour();
            return currentHour >= quietHoursStart || currentHour < quietHoursEnd;
        }
    }

    /**
     * Notification record for rate limiting
     */
    public static class NotificationRecord {
        private final String eventType;
        private final LocalDateTime timestamp;

        public NotificationRecord(String eventType, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.timestamp = timestamp;
        }

        public String getEventType() { return eventType; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}


