package com.relief.service.communication;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * AI-powered chat bot service for common queries and tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private final UserRepository userRepository;
    private final Map<String, ChatSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, BotIntent> intents = new HashMap<>();

    {
        initializeIntents();
    }

    /**
     * Process user message and generate bot response
     */
    @Transactional
    public ChatBotResponse processMessage(String sessionId, String message, UUID userId) {
        // Validate inputs
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        log.info("Processing message from user {} in session {}", userId, sessionId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get or create chat session
        ChatSession session = activeSessions.computeIfAbsent(sessionId, id -> createNewSession(userId));
        
        // Add user message to session
        ChatMessage userMessage = new ChatMessage();
        userMessage.setId(UUID.randomUUID().toString());
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setUserName(user.getFullName());
        userMessage.setMessage(message);
        userMessage.setMessageType(MessageType.USER);
        userMessage.setTimestamp(LocalDateTime.now());
        
        session.getMessages().add(userMessage);
        
        // Process message and generate response
        BotIntent matchedIntent = findMatchingIntent(message);
        ChatBotResponse response = generateResponse(session, userMessage, matchedIntent, user);
        
        // Add bot response to session
        ChatMessage botMessage = new ChatMessage();
        botMessage.setId(UUID.randomUUID().toString());
        botMessage.setSessionId(sessionId);
        botMessage.setUserId(null);
        botMessage.setUserName("ReliefBot");
        botMessage.setMessage(response.getMessage());
        botMessage.setMessageType(MessageType.BOT);
        botMessage.setTimestamp(LocalDateTime.now());
        botMessage.setIntent(matchedIntent != null ? matchedIntent.getName() : "unknown");
        
        session.getMessages().add(botMessage);
        session.setLastActivity(LocalDateTime.now());
        
        log.info("Generated response for user {}: {}", userId, response.getMessage());
        return response;
    }

    /**
     * Find matching intent for user message
     */
    private BotIntent findMatchingIntent(String message) {
        String normalizedMessage = message.toLowerCase().trim();
        
        for (BotIntent intent : intents.values()) {
            for (String pattern : intent.getPatterns()) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(normalizedMessage).find()) {
                    return intent;
                }
            }
        }
        
        return intents.get("fallback");
    }

    /**
     * Generate bot response based on intent
     */
    private ChatBotResponse generateResponse(ChatSession session, ChatMessage userMessage, BotIntent intent, User user) {
        ChatBotResponse response = new ChatBotResponse();
        response.setSessionId(session.getId());
        response.setIntent(intent != null ? intent.getName() : "unknown");
        response.setConfidence(intent != null ? intent.getConfidence() : 0.0);
        
        if (intent != null) {
            // Select random response from intent
            List<String> responses = intent.getResponses();
            String selectedResponse = responses.get(new Random().nextInt(responses.size()));
            
            // Process response template
            String processedResponse = processResponseTemplate(selectedResponse, user, session);
            response.setMessage(processedResponse);
            
            // Add suggested actions
            if (intent.getSuggestedActions() != null) {
                response.setSuggestedActions(intent.getSuggestedActions());
            }
            
            // Add quick replies
            if (intent.getQuickReplies() != null) {
                response.setQuickReplies(intent.getQuickReplies());
            }
        } else {
            response.setMessage("I'm sorry, I didn't understand that. Could you please rephrase your question?");
            response.setSuggestedActions(Arrays.asList("Ask for help", "View available commands", "Contact support"));
        }
        
        return response;
    }

    /**
     * Process response template with user data
     */
    private String processResponseTemplate(String template, User user, ChatSession session) {
        return template
            .replace("{{userName}}", user.getFullName())
            .replace("{{userRole}}", user.getRole())
            .replace("{{sessionId}}", session.getId())
            .replace("{{timestamp}}", LocalDateTime.now().toString());
    }

    /**
     * Create new chat session
     */
    private ChatSession createNewSession(UUID userId) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        session.setMessages(new ArrayList<>());
        session.setStatus(SessionStatus.ACTIVE);
        
        // Add welcome message
        ChatMessage welcomeMessage = new ChatMessage();
        welcomeMessage.setId(UUID.randomUUID().toString());
        welcomeMessage.setSessionId(session.getId());
        welcomeMessage.setUserId(null);
        welcomeMessage.setUserName("ReliefBot");
        welcomeMessage.setMessage("Hello! I'm ReliefBot, your AI assistant for the disaster relief platform. How can I help you today?");
        welcomeMessage.setMessageType(MessageType.BOT);
        welcomeMessage.setTimestamp(LocalDateTime.now());
        
        session.getMessages().add(welcomeMessage);
        
        return session;
    }

    /**
     * Get chat session history
     */
    public ChatSession getChatSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Get user's active sessions
     */
    public List<ChatSession> getUserSessions(UUID userId) {
        return activeSessions.values().stream()
            .filter(session -> session.getUserId().equals(userId))
            .sorted(Comparator.comparing(ChatSession::getLastActivity).reversed())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * End chat session
     */
    @Transactional
    public void endSession(String sessionId) {
        ChatSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.setStatus(SessionStatus.ENDED);
            session.setEndedAt(LocalDateTime.now());
            log.info("Chat session {} ended", sessionId);
        }
    }

    /**
     * Initialize bot intents and patterns
     */
    private void initializeIntents() {
        // Greeting intent
        BotIntent greeting = new BotIntent();
        greeting.setName("greeting");
        greeting.setConfidence(0.9);
        greeting.setPatterns(Arrays.asList(
            "hello", "hi", "hey", "good morning", "good afternoon", "good evening",
            "how are you", "what's up", "greetings"
        ));
        greeting.setResponses(Arrays.asList(
            "Hello {{userName}}! How can I help you today?",
            "Hi there! I'm here to assist you with the disaster relief platform.",
            "Greetings! What can I do for you?"
        ));
        greeting.setQuickReplies(Arrays.asList("Help", "Status", "Tasks", "Emergency"));
        intents.put("greeting", greeting);

        // Help intent
        BotIntent help = new BotIntent();
        help.setName("help");
        help.setConfidence(0.8);
        help.setPatterns(Arrays.asList(
            "help", "assist", "support", "what can you do", "commands", "options"
        ));
        help.setResponses(Arrays.asList(
            "I can help you with:\n• Creating and managing requests\n• Checking task status\n• Finding resources\n• Emergency procedures\n• Platform navigation\n\nWhat would you like to know?",
            "Here's what I can assist you with:\n• Request management\n• Task coordination\n• Resource location\n• Emergency protocols\n• User guidance\n\nHow can I help?"
        ));
        help.setSuggestedActions(Arrays.asList("Create Request", "View Tasks", "Find Resources", "Emergency Help"));
        intents.put("help", help);

        // Emergency intent
        BotIntent emergency = new BotIntent();
        emergency.setName("emergency");
        emergency.setConfidence(0.95);
        emergency.setPatterns(Arrays.asList(
            "emergency", "urgent", "critical", "help now", "danger", "crisis", "disaster"
        ));
        emergency.setResponses(Arrays.asList(
            "🚨 EMERGENCY DETECTED! I'm escalating this to emergency responders immediately. Please stay safe and follow emergency protocols.",
            "URGENT: I've flagged this as an emergency. Emergency services have been notified. Please remain calm and follow safety procedures."
        ));
        emergency.setSuggestedActions(Arrays.asList("Call Emergency Services", "Create Emergency Request", "View Emergency Procedures"));
        intents.put("emergency", emergency);

        // Task status intent
        BotIntent taskStatus = new BotIntent();
        taskStatus.setName("task_status");
        taskStatus.setConfidence(0.8);
        taskStatus.setPatterns(Arrays.asList(
            "task status", "my tasks", "task progress", "what tasks", "task update", "task list"
        ));
        taskStatus.setResponses(Arrays.asList(
            "Let me check your current tasks. You can view all your tasks in the Tasks section of the platform.",
            "I can help you check your task status. Please go to the Tasks page to see your current assignments."
        ));
        taskStatus.setSuggestedActions(Arrays.asList("View My Tasks", "Create New Task", "Update Task Status"));
        intents.put("task_status", taskStatus);

        // Request creation intent
        BotIntent createRequest = new BotIntent();
        createRequest.setName("create_request");
        createRequest.setConfidence(0.8);
        createRequest.setPatterns(Arrays.asList(
            "create request", "new request", "submit request", "report issue", "need help", "request help"
        ));
        createRequest.setResponses(Arrays.asList(
            "I can help you create a new request. Please go to the Requests section and click 'Create New Request' to get started.",
            "To create a new request, navigate to the Requests page and fill out the request form with your specific needs."
        ));
        createRequest.setSuggestedActions(Arrays.asList("Create Request", "View Request Types", "Emergency Request"));
        intents.put("create_request", createRequest);

        // Resource location intent
        BotIntent findResources = new BotIntent();
        findResources.setName("find_resources");
        findResources.setConfidence(0.8);
        findResources.setPatterns(Arrays.asList(
            "find resources", "where is", "location", "nearby", "resources", "supplies", "inventory"
        ));
        findResources.setResponses(Arrays.asList(
            "I can help you find resources. Check the Inventory section to see available supplies and their locations.",
            "To find resources, go to the Inventory page where you can search for specific items and see their availability."
        ));
        findResources.setSuggestedActions(Arrays.asList("View Inventory", "Search Resources", "Check Availability"));
        intents.put("find_resources", findResources);

        // Weather intent
        BotIntent weather = new BotIntent();
        weather.setName("weather");
        weather.setConfidence(0.7);
        weather.setPatterns(Arrays.asList(
            "weather", "forecast", "rain", "storm", "temperature", "climate"
        ));
        weather.setResponses(Arrays.asList(
            "For weather information, please check the Weather section in the platform or visit your local weather service.",
            "Weather updates are available in the platform's Weather section. Stay informed about current conditions."
        ));
        weather.setSuggestedActions(Arrays.asList("View Weather", "Weather Alerts", "Emergency Weather"));
        intents.put("weather", weather);

        // Goodbye intent
        BotIntent goodbye = new BotIntent();
        goodbye.setName("goodbye");
        goodbye.setConfidence(0.9);
        goodbye.setPatterns(Arrays.asList(
            "bye", "goodbye", "see you", "farewell", "thanks", "thank you", "exit", "quit"
        ));
        goodbye.setResponses(Arrays.asList(
            "Goodbye {{userName}}! Stay safe and don't hesitate to reach out if you need help.",
            "Take care! Remember, I'm always here if you need assistance.",
            "Farewell! Stay safe and feel free to contact me anytime."
        ));
        intents.put("goodbye", goodbye);

        // Fallback intent
        BotIntent fallback = new BotIntent();
        fallback.setName("fallback");
        fallback.setConfidence(0.1);
        fallback.setPatterns(Arrays.asList(".*"));
        fallback.setResponses(Arrays.asList(
            "I'm not sure I understand. Could you please rephrase your question?",
            "I didn't quite catch that. Can you try asking in a different way?",
            "I'm still learning. Could you provide more details about what you need help with?"
        ));
        fallback.setSuggestedActions(Arrays.asList("Ask for Help", "View Commands", "Contact Support"));
        intents.put("fallback", fallback);
    }

    // Data classes
    public static class ChatSession {
        private String id;
        private UUID userId;
        private LocalDateTime createdAt;
        private LocalDateTime lastActivity;
        private LocalDateTime endedAt;
        private SessionStatus status;
        private List<ChatMessage> messages = new ArrayList<>();

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

        public LocalDateTime getEndedAt() { return endedAt; }
        public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

        public SessionStatus getStatus() { return status; }
        public void setStatus(SessionStatus status) { this.status = status; }

        public List<ChatMessage> getMessages() { return messages; }
        public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    }

    public static class ChatMessage {
        private String id;
        private String sessionId;
        private UUID userId;
        private String userName;
        private String message;
        private MessageType messageType;
        private LocalDateTime timestamp;
        private String intent;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public MessageType getMessageType() { return messageType; }
        public void setMessageType(MessageType messageType) { this.messageType = messageType; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
    }

    public static class ChatBotResponse {
        private String sessionId;
        private String message;
        private String intent;
        private double confidence;
        private List<String> suggestedActions;
        private List<String> quickReplies;

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public List<String> getSuggestedActions() { return suggestedActions; }
        public void setSuggestedActions(List<String> suggestedActions) { this.suggestedActions = suggestedActions; }

        public List<String> getQuickReplies() { return quickReplies; }
        public void setQuickReplies(List<String> quickReplies) { this.quickReplies = quickReplies; }
    }

    public static class BotIntent {
        private String name;
        private double confidence;
        private List<String> patterns;
        private List<String> responses;
        private List<String> suggestedActions;
        private List<String> quickReplies;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }

        public List<String> getResponses() { return responses; }
        public void setResponses(List<String> responses) { this.responses = responses; }

        public List<String> getSuggestedActions() { return suggestedActions; }
        public void setSuggestedActions(List<String> suggestedActions) { this.suggestedActions = suggestedActions; }

        public List<String> getQuickReplies() { return quickReplies; }
        public void setQuickReplies(List<String> quickReplies) { this.quickReplies = quickReplies; }
    }

    public enum MessageType {
        USER, BOT, SYSTEM
    }

    public enum SessionStatus {
        ACTIVE, ENDED, PAUSED
    }
}
