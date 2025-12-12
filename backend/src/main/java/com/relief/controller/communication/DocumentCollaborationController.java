package com.relief.controller.communication;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.communication.DocumentCollaborationService;
import com.relief.service.communication.DocumentCollaborationService.CollaborativeDocument;
import com.relief.service.communication.DocumentCollaborationService.DocumentJoinResult;
import com.relief.service.communication.DocumentCollaborationService.DocumentChangeResult;
import com.relief.service.communication.DocumentCollaborationService.DocumentParticipant;
import com.relief.service.communication.DocumentCollaborationService.DocumentChange;
import com.relief.service.communication.DocumentCollaborationService.DocumentPermissions;
import com.relief.service.communication.DocumentCollaborationService.DocumentType;
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
 * Document collaboration controller
 */
@RestController
@RequestMapping("/document-collaboration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Collaboration", description = "Real-time document collaboration APIs")
public class DocumentCollaborationController {

    private final DocumentCollaborationService documentCollaborationService;
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
    @Operation(summary = "Create a new collaborative document")
    public ResponseEntity<CollaborativeDocument> createDocument(
            @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        CollaborativeDocument document = documentCollaborationService.createDocument(
            request.getTitle(),
            request.getContent(),
            userId,
            request.getType()
        );
        
        return ResponseEntity.ok(document);
    }

    @PostMapping("/{documentId}/join")
    @Operation(summary = "Join a collaborative document")
    public ResponseEntity<DocumentJoinResult> joinDocument(
            @PathVariable String documentId,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        DocumentJoinResult result = documentCollaborationService.joinDocument(documentId, userId);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{documentId}/changes")
    @Operation(summary = "Apply changes to document")
    public ResponseEntity<DocumentChangeResult> applyChanges(
            @PathVariable String documentId,
            @RequestBody ApplyChangesRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        DocumentChangeResult result = documentCollaborationService.applyChanges(
            documentId,
            request.getChanges(),
            userId
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document details")
    public ResponseEntity<CollaborativeDocument> getDocument(
            @PathVariable String documentId) {
        
        CollaborativeDocument document = documentCollaborationService.getDocument(documentId);
        
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{documentId}/participants")
    @Operation(summary = "Get document participants")
    public ResponseEntity<List<DocumentParticipant>> getDocumentParticipants(
            @PathVariable String documentId) {
        
        List<DocumentParticipant> participants = documentCollaborationService.getDocumentParticipants(documentId);
        
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/{documentId}/changes")
    @Operation(summary = "Get document changes history")
    public ResponseEntity<List<DocumentChange>> getDocumentChanges(
            @PathVariable String documentId,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<DocumentChange> changes = documentCollaborationService.getDocumentChanges(documentId, limit);
        
        return ResponseEntity.ok(changes);
    }

    @PutMapping("/{documentId}/permissions")
    @Operation(summary = "Update document permissions")
    public ResponseEntity<CollaborativeDocument> updatePermissions(
            @PathVariable String documentId,
            @RequestBody DocumentPermissions permissions,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        CollaborativeDocument document = documentCollaborationService.updatePermissions(
            documentId,
            permissions,
            userId
        );
        
        return ResponseEntity.ok(document);
    }

    @PostMapping("/{documentId}/leave")
    @Operation(summary = "Leave document")
    public ResponseEntity<Map<String, String>> leaveDocument(
            @PathVariable String documentId,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        documentCollaborationService.leaveDocument(documentId, userId);
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Left document successfully"));
    }

    @GetMapping("/my-documents")
    @Operation(summary = "Get user's documents")
    public ResponseEntity<List<CollaborativeDocument>> getUserDocuments(
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        List<CollaborativeDocument> documents = documentCollaborationService.getUserDocuments(userId);
        
        return ResponseEntity.ok(documents);
    }

    // Request DTOs
    public static class CreateDocumentRequest {
        private String title;
        private String content;
        private DocumentType type;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public DocumentType getType() { return type; }
        public void setType(DocumentType type) { this.type = type; }
    }

    public static class ApplyChangesRequest {
        private List<DocumentChange> changes;

        // Getters and setters
        public List<DocumentChange> getChanges() { return changes; }
        public void setChanges(List<DocumentChange> changes) { this.changes = changes; }
    }
}


