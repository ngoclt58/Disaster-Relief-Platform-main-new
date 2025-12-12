import { apiService } from './api';

export interface ForecastingModel {
  id: string;
  name: string;
  description: string;
  resourceType: string;
  parameters: Record<string, any>;
  createdAt: string;
  trainedAt?: string;
  accuracy: number;
  status: 'CREATED' | 'TRAINING' | 'READY' | 'ERROR';
}

export interface DemandForecast {
  id: string;
  modelId: string;
  resourceType: string;
  input: ForecastInput;
  forecastedAt: string;
  projectedDemand: number;
  confidence: number;
  timeHorizon: ForecastTimeline;
  peakDemandTime: string;
  recommendedResources: string[];
}

export interface ForecastInput {
  disasterType?: string;
  severity?: number;
  populationDensity?: number;
  historicalDemand?: number;
}

export interface ForecastTimeline {
  immediateHoursStart: number;
  immediateHoursEnd: number;
  shortHoursStart: number;
  shortHoursEnd: number;
  mediumHoursStart: number;
  mediumHoursEnd: number;
  longHoursStart: number;
  longHoursEnd: number;
}

class ResourceForecastingService {
  private baseUrl = '/api/ai/resource-forecasting';

  async createModel(
    name: string,
    description: string,
    resourceType: string,
    parameters: Record<string, any>
  ): Promise<ForecastingModel> {
    return apiService.post(`${this.baseUrl}/models`, {
      name,
      description,
      resourceType,
      parameters
    });
  }

  async trainModel(modelId: string, trainingData: any[]): Promise<void> {
    return apiService.post(`${this.baseUrl}/models/${modelId}/train`, trainingData);
  }

  async forecastDemand(modelId: string, input: ForecastInput): Promise<DemandForecast> {
    return apiService.post(`${this.baseUrl}/models/${modelId}/forecast`, input);
  }

  async getForecasts(modelId: string, startTime: string, endTime: string): Promise<DemandForecast[]> {
    try {
      const response = await apiService.get<DemandForecast[]>(`${this.baseUrl}/models/${modelId}/forecasts`, {
        startTime,
        endTime
      });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get forecasts:', error);
      return [];
    }
  }

  async getModel(modelId: string): Promise<ForecastingModel> {
    return apiService.get(`${this.baseUrl}/models/${modelId}`);
  }

  async getModels(): Promise<ForecastingModel[]> {
    try {
      const response = await apiService.get<ForecastingModel[]>(`${this.baseUrl}/models`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get forecasting models:', error);
      return [];
    }
  }
}

export const resourceForecastingService = new ResourceForecastingService();
export default resourceForecastingService;



