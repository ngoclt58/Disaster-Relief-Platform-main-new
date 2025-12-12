import { apiService } from './api';

export interface Certification {
  id: string;
  name: string;
  description: string;
  category: string;
  issuingOrganization: string;
  validityMonths: number;
  requiredSkills: string[];
  requirements: Record<string, any>;
  createdAt: string;
  isActive: boolean;
}

export interface UserCertification {
  id: string;
  userId: string;
  certificationId: string;
  assignedDate: string;
  completionDate?: string;
  expiryDate: string;
  renewalDate?: string;
  revocationDate?: string;
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'EXPIRED' | 'RENEWED' | 'REVOKED';
  completionMethod?: string;
  completionNotes?: string;
  verifiedBy?: string;
  renewalNotes?: string;
  revocationReason?: string;
  revokedBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CertificationRequirement {
  id: string;
  certificationId: string;
  requirementType: string;
  description: string;
  isMandatory: boolean;
  criteria: Record<string, any>;
  createdAt: string;
}

export interface CertificationProgress {
  id: string;
  userCertificationId: string;
  requirementId: string;
  status: string;
  notes?: string;
  updatedAt: string;
}

export interface CertificationReport {
  id: string;
  userId: string;
  certificationId: string;
  startDate: string;
  endDate: string;
  generatedAt: string;
  totalCertifications: number;
  completedCertifications: number;
  expiredCertifications: number;
  expiringCertifications: number;
  complianceRate: number;
}

export interface CertificationAlert {
  id: string;
  userId: string;
  certificationId: string;
  alertType: string;
  message: string;
  createdAt: string;
  isRead: boolean;
}

export interface CertificationAnalytics {
  organizationId: string;
  totalCertifications: number;
  activeCertifications: number;
  expiredCertifications: number;
  expiringCertifications: number;
  complianceRate: number;
  averageCompletionTime: number;
  popularCertifications: string[];
  certificationTrends: Array<Record<string, any>>;
}

export interface CreateCertificationRequest {
  name: string;
  description: string;
  category: string;
  issuingOrganization: string;
  validityMonths: number;
  requiredSkills: string[];
  requirements: Record<string, any>;
}

export interface AssignCertificationRequest {
  userId: string;
  certificationId: string;
  assignedDate: string;
  expiryDate: string;
}

export interface CompleteCertificationRequest {
  completionMethod: string;
  completionNotes?: string;
}

export interface RenewCertificationRequest {
  newExpiryDate: string;
  renewalNotes?: string;
}

export interface RevokeCertificationRequest {
  reason: string;
}

export interface CreateRequirementRequest {
  certificationId: string;
  requirementType: string;
  description: string;
  isMandatory: boolean;
  criteria: Record<string, any>;
}

export interface TrackProgressRequest {
  userCertificationId: string;
  requirementId: string;
  status: string;
  notes?: string;
}

export interface GenerateReportRequest {
  userId: string;
  certificationId?: string;
  startDate: string;
  endDate: string;
}

class CertificationTrackingService {
  private baseUrl = '/certification-tracking';

  async createCertification(request: CreateCertificationRequest): Promise<Certification> {
    return apiService.post(`${this.baseUrl}/certifications`, request);
  }

  async assignCertification(request: AssignCertificationRequest): Promise<UserCertification> {
    return apiService.post(`${this.baseUrl}/user-certifications`, request);
  }

  async completeCertification(userCertificationId: string, request: CompleteCertificationRequest): Promise<UserCertification> {
    return apiService.post(`${this.baseUrl}/user-certifications/${userCertificationId}/complete`, request);
  }

  async renewCertification(userCertificationId: string, request: RenewCertificationRequest): Promise<UserCertification> {
    return apiService.post(`${this.baseUrl}/user-certifications/${userCertificationId}/renew`, request);
  }

  async revokeCertification(userCertificationId: string, request: RevokeCertificationRequest): Promise<UserCertification> {
    return apiService.post(`${this.baseUrl}/user-certifications/${userCertificationId}/revoke`, request);
  }

  async getUserCertifications(userId: string, status?: string): Promise<UserCertification[]> {
    return apiService.get(`${this.baseUrl}/user-certifications`, { userId, status });
  }

  async getExpiringCertifications(daysBeforeExpiry: number = 30): Promise<UserCertification[]> {
    return apiService.get(`${this.baseUrl}/certifications/expiring`, { daysBeforeExpiry });
  }

  async getExpiredCertifications(): Promise<UserCertification[]> {
    return apiService.get(`${this.baseUrl}/certifications/expired`);
  }

  async getCertifications(filters: {
    category?: string;
    isActive?: boolean;
  } = {}): Promise<Certification[]> {
    return apiService.get(`${this.baseUrl}/certifications`, filters);
  }

  async getCertification(certificationId: string): Promise<Certification> {
    return apiService.get(`${this.baseUrl}/certifications/${certificationId}`);
  }

  async getUserCertification(userCertificationId: string): Promise<UserCertification> {
    return apiService.get(`${this.baseUrl}/user-certifications/${userCertificationId}`);
  }

  async createRequirement(request: CreateRequirementRequest): Promise<CertificationRequirement> {
    return apiService.post(`${this.baseUrl}/requirements`, request);
  }

  async getCertificationRequirements(certificationId: string): Promise<CertificationRequirement[]> {
    return apiService.get(`${this.baseUrl}/certifications/${certificationId}/requirements`);
  }

  async trackProgress(request: TrackProgressRequest): Promise<CertificationProgress> {
    return apiService.post(`${this.baseUrl}/progress`, request);
  }

  async getCertificationProgress(userCertificationId: string): Promise<CertificationProgress[]> {
    return apiService.get(`${this.baseUrl}/user-certifications/${userCertificationId}/progress`);
  }

  async generateReport(request: GenerateReportRequest): Promise<CertificationReport> {
    return apiService.post(`${this.baseUrl}/reports`, request);
  }

  async getCertificationAlerts(userId: string): Promise<CertificationAlert[]> {
    return apiService.get(`${this.baseUrl}/alerts`, { userId });
  }

  async getCertificationAnalytics(organizationId: string): Promise<CertificationAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, { organizationId });
  }

  async sendExpiryNotifications(): Promise<void> {
    return apiService.post(`${this.baseUrl}/notifications/expiry`);
  }

  async sendRenewalReminders(): Promise<void> {
    return apiService.post(`${this.baseUrl}/notifications/renewal`);
  }
}

export const certificationTrackingService = new CertificationTrackingService();


