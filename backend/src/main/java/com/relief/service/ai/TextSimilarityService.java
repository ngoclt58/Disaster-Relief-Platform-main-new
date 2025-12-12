package com.relief.service.ai;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Text similarity service using various NLP techniques
 */
@Service
public class TextSimilarityService {

    /**
     * Calculate similarity between two text strings using multiple algorithms
     */
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return 0.0;
        }

        // Normalize texts
        String normalized1 = normalizeText(text1);
        String normalized2 = normalizeText(text2);

        // Calculate multiple similarity metrics
        double jaccardSimilarity = calculateJaccardSimilarity(normalized1, normalized2);
        double cosineSimilarity = calculateCosineSimilarity(normalized1, normalized2);
        double levenshteinSimilarity = calculateLevenshteinSimilarity(normalized1, normalized2);

        // Weighted combination
        return (jaccardSimilarity * 0.4) + (cosineSimilarity * 0.4) + (levenshteinSimilarity * 0.2);
    }

    /**
     * Normalize text for comparison
     */
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ") // Remove special characters
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    /**
     * Calculate Jaccard similarity (intersection over union)
     */
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    /**
     * Calculate cosine similarity
     */
    private double calculateCosineSimilarity(String text1, String text2) {
        Map<String, Integer> vector1 = createWordVector(text1);
        Map<String, Integer> vector2 = createWordVector(text2);

        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            int count1 = vector1.getOrDefault(word, 0);
            int count2 = vector2.getOrDefault(word, 0);

            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Calculate Levenshtein similarity (1 - normalized distance)
     */
    private double calculateLevenshteinSimilarity(String text1, String text2) {
        int distance = calculateLevenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        if (maxLength == 0) return 1.0;
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Calculate Levenshtein distance
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Create word frequency vector
     */
    private Map<String, Integer> createWordVector(String text) {
        return Arrays.stream(text.split("\\s+"))
                .filter(word -> word.length() > 2) // Filter out short words
                .collect(Collectors.toMap(
                    word -> word,
                    word -> 1,
                    Integer::sum
                ));
    }
}


