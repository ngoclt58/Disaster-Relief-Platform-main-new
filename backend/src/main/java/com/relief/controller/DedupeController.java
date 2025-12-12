package com.relief.controller;

import com.relief.security.Permission;
import com.relief.security.RequiresPermission;
import com.relief.service.DedupeService;
import com.relief.service.DedupeService.DedupeCandidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dedupe")
@RequiredArgsConstructor
@Tag(name = "Dedupe", description = "Endpoints for duplicate grouping and merging")
public class DedupeController {

    private final DedupeService dedupeService;

    @PostMapping("/groups")
    @Operation(summary = "Create a dedupe group with candidate links")
    @RequiresPermission(Permission.ADMIN_USERS)
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody CreateGroupRequest request,
                                                           Authentication authentication) {
        var user = (com.relief.entity.User) authentication.getPrincipal();
        var group = dedupeService.createGroup(request.entityType, request.toCandidates(), user, request.note);
        return ResponseEntity.ok(Map.of("id", group.getId(), "status", group.getStatus()));
    }

    @GetMapping("/groups/{groupId}/links")
    @Operation(summary = "List links for a dedupe group")
    @RequiresPermission(Permission.ADMIN_USERS)
    public ResponseEntity<List<?>> listLinks(@PathVariable UUID groupId) {
        return ResponseEntity.ok(dedupeService.getGroupLinks(groupId));
    }

    @PostMapping("/groups/{groupId}/dismiss")
    @Operation(summary = "Dismiss a dedupe group")
    @RequiresPermission(Permission.ADMIN_USERS)
    public ResponseEntity<?> dismiss(@PathVariable UUID groupId, @RequestBody(required = false) Map<String, String> body,
                                     Authentication authentication) {
        var user = (com.relief.entity.User) authentication.getPrincipal();
        dedupeService.dismissGroup(groupId, user, body != null ? body.getOrDefault("reason", "") : "");
        return ResponseEntity.ok(Map.of("status", "DISMISSED"));
    }

    @PostMapping("/groups/{groupId}/merge")
    @Operation(summary = "Merge a dedupe group into a canonical entity")
    @RequiresPermission(Permission.ADMIN_USERS)
    public ResponseEntity<?> merge(@PathVariable UUID groupId, @RequestBody Map<String, String> body,
                                   Authentication authentication) {
        var user = (com.relief.entity.User) authentication.getPrincipal();
        UUID canonical = UUID.fromString(body.get("canonicalId"));
        dedupeService.mergeGroup(groupId, canonical, user);
        return ResponseEntity.ok(Map.of("status", "MERGED"));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge duplicate requests into a group")
    @RequiresPermission(Permission.ADMIN_USERS)
    public ResponseEntity<Result> mergeRequests(@RequestBody MergeBody body) {
        int updated = dedupeService.mergeRequests(body.getRequestIds(), body.getReason());
        Result result = new Result();
        result.setUpdated(updated);
        return ResponseEntity.ok(result);
    }

    public static class CreateGroupRequest {
        public String entityType;
        public String note;
        public List<Link> links;

        public List<DedupeCandidate> toCandidates() {
            return links == null ? List.of() : links.stream()
                    .map(l -> new DedupeCandidate(UUID.fromString(l.entityId), l.score, l.reason))
                    .toList();
        }
    }

    public static class Link {
        public String entityId;
        public Double score;
        public String reason;
    }

    public static class MergeBody {
        private List<UUID> requestIds;
        private String reason;

        public List<UUID> getRequestIds() {
            return requestIds;
        }

        public void setRequestIds(List<UUID> requestIds) {
            this.requestIds = requestIds;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class Result {
        private int updated;

        public int getUpdated() {
            return updated;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }
    }
}
