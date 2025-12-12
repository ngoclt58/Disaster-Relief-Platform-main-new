import { apiService } from './api';

export interface TrendAnalyzer {
  id: string;
  name: string;
  description: string;
  dataSource: string;
  metric: string;
  configuration: Record<string, any>;
  createdAt: string;
  updatedAt?: string;
  isActive: boolean;
  windowSize: number;
}

export interface DataPoint {
  id: string;
  value: number;
  timestamp: string;
  metadata: Record<string, any>;
  isForecast: boolean;
  isAnomaly: boolean;
}

export interface TrendResult {
  id: string;
  analyzerId: string;
  analyzedAt: string;
  dataPoints: DataPoint[];
  trendDirection: 'INCREASING' | 'DECREASING' | 'STABLE' | 'INSUFFICIENT_DATA';
  trendStrength: number;
  confidence: number;
  slope: number;
  r2: number;
  isSignificant: boolean;
  trendType: string;
  forecast: DataPoint[];
  anomalies: DataPoint[];
}

export interface TrendAlert {
  id: string;
  analyzerId: string;
  name: string;
  condition: string;
  threshold: string;
  action: string;
  createdAt: string;
  triggeredAt?: string;
  isActive: boolean;
  triggerCount: number;
}

export interface TrendAnalytics {
  analyzerId: string;
  totalDataPoints: number;
  totalTrends: number;
  averageConfidence: number;
  lastAnalyzed: string;
}

class TrendAnalysisService {
  private baseUrl = '/realtime/trend-analysis';

  async createAnalyzer(
    name: string,
    description: string,
    dataSource: string,
    metric: string,
    configuration: Record<string, any>
  ): Promise<TrendAnalyzer> {
    return apiService.post(`${this.baseUrl}/analyzers`, {
      name,
      description,
      dataSource,
      metric,
      configuration
    });
  }

  async addDataPoint(analyzerId: string, dataPoint: DataPoint): Promise<void> {
    return apiService.post(`${this.baseUrl}/analyzers/${analyzerId}/data`, dataPoint);
  }

  async analyzeTrend(analyzerId: string): Promise<TrendResult> {
    return apiService.post(`${this.baseUrl}/analyzers/${analyzerId}/analyze`);
  }

  async getTrends(
    analyzerId: string,
    startTime: string,
    endTime: string
  ): Promise<TrendResult[]> {
    try {
      const response = await apiService.get<TrendResult[]>(`${this.baseUrl}/analyzers/${analyzerId}/trends`, {
        startTime,
        endTime
      });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get trends:', error);
      return [];
    }
  }

  async createAlert(
    analyzerId: string,
    name: string,
    condition: string,
    threshold: string,
    action: string
  ): Promise<TrendAlert> {
    return apiService.post(`${this.baseUrl}/alerts`, {
      analyzerId,
      name,
      condition,
      threshold,
      action
    });
  }

  async checkAlerts(analyzerId: string): Promise<TrendAlert[]> {
    return apiService.get(`${this.baseUrl}/analyzers/${analyzerId}/alerts`);
  }

  async getAnalytics(analyzerId: string): Promise<TrendAnalytics> {
    return apiService.get(`${this.baseUrl}/analyzers/${analyzerId}/analytics`);
  }

  async getAnalyzer(analyzerId: string): Promise<TrendAnalyzer> {
    return apiService.get(`${this.baseUrl}/analyzers/${analyzerId}`);
  }

  async getAnalyzers(): Promise<TrendAnalyzer[]> {
    try {
      const response = await apiService.get<TrendAnalyzer[]>(`${this.baseUrl}/analyzers`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get analyzers:', error);
      return [];
    }
  }

  async updateAnalyzer(
    analyzerId: string,
    name: string,
    description: string,
    configuration: Record<string, any>
  ): Promise<void> {
    return apiService.put(`/realtime/trend-analysis/analyzers/${analyzerId}`, {
      name,
      description,
      configuration
    });
  }

  async deleteAnalyzer(analyzerId: string): Promise<void> {
    return apiService.delete(`/realtime/trend-analysis/analyzers/${analyzerId}`);
  }
}

export const trendAnalysisService = new TrendAnalysisService();


