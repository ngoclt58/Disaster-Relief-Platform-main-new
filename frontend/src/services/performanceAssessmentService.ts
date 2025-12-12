import { apiService } from './api';

export interface Assessment {
  id: string;
  name: string;
  description: string;
  assessmentType: string;
  category: string;
  criteria: Record<string, any>;
  createdBy: string;
  createdAt: string;
  isActive: boolean;
}

export interface AssessmentSession {
  id: string;
  assessmentId: string;
  participantId: string;
  sessionName: string;
  context: Record<string, any>;
  startedAt: string;
  completedAt?: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  completionNotes?: string;
}

export interface AssessmentQuestion {
  id: string;
  assessmentId: string;
  questionText: string;
  questionType: string;
  options: string[];
  correctAnswer: string;
  metadata: Record<string, any>;
  createdAt: string;
  isActive: boolean;
}

export interface AssessmentResponse {
  id: string;
  sessionId: string;
  questionId: string;
  responseType: string;
  responseData: any;
  timeSpent: number;
  timestamp: string;
}

export interface AssessmentScore {
  id: string;
  sessionId: string;
  totalScore: number;
  maxScore: number;
  percentage: number;
  correctAnswers: number;
  totalQuestions: number;
  timeToComplete: number;
  calculatedAt: string;
}

export interface AssessmentResult {
  id: string;
  sessionId: string;
  overallScore: number;
  competencyScores: Record<string, any>;
  strengths: string[];
  areasForImprovement: string[];
  recommendations: string[];
  generatedAt: string;
}

export interface CompetencyProfile {
  id: string;
  userId: string;
  competencyType: string;
  competencies: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface TrainingRecommendation {
  id: string;
  userId: string;
  recommendedCourses: string[];
  priorityAreas: string[];
  learningPath: string[];
  estimatedDuration: number;
  generatedAt: string;
}

export interface PerformanceReport {
  id: string;
  userId: string;
  startDate: string;
  endDate: string;
  generatedAt: string;
  overallPerformance: number;
  competencyBreakdown: Record<string, any>;
  improvementAreas: string[];
  strengths: string[];
  recommendations: string[];
}

export interface AssessmentAnalytics {
  assessmentId: string;
  totalSessions: number;
  averageScore: number;
  completionRate: number;
  averageTimeToComplete: number;
  difficultyAnalysis: Record<string, any>;
  performanceTrends: Array<Record<string, any>>;
}

export interface CreateAssessmentRequest {
  name: string;
  description: string;
  assessmentType: string;
  category: string;
  criteria: Record<string, any>;
}

export interface StartAssessmentRequest {
  assessmentId: string;
  sessionName: string;
  context: Record<string, any>;
}

export interface RecordResponseRequest {
  questionId: string;
  responseType: string;
  responseData: any;
  timeSpent: number;
}

export interface CompleteAssessmentRequest {
  completionNotes?: string;
}

export interface CreateQuestionRequest {
  assessmentId: string;
  questionText: string;
  questionType: string;
  options: string[];
  correctAnswer: string;
  metadata: Record<string, any>;
}

export interface CreateCompetencyProfileRequest {
  competencyType: string;
  competencies: Record<string, any>;
}

export interface UpdateCompetencyProfileRequest {
  competencies: Record<string, any>;
}

export interface GeneratePerformanceReportRequest {
  startDate: string;
  endDate: string;
}

class PerformanceAssessmentService {
  private baseUrl = '/performance-assessment';

  async createAssessment(request: CreateAssessmentRequest): Promise<Assessment> {
    return apiService.post(`${this.baseUrl}/assessments`, request);
  }

  async startAssessment(request: StartAssessmentRequest): Promise<AssessmentSession> {
    return apiService.post(`${this.baseUrl}/sessions`, request);
  }

  async recordResponse(sessionId: string, request: RecordResponseRequest): Promise<AssessmentResponse> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/responses`, request);
  }

  async calculateScore(sessionId: string): Promise<AssessmentScore> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/score`);
  }

  async completeAssessment(sessionId: string, request: CompleteAssessmentRequest): Promise<AssessmentSession> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/complete`, request);
  }

  async createQuestion(request: CreateQuestionRequest): Promise<AssessmentQuestion> {
    return apiService.post(`${this.baseUrl}/questions`, request);
  }

  async getAssessmentQuestions(assessmentId: string): Promise<AssessmentQuestion[]> {
    return apiService.get(`${this.baseUrl}/assessments/${assessmentId}/questions`);
  }

  async generateResult(sessionId: string): Promise<AssessmentResult> {
    return apiService.post(`${this.baseUrl}/sessions/${sessionId}/result`);
  }

  async getAssessments(filters: {
    category?: string;
    assessmentType?: string;
    isActive?: boolean;
  } = {}): Promise<Assessment[]> {
    return apiService.get(`${this.baseUrl}/assessments`, filters);
  }

  async getAssessment(assessmentId: string): Promise<Assessment> {
    return apiService.get(`${this.baseUrl}/assessments/${assessmentId}`);
  }

  async getUserSessions(participantId: string, status?: string): Promise<AssessmentSession[]> {
    return apiService.get(`${this.baseUrl}/sessions`, { participantId, status });
  }

  async getSession(sessionId: string): Promise<AssessmentSession> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}`);
  }

  async getSessionResponses(sessionId: string): Promise<AssessmentResponse[]> {
    return apiService.get(`${this.baseUrl}/sessions/${sessionId}/responses`);
  }

  async getAssessmentAnalytics(assessmentId: string): Promise<AssessmentAnalytics> {
    return apiService.get(`${this.baseUrl}/assessments/${assessmentId}/analytics`);
  }

  async createCompetencyProfile(request: CreateCompetencyProfileRequest): Promise<CompetencyProfile> {
    return apiService.post(`${this.baseUrl}/competencies`, request);
  }

  async updateCompetencyProfile(profileId: string, request: UpdateCompetencyProfileRequest): Promise<CompetencyProfile> {
    return apiService.put(`${this.baseUrl}/competencies/${profileId}`, request);
  }

  async getUserCompetencies(): Promise<CompetencyProfile[]> {
    return apiService.get(`${this.baseUrl}/competencies`);
  }

  async generateTrainingRecommendations(): Promise<TrainingRecommendation> {
    return apiService.post(`${this.baseUrl}/recommendations`);
  }

  async generatePerformanceReport(request: GeneratePerformanceReportRequest): Promise<PerformanceReport> {
    return apiService.post(`${this.baseUrl}/reports`, request);
  }
}

export const performanceAssessmentService = new PerformanceAssessmentService();


