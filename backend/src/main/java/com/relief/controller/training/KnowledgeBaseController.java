package com.relief.controller.training;

import com.relief.service.training.KnowledgeBaseService;
import com.relief.service.training.KnowledgeBaseService.KnowledgeArticle;
import com.relief.service.training.KnowledgeBaseService.KnowledgeCategory;
import com.relief.service.training.KnowledgeBaseService.KnowledgeTag;
import com.relief.service.training.KnowledgeBaseService.KnowledgeAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Knowledge base controller
 */
@RestController
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Knowledge Base", description = "Searchable repository of best practices and procedures")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/articles")
    @Operation(summary = "Create a knowledge article")
    public ResponseEntity<KnowledgeArticle> createArticle(
            @RequestBody CreateArticleRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String authorId = principal.getUsername();
        
        KnowledgeArticle article = knowledgeBaseService.createArticle(
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            request.getTags(),
            authorId,
            request.getLanguage(),
            request.isPublic()
        );
        
        return ResponseEntity.ok(article);
    }

    @PutMapping("/articles/{articleId}")
    @Operation(summary = "Update a knowledge article")
    public ResponseEntity<KnowledgeArticle> updateArticle(
            @PathVariable String articleId,
            @RequestBody UpdateArticleRequest request) {
        
        KnowledgeArticle article = knowledgeBaseService.updateArticle(
            articleId,
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            request.getTags(),
            request.getLanguage(),
            request.isPublic()
        );
        
        return ResponseEntity.ok(article);
    }

    @GetMapping("/articles/{articleId}")
    @Operation(summary = "Get a knowledge article")
    public ResponseEntity<KnowledgeArticle> getArticle(@PathVariable String articleId) {
        KnowledgeArticle article = knowledgeBaseService.getArticle(articleId);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/articles")
    @Operation(summary = "Search knowledge articles")
    public ResponseEntity<List<KnowledgeArticle>> searchArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "true") boolean isPublic,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.searchArticles(
            query, category, tags, language, isPublic, limit
        );
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/category/{category}")
    @Operation(summary = "Get articles by category")
    public ResponseEntity<List<KnowledgeArticle>> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.getArticlesByCategory(category, limit);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/popular")
    @Operation(summary = "Get popular articles")
    public ResponseEntity<List<KnowledgeArticle>> getPopularArticles(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.getPopularArticles(limit);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/recent")
    @Operation(summary = "Get recent articles")
    public ResponseEntity<List<KnowledgeArticle>> getRecentArticles(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.getRecentArticles(limit);
        return ResponseEntity.ok(articles);
    }

    @PostMapping("/articles/{articleId}/rate")
    @Operation(summary = "Rate a knowledge article")
    public ResponseEntity<KnowledgeArticle> rateArticle(
            @PathVariable String articleId,
            @RequestBody RateArticleRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        
        KnowledgeArticle article = knowledgeBaseService.rateArticle(
            articleId,
            userId,
            request.getRating(),
            request.getComment()
        );
        
        return ResponseEntity.ok(article);
    }

    @PostMapping("/articles/{articleId}/bookmark")
    @Operation(summary = "Bookmark a knowledge article")
    public ResponseEntity<KnowledgeArticle> bookmarkArticle(
            @PathVariable String articleId,
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        KnowledgeArticle article = knowledgeBaseService.bookmarkArticle(articleId, userId);
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/articles/{articleId}/bookmark")
    @Operation(summary = "Remove bookmark from article")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable String articleId,
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        knowledgeBaseService.removeBookmark(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "Get user bookmarks")
    public ResponseEntity<List<KnowledgeArticle>> getUserBookmarks(
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        List<KnowledgeArticle> bookmarks = knowledgeBaseService.getUserBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/articles/{articleId}/related")
    @Operation(summary = "Get related articles")
    public ResponseEntity<List<KnowledgeArticle>> getRelatedArticles(
            @PathVariable String articleId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.getRelatedArticles(articleId, limit);
        return ResponseEntity.ok(articles);
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a knowledge category")
    public ResponseEntity<KnowledgeCategory> createCategory(
            @RequestBody CreateCategoryRequest request) {
        
        KnowledgeCategory category = knowledgeBaseService.createCategory(
            request.getName(),
            request.getDescription(),
            request.getParentCategoryId()
        );
        
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get knowledge categories")
    public ResponseEntity<List<KnowledgeCategory>> getCategories() {
        List<KnowledgeCategory> categories = knowledgeBaseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/tags")
    @Operation(summary = "Create a knowledge tag")
    public ResponseEntity<KnowledgeTag> createTag(
            @RequestBody CreateTagRequest request) {
        
        KnowledgeTag tag = knowledgeBaseService.createTag(
            request.getName(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(tag);
    }

    @GetMapping("/tags/popular")
    @Operation(summary = "Get popular tags")
    public ResponseEntity<List<KnowledgeTag>> getPopularTags(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<KnowledgeTag> tags = knowledgeBaseService.getPopularTags(limit);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/articles/{articleId}/analytics")
    @Operation(summary = "Get article analytics")
    public ResponseEntity<KnowledgeAnalytics> getArticleAnalytics(@PathVariable String articleId) {
        KnowledgeAnalytics analytics = knowledgeBaseService.getAnalytics(articleId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/articles/trending")
    @Operation(summary = "Get trending articles")
    public ResponseEntity<List<KnowledgeArticle>> getTrendingArticles(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<KnowledgeArticle> articles = knowledgeBaseService.getTrendingArticles(limit);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/search/suggestions")
    @Operation(summary = "Get search suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam String query) {
        
        Map<String, Object> suggestions = knowledgeBaseService.getSearchSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/articles/{articleId}/translate")
    @Operation(summary = "Translate article")
    public ResponseEntity<KnowledgeArticle> translateArticle(
            @PathVariable String articleId,
            @RequestParam String targetLanguage) {
        
        KnowledgeArticle article = knowledgeBaseService.translateArticle(articleId, targetLanguage);
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/articles/{articleId}")
    @Operation(summary = "Delete knowledge article")
    public ResponseEntity<Void> deleteArticle(@PathVariable String articleId) {
        knowledgeBaseService.deleteArticle(articleId);
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    public static class CreateArticleRequest {
        private String title;
        private String content;
        private String category;
        private String tags;
        private String language;
        private boolean isPublic;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
    }

    public static class UpdateArticleRequest {
        private String title;
        private String content;
        private String category;
        private String tags;
        private String language;
        private boolean isPublic;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
    }

    public static class RateArticleRequest {
        private double rating;
        private String comment;

        // Getters and setters
        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class CreateCategoryRequest {
        private String name;
        private String description;
        private String parentCategoryId;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getParentCategoryId() { return parentCategoryId; }
        public void setParentCategoryId(String parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    }

    public static class CreateTagRequest {
        private String name;
        private String description;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}


