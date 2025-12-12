package com.relief.service;

import com.relief.dto.RequestsFilter;
import com.relief.entity.NeedsRequest;
import com.relief.repository.NeedsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestQueryService {

    private final NeedsRequestRepository needsRequestRepository;

    public Page<NeedsRequest> search(RequestsFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (filter.getBbox() != null && !filter.getBbox().isBlank()) {
            // bbox as POLYGON WKT expected by repo method
            return needsRequestRepository.findWithinBoundingBox(filter.getBbox(), pageable);
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            return needsRequestRepository.findByStatus(filter.getStatus(), pageable);
        }

        if (filter.getType() != null && !filter.getType().isBlank()) {
            return needsRequestRepository.findByType(filter.getType(), pageable);
        }

        if (filter.getMinSeverity() != null) {
            return needsRequestRepository.findByMinSeverity(filter.getMinSeverity(), pageable);
        }

        if (filter.getFrom() != null && filter.getTo() != null 
            && !filter.getFrom().isBlank() && !filter.getTo().isBlank()) {
            LocalDateTime from = LocalDateTime.ofInstant(Instant.parse(filter.getFrom()), ZoneOffset.UTC);
            LocalDateTime to = LocalDateTime.ofInstant(Instant.parse(filter.getTo()), ZoneOffset.UTC);
            return needsRequestRepository.findByDateRange(from, to, pageable);
        }

        // Return all requests when no filter is specified, ordered by creation date (newest first)
        return needsRequestRepository.findAll(pageable);
    }

    @Transactional
    public void updateStatus(UUID requestId, String newStatus) {
        System.out.println("RequestQueryService.updateStatus called for ID: " + requestId + ", status: " + newStatus);
        
        NeedsRequest request = needsRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));
        
        System.out.println("Found request: " + request.getId() + ", current status: " + request.getStatus());
        
        request.setStatus(newStatus);
        needsRequestRepository.save(request);
        
        System.out.println("Successfully updated request status to: " + newStatus);
    }
}





