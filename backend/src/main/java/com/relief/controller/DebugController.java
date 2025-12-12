package com.relief.controller;

import com.relief.entity.NeedsRequest;
import com.relief.repository.NeedsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final NeedsRequestRepository needsRequestRepository;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        try {
            List<NeedsRequest> requests = needsRequestRepository.findAll();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/requests/count")
    public ResponseEntity<?> getRequestsCount() {
        try {
            long count = needsRequestRepository.count();
            return ResponseEntity.ok("Total requests: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
}