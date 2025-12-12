/**
 * Chat Bot Service
 * Handles AI-powered chat bot interactions
 */

import { apiService } from './api';

export interface ChatSession {
  id: string;
  userId: string;
  createdAt: string;
  lastActivity: string;
  endedAt?: string;
  status: 'ACTIVE' | 'ENDED' | 'PAUSED';
  messages: ChatMessage[];
}

export interface ChatMessage {
  id: string;
  sessionId: string;
  userId?: string;
  userName: string;
  message: string;
  messageType: 'USER' | 'BOT' | 'SYSTEM';
  timestamp: string;
  intent?: string;
}

export interface ChatBotResponse {
  sessionId: string;
  message: string;
  intent: string;
  confidence: number;
  suggestedActions?: string[];
  quickReplies?: string[];
}

export interface BotIntent {
  name: string;
  confidence: number;
  patterns: string[];
  responses: string[];
  suggestedActions?: string[];
  quickReplies?: string[];
}

class ChatBotService {
  private currentSessionId: string | null = null;
  private messageHandlers: ((message: ChatMessage) => void)[] = [];

  /**
   * Send message to chat bot
   */
  async sendMessage(message: string, sessionId?: string): Promise<ChatBotResponse> {
    // Ensure we have a sessionId - generate one if needed
    const effectiveSessionId = sessionId || this.currentSessionId || this.generateSessionId();
    
    const response = await apiService.post<ChatBotResponse>('/chatbot/message', {
      sessionId: effectiveSessionId,
      message: message.trim()
    });

    // Update current session ID
    if (response.sessionId) {
      this.currentSessionId = response.sessionId;
    }

    return response;
  }

  /**
   * Generate a new session ID
   */
  private generateSessionId(): string {
    const sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    this.currentSessionId = sessionId;
    return sessionId;
  }

  /**
   * Get user's chat sessions
   */
  async getUserSessions(): Promise<ChatSession[]> {
    try {
      const response = await apiService.get<ChatSession[]>('/chatbot/sessions');
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get user sessions:', error);
      return [];
    }
  }

  /**
   * Get chat session details
   */
  async getChatSession(sessionId: string): Promise<ChatSession> {
    try {
      const response = await apiService.get<ChatSession>(`/chatbot/sessions/${sessionId}`);
      // Ensure messages is always an array
      if (response && !Array.isArray(response.messages)) {
        response.messages = [];
      }
      return response;
    } catch (error) {
      console.error('Failed to get chat session:', error);
      // Return a default session structure
      return {
        id: sessionId,
        userId: '',
        createdAt: new Date().toISOString(),
        lastActivity: new Date().toISOString(),
        status: 'ACTIVE',
        messages: []
      };
    }
  }

  /**
   * End chat session
   */
  async endSession(sessionId: string): Promise<void> {
    await apiService.post(`/chatbot/sessions/${sessionId}/end`);
    
    if (this.currentSessionId === sessionId) {
      this.currentSessionId = null;
    }
  }

  /**
   * Start new chat session
   */
  async startNewSession(): Promise<ChatSession> {
    try {
      // Generate a new session ID first
      const sessionId = this.generateSessionId();
      
      // Send a greeting message to start new session
      const response = await this.sendMessage('Hello', sessionId);
      
      // Get the session details
      const session = await this.getChatSession(response.sessionId);
      
      // Ensure messages is always an array
      if (!Array.isArray(session.messages)) {
        session.messages = [];
      }
      
      return session;
    } catch (error) {
      console.error('Failed to start new session:', error);
      // Return a default session structure
      const sessionId = this.generateSessionId();
      return {
        id: sessionId,
        userId: '',
        createdAt: new Date().toISOString(),
        lastActivity: new Date().toISOString(),
        status: 'ACTIVE',
        messages: []
      };
    }
  }

  /**
   * Get current session ID
   */
  getCurrentSessionId(): string | null {
    return this.currentSessionId;
  }

  /**
   * Set current session ID
   */
  setCurrentSessionId(sessionId: string): void {
    this.currentSessionId = sessionId;
  }

  /**
   * Add message handler
   */
  addMessageHandler(handler: (message: ChatMessage) => void): void {
    this.messageHandlers.push(handler);
  }

  /**
   * Remove message handler
   */
  removeMessageHandler(handler: (message: ChatMessage) => void): void {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  /**
   * Notify message handlers
   */
  private notifyMessageHandlers(message: ChatMessage): void {
    this.messageHandlers.forEach(handler => {
      try {
        handler(message);
      } catch (error) {
        console.error('Error in message handler:', error);
      }
    });
  }

  /**
   * Process bot response and create message
   */
  processBotResponse(response: ChatBotResponse): ChatMessage {
    const message: ChatMessage = {
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      sessionId: response.sessionId,
      userId: undefined,
      userName: 'ReliefBot',
      message: response.message,
      messageType: 'BOT',
      timestamp: new Date().toISOString(),
      intent: response.intent
    };

    this.notifyMessageHandlers(message);
    return message;
  }

  /**
   * Create user message
   */
  createUserMessage(message: string): ChatMessage {
    const userMessage: ChatMessage = {
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      sessionId: this.currentSessionId || '',
      userId: 'current_user',
      userName: 'You',
      message,
      messageType: 'USER',
      timestamp: new Date().toISOString()
    };

    this.notifyMessageHandlers(userMessage);
    return userMessage;
  }

  /**
   * Get quick replies for common intents
   */
  getQuickReplies(): string[] {
    return [
      'Help',
      'Emergency',
      'My Tasks',
      'Create Request',
      'Find Resources',
      'Weather',
      'Goodbye'
    ];
  }

  /**
   * Get suggested actions for common intents
   */
  getSuggestedActions(): string[] {
    return [
      'Create Request',
      'View Tasks',
      'Find Resources',
      'Emergency Help',
      'Contact Support'
    ];
  }

  /**
   * Check if message is emergency
   */
  isEmergencyMessage(message: string): boolean {
    const emergencyKeywords = [
      'emergency', 'urgent', 'critical', 'help now', 'danger', 'crisis', 'disaster'
    ];
    
    const lowerMessage = message.toLowerCase();
    return emergencyKeywords.some(keyword => lowerMessage.includes(keyword));
  }

  /**
   * Get emergency response
   */
  getEmergencyResponse(): ChatBotResponse {
    return {
      sessionId: this.currentSessionId || '',
      message: '🚨 EMERGENCY DETECTED! I\'m escalating this to emergency responders immediately. Please stay safe and follow emergency protocols.',
      intent: 'emergency',
      confidence: 0.95,
      suggestedActions: ['Call Emergency Services', 'Create Emergency Request', 'View Emergency Procedures']
    };
  }

  /**
   * Format message for display
   */
  formatMessage(message: ChatMessage): string {
    if (message.messageType === 'BOT') {
      return `🤖 ${message.message}`;
    } else if (message.messageType === 'USER') {
      return `👤 ${message.message}`;
    } else {
      return `ℹ️ ${message.message}`;
    }
  }

  /**
   * Get message timestamp
   */
  getMessageTimestamp(message: ChatMessage): string {
    const date = new Date(message.timestamp);
    return date.toLocaleTimeString();
  }

  /**
   * Check if session is active
   */
  isSessionActive(session: ChatSession): boolean {
    return session.status === 'ACTIVE';
  }

  /**
   * Get session duration
   */
  getSessionDuration(session: ChatSession): string {
    const start = new Date(session.createdAt);
    const end = session.endedAt ? new Date(session.endedAt) : new Date();
    const duration = end.getTime() - start.getTime();
    
    const minutes = Math.floor(duration / 60000);
    const seconds = Math.floor((duration % 60000) / 1000);
    
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  /**
   * Get message count for session
   */
  getMessageCount(session: ChatSession): number {
    return session.messages.length;
  }

  /**
   * Get bot message count for session
   */
  getBotMessageCount(session: ChatSession): number {
    return session.messages.filter(msg => msg.messageType === 'BOT').length;
  }

  /**
   * Get user message count for session
   */
  getUserMessageCount(session: ChatSession): number {
    return session.messages.filter(msg => msg.messageType === 'USER').length;
  }
}

export const chatBotService = new ChatBotService();


