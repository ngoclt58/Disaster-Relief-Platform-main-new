import { apiService } from './api';

export interface WarningRule {
  id: string;
  name: string;
  description: string;
  triggerCondition: string;
  thresholds: Record<string, any>;
  action: string;
  createdAt: string;
  updatedAt?: string;
  isActive: boolean;
  severity: string;
}

export interface EarlyWarning {
  id: string;
  ruleId: string;
  title: string;
  message: string;
  severity: string;
  triggeredAt: string;
  action: string;
  isAcknowledged: boolean;
  acknowledgedAt?: string;
  acknowledgedBy?: string;
}

class EarlyWarningService {
  private baseUrl = '/api/ai/early-warnings';

  async createRule(
    name: string,
    description: string,
    triggerCondition: string,
    thresholds: Record<string, any>,
    action: string
  ): Promise<WarningRule> {
    return apiService.post(`${this.baseUrl}/rules`, {
      name,
      description,
      triggerCondition,
      thresholds,
      action
    });
  }

  async getActiveWarnings(): Promise<EarlyWarning[]> {
    try {
      const response = await apiService.get<EarlyWarning[]>(`${this.baseUrl}/active`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get active warnings:', error);
      return [];
    }
  }

  async getWarnings(ruleId: string, startTime: string, endTime: string): Promise<EarlyWarning[]> {
    return apiService.get(`${this.baseUrl}/warnings`, {
      ruleId,
      startTime,
      endTime
    });
  }

  async acknowledgeWarning(warningId: string, acknowledgedBy: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/warnings/${warningId}/acknowledge`, {
      acknowledgedBy
    });
  }

  async getRule(ruleId: string): Promise<WarningRule> {
    return apiService.get(`${this.baseUrl}/rules/${ruleId}`);
  }

  async getRules(): Promise<WarningRule[]> {
    return apiService.get(`${this.baseUrl}/rules`);
  }

  async updateRule(
    ruleId: string,
    name: string,
    description: string,
    triggerCondition: string,
    thresholds: Record<string, any>,
    action: string,
    severity: string
  ): Promise<void> {
    return apiService.put(`${this.baseUrl}/rules/${ruleId}`, {
      name,
      description,
      triggerCondition,
      thresholds,
      action,
      severity
    });
  }

  async deleteRule(ruleId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/rules/${ruleId}`);
  }
}

export const earlyWarningService = new EarlyWarningService();
export default earlyWarningService;



