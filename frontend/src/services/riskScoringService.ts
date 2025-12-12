import { apiService } from './api';

export interface RiskScore {
  id: string;
  input: RiskInput;
  calculatedAt: string;
  overallRisk: number;
  riskLevel: string;
  geographicRisk: number;
  weatherRisk: number;
  populationRisk: number;
  infrastructureRisk: number;
  historicalRisk: number;
  riskFactors: RiskFactor[];
  recommendations: string[];
}

export interface RiskInput {
  geographicData?: Record<string, any>;
  weatherData?: Record<string, any>;
  populationData?: Record<string, any>;
  infrastructureData?: Record<string, any>;
  historicalData?: Record<string, any>;
}

export interface RiskFactor {
  category: string;
  score: number;
  description: string;
}

export interface RiskComparison {
  comparedAt: string;
  highestRisk?: RiskScore;
  lowestRisk?: RiskScore;
  averageRisk: number;
}

class RiskScoringService {
  private baseUrl = '/api/ai/risk-scoring';

  async calculateRiskScore(input: RiskInput): Promise<RiskScore> {
    return apiService.post(`${this.baseUrl}/calculate`, input);
  }

  async getRiskScores(startTime: string, endTime: string): Promise<RiskScore[]> {
    try {
      const response = await apiService.get<RiskScore[]>(`${this.baseUrl}/scores`, { startTime, endTime });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get risk scores:', error);
      return [];
    }
  }

  async getRiskScore(scoreId: string): Promise<RiskScore> {
    return apiService.get(`${this.baseUrl}/scores/${scoreId}`);
  }

  async compareRiskScores(scoreIds: string[]): Promise<RiskComparison> {
    return apiService.post(`${this.baseUrl}/compare`, scoreIds);
  }
}

export const riskScoringService = new RiskScoringService();
export default riskScoringService;

