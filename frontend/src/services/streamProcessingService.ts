import { apiService } from './api';

export interface StreamProcessor {
  id: string;
  name: string;
  description: string;
  dataSource: string;
  configuration: Record<string, any>;
  createdAt: string;
  startedAt?: string;
  stoppedAt?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ERROR' | 'MAINTENANCE';
  isRunning: boolean;
}

export interface StreamRule {
  id: string;
  name: string;
  description: string;
  condition: string;
  field: string;
  value: any;
  action: string;
  parameters: Record<string, any>;
  isActive: boolean;
}

export interface StreamData {
  id: string;
  processorId: string;
  data: Record<string, any>;
  timestamp: string;
  processedAt: string;
  triggers: string[];
}

export interface StreamMetrics {
  processorId: string;
  totalProcessed: number;
  processingRate: number;
  errorRate: number;
  averageLatency: number;
  lastProcessed: string;
  uptime: number;
}

class StreamProcessingService {
  private baseUrl = '/api/realtime/stream-processing';

  async createProcessor(
    name: string,
    description: string,
    dataSource: string,
    configuration: Record<string, any>
  ): Promise<StreamProcessor> {
    return apiService.post(`${this.baseUrl}/processors`, {
      name,
      description,
      dataSource,
      configuration
    });
  }

  async startProcessor(processorId: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/processors/${processorId}/start`);
  }

  async stopProcessor(processorId: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/processors/${processorId}/stop`);
  }

  async getProcessor(processorId: string): Promise<StreamProcessor> {
    return apiService.get(`${this.baseUrl}/processors/${processorId}`);
  }

  async getProcessors(): Promise<StreamProcessor[]> {
    try {
      const response = await apiService.get<StreamProcessor[]>(`${this.baseUrl}/processors`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get processors:', error);
      return [];
    }
  }

  async addStreamRule(processorId: string, rule: StreamRule): Promise<void> {
    return apiService.post(`${this.baseUrl}/processors/${processorId}/rules`, rule);
  }

  async removeStreamRule(processorId: string, ruleId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/processors/${processorId}/rules/${ruleId}`);
  }

  async processData(processorId: string, data: Record<string, any>): Promise<StreamData> {
    return apiService.post(`${this.baseUrl}/processors/${processorId}/process`, data);
  }

  async getProcessorMetrics(processorId: string): Promise<StreamMetrics> {
    return apiService.get(`${this.baseUrl}/processors/${processorId}/metrics`);
  }

  async deleteProcessor(processorId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/processors/${processorId}`);
  }
}

export const streamProcessingService = new StreamProcessingService();


