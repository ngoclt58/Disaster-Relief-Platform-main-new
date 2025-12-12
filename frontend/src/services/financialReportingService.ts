import { apiService } from './api';

export interface FinancialReport {
  id: string;
  type: ReportType;
  format: ReportFormat;
  title: string;
  description: string;
  status: 'GENERATING' | 'COMPLETED' | 'FAILED';
  fileUrl?: string;
  fileSize?: number;
  generatedBy: string;
  createdAt: string;
  completedAt?: string;
  startDate: string;
  endDate: string;
  filters: Record<string, any>;
  metadata: Record<string, any>;
}

export type ReportType = 
  | 'INCOME_STATEMENT' 
  | 'BALANCE_SHEET' 
  | 'CASH_FLOW' 
  | 'BUDGET_VS_ACTUAL' 
  | 'DONATION_SUMMARY' 
  | 'EXPENSE_ANALYSIS' 
  | 'COST_BREAKDOWN' 
  | 'FINANCIAL_DASHBOARD' 
  | 'CUSTOM';

export type ReportFormat = 'PDF' | 'EXCEL' | 'CSV' | 'JSON';

export type ReportSchedule = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';

export interface GenerateReportRequest {
  type: ReportType;
  format: ReportFormat;
  startDate: string;
  endDate: string;
  filters?: Record<string, any>;
}

export interface ScheduleReportRequest {
  schedule: ReportSchedule;
  recipients: string[];
  enabled: boolean;
}

export interface ShareReportRequest {
  recipients: string[];
  expiryDate?: string;
  permissions?: Record<string, any>;
}

export interface CreateTemplateRequest {
  name: string;
  description: string;
  type: ReportType;
  configuration: Record<string, any>;
}

export interface UpdateTemplateRequest {
  name?: string;
  description?: string;
  configuration?: Record<string, any>;
}

class FinancialReportingService {
  private baseUrl = '/financial-reporting';

  async generateReport(request: GenerateReportRequest): Promise<FinancialReport> {
    return apiService.post(`${this.baseUrl}/reports`, request);
  }

  async getReport(reportId: string): Promise<FinancialReport> {
    return apiService.get(`${this.baseUrl}/reports/${reportId}`);
  }

  async getReports(filters: {
    type?: ReportType;
    startDate?: string;
    endDate?: string;
    limit?: number;
  } = {}): Promise<FinancialReport[]> {
    return apiService.get(`${this.baseUrl}/reports`, filters);
  }

  async scheduleReport(
    reportId: string,
    request: ScheduleReportRequest
  ): Promise<Record<string, any>> {
    return apiService.post(`${this.baseUrl}/reports/${reportId}/schedule`, request);
  }

  async cancelScheduledReport(reportId: string): Promise<Record<string, any>> {
    return apiService.delete(`${this.baseUrl}/reports/${reportId}/schedule`);
  }

  async downloadReport(reportId: string, format: ReportFormat): Promise<Blob> {
    // Use apiService.getHeaders() and baseUrl instead of direct fetch
    const headers = apiService.getHeaders();
    const url = `${apiService.baseUrl}${this.baseUrl}/reports/${reportId}/download?format=${format}`;
    const response = await fetch(url, { headers });
    if (!response.ok) {
      throw new Error(`Failed to download report: ${response.status}`);
    }
    return response.blob();
  }

  async shareReport(
    reportId: string,
    request: ShareReportRequest
  ): Promise<Record<string, any>> {
    return apiService.post(`${this.baseUrl}/reports/${reportId}/share`, request);
  }

  async getReportAnalytics(reportId: string): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/reports/${reportId}/analytics`);
  }

  async getReportTemplates(): Promise<Record<string, any>[]> {
    return apiService.get(`${this.baseUrl}/templates`);
  }

  async createReportTemplate(request: CreateTemplateRequest): Promise<Record<string, any>> {
    return apiService.post(`${this.baseUrl}/templates`, request);
  }

  async updateReportTemplate(
    templateId: string,
    request: UpdateTemplateRequest
  ): Promise<Record<string, any>> {
    return apiService.put(`${this.baseUrl}/templates/${templateId}`, request);
  }

  async deleteReportTemplate(templateId: string): Promise<Record<string, any>> {
    return apiService.delete(`${this.baseUrl}/templates/${templateId}`);
  }

  async getFinancialDashboard(
    startDate?: string,
    endDate?: string
  ): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/dashboard`, {
      startDate,
      endDate
    });
  }

  async getFinancialKPIs(
    startDate?: string,
    endDate?: string
  ): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/kpis`, {
      startDate,
      endDate
    });
  }
}

export const financialReportingService = new FinancialReportingService();


