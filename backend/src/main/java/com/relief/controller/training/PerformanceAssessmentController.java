package com.relief.controller.training;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.training.PerformanceAssessmentService;
import com.relief.service.training.PerformanceAssessmentService.Assessment;
import com.relief.service.training.PerformanceAssessmentService.AssessmentSession;
import com.relief.service.training.PerformanceAssessmentService.AssessmentQuestion;
import com.relief.service.training.PerformanceAssessmentService.AssessmentResponse;
import com.relief.service.training.PerformanceAssessmentService.AssessmentScore;
import com.relief.service.training.PerformanceAssessmentService.AssessmentResult;
import com.relief.service.training.PerformanceAssessmentService.CompetencyProfile;
import com.relief.service.training.PerformanceAssessmentService.TrainingRecommendation;
import com.relief.service.training.PerformanceAssessmentService.PerformanceReport;
import com.relief.service.training.PerformanceAssessmentService.AssessmentAnalytics;
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
 * Performance assessment controller
 */
@RestController
@RequestMapping("/performance-assessment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Performance Assessment", description = "Evaluate and improve response capabilities")
public class PerformanceAssessmentController {

    private final PerformanceAssessmentService performanceAssessmentService;
    private final UserRepository userRepository;

    private UUID getUserIdFromPrincipal(UserDetails principal) {
        String username = principal.getUsername();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
            return user.getId();
        }
    }

    @PostMapping("/assessments")
    @Operation(summary = "Create a performance assessment")
    public ResponseEntity<Assessment> createAssessment(
            @RequestBody CreateAssessmentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        Assessment assessment = performanceAssessmentService.createAssessment(
            request.getName(),
            request.getDescription(),
            request.getAssessmentType(),
            request.getCategory(),
            request.getCriteria(),
            userId
        );
        
        return ResponseEntity.ok(assessment);
    }

    @PostMapping("/sessions")
    @Operation(summary = "Start an assessment session")
    public ResponseEntity<AssessmentSession> startAssessment(
            @RequestBody StartAssessmentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String participantId = principal.getUsername();
        
        AssessmentSession session = performanceAssessmentService.startAssessment(
            request.getAssessmentId(),
            participantId,
            request.getSessionName(),
            request.getContext()
        );
        
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/responses")
    @Operation(summary = "Record an assessment response")
    public ResponseEntity<AssessmentResponse> recordResponse(
            @PathVariable String sessionId,
            @RequestBody RecordResponseRequest request) {
        
        AssessmentResponse response = performanceAssessmentService.recordResponse(
            sessionId,
            request.getQuestionId(),
            request.getResponseType(),
            request.getResponseData(),
            request.getTimeSpent()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/score")
    @Operation(summary = "Calculate assessment score")
    public ResponseEntity<AssessmentScore> calculateScore(@PathVariable String sessionId) {
        AssessmentScore score = performanceAssessmentService.calculateScore(sessionId);
        return ResponseEntity.ok(score);
    }

    @PostMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete assessment session")
    public ResponseEntity<AssessmentSession> completeAssessment(
            @PathVariable String sessionId,
            @RequestBody CompleteAssessmentRequest request) {
        
        AssessmentSession session = performanceAssessmentService.completeAssessment(
            sessionId,
            request.getCompletionNotes()
        );
        
        return ResponseEntity.ok(session);
    }

    @PostMapping("/questions")
    @Operation(summary = "Create an assessment question")
    public ResponseEntity<AssessmentQuestion> createQuestion(
            @RequestBody CreateQuestionRequest request) {
        
        AssessmentQuestion question = performanceAssessmentService.createQuestion(
            request.getAssessmentId(),
            request.getQuestionText(),
            request.getQuestionType(),
            request.getOptions(),
            request.getCorrectAnswer(),
            request.getMetadata()
        );
        
        return ResponseEntity.ok(question);
    }

    @GetMapping("/assessments/{assessmentId}/questions")
    @Operation(summary = "Get assessment questions")
    public ResponseEntity<List<AssessmentQuestion>> getAssessmentQuestions(
            @PathVariable String assessmentId) {
        
        List<AssessmentQuestion> questions = performanceAssessmentService.getAssessmentQuestions(assessmentId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/sessions/{sessionId}/result")
    @Operation(summary = "Generate assessment result")
    public ResponseEntity<AssessmentResult> generateResult(@PathVariable String sessionId) {
        AssessmentResult result = performanceAssessmentService.generateResult(sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/assessments")
    @Operation(summary = "Get assessments")
    public ResponseEntity<List<Assessment>> getAssessments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String assessmentType,
            @RequestParam(defaultValue = "true") boolean isActive) {
        
        List<Assessment> assessments = performanceAssessmentService.getAssessments(
            category, assessmentType, isActive
        );
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/{assessmentId}")
    @Operation(summary = "Get assessment details")
    public ResponseEntity<Assessment> getAssessment(@PathVariable String assessmentId) {
        Assessment assessment = performanceAssessmentService.getAssessment(assessmentId);
        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get user assessment sessions")
    public ResponseEntity<List<AssessmentSession>> getUserSessions(
            @RequestParam String participantId,
            @RequestParam(required = false) String status) {
        
        List<AssessmentSession> sessions = performanceAssessmentService.getUserSessions(
            participantId,
            status != null ? PerformanceAssessmentService.AssessmentStatus.valueOf(status) : null
        );
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session details")
    public ResponseEntity<AssessmentSession> getSession(@PathVariable String sessionId) {
        AssessmentSession session = performanceAssessmentService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{sessionId}/responses")
    @Operation(summary = "Get session responses")
    public ResponseEntity<List<AssessmentResponse>> getSessionResponses(@PathVariable String sessionId) {
        List<AssessmentResponse> responses = performanceAssessmentService.getSessionResponses(sessionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assessments/{assessmentId}/analytics")
    @Operation(summary = "Get assessment analytics")
    public ResponseEntity<AssessmentAnalytics> getAssessmentAnalytics(@PathVariable String assessmentId) {
        AssessmentAnalytics analytics = performanceAssessmentService.getAssessmentAnalytics(assessmentId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/competencies")
    @Operation(summary = "Create competency profile")
    public ResponseEntity<CompetencyProfile> createCompetencyProfile(
            @RequestBody CreateCompetencyProfileRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        
        CompetencyProfile profile = performanceAssessmentService.createCompetencyProfile(
            userId,
            request.getCompetencyType(),
            request.getCompetencies()
        );
        
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/competencies/{profileId}")
    @Operation(summary = "Update competency profile")
    public ResponseEntity<CompetencyProfile> updateCompetencyProfile(
            @PathVariable String profileId,
            @RequestBody UpdateCompetencyProfileRequest request) {
        
        CompetencyProfile profile = performanceAssessmentService.updateCompetencyProfile(
            profileId,
            request.getCompetencies()
        );
        
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/competencies")
    @Operation(summary = "Get user competencies")
    public ResponseEntity<List<CompetencyProfile>> getUserCompetencies(
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        List<CompetencyProfile> competencies = performanceAssessmentService.getUserCompetencies(userId);
        return ResponseEntity.ok(competencies);
    }

    @PostMapping("/recommendations")
    @Operation(summary = "Generate training recommendations")
    public ResponseEntity<TrainingRecommendation> generateTrainingRecommendations(
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        TrainingRecommendation recommendation = performanceAssessmentService.generateTrainingRecommendations(userId);
        return ResponseEntity.ok(recommendation);
    }

    @PostMapping("/reports")
    @Operation(summary = "Generate performance report")
    public ResponseEntity<PerformanceReport> generatePerformanceReport(
            @RequestBody GeneratePerformanceReportRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        String userId = principal.getUsername();
        
        PerformanceReport report = performanceAssessmentService.generatePerformanceReport(
            userId,
            request.getStartDate(),
            request.getEndDate()
        );
        
        return ResponseEntity.ok(report);
    }

    // Request DTOs
    public static class CreateAssessmentRequest {
        private String name;
        private String description;
        private String assessmentType;
        private String category;
        private Map<String, Object> criteria;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAssessmentType() { return assessmentType; }
        public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Map<String, Object> getCriteria() { return criteria; }
        public void setCriteria(Map<String, Object> criteria) { this.criteria = criteria; }
    }

    public static class StartAssessmentRequest {
        private String assessmentId;
        private String sessionName;
        private Map<String, Object> context;

        // Getters and setters
        public String getAssessmentId() { return assessmentId; }
        public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

        public String getSessionName() { return sessionName; }
        public void setSessionName(String sessionName) { this.sessionName = sessionName; }

        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
    }

    public static class RecordResponseRequest {
        private String questionId;
        private String responseType;
        private Object responseData;
        private int timeSpent;

        // Getters and setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }

        public String getResponseType() { return responseType; }
        public void setResponseType(String responseType) { this.responseType = responseType; }

        public Object getResponseData() { return responseData; }
        public void setResponseData(Object responseData) { this.responseData = responseData; }

        public int getTimeSpent() { return timeSpent; }
        public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }
    }

    public static class CompleteAssessmentRequest {
        private String completionNotes;

        // Getters and setters
        public String getCompletionNotes() { return completionNotes; }
        public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    }

    public static class CreateQuestionRequest {
        private String assessmentId;
        private String questionText;
        private String questionType;
        private List<String> options;
        private String correctAnswer;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getAssessmentId() { return assessmentId; }
        public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class CreateCompetencyProfileRequest {
        private String competencyType;
        private Map<String, Object> competencies;

        // Getters and setters
        public String getCompetencyType() { return competencyType; }
        public void setCompetencyType(String competencyType) { this.competencyType = competencyType; }

        public Map<String, Object> getCompetencies() { return competencies; }
        public void setCompetencies(Map<String, Object> competencies) { this.competencies = competencies; }
    }

    public static class UpdateCompetencyProfileRequest {
        private Map<String, Object> competencies;

        // Getters and setters
        public Map<String, Object> getCompetencies() { return competencies; }
        public void setCompetencies(Map<String, Object> competencies) { this.competencies = competencies; }
    }

    public static class GeneratePerformanceReportRequest {
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        // Getters and setters
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }
}


