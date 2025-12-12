import { apiService } from './api';

export interface DisasterPredictionModel {
  id: string;
  name: string;
  description: string;
  disasterType: string;
  features: Record<string, any>;
  parameters: Record<string, any>;
  createdAt: string;
  trainedAt?: string;
  accuracy: number;
  status: 'CREATED' | 'TRAINING' | 'READY' | 'ERROR';
}

export interface DisasterPrediction {
  id: string;
  modelId: string;
  disasterType: string;
  input: PredictionInput;
  predictedAt: string;
  likelihood: number;
  confidence: number;
  severity: string;
  timeline: PredictionTimeline;
  affectedAreas: string[];
  recommendedActions: string[];
}

export interface PredictionInput {
  weatherData?: Record<string, any>;
  geographicData?: Record<string, any>;
  historicalData?: Record<string, any>;
  realTimeData?: Record<string, any>;
}

export interface PredictionTimeline {
  immediateRisk: boolean;
  immediateRiskStart?: string;
  immediateRiskEnd?: string;
  peakRiskStart?: string;
  peakRiskEnd?: string;
  expectedStart?: string;
  expectedEnd?: string;
}

class DisasterPredictionService {
  private baseUrl = '/api/ai/disaster-prediction';

  async createModel(
    name: string,
    description: string,
    disasterType: string,
    features: Record<string, any>,
    parameters: Record<string, any>
  ): Promise<DisasterPredictionModel> {
    return apiService.post(`${this.baseUrl}/models`, {
      name,
      description,
      disasterType,
      features,
      parameters
    });
  }

  async trainModel(modelId: string, trainingData: any[]): Promise<void> {
    return apiService.post(`${this.baseUrl}/models/${modelId}/train`, trainingData);
  }

  async predictDisaster(modelId: string, input: PredictionInput): Promise<DisasterPrediction> {
    return apiService.post(`${this.baseUrl}/models/${modelId}/predict`, input);
  }

  async getPredictions(modelId: string, startTime: string, endTime: string): Promise<DisasterPrediction[]> {
    try {
      const response = await apiService.get<DisasterPrediction[]>(`${this.baseUrl}/models/${modelId}/predictions`, {
        startTime,
        endTime
      });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get predictions:', error);
      return [];
    }
  }

  async evaluateModel(modelId: string, testData: any[]): Promise<any> {
    return apiService.post(`${this.baseUrl}/models/${modelId}/evaluate`, testData);
  }

  async getModel(modelId: string): Promise<DisasterPredictionModel> {
    return apiService.get(`${this.baseUrl}/models/${modelId}`);
  }

  async getModels(): Promise<DisasterPredictionModel[]> {
    try {
      const response = await apiService.get<DisasterPredictionModel[]>(`${this.baseUrl}/models`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get models:', error);
      return [];
    }
  }
}

export const disasterPredictionService = new DisasterPredictionService();
export default disasterPredictionService;



