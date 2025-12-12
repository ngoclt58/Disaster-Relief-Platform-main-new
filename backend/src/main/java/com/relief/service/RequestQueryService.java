package com.relief.service;

import com.relief.dto.RequestsFilter;
import com.relief.entity.NeedsRequest;
import com.relief.repository.NeedsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

        if (filter.getStatus() != null) {
            return needsRequestRepository.findByStatus(filter.getStatus(), pageable);
        }

        if (filter.getType() != null) {
            return needsRequestRepository.findByType(filter.getType(), pageable);
        }

        if (filter.getMinSeverity() != null) {
            return needsRequestRepository.findByMinSeverity(filter.getMinSeverity(), pageable);
        }

        if (filter.getFrom() != null && filter.getTo() != null) {
            LocalDateTime from = LocalDateTime.ofInstant(Instant.parse(filter.getFrom()), ZoneOffset.UTC);
            LocalDateTime to = LocalDateTime.ofInstant(Instant.parse(filter.getTo()), ZoneOffset.UTC);
            return needsRequestRepository.findByDateRange(from, to, pageable);
        }

        return needsRequestRepository.findByStatus("new", pageable);
    }
}





