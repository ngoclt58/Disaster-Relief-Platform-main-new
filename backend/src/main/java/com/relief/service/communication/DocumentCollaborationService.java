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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Real-time document collaboration service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCollaborationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentCollaborationService.class);
    private final UserRepository userRepository;
    private final Map<String, CollaborativeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, List<DocumentParticipant>> documentParticipants = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> documentLocks = new ConcurrentHashMap<>();
    private final Map<String, List<DocumentChange>> documentChanges = new ConcurrentHashMap<>();

    /**
     * Create a new collaborative document
     */
    @Transactional
    public CollaborativeDocument createDocument(String title, String content, UUID creatorId, DocumentType type) {
        log.info("Creating collaborative document: {} by user {}", title, creatorId);
        
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        
        CollaborativeDocument document = new CollaborativeDocument();
        document.setId(UUID.randomUUID().toString());
        document.setTitle(title);
        document.setContent(content);
        document.setType(type);
        document.setCreatorId(creatorId);
        document.setCreatorName(creator.getFullName());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document.setStatus(DocumentStatus.ACTIVE);
        document.setVersion(1);
        document.setPermissions(DocumentPermissions.getDefaultPermissions());
        
        documents.put(document.getId(), document);
        documentParticipants.put(document.getId(), new ArrayList<>());
        documentChanges.put(document.getId(), new ArrayList<>());
        documentLocks.put(document.getId(), new ReentrantReadWriteLock());
        
        // Add creator as participant
        addParticipant(document.getId(), creatorId, ParticipantRole.OWNER);
        
        log.info("Collaborative document created: {} with ID: {}", document.getId(), document.getId());
        return document;
    }

    /**
     * Join a collaborative document
     */
    @Transactional
    public DocumentJoinResult joinDocument(String documentId, UUID userId) {
        log.info("User {} joining document {}", userId, documentId);
        
        CollaborativeDocument document = documents.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }
        
        // Check if user is already in document
        List<DocumentParticipant> participants = documentParticipants.get(documentId);
        boolean alreadyJoined = participants.stream()
            .anyMatch(p -> p.getUserId().equals(userId));
        
        if (alreadyJoined) {
            throw new IllegalStateException("User already in document");
        }
        
        // Add participant
        addParticipant(documentId, userId, ParticipantRole.COLLABORATOR);
        
        DocumentJoinResult result = new DocumentJoinResult();
        result.setDocument(document);
        result.setParticipant(getParticipant(documentId, userId));
        result.setWebSocketUrl(generateWebSocketUrl(documentId));
        result.setCollaborationConfig(generateCollaborationConfig(document));
        
        log.info("User {} successfully joined document {}", userId, documentId);
        return result;
    }

    /**
     * Apply changes to document
     */
    @Transactional
    public DocumentChangeResult applyChanges(String documentId, List<DocumentChange> changes, UUID userId) {
        log.info("Applying {} changes to document {} by user {}", changes.size(), documentId, userId);
        
        CollaborativeDocument document = documents.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }
        
        DocumentParticipant participant = getParticipant(documentId, userId);
        if (participant == null) {
            throw new IllegalStateException("User not in document");
        }
        
        ReadWriteLock lock = documentLocks.get(documentId);
        lock.writeLock().lock();
        
        try {
            List<DocumentChange> appliedChanges = new ArrayList<>();
            String newContent = document.getContent();
            
            for (DocumentChange change : changes) {
                if (isValidChange(change, newContent)) {
                    newContent = applyChange(newContent, change);
                    change.setAppliedAt(LocalDateTime.now());
                    change.setAppliedBy(userId);
                    appliedChanges.add(change);
                }
            }
            
            if (!appliedChanges.isEmpty()) {
                document.setContent(newContent);
                document.setUpdatedAt(LocalDateTime.now());
                document.setVersion(document.getVersion() + 1);
                
                // Store changes
                documentChanges.get(documentId).addAll(appliedChanges);
                
                // Update participant activity
                participant.setLastActivity(LocalDateTime.now());
                participant.setChangesCount(participant.getChangesCount() + appliedChanges.size());
            }
            
            DocumentChangeResult result = new DocumentChangeResult();
            result.setDocument(document);
            result.setAppliedChanges(appliedChanges);
            result.setVersion(document.getVersion());
            result.setSuccess(true);
            
            log.info("Applied {} changes to document {} (version: {})", appliedChanges.size(), documentId, document.getVersion());
            return result;
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get document with current content
     */
    @Transactional(readOnly = true)
    public CollaborativeDocument getDocument(String documentId) {
        CollaborativeDocument document = documents.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }
        return document;
    }

    /**
     * Get document participants
     */
    @Transactional(readOnly = true)
    public List<DocumentParticipant> getDocumentParticipants(String documentId) {
        return documentParticipants.getOrDefault(documentId, new ArrayList<>());
    }

    /**
     * Get document changes history
     */
    @Transactional(readOnly = true)
    public List<DocumentChange> getDocumentChanges(String documentId, int limit) {
        List<DocumentChange> changes = documentChanges.getOrDefault(documentId, new ArrayList<>());
        return changes.stream()
            .sorted(Comparator.comparing(DocumentChange::getAppliedAt).reversed())
            .limit(limit)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Update document permissions
     */
    @Transactional
    public CollaborativeDocument updatePermissions(String documentId, DocumentPermissions permissions, UUID userId) {
        CollaborativeDocument document = documents.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }
        
        DocumentParticipant participant = getParticipant(documentId, userId);
        if (participant == null || participant.getRole() != ParticipantRole.OWNER) {
            throw new IllegalStateException("Only document owner can update permissions");
        }
        
        document.setPermissions(permissions);
        document.setUpdatedAt(LocalDateTime.now());
        
        log.info("Document {} permissions updated by user {}", documentId, userId);
        return document;
    }

    /**
     * Leave document
     */
    @Transactional
    public void leaveDocument(String documentId, UUID userId) {
        log.info("User {} leaving document {}", userId, documentId);
        
        List<DocumentParticipant> participants = documentParticipants.get(documentId);
        if (participants != null) {
            participants.removeIf(p -> p.getUserId().equals(userId));
            
            // If no participants left, archive document
            if (participants.isEmpty()) {
                CollaborativeDocument document = documents.get(documentId);
                if (document != null) {
                    document.setStatus(DocumentStatus.ARCHIVED);
                    document.setUpdatedAt(LocalDateTime.now());
                }
            }
        }
        
        log.info("User {} left document {}", userId, documentId);
    }

    /**
     * Get user's documents
     */
    @Transactional(readOnly = true)
    public List<CollaborativeDocument> getUserDocuments(UUID userId) {
        return documents.values().stream()
            .filter(doc -> {
                List<DocumentParticipant> participants = documentParticipants.get(doc.getId());
                return participants != null && participants.stream()
                    .anyMatch(p -> p.getUserId().equals(userId));
            })
            .sorted(Comparator.comparing(CollaborativeDocument::getUpdatedAt).reversed())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Add participant to document
     */
    private void addParticipant(String documentId, UUID userId, ParticipantRole role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        DocumentParticipant participant = new DocumentParticipant();
        participant.setUserId(userId);
        participant.setUserName(user.getFullName());
        participant.setUserEmail(user.getEmail());
        participant.setRole(role);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setLastActivity(LocalDateTime.now());
        participant.setChangesCount(0);
        
        documentParticipants.get(documentId).add(participant);
    }

    /**
     * Get participant from document
     */
    private DocumentParticipant getParticipant(String documentId, UUID userId) {
        List<DocumentParticipant> participants = documentParticipants.get(documentId);
        if (participants != null) {
            return participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    /**
     * Check if change is valid
     */
    private boolean isValidChange(DocumentChange change, String content) {
        // Basic validation - in real implementation, use operational transformation
        return change.getPosition() >= 0 && change.getPosition() <= content.length();
    }

    /**
     * Apply change to content
     */
    private String applyChange(String content, DocumentChange change) {
        switch (change.getType()) {
            case INSERT:
                return content.substring(0, change.getPosition()) + 
                       change.getText() + 
                       content.substring(change.getPosition());
            case DELETE:
                int endPos = change.getPosition() + change.getLength();
                return content.substring(0, change.getPosition()) + 
                       content.substring(endPos);
            case REPLACE:
                int replaceEndPos = change.getPosition() + change.getLength();
                return content.substring(0, change.getPosition()) + 
                       change.getText() + 
                       content.substring(replaceEndPos);
            default:
                return content;
        }
    }

    /**
     * Generate WebSocket URL
     */
    private String generateWebSocketUrl(String documentId) {
        return "ws://relief-platform.com/ws/document/" + documentId;
    }

    /**
     * Generate collaboration config
     */
    private CollaborationConfig generateCollaborationConfig(CollaborativeDocument document) {
        CollaborationConfig config = new CollaborationConfig();
        config.setDocumentId(document.getId());
        config.setVersion(document.getVersion());
        config.setMaxParticipants(50);
        config.setRealTimeSync(true);
        config.setConflictResolution(true);
        config.setAutoSave(true);
        config.setAutoSaveInterval(30); // seconds
        return config;
    }

    // Data classes
    public static class CollaborativeDocument {
        private String id;
        private String title;
        private String content;
        private DocumentType type;
        private UUID creatorId;
        private String creatorName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private DocumentStatus status;
        private int version;
        private DocumentPermissions permissions;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public DocumentType getType() { return type; }
        public void setType(DocumentType type) { this.type = type; }

        public UUID getCreatorId() { return creatorId; }
        public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }

        public String getCreatorName() { return creatorName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public DocumentStatus getStatus() { return status; }
        public void setStatus(DocumentStatus status) { this.status = status; }

        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }

        public DocumentPermissions getPermissions() { return permissions; }
        public void setPermissions(DocumentPermissions permissions) { this.permissions = permissions; }
    }

    public static class DocumentParticipant {
        private UUID userId;
        private String userName;
        private String userEmail;
        private ParticipantRole role;
        private LocalDateTime joinedAt;
        private LocalDateTime lastActivity;
        private int changesCount;

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

        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

        public int getChangesCount() { return changesCount; }
        public void setChangesCount(int changesCount) { this.changesCount = changesCount; }
    }

    public static class DocumentChange {
        private String id;
        private String documentId;
        private ChangeType type;
        private int position;
        private int length;
        private String text;
        private UUID appliedBy;
        private LocalDateTime appliedAt;

        public DocumentChange() {
            this.id = UUID.randomUUID().toString();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

        public ChangeType getType() { return type; }
        public void setType(ChangeType type) { this.type = type; }

        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }

        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public UUID getAppliedBy() { return appliedBy; }
        public void setAppliedBy(UUID appliedBy) { this.appliedBy = appliedBy; }

        public LocalDateTime getAppliedAt() { return appliedAt; }
        public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    }

    public static class DocumentJoinResult {
        private CollaborativeDocument document;
        private DocumentParticipant participant;
        private String webSocketUrl;
        private CollaborationConfig collaborationConfig;

        // Getters and setters
        public CollaborativeDocument getDocument() { return document; }
        public void setDocument(CollaborativeDocument document) { this.document = document; }

        public DocumentParticipant getParticipant() { return participant; }
        public void setParticipant(DocumentParticipant participant) { this.participant = participant; }

        public String getWebSocketUrl() { return webSocketUrl; }
        public void setWebSocketUrl(String webSocketUrl) { this.webSocketUrl = webSocketUrl; }

        public CollaborationConfig getCollaborationConfig() { return collaborationConfig; }
        public void setCollaborationConfig(CollaborationConfig collaborationConfig) { this.collaborationConfig = collaborationConfig; }
    }

    public static class DocumentChangeResult {
        private CollaborativeDocument document;
        private List<DocumentChange> appliedChanges;
        private int version;
        private boolean success;

        // Getters and setters
        public CollaborativeDocument getDocument() { return document; }
        public void setDocument(CollaborativeDocument document) { this.document = document; }

        public List<DocumentChange> getAppliedChanges() { return appliedChanges; }
        public void setAppliedChanges(List<DocumentChange> appliedChanges) { this.appliedChanges = appliedChanges; }

        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    public static class DocumentPermissions {
        private boolean canEdit = true;
        private boolean canComment = true;
        private boolean canShare = true;
        private boolean canDelete = false;
        private boolean canManagePermissions = false;

        public static DocumentPermissions getDefaultPermissions() {
            return new DocumentPermissions();
        }

        // Getters and setters
        public boolean isCanEdit() { return canEdit; }
        public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

        public boolean isCanComment() { return canComment; }
        public void setCanComment(boolean canComment) { this.canComment = canComment; }

        public boolean isCanShare() { return canShare; }
        public void setCanShare(boolean canShare) { this.canShare = canShare; }

        public boolean isCanDelete() { return canDelete; }
        public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

        public boolean isCanManagePermissions() { return canManagePermissions; }
        public void setCanManagePermissions(boolean canManagePermissions) { this.canManagePermissions = canManagePermissions; }
    }

    public static class CollaborationConfig {
        private String documentId;
        private int version;
        private int maxParticipants;
        private boolean realTimeSync;
        private boolean conflictResolution;
        private boolean autoSave;
        private int autoSaveInterval; // seconds

        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }

        public int getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

        public boolean isRealTimeSync() { return realTimeSync; }
        public void setRealTimeSync(boolean realTimeSync) { this.realTimeSync = realTimeSync; }

        public boolean isConflictResolution() { return conflictResolution; }
        public void setConflictResolution(boolean conflictResolution) { this.conflictResolution = conflictResolution; }

        public boolean isAutoSave() { return autoSave; }
        public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }

        public int getAutoSaveInterval() { return autoSaveInterval; }
        public void setAutoSaveInterval(int autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
    }

    public enum DocumentType {
        EMERGENCY_PLAN, COORDINATION_DOC, TRAINING_MATERIAL, REPORT, MEETING_NOTES
    }

    public enum DocumentStatus {
        ACTIVE, ARCHIVED, DELETED
    }

    public enum ParticipantRole {
        OWNER, COLLABORATOR, VIEWER
    }

    public enum ChangeType {
        INSERT, DELETE, REPLACE
    }
}
