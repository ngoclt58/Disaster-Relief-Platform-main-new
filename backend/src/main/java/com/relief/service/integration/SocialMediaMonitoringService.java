package com.relief.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Social media monitoring service for tracking and analyzing disaster information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialMediaMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaMonitoringService.class);

    private final RestTemplate restTemplate;
    private final IntegrationConfigService integrationConfigService;

    /**
     * Check if social media API is configured
     */
    private boolean isApiConfigured() {
        String apiUrl = integrationConfigService.getSocialMediaApiUrl();
        String apiKey = integrationConfigService.getSocialMediaApiKey();
        // Skip if using placeholder URL or no API key
        return apiUrl != null && !apiUrl.isEmpty() && 
               !apiUrl.contains("api.socialmedia.com") && 
               apiKey != null && !apiKey.isEmpty();
    }

    public List<SocialMediaPost> searchPosts(String query, String platform, int limit) {
        if (!isApiConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/posts/search?query=%s&platform=%s&limit=%d", 
                apiUrl, query, platform, limit);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaPost[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaPost[].class
            );
            
            log.info("Retrieved {} social media posts for query: {}", response.getBody().length, query);
            return Arrays.asList(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Connection timeout or network error - expected when API is not available
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error searching social media posts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SocialMediaPost> getTrendingPosts(String platform, String category) {
        if (!isApiConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/posts/trending?platform=%s&category=%s", apiUrl, platform, category);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaPost[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaPost[].class
            );
            
            log.info("Retrieved {} trending posts from platform: {}", response.getBody().length, platform);
            return Arrays.asList(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error retrieving trending posts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public SocialMediaAnalytics getAnalytics(String platform, String timeRange) {
        if (!isApiConfigured()) {
            return new SocialMediaAnalytics();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/analytics?platform=%s&timeRange=%s", apiUrl, platform, timeRange);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaAnalytics> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaAnalytics.class
            );
            
            log.info("Retrieved social media analytics for platform: {}", platform);
            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return new SocialMediaAnalytics();
        } catch (Exception e) {
            log.warn("Error retrieving social media analytics: {}", e.getMessage());
            return new SocialMediaAnalytics();
        }
    }

    public List<SocialMediaInfluencer> getInfluencers(String platform, String category) {
        if (!isApiConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/influencers?platform=%s&category=%s", apiUrl, platform, category);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaInfluencer[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaInfluencer[].class
            );
            
            log.info("Retrieved {} influencers from platform: {}", response.getBody().length, platform);
            return Arrays.asList(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error retrieving social media influencers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public SocialMediaSentiment getSentimentAnalysis(String text, String language) {
        if (!isApiConfigured()) {
            return new SocialMediaSentiment();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = apiUrl + "/sentiment";
            
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            request.put("language", language);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<SocialMediaSentiment> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, SocialMediaSentiment.class
            );
            
            log.info("Analyzed sentiment for text");
            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return new SocialMediaSentiment();
        } catch (Exception e) {
            log.warn("Error analyzing sentiment: {}", e.getMessage());
            return new SocialMediaSentiment();
        }
    }

    public List<SocialMediaHashtag> getTrendingHashtags(String platform, String category) {
        if (!isApiConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/hashtags/trending?platform=%s&category=%s", apiUrl, platform, category);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaHashtag[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaHashtag[].class
            );
            
            log.info("Retrieved {} trending hashtags from platform: {}", response.getBody().length, platform);
            return Arrays.asList(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error retrieving trending hashtags: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public SocialMediaCrisisDetection detectCrisis(String location, String timeRange) {
        if (!isApiConfigured()) {
            return new SocialMediaCrisisDetection();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/crisis/detect?location=%s&timeRange=%s", apiUrl, location, timeRange);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaCrisisDetection> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaCrisisDetection.class
            );
            
            log.info("Detected crisis indicators for location: {}", location);
            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return new SocialMediaCrisisDetection();
        } catch (Exception e) {
            log.warn("Error detecting crisis indicators: {}", e.getMessage());
            return new SocialMediaCrisisDetection();
        }
    }

    public List<SocialMediaAlert> getAlerts(String platform, String severity) {
        if (!isApiConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/alerts?platform=%s&severity=%s", apiUrl, platform, severity);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaAlert[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaAlert[].class
            );
            
            log.info("Retrieved {} social media alerts", response.getBody().length);
            return Arrays.asList(response.getBody());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error retrieving social media alerts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void subscribeToKeywords(List<String> keywords, String callbackUrl) {
        if (!isApiConfigured()) {
            return;
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = apiUrl + "/subscriptions";
            
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("keywords", keywords);
            subscription.put("callbackUrl", callbackUrl);
            subscription.put("createdAt", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(subscription, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
            );
            
            log.info("Subscribed to keywords: {}", keywords);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Error subscribing to keywords: {}", e.getMessage());
        }
    }

    public SocialMediaReport generateReport(String platform, String timeRange, String reportType) {
        if (!isApiConfigured()) {
            return new SocialMediaReport();
        }
        
        try {
            String apiUrl = integrationConfigService.getSocialMediaApiUrl();
            String endpoint = String.format("%s/reports?platform=%s&timeRange=%s&type=%s", 
                apiUrl, platform, timeRange, reportType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getSocialMediaApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SocialMediaReport> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SocialMediaReport.class
            );
            
            log.info("Generated social media report for platform: {}", platform);
            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.debug("Social media API unavailable (connection timeout): {}", e.getMessage());
            return new SocialMediaReport();
        } catch (Exception e) {
            log.warn("Error generating social media report: {}", e.getMessage());
            return new SocialMediaReport();
        }
    }

    // Data classes
    public static class SocialMediaPost {
        private String id;
        private String platform;
        private String author;
        private String content;
        private LocalDateTime publishedAt;
        private int likes;
        private int shares;
        private int comments;
        private List<String> hashtags;
        private String location;
        private String sentiment;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }

        public int getShares() { return shares; }
        public void setShares(int shares) { this.shares = shares; }

        public int getComments() { return comments; }
        public void setComments(int comments) { this.comments = comments; }

        public List<String> getHashtags() { return hashtags; }
        public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class SocialMediaAnalytics {
        private String platform;
        private String timeRange;
        private int totalPosts;
        private int totalEngagement;
        private double averageSentiment;
        private List<String> topHashtags;
        private List<String> topInfluencers;
        private Map<String, Object> demographics;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

        public int getTotalPosts() { return totalPosts; }
        public void setTotalPosts(int totalPosts) { this.totalPosts = totalPosts; }

        public int getTotalEngagement() { return totalEngagement; }
        public void setTotalEngagement(int totalEngagement) { this.totalEngagement = totalEngagement; }

        public double getAverageSentiment() { return averageSentiment; }
        public void setAverageSentiment(double averageSentiment) { this.averageSentiment = averageSentiment; }

        public List<String> getTopHashtags() { return topHashtags; }
        public void setTopHashtags(List<String> topHashtags) { this.topHashtags = topHashtags; }

        public List<String> getTopInfluencers() { return topInfluencers; }
        public void setTopInfluencers(List<String> topInfluencers) { this.topInfluencers = topInfluencers; }

        public Map<String, Object> getDemographics() { return demographics; }
        public void setDemographics(Map<String, Object> demographics) { this.demographics = demographics; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class SocialMediaInfluencer {
        private String id;
        private String username;
        private String platform;
        private int followers;
        private int engagement;
        private String category;
        private double influenceScore;
        private String location;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public int getFollowers() { return followers; }
        public void setFollowers(int followers) { this.followers = followers; }

        public int getEngagement() { return engagement; }
        public void setEngagement(int engagement) { this.engagement = engagement; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getInfluenceScore() { return influenceScore; }
        public void setInfluenceScore(double influenceScore) { this.influenceScore = influenceScore; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class SocialMediaSentiment {
        private String text;
        private String sentiment;
        private double confidence;
        private Map<String, Double> emotions;
        private String language;

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public Map<String, Double> getEmotions() { return emotions; }
        public void setEmotions(Map<String, Double> emotions) { this.emotions = emotions; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class SocialMediaHashtag {
        private String hashtag;
        private String platform;
        private int usageCount;
        private double trendScore;
        private String category;
        private LocalDateTime lastUsed;

        // Getters and setters
        public String getHashtag() { return hashtag; }
        public void setHashtag(String hashtag) { this.hashtag = hashtag; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

        public double getTrendScore() { return trendScore; }
        public void setTrendScore(double trendScore) { this.trendScore = trendScore; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDateTime getLastUsed() { return lastUsed; }
        public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    }

    public static class SocialMediaCrisisDetection {
        private String location;
        private String timeRange;
        private boolean crisisDetected;
        private double crisisScore;
        private List<String> indicators;
        private String severity;
        private LocalDateTime detectedAt;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

        public boolean isCrisisDetected() { return crisisDetected; }
        public void setCrisisDetected(boolean crisisDetected) { this.crisisDetected = crisisDetected; }

        public double getCrisisScore() { return crisisScore; }
        public void setCrisisScore(double crisisScore) { this.crisisScore = crisisScore; }

        public List<String> getIndicators() { return indicators; }
        public void setIndicators(List<String> indicators) { this.indicators = indicators; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class SocialMediaAlert {
        private String id;
        private String platform;
        private String alertType;
        private String severity;
        private String message;
        private LocalDateTime createdAt;
        private String location;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class SocialMediaReport {
        private String id;
        private String platform;
        private String timeRange;
        private String reportType;
        private Map<String, Object> data;
        private LocalDateTime generatedAt;
        private String fileUrl;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    }
}


