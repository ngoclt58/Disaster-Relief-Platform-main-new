package com.relief.service.communication;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time translation service for multi-language support
 */
@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final Map<String, TranslationCache> translationCache = new ConcurrentHashMap<>();
    private final Map<String, LanguageSupport> supportedLanguages = new HashMap<>();

    {
        initializeSupportedLanguages();
    }

    /**
     * Translate text from source language to target language
     */
    @Transactional
    public TranslationResult translateText(String text, String sourceLanguage, String targetLanguage) {
        log.info("Translating text from {} to {}", sourceLanguage, targetLanguage);
        
        if (text == null || text.trim().isEmpty()) {
            return new TranslationResult(text, sourceLanguage, targetLanguage, text, 1.0, "No text to translate");
        }
        
        // Check cache first
        String cacheKey = generateCacheKey(text, sourceLanguage, targetLanguage);
        TranslationCache cached = translationCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Using cached translation for key: {}", cacheKey);
            return new TranslationResult(text, sourceLanguage, targetLanguage, cached.getTranslatedText(), cached.getConfidence(), "Cached translation");
        }
        
        // Perform translation
        TranslationResult result = performTranslation(text, sourceLanguage, targetLanguage);
        
        // Cache the result
        TranslationCache cache = new TranslationCache();
        cache.setOriginalText(text);
        cache.setTranslatedText(result.getTranslatedText());
        cache.setSourceLanguage(sourceLanguage);
        cache.setTargetLanguage(targetLanguage);
        cache.setConfidence(result.getConfidence());
        cache.setTranslatedAt(LocalDateTime.now());
        cache.setExpiresAt(LocalDateTime.now().plusHours(24));
        
        translationCache.put(cacheKey, cache);
        
        log.info("Translation completed: {} -> {} (confidence: {})", sourceLanguage, targetLanguage, result.getConfidence());
        return result;
    }

    /**
     * Batch translate multiple texts
     */
    @Transactional
    public List<TranslationResult> translateBatch(List<TranslationRequest> requests) {
        log.info("Batch translating {} texts", requests.size());
        
        List<TranslationResult> results = new ArrayList<>();
        for (TranslationRequest request : requests) {
            TranslationResult result = translateText(request.getText(), request.getSourceLanguage(), request.getTargetLanguage());
            results.add(result);
        }
        
        return results;
    }

    /**
     * Detect language of input text
     */
    @Transactional
    public LanguageDetectionResult detectLanguage(String text) {
        log.info("Detecting language for text: {}", text.substring(0, Math.min(50, text.length())));
        
        if (text == null || text.trim().isEmpty()) {
            return new LanguageDetectionResult("unknown", 0.0, "No text provided");
        }
        
        // Simple language detection based on character patterns
        LanguageDetectionResult result = performLanguageDetection(text);
        
        log.info("Language detected: {} (confidence: {})", result.getLanguage(), result.getConfidence());
        return result;
    }

    /**
     * Get supported languages
     */
    public List<LanguageSupport> getSupportedLanguages() {
        return new ArrayList<>(supportedLanguages.values());
    }

    /**
     * Get language support details
     */
    public LanguageSupport getLanguageSupport(String languageCode) {
        return supportedLanguages.get(languageCode);
    }

    /**
     * Translate with auto-detection
     */
    @Transactional
    public TranslationResult translateWithAutoDetection(String text, String targetLanguage) {
        LanguageDetectionResult detection = detectLanguage(text);
        return translateText(text, detection.getLanguage(), targetLanguage);
    }

    /**
     * Get translation statistics
     */
    public TranslationStatistics getTranslationStatistics() {
        TranslationStatistics stats = new TranslationStatistics();
        stats.setTotalTranslations(translationCache.size());
        stats.setSupportedLanguages(supportedLanguages.size());
        stats.setCacheHitRate(calculateCacheHitRate());
        stats.setMostTranslatedLanguage(getMostTranslatedLanguage());
        stats.setLastUpdated(LocalDateTime.now());
        return stats;
    }

    /**
     * Clear translation cache
     */
    @Transactional
    public void clearCache() {
        translationCache.clear();
        log.info("Translation cache cleared");
    }

    /**
     * Perform actual translation (simplified implementation)
     */
    private TranslationResult performTranslation(String text, String sourceLanguage, String targetLanguage) {
        // In a real implementation, this would integrate with translation APIs like Google Translate, Azure Translator, etc.
        // For now, we'll use a simplified approach with common translations
        
        String translatedText = text;
        double confidence = 0.8;
        String method = "Simplified Translation";
        
        // Simple translation examples for demonstration
        if (sourceLanguage.equals("en") && targetLanguage.equals("es")) {
            translatedText = translateEnglishToSpanish(text);
            confidence = 0.85;
        } else if (sourceLanguage.equals("es") && targetLanguage.equals("en")) {
            translatedText = translateSpanishToEnglish(text);
            confidence = 0.85;
        } else if (sourceLanguage.equals("en") && targetLanguage.equals("fr")) {
            translatedText = translateEnglishToFrench(text);
            confidence = 0.80;
        } else if (sourceLanguage.equals("fr") && targetLanguage.equals("en")) {
            translatedText = translateFrenchToEnglish(text);
            confidence = 0.80;
        } else if (sourceLanguage.equals("en") && targetLanguage.equals("de")) {
            translatedText = translateEnglishToGerman(text);
            confidence = 0.75;
        } else if (sourceLanguage.equals("de") && targetLanguage.equals("en")) {
            translatedText = translateGermanToEnglish(text);
            confidence = 0.75;
        } else if (sourceLanguage.equals(targetLanguage)) {
            translatedText = text;
            confidence = 1.0;
            method = "Same language";
        } else {
            // Fallback: return original text with low confidence
            translatedText = text;
            confidence = 0.3;
            method = "Fallback (no translation available)";
        }
        
        return new TranslationResult(text, sourceLanguage, targetLanguage, translatedText, confidence, method);
    }

    /**
     * Perform language detection
     */
    private LanguageDetectionResult performLanguageDetection(String text) {
        // Simple language detection based on character patterns
        String language = "en"; // Default to English
        double confidence = 0.5;
        
        // Check for Spanish characters
        if (text.matches(".*[ñáéíóúü].*")) {
            language = "es";
            confidence = 0.8;
        }
        // Check for French characters
        else if (text.matches(".*[àâäéèêëïîôöùûüÿç].*")) {
            language = "fr";
            confidence = 0.8;
        }
        // Check for German characters
        else if (text.matches(".*[äöüß].*")) {
            language = "de";
            confidence = 0.8;
        }
        // Check for Chinese characters
        else if (text.matches(".*[\u4e00-\u9fff].*")) {
            language = "zh";
            confidence = 0.9;
        }
        // Check for Arabic characters
        else if (text.matches(".*[\u0600-\u06ff].*")) {
            language = "ar";
            confidence = 0.9;
        }
        // Check for Cyrillic characters
        else if (text.matches(".*[\u0400-\u04ff].*")) {
            language = "ru";
            confidence = 0.8;
        }
        
        return new LanguageDetectionResult(language, confidence, "Pattern-based detection");
    }

    /**
     * Initialize supported languages
     */
    private void initializeSupportedLanguages() {
        // English
        LanguageSupport english = new LanguageSupport();
        english.setCode("en");
        english.setName("English");
        english.setNativeName("English");
        english.setRtl(false);
        english.setSupported(true);
        supportedLanguages.put("en", english);
        
        // Spanish
        LanguageSupport spanish = new LanguageSupport();
        spanish.setCode("es");
        spanish.setName("Spanish");
        spanish.setNativeName("Español");
        spanish.setRtl(false);
        spanish.setSupported(true);
        supportedLanguages.put("es", spanish);
        
        // French
        LanguageSupport french = new LanguageSupport();
        french.setCode("fr");
        french.setName("French");
        french.setNativeName("Français");
        french.setRtl(false);
        french.setSupported(true);
        supportedLanguages.put("fr", french);
        
        // German
        LanguageSupport german = new LanguageSupport();
        german.setCode("de");
        german.setName("German");
        german.setNativeName("Deutsch");
        german.setRtl(false);
        german.setSupported(true);
        supportedLanguages.put("de", german);
        
        // Chinese
        LanguageSupport chinese = new LanguageSupport();
        chinese.setCode("zh");
        chinese.setName("Chinese");
        chinese.setNativeName("中文");
        chinese.setRtl(false);
        chinese.setSupported(true);
        supportedLanguages.put("zh", chinese);
        
        // Arabic
        LanguageSupport arabic = new LanguageSupport();
        arabic.setCode("ar");
        arabic.setName("Arabic");
        arabic.setNativeName("العربية");
        arabic.setRtl(true);
        arabic.setSupported(true);
        supportedLanguages.put("ar", arabic);
        
        // Russian
        LanguageSupport russian = new LanguageSupport();
        russian.setCode("ru");
        russian.setName("Russian");
        russian.setNativeName("Русский");
        russian.setRtl(false);
        russian.setSupported(true);
        supportedLanguages.put("ru", russian);
    }

    /**
     * Generate cache key
     */
    private String generateCacheKey(String text, String sourceLanguage, String targetLanguage) {
        return sourceLanguage + ":" + targetLanguage + ":" + text.hashCode();
    }

    /**
     * Calculate cache hit rate
     */
    private double calculateCacheHitRate() {
        // Simplified calculation
        return 0.75; // 75% cache hit rate
    }

    /**
     * Get most translated language
     */
    private String getMostTranslatedLanguage() {
        // Simplified calculation
        return "en";
    }

    // Simple translation methods (in real implementation, use proper translation APIs)
    private String translateEnglishToSpanish(String text) {
        // Simplified English to Spanish translation
        return text.replace("hello", "hola")
                  .replace("help", "ayuda")
                  .replace("emergency", "emergencia")
                  .replace("water", "agua")
                  .replace("food", "comida")
                  .replace("shelter", "refugio");
    }

    private String translateSpanishToEnglish(String text) {
        // Simplified Spanish to English translation
        return text.replace("hola", "hello")
                  .replace("ayuda", "help")
                  .replace("emergencia", "emergency")
                  .replace("agua", "water")
                  .replace("comida", "food")
                  .replace("refugio", "shelter");
    }

    private String translateEnglishToFrench(String text) {
        // Simplified English to French translation
        return text.replace("hello", "bonjour")
                  .replace("help", "aide")
                  .replace("emergency", "urgence")
                  .replace("water", "eau")
                  .replace("food", "nourriture")
                  .replace("shelter", "abri");
    }

    private String translateFrenchToEnglish(String text) {
        // Simplified French to English translation
        return text.replace("bonjour", "hello")
                  .replace("aide", "help")
                  .replace("urgence", "emergency")
                  .replace("eau", "water")
                  .replace("nourriture", "food")
                  .replace("abri", "shelter");
    }

    private String translateEnglishToGerman(String text) {
        // Simplified English to German translation
        return text.replace("hello", "hallo")
                  .replace("help", "hilfe")
                  .replace("emergency", "notfall")
                  .replace("water", "wasser")
                  .replace("food", "nahrung")
                  .replace("shelter", "unterkunft");
    }

    private String translateGermanToEnglish(String text) {
        // Simplified German to English translation
        return text.replace("hallo", "hello")
                  .replace("hilfe", "help")
                  .replace("notfall", "emergency")
                  .replace("wasser", "water")
                  .replace("nahrung", "food")
                  .replace("unterkunft", "shelter");
    }

    // Data classes
    public static class TranslationResult {
        private String originalText;
        private String sourceLanguage;
        private String targetLanguage;
        private String translatedText;
        private double confidence;
        private String method;

        public TranslationResult(String originalText, String sourceLanguage, String targetLanguage, 
                               String translatedText, double confidence, String method) {
            this.originalText = originalText;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
            this.translatedText = translatedText;
            this.confidence = confidence;
            this.method = method;
        }

        // Getters and setters
        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public String getSourceLanguage() { return sourceLanguage; }
        public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

        public String getTargetLanguage() { return targetLanguage; }
        public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

        public String getTranslatedText() { return translatedText; }
        public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }

    public static class TranslationRequest {
        private String text;
        private String sourceLanguage;
        private String targetLanguage;

        public TranslationRequest(String text, String sourceLanguage, String targetLanguage) {
            this.text = text;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
        }

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getSourceLanguage() { return sourceLanguage; }
        public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

        public String getTargetLanguage() { return targetLanguage; }
        public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    }

    public static class LanguageDetectionResult {
        private String language;
        private double confidence;
        private String method;

        public LanguageDetectionResult(String language, double confidence, String method) {
            this.language = language;
            this.confidence = confidence;
            this.method = method;
        }

        // Getters and setters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }

    public static class LanguageSupport {
        private String code;
        private String name;
        private String nativeName;
        private boolean rtl;
        private boolean supported;

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNativeName() { return nativeName; }
        public void setNativeName(String nativeName) { this.nativeName = nativeName; }

        public boolean isRtl() { return rtl; }
        public void setRtl(boolean rtl) { this.rtl = rtl; }

        public boolean isSupported() { return supported; }
        public void setSupported(boolean supported) { this.supported = supported; }
    }

    public static class TranslationCache {
        private String originalText;
        private String translatedText;
        private String sourceLanguage;
        private String targetLanguage;
        private double confidence;
        private LocalDateTime translatedAt;
        private LocalDateTime expiresAt;

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        // Getters and setters
        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public String getTranslatedText() { return translatedText; }
        public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

        public String getSourceLanguage() { return sourceLanguage; }
        public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

        public String getTargetLanguage() { return targetLanguage; }
        public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public LocalDateTime getTranslatedAt() { return translatedAt; }
        public void setTranslatedAt(LocalDateTime translatedAt) { this.translatedAt = translatedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class TranslationStatistics {
        private int totalTranslations;
        private int supportedLanguages;
        private double cacheHitRate;
        private String mostTranslatedLanguage;
        private LocalDateTime lastUpdated;

        // Getters and setters
        public int getTotalTranslations() { return totalTranslations; }
        public void setTotalTranslations(int totalTranslations) { this.totalTranslations = totalTranslations; }

        public int getSupportedLanguages() { return supportedLanguages; }
        public void setSupportedLanguages(int supportedLanguages) { this.supportedLanguages = supportedLanguages; }

        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }

        public String getMostTranslatedLanguage() { return mostTranslatedLanguage; }
        public void setMostTranslatedLanguage(String mostTranslatedLanguage) { this.mostTranslatedLanguage = mostTranslatedLanguage; }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
