import React, { useState, useEffect, useRef } from 'react';
import { chatBotService, ChatSession, ChatMessage, ChatBotResponse } from '../../services/chatBotService';

interface ChatBotProps {
  onEmergencyDetected?: () => void;
  onClose?: () => void;
}

const ChatBot: React.FC<ChatBotProps> = ({ onEmergencyDetected, onClose }) => {
  const [session, setSession] = useState<ChatSession | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isMinimized, setIsMinimized] = useState(false);
  const [isSending, setIsSending] = useState(false); // Prevent duplicate sends

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const lastSentMessage = useRef<string>(''); // Track last sent message to prevent duplicates

  useEffect(() => {
    initializeChat();
    
    // Add message handler with deduplication
    const handleMessage = (message: ChatMessage) => {
      setMessages(prev => {
        // Check if message already exists to prevent duplicates
        const exists = prev.some(m => m.id === message.id);
        if (exists) {
          console.warn('Duplicate message detected, skipping:', message.id);
          return prev;
        }
        return [...prev, message];
      });
      scrollToBottom();
    };

    chatBotService.addMessageHandler(handleMessage);

    return () => {
      chatBotService.removeMessageHandler(handleMessage);
    };
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const initializeChat = async () => {
    try {
      const newSession = await chatBotService.startNewSession();
      setSession(newSession);
      // Ensure messages is always an array
      setMessages(Array.isArray(newSession?.messages) ? newSession.messages : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to initialize chat');
      // Initialize with empty messages array on error
      setMessages([]);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const sendMessage = async () => {
    if (!inputMessage.trim() || isSending) return;

    const message = inputMessage.trim();
    
    // Prevent duplicate sends of same message
    if (message === lastSentMessage.current) {
      console.warn('Duplicate message send prevented:', message);
      return;
    }
    
    lastSentMessage.current = message;
    setInputMessage('');
    setIsSending(true);

    try {
      // Check for emergency
      if (chatBotService.isEmergencyMessage(message)) {
        onEmergencyDetected?.();
        const emergencyResponse = chatBotService.getEmergencyResponse();
        const emergencyMessage = chatBotService.processBotResponse(emergencyResponse);
        return;
      }

      // Create user message - this will trigger message handler
      const userMessage = chatBotService.createUserMessage(message);

      // Show typing indicator
      setIsTyping(true);

      try {
        const response = await chatBotService.sendMessage(message);
        // Process bot response - this will trigger message handler
        const botMessage = chatBotService.processBotResponse(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to send message');
      } finally {
        setIsTyping(false);
      }
    } finally {
      setIsSending(false);
      // Clear last sent message after a delay to allow new messages
      setTimeout(() => {
        lastSentMessage.current = '';
      }, 1000);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const sendQuickReply = (reply: string) => {
    setInputMessage(reply);
    inputRef.current?.focus();
  };

  const formatMessage = (message: ChatMessage) => {
    return chatBotService.formatMessage(message);
  };

  const getMessageTimestamp = (message: ChatMessage) => {
    return chatBotService.getMessageTimestamp(message);
  };

  const getQuickReplies = () => {
    return chatBotService.getQuickReplies();
  };

  const getSuggestedActions = () => {
    return chatBotService.getSuggestedActions();
  };

  if (isMinimized) {
    return (
      <div className="fixed bottom-4 right-4 z-50">
        <button
          onClick={() => setIsMinimized(false)}
          className="bg-blue-600 text-white p-4 rounded-full shadow-lg hover:bg-blue-700 transition-colors"
        >
          <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
          </svg>
        </button>
      </div>
    );
  }

  return (
    <div className="fixed bottom-4 right-4 w-96 h-96 bg-white rounded-lg shadow-lg border border-gray-200 z-50 flex flex-col">
      {/* Header */}
      <div className="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 bg-green-400 rounded-full"></div>
          <span className="font-semibold">ReliefBot</span>
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setIsMinimized(true)}
            className="text-white hover:text-gray-200 transition-colors"
          >
            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clipRule="evenodd" />
            </svg>
          </button>
          {onClose && (
            <button
              onClick={onClose}
              className="text-white hover:text-gray-200 transition-colors"
            >
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 p-4 overflow-y-auto space-y-3">
        {(Array.isArray(messages) ? messages : []).map((message) => (
          <div
            key={message.id}
            className={`flex ${message.messageType === 'USER' ? 'justify-end' : 'justify-start'}`}
          >
            <div
              className={`max-w-xs px-4 py-2 rounded-lg ${
                message.messageType === 'USER'
                  ? 'bg-blue-600 text-white'
                  : message.messageType === 'BOT'
                  ? 'bg-gray-100 text-gray-900'
                  : 'bg-yellow-100 text-yellow-900'
              }`}
            >
              <p className="text-sm">{formatMessage(message)}</p>
              <p className="text-xs opacity-75 mt-1">{getMessageTimestamp(message)}</p>
            </div>
          </div>
        ))}

        {isTyping && (
          <div className="flex justify-start">
            <div className="bg-gray-100 text-gray-900 px-4 py-2 rounded-lg">
              <div className="flex space-x-1">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
              </div>
            </div>
          </div>
        )}

        {error && (
          <div className="flex justify-start">
            <div className="bg-red-100 text-red-900 px-4 py-2 rounded-lg">
              <p className="text-sm">Error: {error}</p>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Quick Replies */}
      {messages.length > 0 && (
        <div className="p-4 border-t border-gray-200">
          <div className="flex flex-wrap gap-2">
            {(getQuickReplies() || []).slice(0, 4).map((reply) => (
              <button
                key={reply}
                onClick={() => sendQuickReply(reply)}
                className="text-xs bg-gray-100 text-gray-700 px-3 py-1 rounded-full hover:bg-gray-200 transition-colors"
              >
                {reply}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Input */}
      <div className="p-4 border-t border-gray-200">
        <div className="flex space-x-2">
          <input
            ref={inputRef}
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type your message..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={sendMessage}
            disabled={!inputMessage.trim() || isSending}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isSending ? 'Sending...' : 'Send'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatBot;


