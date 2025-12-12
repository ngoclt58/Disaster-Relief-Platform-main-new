package com.relief.controller;

import com.relief.dto.RequestsFilter;
import com.relief.entity.NeedsRequest;
import com.relief.service.RequestQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "List and filter requests")
public class RequestsController {

    private final RequestQueryService requestQueryService;

    @GetMapping
    @Operation(summary = "List requests with filters and optional bbox")
    public ResponseEntity<Page<NeedsRequest>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minSeverity,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String bbox,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RequestsFilter filter = new RequestsFilter();
        filter.setStatus(status);
        filter.setType(type);
        filter.setMinSeverity(minSeverity);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setBbox(bbox);
        return ResponseEntity.ok(requestQueryService.search(filter, page, size));
    }
}





