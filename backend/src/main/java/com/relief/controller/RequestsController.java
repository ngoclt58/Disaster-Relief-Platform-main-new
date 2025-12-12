package com.relief.controller;

import com.relief.dto.NeedsRequestResponse;
import com.relief.dto.RequestsFilter;
import com.relief.entity.NeedsRequest;
import com.relief.service.RequestQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "List and filter requests")
public class RequestsController {

    private final RequestQueryService requestQueryService;

    @GetMapping
    @Operation(summary = "List requests with filters and optional bbox")
    public ResponseEntity<Page<NeedsRequestResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minSeverity,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String bbox,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Log for debugging - track all incoming requests
        System.out.println("=== GET /requests called ===");
        System.out.println("Parameters: status=" + status + ", type=" + type + ", minSeverity=" + minSeverity + 
            ", from=" + from + ", to=" + to + ", bbox=" + bbox + ", page=" + page + ", size=" + size);
        
        RequestsFilter filter = new RequestsFilter();
        filter.setStatus(status);
        filter.setType(type);
        filter.setMinSeverity(minSeverity);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setBbox(bbox);
        
        Page<NeedsRequest> needsPage = requestQueryService.search(filter, page, size);
        
        // Log for debugging
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestsController.class);
        log.info("GET /requests - Filter: {}", filter);
        log.info("GET /requests - Found {} requests (page {}, size {})", 
            needsPage.getTotalElements(), page, size);
        log.info("GET /requests - First few requests: {}", 
            needsPage.getContent().stream().limit(3).map(nr -> 
                String.format("ID=%s, type=%s, status=%s, hasLocation=%s", 
                    nr.getId(), nr.getType(), nr.getStatus(), nr.getGeomPoint() != null)
            ).toList());
        
        // Convert entities to response DTOs safely
        List<NeedsRequestResponse> responseList = needsPage.getContent().stream()
            .map(nr -> {
                try {
                    return NeedsRequestResponse.fromEntity(nr);
                } catch (Exception e) {
                    log.error("Error converting entity to response: {}", e.getMessage());
                    // Return a minimal response if conversion fails
                    return NeedsRequestResponse.builder()
                        .id(nr.getId())
                        .type(nr.getType())
                        .category(nr.getType())
                        .severity(nr.getSeverity())
                        .status(nr.getStatus())
                        .notes(nr.getNotes())
                        .description(nr.getNotes())
                        .createdAt(nr.getCreatedAt())
                        .build();
                }
            })
            .peek(response -> {
                log.debug("Request {}: type={}, status={}, hasLocation={}", 
                    response.getId(), response.getType(), response.getStatus(),
                    response.getLocation() != null);
            })
            .collect(Collectors.toList());
            
        Page<NeedsRequestResponse> responsePage = new PageImpl<>(
            responseList,
            needsPage.getPageable(),
            needsPage.getTotalElements()
        );
        
        return ResponseEntity.ok(responsePage);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update request status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request
    ) {
        System.out.println("=== PATCH /requests/" + id + "/status called ===");
        System.out.println("New status: " + request.getStatus());
        
        try {
            UUID requestId = UUID.fromString(id);
            requestQueryService.updateStatus(requestId, request.getStatus());
            
            System.out.println("Successfully updated request " + id + " status to " + request.getStatus());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error updating request status: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO for status update request
    public static class UpdateStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}





