package com.relief.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * AI-powered intelligent categorization service using NLP and ML techniques
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentCategorizationService {

    private final TextSimilarityService textSimilarityService;

    /**
     * Categorize a request based on its description and context
     */
    public CategorizationResult categorizeRequest(String description, String currentType, Integer currentSeverity) {
        log.info("Categorizing request with description: {}", description);
        
        if (description == null || description.trim().isEmpty()) {
            return new CategorizationResult(currentType, currentSeverity, 0.0, "No description provided");
        }

        String normalizedDescription = normalizeText(description);
        
        // Analyze text for category and severity
        CategoryAnalysis categoryAnalysis = analyzeCategory(normalizedDescription);
        SeverityAnalysis severityAnalysis = analyzeSeverity(normalizedDescription);
        PriorityAnalysis priorityAnalysis = analyzePriority(normalizedDescription, categoryAnalysis, severityAnalysis);
        
        // Combine results
        String suggestedType = categoryAnalysis.suggestedType != null ? categoryAnalysis.suggestedType : currentType;
        Integer suggestedSeverity = severityAnalysis.suggestedSeverity != null ? severityAnalysis.suggestedSeverity : currentSeverity;
        String suggestedPriority = priorityAnalysis.suggestedPriority;
        
        double confidence = calculateConfidence(categoryAnalysis, severityAnalysis, priorityAnalysis);
        String reasoning = generateReasoning(categoryAnalysis, severityAnalysis, priorityAnalysis);
        
        return new CategorizationResult(suggestedType, suggestedSeverity, confidence, reasoning);
    }

    /**
     * Analyze text to determine category
     */
    private CategoryAnalysis analyzeCategory(String text) {
        Map<String, Double> categoryScores = new HashMap<>();
        
        // Medical/Emergency keywords
        double medicalScore = calculateKeywordScore(text, Arrays.asList(
            "medical", "emergency", "ambulance", "hospital", "doctor", "nurse", "injury", "wound", "bleeding",
            "heart", "stroke", "seizure", "unconscious", "breathing", "pain", "medicine", "medication",
            "urgent", "critical", "life", "death", "serious", "severe"
        ));
        categoryScores.put("Medical Emergency", medicalScore);
        
        // Evacuation keywords
        double evacuationScore = calculateKeywordScore(text, Arrays.asList(
            "evacuate", "evacuation", "leave", "escape", "shelter", "safe", "danger", "hazard",
            "flood", "fire", "storm", "hurricane", "earthquake", "tsunami", "disaster",
            "building", "collapse", "unsafe", "evacuation", "relocation"
        ));
        categoryScores.put("Evacuation", evacuationScore);
        
        // Food keywords
        double foodScore = calculateKeywordScore(text, Arrays.asList(
            "food", "hungry", "hunger", "starving", "meal", "eat", "eating", "restaurant",
            "grocery", "supermarket", "cooking", "kitchen", "nutrition", "diet", "supplies",
            "rations", "canned", "fresh", "bread", "rice", "pasta"
        ));
        categoryScores.put("Food Request", foodScore);
        
        // Water keywords
        double waterScore = calculateKeywordScore(text, Arrays.asList(
            "water", "thirsty", "thirst", "drink", "drinking", "clean", "safe", "contaminated",
            "bottled", "tap", "well", "supply", "shortage", "drought", "dehydration"
        ));
        categoryScores.put("Water Request", waterScore);
        
        // Shelter keywords
        double shelterScore = calculateKeywordScore(text, Arrays.asList(
            "shelter", "housing", "home", "place", "stay", "accommodation", "hotel", "motel",
            "tent", "camp", "refugee", "homeless", "roof", "bed", "sleep", "warm", "cold"
        ));
        categoryScores.put("Shelter", shelterScore);
        
        // Transportation keywords
        double transportScore = calculateKeywordScore(text, Arrays.asList(
            "transport", "transportation", "ride", "car", "vehicle", "bus", "taxi", "lift",
            "drive", "driver", "gas", "fuel", "road", "highway", "airport", "station"
        ));
        categoryScores.put("Transportation", transportScore);
        
        // Find best category
        String bestCategory = categoryScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Other");
            
        double confidence = categoryScores.get(bestCategory);
        
        return new CategoryAnalysis(bestCategory, confidence, categoryScores);
    }

    /**
     * Analyze text to determine severity level
     */
    private SeverityAnalysis analyzeSeverity(String text) {
        Map<Integer, Double> severityScores = new HashMap<>();
        
        // Severity 5 - Critical/Life-threatening
        double severity5Score = calculateKeywordScore(text, Arrays.asList(
            "critical", "urgent", "emergency", "life", "death", "dying", "unconscious", "bleeding",
            "severe", "serious", "immediate", "now", "asap", "help", "save", "rescue"
        ));
        severityScores.put(5, severity5Score);
        
        // Severity 4 - High priority
        double severity4Score = calculateKeywordScore(text, Arrays.asList(
            "important", "priority", "soon", "quickly", "fast", "urgent", "needed", "required",
            "necessary", "essential", "vital", "critical", "serious"
        ));
        severityScores.put(4, severity4Score);
        
        // Severity 3 - Medium priority
        double severity3Score = calculateKeywordScore(text, Arrays.asList(
            "need", "want", "require", "please", "help", "assistance", "support", "soon",
            "when possible", "if possible", "convenient"
        ));
        severityScores.put(3, severity3Score);
        
        // Severity 2 - Low priority
        double severity2Score = calculateKeywordScore(text, Arrays.asList(
            "would like", "prefer", "optional", "if available", "not urgent", "when convenient",
            "sometime", "later", "eventually"
        ));
        severityScores.put(2, severity2Score);
        
        // Severity 1 - Very low priority
        double severity1Score = calculateKeywordScore(text, Arrays.asList(
            "wish", "hope", "maybe", "perhaps", "someday", "future", "eventually"
        ));
        severityScores.put(1, severity1Score);
        
        // Find best severity
        Integer bestSeverity = severityScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(3);
            
        double confidence = severityScores.get(bestSeverity);
        
        return new SeverityAnalysis(bestSeverity, confidence, severityScores);
    }

    /**
     * Analyze text to determine priority level
     */
    private PriorityAnalysis analyzePriority(String text, CategoryAnalysis category, SeverityAnalysis severity) {
        String priority = "NORMAL";
        
        // High priority conditions
        if (severity.suggestedSeverity >= 5 || 
            category.suggestedType.equals("Medical Emergency") ||
            text.contains("urgent") || text.contains("emergency") || text.contains("critical")) {
            priority = "HIGH";
        } else if (severity.suggestedSeverity >= 4 || 
                   category.suggestedType.equals("Evacuation")) {
            priority = "MEDIUM";
        }
        
        return new PriorityAnalysis(priority, 0.8);
    }

    /**
     * Calculate keyword score for text analysis
     */
    private double calculateKeywordScore(String text, List<String> keywords) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        
        String lowerText = text.toLowerCase();
        int matches = 0;
        int totalKeywords = keywords.size();
        
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                matches++;
            }
        }
        
        return (double) matches / totalKeywords;
    }

    /**
     * Normalize text for analysis
     */
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Calculate overall confidence score
     */
    private double calculateConfidence(CategoryAnalysis category, SeverityAnalysis severity, PriorityAnalysis priority) {
        return (category.confidence + severity.confidence + priority.confidence) / 3.0;
    }

    /**
     * Generate human-readable reasoning
     */
    private String generateReasoning(CategoryAnalysis category, SeverityAnalysis severity, PriorityAnalysis priority) {
        List<String> reasons = new ArrayList<>();
        
        if (category.confidence > 0.5) {
            reasons.add(String.format("Category: %s (%.1f%% confidence)", category.suggestedType, category.confidence * 100));
        }
        
        if (severity.confidence > 0.5) {
            reasons.add(String.format("Severity: %d (%.1f%% confidence)", severity.suggestedSeverity, severity.confidence * 100));
        }
        
        if (priority.confidence > 0.5) {
            reasons.add(String.format("Priority: %s", priority.suggestedPriority));
        }
        
        return reasons.isEmpty() ? "Low confidence analysis" : String.join(", ", reasons);
    }

    // Data classes
    public static class CategorizationResult {
        public final String suggestedType;
        public final Integer suggestedSeverity;
        public final double confidence;
        public final String reasoning;

        public CategorizationResult(String suggestedType, Integer suggestedSeverity, double confidence, String reasoning) {
            this.suggestedType = suggestedType;
            this.suggestedSeverity = suggestedSeverity;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }
    }

    private static class CategoryAnalysis {
        final String suggestedType;
        final double confidence;
        final Map<String, Double> allScores;

        CategoryAnalysis(String suggestedType, double confidence, Map<String, Double> allScores) {
            this.suggestedType = suggestedType;
            this.confidence = confidence;
            this.allScores = allScores;
        }
    }

    private static class SeverityAnalysis {
        final Integer suggestedSeverity;
        final double confidence;
        final Map<Integer, Double> allScores;

        SeverityAnalysis(Integer suggestedSeverity, double confidence, Map<Integer, Double> allScores) {
            this.suggestedSeverity = suggestedSeverity;
            this.confidence = confidence;
            this.allScores = allScores;
        }
    }

    private static class PriorityAnalysis {
        final String suggestedPriority;
        final double confidence;

        PriorityAnalysis(String suggestedPriority, double confidence) {
            this.suggestedPriority = suggestedPriority;
            this.confidence = confidence;
        }
    }
}