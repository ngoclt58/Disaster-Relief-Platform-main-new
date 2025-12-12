import { apiService } from './api';

export interface Donation {
  id: string;
  donorId: string;
  amount: number;
  type: DonationType;
  description: string;
  status: DonationStatus;
  campaignId?: string;
  referenceId?: string;
  recordedBy: string;
  createdAt: string;
  updatedAt: string;
  processedAt?: string;
}

export interface Donor {
  id: string;
  name: string;
  email: string;
  phone?: string;
  address?: string;
  organization?: string;
  type: 'INDIVIDUAL' | 'CORPORATE' | 'FOUNDATION' | 'GOVERNMENT';
  totalDonated: number;
  donationCount: number;
  lastDonationDate?: string;
  registeredBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface DonationSummary {
  totalDonations: number;
  totalAmount: number;
  averageDonation: number;
  donationCount: number;
  topDonors: Array<{
    donorId: string;
    donorName: string;
    amount: number;
    percentage: number;
  }>;
  donationsByType: Array<{
    type: DonationType;
    amount: number;
    percentage: number;
  }>;
  donationsByStatus: Array<{
    status: DonationStatus;
    amount: number;
    percentage: number;
  }>;
}

export interface DonationAnalytics {
  donationTrends: Array<{
    date: string;
    amount: number;
    count: number;
  }>;
  donorRetention: {
    newDonors: number;
    returningDonors: number;
    retentionRate: number;
  };
  campaignPerformance: Array<{
    campaignId: string;
    campaignName: string;
    totalRaised: number;
    donorCount: number;
    averageDonation: number;
  }>;
  geographicDistribution: Array<{
    region: string;
    amount: number;
    percentage: number;
  }>;
}

export type DonationType = 'CASH' | 'IN_KIND' | 'SERVICES' | 'EQUIPMENT' | 'FOOD' | 'MEDICAL' | 'OTHER';
export type DonationStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSED' | 'REFUNDED' | 'CANCELLED';

export interface RecordDonationRequest {
  donorName: string;
  donorEmail?: string;
  donorPhone?: string;
  amount: number;
  currency?: string;
  paymentMethod?: string;
  campaignId?: string;
  notes?: string;
}

export interface RegisterDonorRequest {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  organization?: string;
  type: 'INDIVIDUAL' | 'CORPORATE' | 'FOUNDATION' | 'GOVERNMENT';
}

export interface UpdateDonationStatusRequest {
  status: DonationStatus;
  notes?: string;
}

export interface UpdateDonorRequest {
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
  organization?: string;
}

export interface ProcessRefundRequest {
  amount: number;
  reason: string;
}

class DonationManagementService {
  private baseUrl = '/donation-management';

  async recordDonation(request: RecordDonationRequest): Promise<Donation> {
    return apiService.post(`${this.baseUrl}/donations`, request);
  }

  async registerDonor(request: RegisterDonorRequest): Promise<Donor> {
    return apiService.post(`${this.baseUrl}/donors`, request);
  }

  async getDonation(donationId: string): Promise<Donation> {
    return apiService.get(`${this.baseUrl}/donations/${donationId}`);
  }

  async getDonor(donorId: string): Promise<Donor> {
    return apiService.get(`${this.baseUrl}/donors/${donorId}`);
  }

  async getDonations(filters: {
    donorId?: string;
    campaignId?: string;
    type?: DonationType;
    status?: DonationStatus;
    limit?: number;
  } = {}): Promise<Donation[]> {
    return apiService.get(`${this.baseUrl}/donations`, filters);
  }

  async getDonors(filters: {
    organization?: string;
    type?: string;
    limit?: number;
  } = {}): Promise<Donor[]> {
    return apiService.get(`${this.baseUrl}/donors`, filters);
  }

  async updateDonationStatus(donationId: string, request: UpdateDonationStatusRequest): Promise<Donation> {
    return apiService.put(`${this.baseUrl}/donations/${donationId}/status`, request);
  }

  async updateDonor(donorId: string, request: UpdateDonorRequest): Promise<Donor> {
    return apiService.put(`${this.baseUrl}/donors/${donorId}`, request);
  }

  async getCampaignDonations(campaignId: string, limit: number = 50): Promise<Donation[]> {
    return apiService.get(`${this.baseUrl}/campaigns/${campaignId}/donations`, { limit });
  }

  async getDonorDonations(donorId: string, limit: number = 50): Promise<Donation[]> {
    return apiService.get(`${this.baseUrl}/donors/${donorId}/donations`, { limit });
  }

  async getDonationSummary(filters: {
    campaignId?: string;
    startDate?: string;
    endDate?: string;
  } = {}): Promise<DonationSummary> {
    return apiService.get(`${this.baseUrl}/summary`, filters);
  }

  async getDonationAnalytics(filters: {
    campaignId?: string;
    startDate?: string;
    endDate?: string;
  } = {}): Promise<DonationAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, filters);
  }

  async getDonorSummary(donorId: string): Promise<DonationSummary> {
    return apiService.get(`${this.baseUrl}/donors/${donorId}/summary`);
  }

  async processRefund(donationId: string, request: ProcessRefundRequest): Promise<Donation> {
    return apiService.post(`${this.baseUrl}/donations/${donationId}/refund`, request);
  }
}

export const donationManagementService = new DonationManagementService();


