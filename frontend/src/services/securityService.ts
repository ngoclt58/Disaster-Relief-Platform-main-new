import { apiService } from './api';

export interface VerificationResult {
  id: string;
  userId: string;
  deviceId: string;
  ipAddress: string;
  resource: string;
  action: string;
  allowed: boolean;
  reason: string;
  timestamp: string;
}

export interface SecurityEvent {
  id: string;
  userId?: string;
  ip: string;
  action: string;
  context?: Record<string, any>;
  timestamp: string;
  anomalyScore: number;
}

export interface ThreatAlert {
  id: string;
  eventId: string;
  title: string;
  message: string;
  severity: string;
  acknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  createdAt: string;
}

export interface DLPFinding {
  type: string;
  excerpt: string;
  start: number;
  end: number;
}

export interface DLPScanResult {
  id: string;
  riskLevel: string;
  findings: DLPFinding[];
  scannedAt: string;
}

export interface Metric {
  id: string;
  name: string;
  value: number;
  labels: Record<string, string>;
  timestamp: string;
}

class SecurityService {
  async zeroTrustVerify(params: { userId: string; deviceId: string; ipAddress: string; resource: string; action: string; }): Promise<VerificationResult> {
    const query = new URLSearchParams(params as any).toString();
    return apiService.post(`/security/zero-trust/verify?${query}`, {});
  }

  async recordSecurityEvent(ip: string, action: string, context?: Record<string, any>, userId?: string): Promise<SecurityEvent> {
    const query = new URLSearchParams({ ip, action, userId: userId || '' }).toString();
    return apiService.post(`/security/threats/events?${query}`, context || {});
  }

  async getThreatAlerts(): Promise<ThreatAlert[]> {
    return apiService.get(`/security/threats/alerts`);
  }

  async acknowledgeAlert(alertId: string, userId: string): Promise<void> {
    return apiService.post(`/security/threats/alerts/${alertId}/ack?userId=${encodeURIComponent(userId)}`, {});
  }

  async dlpScan(content: string): Promise<DLPScanResult> {
    return apiService.post(`/security/dlp/scan`, content);
  }

  async trackMetric(name: string, value: number, labels?: Record<string, string>): Promise<void> {
    const query = new URLSearchParams({ name, value: String(value) }).toString();
    return apiService.post(`/security/analytics/track?${query}`, labels || {});
  }

  async getRecentMetrics(limit: number = 50): Promise<Metric[]> {
    return apiService.get(`/security/analytics/recent?limit=${limit}`);
  }

  async aggregateMetrics(name: string): Promise<Record<string, number>> {
    return apiService.get(`/security/analytics/aggregate?name=${encodeURIComponent(name)}`);
  }
}

export const securityService = new SecurityService();
export default securityService;




