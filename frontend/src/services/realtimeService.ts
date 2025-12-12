import { useAuthStore } from '../store/authStore';

// Import API_BASE_URL from api.ts to ensure consistency
const getApiBaseUrl = () => {
  return process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
};

export type RealtimeEventType = 
  | 'needs.created'
  | 'needs.updated'
  | 'task.created'
  | 'task.assigned'
  | 'task.updated'
  | 'delivery.created'
  | 'inventory.updated'
  | 'heartbeat';

export interface RealtimeEvent {
  id: string;
  type: RealtimeEventType;
  ts: string;
  data: any;
}

export class RealtimeService {
  private eventSource: EventSource | null = null;
  private listeners: Map<RealtimeEventType, Set<(event: RealtimeEvent) => void>> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect(): void {
    if (this.eventSource) {
      this.disconnect();
    }

    const { token } = useAuthStore.getState();
    const baseUrl = getApiBaseUrl();
    if (!baseUrl || baseUrl === 'undefined') {
      console.error('API_BASE_URL is undefined. Please set REACT_APP_API_URL environment variable.');
      return;
    }
    const url = `${baseUrl}/requests/stream${token ? `?token=${token}` : ''}`;
    
    console.log('Connecting to SSE:', url);
    this.eventSource = new EventSource(url);
    
    this.eventSource.onopen = () => {
      console.log('SSE connection opened');
      this.reconnectAttempts = 0;
    };

    this.eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.handleEvent(data);
      } catch (error) {
        console.error('Failed to parse SSE event:', error);
      }
    };

    this.eventSource.onerror = (error) => {
      // Only log error if not already reconnecting
      if (this.reconnectAttempts === 0) {
        console.warn('SSE connection error. Will attempt to reconnect...');
      }
      this.handleReconnect();
    };

    // Listen for specific event types including smart automation and task management events
    Object.values([
      'needs.created', 'needs.updated', 'task.created', 'task.assigned', 'task.updated', 
      'delivery.created', 'inventory.updated', 'heartbeat',
      // Smart automation events
      'ai.categorization.completed', 'workflow.started', 'workflow.completed', 'workflow.failed',
      'notification.sent', 'escalation.triggered', 'dedupe.group.created', 'dedupe.group.merged',
      // Advanced task management events
      'task.dynamic.created', 'task.skill.matched', 'task.auto.assigned', 'task.dependency.ready',
      'task.workflow.updated', 'task.performance.updated', 'task.analytics.updated'
    ] as RealtimeEventType[]).forEach(eventType => {
      this.eventSource?.addEventListener(eventType, (event: any) => {
        try {
          const data = JSON.parse(event.data);
          this.handleEvent(data);
        } catch (error) {
          console.error(`Failed to parse ${eventType} event:`, error);
        }
      });
    });
  }

  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

  subscribe(eventType: RealtimeEventType, callback: (event: RealtimeEvent) => void): () => void {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, new Set());
    }
    
    this.listeners.get(eventType)!.add(callback);
    
    // Return unsubscribe function
    return () => {
      this.listeners.get(eventType)?.delete(callback);
    };
  }

  private handleEvent(event: RealtimeEvent): void {
    const callbacks = this.listeners.get(event.type);
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(event);
        } catch (error) {
          console.error('Error in event callback:', error);
        }
      });
    }
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      // Only log once when max attempts reached
      if (this.reconnectAttempts === this.maxReconnectAttempts) {
        console.warn('Max reconnection attempts reached for SSE. Backend may not be running.');
      }
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    
    // Only log first few reconnection attempts
    if (this.reconnectAttempts <= 3) {
      console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
    }
    
    setTimeout(() => {
      this.connect();
    }, delay);
  }

  isConnected(): boolean {
    return this.eventSource?.readyState === EventSource.OPEN;
  }

  getConnectionState(): number {
    return this.eventSource?.readyState ?? EventSource.CLOSED;
  }
}

export const realtimeService = new RealtimeService();

