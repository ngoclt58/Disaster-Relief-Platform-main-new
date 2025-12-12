import { apiService } from './api';

export interface AnomalyDetector {
  id: string;
  name: string;
  description: string;
  dataSource: string;
  detectionType: 'STATISTICAL' | 'ISOLATION_FOREST' | 'ONE_CLASS_SVM' | 'DENSITY_BASED';
  configuration: Record<string, any>;
  createdAt: string;
  updatedAt?: string;
  isActive: boolean;
  sensitivity: number;
  modelId?: string;
}

export interface DataPoint {
  id: string;
  value: number;
  timestamp: string;
  metadata: Record<string, any>;
  isForecast: boolean;
  isAnomaly: boolean;
}

export interface DetectionModel {
  id: string;
  detectorId: string;
  modelType: string;
  parameters: Record<string, any>;
  trainingData: DataPoint[];
  trainedAt: string;
  accuracy: number;
  isReady: boolean;
}

export interface Anomaly {
  id: string;
  detectorId: string;
  dataPoint: DataPoint;
  anomalyScore: number;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  detectedAt: string;
  description: string;
  isResolved: boolean;
  resolution?: string;
  resolvedBy?: string;
  resolvedAt?: string;
}

export interface AnomalySummary {
  detectorId: string;
  totalAnomalies: number;
  highSeverityCount: number;
  mediumSeverityCount: number;
  lowSeverityCount: number;
  resolvedCount: number;
  averageScore: number;
  lastDetected: string;
}

export interface AnomalyPattern {
  id: string;
  detectorId: string;
  anomalies: Anomaly[];
  patternType: string;
  confidence: number;
  detectedAt: string;
  startTime: string;
  endTime: string;
}

export interface DetectionAnalytics {
  detectorId: string;
  totalAnomalies: number;
  detectionRate: number;
  falsePositiveRate: number;
  averageScore: number;
  lastDetected: string;
}

class AnomalyDetectionService {
  private baseUrl = '/api/realtime/anomaly-detection';

  async createDetector(
    name: string,
    description: string,
    dataSource: string,
    detectionType: 'STATISTICAL' | 'ISOLATION_FOREST' | 'ONE_CLASS_SVM' | 'DENSITY_BASED',
    configuration: Record<string, any>
  ): Promise<AnomalyDetector> {
    return apiService.post(`${this.baseUrl}/detectors`, {
      name,
      description,
      dataSource,
      detectionType,
      configuration
    });
  }

  async trainModel(
    detectorId: string,
    trainingData: DataPoint[],
    parameters: Record<string, any>
  ): Promise<DetectionModel> {
    return apiService.post(`${this.baseUrl}/models`, {
      detectorId,
      trainingData,
      parameters
    });
  }

  async detectAnomaly(detectorId: string, dataPoint: DataPoint): Promise<Anomaly> {
    return apiService.post(`${this.baseUrl}/detect`, {
      detectorId,
      dataPoint
    });
  }

  async getAnomalies(
    detectorId: string,
    startTime: string,
    endTime: string
  ): Promise<Anomaly[]> {
    try {
      const response = await apiService.get<Anomaly[]>(`${this.baseUrl}/anomalies`, {
        detectorId,
        startTime,
        endTime
      });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get anomalies:', error);
      return [];
    }
  }

  async getAnomalySummary(
    detectorId: string,
    startTime: string,
    endTime: string
  ): Promise<AnomalySummary> {
    return apiService.get(`${this.baseUrl}/anomalies/summary`, {
      detectorId,
      startTime,
      endTime
    });
  }

  async resolveAnomaly(
    anomalyId: string,
    resolution: string,
    resolvedBy: string
  ): Promise<void> {
    return apiService.post(`${this.baseUrl}/anomalies/${anomalyId}/resolve`, {
      resolution,
      resolvedBy
    });
  }

  async detectPattern(
    detectorId: string,
    startTime: string,
    endTime: string
  ): Promise<AnomalyPattern> {
    return apiService.post(`${this.baseUrl}/patterns/detect`, {
      detectorId,
      startTime,
      endTime
    });
  }

  async getAnalytics(detectorId: string): Promise<DetectionAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, { detectorId });
  }

  async getDetector(detectorId: string): Promise<AnomalyDetector> {
    return apiService.get(`${this.baseUrl}/detectors/${detectorId}`);
  }

  async getDetectors(): Promise<AnomalyDetector[]> {
    try {
      const response = await apiService.get<AnomalyDetector[]>(`${this.baseUrl}/detectors`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get detectors:', error);
      return [];
    }
  }

  async updateDetector(
    detectorId: string,
    name: string,
    description: string,
    configuration: Record<string, any>,
    sensitivity: number
  ): Promise<void> {
    return apiService.put(`${this.baseUrl}/detectors/${detectorId}`, {
      name,
      description,
      configuration,
      sensitivity
    });
  }

  async deleteDetector(detectorId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/detectors/${detectorId}`);
  }
}

export const anomalyDetectionService = new AnomalyDetectionService();


