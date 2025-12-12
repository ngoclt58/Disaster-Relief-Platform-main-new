package com.relief.controller.integration;

import com.relief.service.integration.SocialMediaMonitoringService;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaPost;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaAnalytics;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaInfluencer;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaSentiment;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaHashtag;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaCrisisDetection;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaAlert;
import com.relief.service.integration.SocialMediaMonitoringService.SocialMediaReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Social media monitoring controller
 */
@RestController
@RequestMapping("/integration/social-media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Social Media Monitoring", description = "Track and analyze social media for disaster information")
public class SocialMediaController {

    private final SocialMediaMonitoringService socialMediaMonitoringService;

    @GetMapping("/posts/search")
    @Operation(summary = "Search social media posts")
    public ResponseEntity<List<SocialMediaPost>> searchPosts(
            @RequestParam String query,
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<SocialMediaPost> posts = socialMediaMonitoringService.searchPosts(query, platform, limit);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/trending")
    @Operation(summary = "Get trending posts")
    public ResponseEntity<List<SocialMediaPost>> getTrendingPosts(
            @RequestParam String platform,
            @RequestParam(required = false) String category) {
        
        List<SocialMediaPost> posts = socialMediaMonitoringService.getTrendingPosts(platform, category);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get social media analytics")
    public ResponseEntity<SocialMediaAnalytics> getAnalytics(
            @RequestParam String platform,
            @RequestParam String timeRange) {
        
        SocialMediaAnalytics analytics = socialMediaMonitoringService.getAnalytics(platform, timeRange);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/influencers")
    @Operation(summary = "Get social media influencers")
    public ResponseEntity<List<SocialMediaInfluencer>> getInfluencers(
            @RequestParam String platform,
            @RequestParam(required = false) String category) {
        
        List<SocialMediaInfluencer> influencers = socialMediaMonitoringService.getInfluencers(platform, category);
        return ResponseEntity.ok(influencers);
    }

    @PostMapping("/sentiment")
    @Operation(summary = "Analyze sentiment of text")
    public ResponseEntity<SocialMediaSentiment> analyzeSentiment(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String language) {
        
        SocialMediaSentiment sentiment = socialMediaMonitoringService.getSentimentAnalysis(text, language);
        return ResponseEntity.ok(sentiment);
    }

    @GetMapping("/hashtags/trending")
    @Operation(summary = "Get trending hashtags")
    public ResponseEntity<List<SocialMediaHashtag>> getTrendingHashtags(
            @RequestParam String platform,
            @RequestParam(required = false) String category) {
        
        List<SocialMediaHashtag> hashtags = socialMediaMonitoringService.getTrendingHashtags(platform, category);
        return ResponseEntity.ok(hashtags);
    }

    @GetMapping("/crisis/detect")
    @Operation(summary = "Detect crisis indicators")
    public ResponseEntity<SocialMediaCrisisDetection> detectCrisis(
            @RequestParam String location,
            @RequestParam String timeRange) {
        
        SocialMediaCrisisDetection detection = socialMediaMonitoringService.detectCrisis(location, timeRange);
        return ResponseEntity.ok(detection);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get social media alerts")
    public ResponseEntity<List<SocialMediaAlert>> getAlerts(
            @RequestParam String platform,
            @RequestParam(required = false) String severity) {
        
        List<SocialMediaAlert> alerts = socialMediaMonitoringService.getAlerts(platform, severity);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to keyword monitoring")
    public ResponseEntity<Void> subscribeToKeywords(
            @RequestBody List<String> keywords,
            @RequestParam String callbackUrl) {
        
        socialMediaMonitoringService.subscribeToKeywords(keywords, callbackUrl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports")
    @Operation(summary = "Generate social media report")
    public ResponseEntity<SocialMediaReport> generateReport(
            @RequestParam String platform,
            @RequestParam String timeRange,
            @RequestParam String reportType) {
        
        SocialMediaReport report = socialMediaMonitoringService.generateReport(platform, timeRange, reportType);
        return ResponseEntity.ok(report);
    }
}


