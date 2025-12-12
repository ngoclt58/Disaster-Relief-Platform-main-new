package com.relief.controller.communication;

import com.relief.service.communication.TranslationService;
import com.relief.service.communication.TranslationService.TranslationResult;
import com.relief.service.communication.TranslationService.TranslationRequest;
import com.relief.service.communication.TranslationService.LanguageDetectionResult;
import com.relief.service.communication.TranslationService.LanguageSupport;
import com.relief.service.communication.TranslationService.TranslationStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Translation controller
 */
@RestController
@RequestMapping("/translation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Translation", description = "Real-time translation APIs")
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    @Operation(summary = "Translate text from source to target language")
    public ResponseEntity<TranslationResult> translateText(
            @RequestBody TranslateTextRequest request) {
        
        TranslationResult result = translationService.translateText(
            request.getText(),
            request.getSourceLanguage(),
            request.getTargetLanguage()
        );
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/translate-batch")
    @Operation(summary = "Batch translate multiple texts")
    public ResponseEntity<List<TranslationResult>> translateBatch(
            @RequestBody List<TranslationRequest> requests) {
        
        List<TranslationResult> results = translationService.translateBatch(requests);
        
        return ResponseEntity.ok(results);
    }

    @PostMapping("/detect-language")
    @Operation(summary = "Detect language of input text")
    public ResponseEntity<LanguageDetectionResult> detectLanguage(
            @RequestBody DetectLanguageRequest request) {
        
        LanguageDetectionResult result = translationService.detectLanguage(request.getText());
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/translate-auto")
    @Operation(summary = "Translate text with auto-detection")
    public ResponseEntity<TranslationResult> translateWithAutoDetection(
            @RequestBody TranslateAutoRequest request) {
        
        TranslationResult result = translationService.translateWithAutoDetection(
            request.getText(),
            request.getTargetLanguage()
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/languages")
    @Operation(summary = "Get supported languages")
    public ResponseEntity<List<LanguageSupport>> getSupportedLanguages() {
        
        List<LanguageSupport> languages = translationService.getSupportedLanguages();
        
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/languages/{languageCode}")
    @Operation(summary = "Get language support details")
    public ResponseEntity<LanguageSupport> getLanguageSupport(
            @PathVariable String languageCode) {
        
        LanguageSupport language = translationService.getLanguageSupport(languageCode);
        
        return ResponseEntity.ok(language);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get translation statistics")
    public ResponseEntity<TranslationStatistics> getTranslationStatistics() {
        
        TranslationStatistics stats = translationService.getTranslationStatistics();
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/cache")
    @Operation(summary = "Clear translation cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        
        translationService.clearCache();
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Cache cleared successfully"));
    }

    // Request DTOs
    public static class TranslateTextRequest {
        private String text;
        private String sourceLanguage;
        private String targetLanguage;

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getSourceLanguage() { return sourceLanguage; }
        public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

        public String getTargetLanguage() { return targetLanguage; }
        public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    }

    public static class DetectLanguageRequest {
        private String text;

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class TranslateAutoRequest {
        private String text;
        private String targetLanguage;

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getTargetLanguage() { return targetLanguage; }
        public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    }
}


