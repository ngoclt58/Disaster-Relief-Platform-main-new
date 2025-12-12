package com.relief.service.ai;

import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.service.DedupeService;
import com.relief.service.DedupeService.DedupeCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-powered duplicate detection service using ML algorithms
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIDedupeService {

    private static final Logger log = LoggerFactory.getLogger(AIDedupeService.class);

    private final NeedsRequestRepository needsRequestRepository;
    private final DedupeService dedupeService;
    private final TextSimilarityService textSimilarityService;
    private final LocationSimilarityService locationSimilarityService;

    /**
     * Automatically detect potential duplicates for a new request
     */
    @Transactional
    public List<DedupeCandidate> findPotentialDuplicates(NeedsRequest newRequest) {
        log.info("Finding potential duplicates for request: {}", newRequest.getId());
        
        // Get recent requests within time window and geographic area
        List<NeedsRequest> candidateRequests = getCandidateRequests(newRequest);
        
        List<DedupeCandidate> duplicates = new ArrayList<>();
        
        for (NeedsRequest candidate : candidateRequests) {
            double similarityScore = calculateSimilarityScore(newRequest, candidate);
            
            if (similarityScore >= 0.7) { // 70% similarity threshold
                String reason = generateSimilarityReason(newRequest, candidate, similarityScore);
                duplicates.add(new DedupeService.DedupeCandidate(
                    candidate.getId(), 
                    similarityScore, 
                    reason
                ));
            }
        }
        
        // Sort by similarity score (highest first)
        duplicates.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        log.info("Found {} potential duplicates for request {}", duplicates.size(), newRequest.getId());
        return duplicates;
    }

    /**
     * Automatically create dedupe group if high-confidence duplicates found
     */
    @Transactional
    public void autoCreateDedupeGroup(NeedsRequest newRequest, User createdBy) {
        List<DedupeCandidate> duplicates = findPotentialDuplicates(newRequest);
        
        // Only auto-create if we have high-confidence duplicates (90%+ similarity)
        List<DedupeCandidate> highConfidenceDuplicates = duplicates.stream()
            .filter(d -> d.score() >= 0.9)
            .collect(Collectors.toList());
            
        if (!highConfidenceDuplicates.isEmpty()) {
            // Add the new request as the first candidate
            List<DedupeCandidate> allCandidates = new ArrayList<>();
            allCandidates.add(new DedupeCandidate(
                newRequest.getId(), 
                1.0, 
                "New request"
            ));
            allCandidates.addAll(highConfidenceDuplicates);
            
            String note = String.format("Auto-detected %d high-confidence duplicates (%.1f%%+ similarity)", 
                highConfidenceDuplicates.size(), 90.0);
                
            dedupeService.createGroup("NEEDS_REQUEST", allCandidates, createdBy, note);
            
            log.info("Auto-created dedupe group for request {} with {} duplicates", 
                newRequest.getId(), highConfidenceDuplicates.size());
        }
    }

    /**
     * Get candidate requests for similarity comparison
     */
    private List<NeedsRequest> getCandidateRequests(NeedsRequest newRequest) {
        LocalDateTime timeWindow = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        
        // Get requests from last 24 hours within 10km radius
        return needsRequestRepository.findRecentRequestsInArea(
            newRequest.getGeomPoint(),
            10000, // 10km radius
            timeWindow
        );
    }

    /**
     * Calculate comprehensive similarity score between two requests
     */
    private double calculateSimilarityScore(NeedsRequest request1, NeedsRequest request2) {
        double textSimilarity = calculateTextSimilarity(request1, request2);
        double locationSimilarity = calculateLocationSimilarity(request1, request2);
        double temporalSimilarity = calculateTemporalSimilarity(request1, request2);
        double categorySimilarity = calculateCategorySimilarity(request1, request2);
        
        // Weighted combination of similarity factors
        return (textSimilarity * 0.4) + 
               (locationSimilarity * 0.3) + 
               (temporalSimilarity * 0.2) + 
               (categorySimilarity * 0.1);
    }

    /**
     * Calculate text similarity using NLP techniques
     */
    private double calculateTextSimilarity(NeedsRequest request1, NeedsRequest request2) {
        String text1 = (request1.getNotes() != null ? request1.getNotes() : "") + 
                      " " + request1.getType();
        String text2 = (request2.getNotes() != null ? request2.getNotes() : "") + 
                      " " + request2.getType();
        
        return textSimilarityService.calculateSimilarity(text1, text2);
    }

    /**
     * Calculate location similarity based on distance
     */
    private double calculateLocationSimilarity(NeedsRequest request1, NeedsRequest request2) {
        if (request1.getGeomPoint() == null || request2.getGeomPoint() == null) {
            return 0.0;
        }
        
        return locationSimilarityService.calculateSimilarity(
            request1.getGeomPoint(), 
            request2.getGeomPoint()
        );
    }

    /**
     * Calculate temporal similarity based on time difference
     */
    private double calculateTemporalSimilarity(NeedsRequest request1, NeedsRequest request2) {
        long hoursDiff = ChronoUnit.HOURS.between(request1.getCreatedAt(), request2.getCreatedAt());
        
        // Similarity decreases with time difference
        if (hoursDiff <= 1) return 1.0;
        if (hoursDiff <= 6) return 0.8;
        if (hoursDiff <= 12) return 0.6;
        if (hoursDiff <= 24) return 0.4;
        return 0.0;
    }

    /**
     * Calculate category similarity
     */
    private double calculateCategorySimilarity(NeedsRequest request1, NeedsRequest request2) {
        if (Objects.equals(request1.getType(), request2.getType())) {
            return 1.0;
        }
        
        // Check for similar categories
        Set<String> similarCategories = Set.of(
            "medical", "health", "emergency",
            "food", "water", "supplies",
            "shelter", "housing", "accommodation"
        );
        
        String type1 = request1.getType().toLowerCase();
        String type2 = request2.getType().toLowerCase();
        
        boolean hasSimilar = similarCategories.stream()
            .anyMatch(cat -> type1.contains(cat) && type2.contains(cat));
            
        return hasSimilar ? 0.5 : 0.0;
    }

    /**
     * Generate human-readable similarity reason
     */
    private String generateSimilarityReason(NeedsRequest request1, NeedsRequest request2, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (Objects.equals(request1.getType(), request2.getType())) {
            reasons.add("same category");
        }
        
        if (request1.getGeomPoint() != null && request2.getGeomPoint() != null) {
            double distance = locationSimilarityService.calculateDistance(
                request1.getGeomPoint(), 
                request2.getGeomPoint()
            );
            if (distance < 1000) {
                reasons.add("within 1km");
            } else if (distance < 5000) {
                reasons.add("within 5km");
            }
        }
        
        long hoursDiff = ChronoUnit.HOURS.between(request1.getCreatedAt(), request2.getCreatedAt());
        if (hoursDiff <= 1) {
            reasons.add("within 1 hour");
        } else if (hoursDiff <= 6) {
            reasons.add("within 6 hours");
        }
        
        String reasonText = reasons.isEmpty() ? "similar content" : String.join(", ", reasons);
        return String.format("%.1f%% similarity (%s)", score * 100, reasonText);
    }
}


