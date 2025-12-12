import { apiService } from './api';

export interface Event {
  id: string;
  source: string;
  type: string;
  data: Record<string, any>;
  timestamp: string;
  severity: string;
  metadata: Record<string, any>;
}

export interface CorrelationRule {
  id: string;
  name: string;
  description: string;
  pattern: string;
  conditions: Record<string, any>;
  action: string;
  createdAt: string;
  updatedAt?: string;
  isActive: boolean;
  priority: number;
}

export interface CorrelationResult {
  id: string;
  ruleId: string;
  events: Event[];
  correlationScore: number;
  correlatedAt: string;
  isSignificant: boolean;
}

export interface EventPattern {
  id: string;
  source: string;
  eventType: string;
  events: Event[];
  patternType: string;
  confidence: number;
  detectedAt: string;
  startTime: string;
  endTime: string;
}

export interface CorrelationAnalytics {
  source: string;
  totalEvents: number;
  totalCorrelations: number;
  averageCorrelationScore: number;
  patternCount: number;
  lastAnalyzed: string;
}

class EventCorrelationService {
  private baseUrl = '/api/realtime/event-correlation';

  async createRule(
    name: string,
    description: string,
    pattern: string,
    conditions: Record<string, any>,
    action: string
  ): Promise<CorrelationRule> {
    return apiService.post(`${this.baseUrl}/rules`, {
      name,
      description,
      pattern,
      conditions,
      action
    });
  }

  async processEvent(event: Event): Promise<void> {
    return apiService.post(`${this.baseUrl}/events`, event);
  }

  async correlateEvents(ruleId: string, eventIds: string[]): Promise<CorrelationResult> {
    return apiService.post(`${this.baseUrl}/correlate`, {
      ruleId,
      eventIds
    });
  }

  async findCorrelations(
    source?: string,
    startTime?: string,
    endTime?: string
  ): Promise<CorrelationResult[]> {
    try {
      const params = new URLSearchParams();
      if (source) params.append('source', source);
      if (startTime) params.append('startTime', startTime);
      if (endTime) params.append('endTime', endTime);
      
      const response = await apiService.get<CorrelationResult[]>(`${this.baseUrl}/correlations?${params}`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to find correlations:', error);
      return [];
    }
  }

  async detectPattern(
    source: string,
    eventType: string,
    startTime: string,
    endTime: string
  ): Promise<EventPattern | null> {
    try {
      const response = await apiService.post<EventPattern>(`${this.baseUrl}/patterns/detect`, {
        source,
        eventType,
        startTime,
        endTime
      });
      return response || null;
    } catch (error) {
      console.error('Failed to detect pattern:', error);
      return null;
    }
  }

  async getAnalytics(source: string): Promise<CorrelationAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, { source });
  }

  async getRule(ruleId: string): Promise<CorrelationRule> {
    return apiService.get(`${this.baseUrl}/rules/${ruleId}`);
  }

  async getRules(): Promise<CorrelationRule[]> {
    return apiService.get(`${this.baseUrl}/rules`);
  }

  async updateRule(
    ruleId: string,
    name: string,
    description: string,
    pattern: string,
    conditions: Record<string, any>,
    action: string
  ): Promise<void> {
    return apiService.put(`${this.baseUrl}/rules/${ruleId}`, {
      name,
      description,
      pattern,
      conditions,
      action
    });
  }

  async deleteRule(ruleId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/rules/${ruleId}`);
  }
}

export const eventCorrelationService = new EventCorrelationService();


