import { apiService } from './api';

export interface SimulationScenario {
  id: string;
  name: string;
  description: string;
  disasterType: string;
  difficulty: string;
  location: string;
  parameters: Record<string, any>;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  isActive: boolean;
}

export interface SimulationSession {
  id: string;
  scenarioId: string;
  sessionName: string;
  participantIds: string[];
  instructorId: string;
  startedAt: string;
  endedAt?: string;
  status: 'SCHEDULED' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  currentPhase: 'PREPARATION' | 'RESPONSE' | 'RECOVERY' | 'EVALUATION';
  endReason?: string;
}

export interface SimulationEvent {
  id: string;
  sessionId: string;
  eventType: string;
  eventData: Record<string, any>;
  triggeredBy: string;
  timestamp: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface SimulationResponse {
  id: string;
  sessionId: string;
  participantId: string;
  eventId: string;
  responseType: string;
  responseData: Record<string, any>;
  timestamp: string;
  isCorrect: boolean;
}

export interface SimulationScore {
  id: string;
  sessionId: string;
  participantId: string;
  totalScore: number;
  maxScore: number;
  percentage: number;
  correctResponses: number;
  totalResponses: number;
  timeToComplete: number;
  areasOfStrength: string[];
  areasForImprovement: string[];
  calculatedAt: string;
}

export interface SimulationAnalytics {
  sessionId: string;
  totalParticipants: number;
  averageScore: number;
  completionRate: number;
  commonMistakes: string[];
  bestPractices: string[];
  performanceMetrics: Record<string, any>;
}

export interface SimulationTemplate {
  id: string;
  name: string;
  description: string;
  disasterType: string;
  configuration: Record<string, any>;
  createdBy: string;
  createdAt: string;
  isPublic: boolean;
}

export interface CreateScenarioRequest {
  name: string;
  description: string;
  disasterType: string;
  difficulty: string;
  location: string;
  parameters: Record<string, any>;
}

export interface StartSessionRequest {
  scenarioId: string;
  sessionName: string;
  participantIds: string[];
}

export interface TriggerEventRequest {
  eventType: string;
  eventData: Record<string, any>;
}

export interface RecordResponseRequest {
  eventId: string;
  responseType: string;
  responseData: Record<string, any>;
}

export interface EndSessionRequest {
  reason: string;
}

export interface CreateTemplateRequest {
  name: string;
  description: string;
  disasterType: string;
  configuration: Record<string, any>;
}

class DisasterSimulationService {
  private baseUrl = '/disaster-simulation';

  async createScenario(request: CreateScenarioRequest): Promise<SimulationScenario> {
    return apiService.post(`${this.baseUrl}/scenarios`, request);
  }

  async startSession(request: StartSessionRequest): Promise<SimulationSession> {
    return apiService.post(`${this.baseUrl}/sessions`, request);
  }

  async joinSession(sessionId: string): Promise<SimulationSession> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/join`);
  }

  async leaveSession(sessionId: string): Promise<SimulationSession> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/leave`);
  }

  async triggerEvent(sessionId: string, request: TriggerEventRequest): Promise<SimulationEvent> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/events`, request);
  }

  async recordResponse(sessionId: string, request: RecordResponseRequest): Promise<SimulationResponse> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/responses`, request);
  }

  async calculateScore(sessionId: string, participantId: string): Promise<SimulationScore> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/score`, { participantId });
  }

  async endSession(sessionId: string, request: EndSessionRequest): Promise<SimulationSession> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/end`, request);
  }

  async getScenarios(filters: {
    disasterType?: string;
    difficulty?: string;
    isActive?: boolean;
  } = {}): Promise<SimulationScenario[]> {
    return apiService.get(`${this.baseUrl}/scenarios`, filters);
  }

  async getSessions(filters: {
    participantId?: string;
    instructorId?: string;
    status?: string;
  } = {}): Promise<SimulationSession[]> {
    return apiService.get(`${this.baseUrl}/sessions`, filters);
  }

  async getSession(sessionId: string): Promise<SimulationSession> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}`);
  }

  async getSessionEvents(sessionId: string): Promise<SimulationEvent[]> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}/events`);
  }

  async getParticipantResponses(sessionId: string, participantId: string): Promise<SimulationResponse[]> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}/responses`, { participantId });
  }

  async getSessionAnalytics(sessionId: string): Promise<SimulationAnalytics> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}/analytics`);
  }

  async getTemplates(disasterType?: string): Promise<SimulationTemplate[]> {
    return apiService.get(`${this.baseUrl}/templates`, { disasterType });
  }

  async createTemplate(request: CreateTemplateRequest): Promise<SimulationTemplate> {
    return apiService.post(`${this.baseUrl}/templates`, request);
  }
}

export const disasterSimulationService = new DisasterSimulationService();


