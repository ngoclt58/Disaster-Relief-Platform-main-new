package com.relief.service.communication;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for video conferencing and remote coordination
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoConferencingService {

    private static final Logger log = LoggerFactory.getLogger(VideoConferencingService.class);

    private final UserRepository userRepository;
    private final Map<String, VideoConference> activeConferences = new ConcurrentHashMap<>();
    private final Map<String, List<ConferenceParticipant>> conferenceParticipants = new ConcurrentHashMap<>();

    /**
     * Create a new video conference
     */
    @Transactional
    public VideoConference createConference(String title, String description, UUID organizerId, ConferenceType type) {
        log.info("Creating video conference: {} by user {}", title, organizerId);
        
        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        
        VideoConference conference = new VideoConference();
        conference.setId(UUID.randomUUID().toString());
        conference.setTitle(title);
        conference.setDescription(description);
        conference.setOrganizerId(organizerId);
        conference.setOrganizerName(organizer.getFullName());
        conference.setType(type);
        conference.setStatus(ConferenceStatus.SCHEDULED);
        conference.setCreatedAt(LocalDateTime.now());
        conference.setMaxParticipants(getMaxParticipantsForType(type));
        conference.setDurationMinutes(getDefaultDurationForType(type));
        
        // Generate meeting room details
        conference.setRoomId(generateRoomId());
        conference.setMeetingUrl(generateMeetingUrl(conference.getRoomId()));
        conference.setPasscode(generatePasscode());
        
        activeConferences.put(conference.getId(), conference);
        conferenceParticipants.put(conference.getId(), new ArrayList<>());
        
        log.info("Video conference created: {} with room ID: {}", conference.getId(), conference.getRoomId());
        return conference;
    }

    /**
     * Join a video conference
     */
    @Transactional
    public ConferenceJoinResult joinConference(String conferenceId, UUID userId) {
        log.info("User {} joining conference {}", userId, conferenceId);
        
        VideoConference conference = activeConferences.get(conferenceId);
        if (conference == null) {
            throw new IllegalArgumentException("Conference not found");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if conference is full
        List<ConferenceParticipant> participants = conferenceParticipants.get(conferenceId);
        if (participants.size() >= conference.getMaxParticipants()) {
            throw new IllegalStateException("Conference is full");
        }
        
        // Check if user is already in conference
        boolean alreadyJoined = participants.stream()
            .anyMatch(p -> p.getUserId().equals(userId));
        
        if (alreadyJoined) {
            throw new IllegalStateException("User already in conference");
        }
        
        // Add participant
        ConferenceParticipant participant = new ConferenceParticipant();
        participant.setUserId(userId);
        participant.setUserName(user.getFullName());
        participant.setUserEmail(user.getEmail());
        participant.setJoinedAt(LocalDateTime.now());
        participant.setRole(conference.getOrganizerId().equals(userId) ? ParticipantRole.HOST : ParticipantRole.PARTICIPANT);
        
        participants.add(participant);
        
        // Update conference status if first participant
        if (participants.size() == 1) {
            conference.setStatus(ConferenceStatus.ACTIVE);
            conference.setStartedAt(LocalDateTime.now());
        }
        
        ConferenceJoinResult result = new ConferenceJoinResult();
        result.setConference(conference);
        result.setParticipant(participant);
        result.setWebRTCConfig(generateWebRTCConfig(conference));
        result.setIceServers(getIceServers());
        
        log.info("User {} successfully joined conference {}", userId, conferenceId);
        return result;
    }

    /**
     * Leave a video conference
     */
    @Transactional
    public void leaveConference(String conferenceId, UUID userId) {
        log.info("User {} leaving conference {}", userId, conferenceId);
        
        List<ConferenceParticipant> participants = conferenceParticipants.get(conferenceId);
        if (participants != null) {
            participants.removeIf(p -> p.getUserId().equals(userId));
            
            // If no participants left, end conference
            if (participants.isEmpty()) {
                VideoConference conference = activeConferences.get(conferenceId);
                if (conference != null) {
                    conference.setStatus(ConferenceStatus.ENDED);
                    conference.setEndedAt(LocalDateTime.now());
                }
            }
        }
        
        log.info("User {} left conference {}", userId, conferenceId);
    }

    /**
     * Get active conferences for a user
     */
    public List<VideoConference> getUserConferences(UUID userId) {
        return activeConferences.values().stream()
            .filter(conference -> {
                List<ConferenceParticipant> participants = conferenceParticipants.get(conference.getId());
                return participants != null && participants.stream()
                    .anyMatch(p -> p.getUserId().equals(userId));
            })
            .sorted(Comparator.comparing(VideoConference::getCreatedAt).reversed())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Get conference participants
     */
    public List<ConferenceParticipant> getConferenceParticipants(String conferenceId) {
        return conferenceParticipants.getOrDefault(conferenceId, new ArrayList<>());
    }

    /**
     * Update conference settings
     */
    @Transactional
    public VideoConference updateConferenceSettings(String conferenceId, ConferenceSettings settings) {
        VideoConference conference = activeConferences.get(conferenceId);
        if (conference == null) {
            throw new IllegalArgumentException("Conference not found");
        }
        
        if (settings.getTitle() != null) {
            conference.setTitle(settings.getTitle());
        }
        if (settings.getDescription() != null) {
            conference.setDescription(settings.getDescription());
        }
        if (settings.getMaxParticipants() != null) {
            conference.setMaxParticipants(settings.getMaxParticipants());
        }
        if (settings.getDurationMinutes() != null) {
            conference.setDurationMinutes(settings.getDurationMinutes());
        }
        
        conference.setUpdatedAt(LocalDateTime.now());
        
        log.info("Conference {} settings updated", conferenceId);
        return conference;
    }

    /**
     * End a conference
     */
    @Transactional
    public void endConference(String conferenceId, UUID userId) {
        VideoConference conference = activeConferences.get(conferenceId);
        if (conference == null) {
            throw new IllegalArgumentException("Conference not found");
        }
        
        if (!conference.getOrganizerId().equals(userId)) {
            throw new IllegalStateException("Only organizer can end conference");
        }
        
        conference.setStatus(ConferenceStatus.ENDED);
        conference.setEndedAt(LocalDateTime.now());
        
        // Clear participants
        conferenceParticipants.remove(conferenceId);
        
        log.info("Conference {} ended by user {}", conferenceId, userId);
    }

    /**
     * Generate WebRTC configuration
     */
    private WebRTCConfig generateWebRTCConfig(VideoConference conference) {
        WebRTCConfig config = new WebRTCConfig();
        config.setRoomId(conference.getRoomId());
        config.setConferenceId(conference.getId());
        config.setMaxParticipants(conference.getMaxParticipants());
        config.setAudioEnabled(true);
        config.setVideoEnabled(true);
        config.setScreenShareEnabled(true);
        config.setChatEnabled(true);
        config.setRecordingEnabled(conference.getType() == ConferenceType.EMERGENCY);
        return config;
    }

    /**
     * Get ICE servers for WebRTC
     */
    private List<IceServer> getIceServers() {
        List<IceServer> iceServers = new ArrayList<>();
        
        // Google STUN servers
        iceServers.add(new IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun1.l.google.com:19302"));
        
        // Add TURN servers in production
        // iceServers.add(new IceServer("turn:your-turn-server.com:3478", "username", "password"));
        
        return iceServers;
    }

    /**
     * Generate unique room ID
     */
    private String generateRoomId() {
        return "room_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Generate meeting URL
     */
    private String generateMeetingUrl(String roomId) {
        return "https://relief-platform.com/meeting/" + roomId;
    }

    /**
     * Generate passcode
     */
    private String generatePasscode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * Get max participants for conference type
     */
    private int getMaxParticipantsForType(ConferenceType type) {
        switch (type) {
            case EMERGENCY: return 20;
            case COORDINATION: return 50;
            case TRAINING: return 100;
            case MEETING: return 10;
            default: return 25;
        }
    }

    /**
     * Get default duration for conference type
     */
    private int getDefaultDurationForType(ConferenceType type) {
        switch (type) {
            case EMERGENCY: return 60;
            case COORDINATION: return 120;
            case TRAINING: return 180;
            case MEETING: return 60;
            default: return 90;
        }
    }

    // Data classes
    public static class VideoConference {
        private String id;
        private String title;
        private String description;
        private UUID organizerId;
        private String organizerName;
        private ConferenceType type;
        private ConferenceStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private LocalDateTime updatedAt;
        private int maxParticipants;
        private int durationMinutes;
        private String roomId;
        private String meetingUrl;
        private String passcode;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public UUID getOrganizerId() { return organizerId; }
        public void setOrganizerId(UUID organizerId) { this.organizerId = organizerId; }

        public String getOrganizerName() { return organizerName; }
        public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

        public ConferenceType getType() { return type; }
        public void setType(ConferenceType type) { this.type = type; }

        public ConferenceStatus getStatus() { return status; }
        public void setStatus(ConferenceStatus status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

        public LocalDateTime getEndedAt() { return endedAt; }
        public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public int getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getMeetingUrl() { return meetingUrl; }
        public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

        public String getPasscode() { return passcode; }
        public void setPasscode(String passcode) { this.passcode = passcode; }
    }

    public static class ConferenceParticipant {
        private UUID userId;
        private String userName;
        private String userEmail;
        private ParticipantRole role;
        private LocalDateTime joinedAt;
        private LocalDateTime leftAt;
        private boolean audioEnabled = true;
        private boolean videoEnabled = true;
        private boolean screenSharing = false;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public ParticipantRole getRole() { return role; }
        public void setRole(ParticipantRole role) { this.role = role; }

        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

        public LocalDateTime getLeftAt() { return leftAt; }
        public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

        public boolean isAudioEnabled() { return audioEnabled; }
        public void setAudioEnabled(boolean audioEnabled) { this.audioEnabled = audioEnabled; }

        public boolean isVideoEnabled() { return videoEnabled; }
        public void setVideoEnabled(boolean videoEnabled) { this.videoEnabled = videoEnabled; }

        public boolean isScreenSharing() { return screenSharing; }
        public void setScreenSharing(boolean screenSharing) { this.screenSharing = screenSharing; }
    }

    public static class ConferenceJoinResult {
        private VideoConference conference;
        private ConferenceParticipant participant;
        private WebRTCConfig webRTCConfig;
        private List<IceServer> iceServers;

        // Getters and setters
        public VideoConference getConference() { return conference; }
        public void setConference(VideoConference conference) { this.conference = conference; }

        public ConferenceParticipant getParticipant() { return participant; }
        public void setParticipant(ConferenceParticipant participant) { this.participant = participant; }

        public WebRTCConfig getWebRTCConfig() { return webRTCConfig; }
        public void setWebRTCConfig(WebRTCConfig webRTCConfig) { this.webRTCConfig = webRTCConfig; }

        public List<IceServer> getIceServers() { return iceServers; }
        public void setIceServers(List<IceServer> iceServers) { this.iceServers = iceServers; }
    }

    public static class ConferenceSettings {
        private String title;
        private String description;
        private Integer maxParticipants;
        private Integer durationMinutes;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    }

    public static class WebRTCConfig {
        private String roomId;
        private String conferenceId;
        private int maxParticipants;
        private boolean audioEnabled;
        private boolean videoEnabled;
        private boolean screenShareEnabled;
        private boolean chatEnabled;
        private boolean recordingEnabled;

        // Getters and setters
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getConferenceId() { return conferenceId; }
        public void setConferenceId(String conferenceId) { this.conferenceId = conferenceId; }

        public int getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

        public boolean isAudioEnabled() { return audioEnabled; }
        public void setAudioEnabled(boolean audioEnabled) { this.audioEnabled = audioEnabled; }

        public boolean isVideoEnabled() { return videoEnabled; }
        public void setVideoEnabled(boolean videoEnabled) { this.videoEnabled = videoEnabled; }

        public boolean isScreenShareEnabled() { return screenShareEnabled; }
        public void setScreenShareEnabled(boolean screenShareEnabled) { this.screenShareEnabled = screenShareEnabled; }

        public boolean isChatEnabled() { return chatEnabled; }
        public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }

        public boolean isRecordingEnabled() { return recordingEnabled; }
        public void setRecordingEnabled(boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }
    }

    public static class IceServer {
        private String url;
        private String username;
        private String credential;

        public IceServer(String url) {
            this.url = url;
        }

        public IceServer(String url, String username, String credential) {
            this.url = url;
            this.username = username;
            this.credential = credential;
        }

        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getCredential() { return credential; }
        public void setCredential(String credential) { this.credential = credential; }
    }

    public enum ConferenceType {
        EMERGENCY, COORDINATION, TRAINING, MEETING
    }

    public enum ConferenceStatus {
        SCHEDULED, ACTIVE, ENDED, CANCELLED
    }

    public enum ParticipantRole {
        HOST, PARTICIPANT, OBSERVER
    }
}


