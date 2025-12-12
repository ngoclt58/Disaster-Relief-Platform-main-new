import { apiService } from './api';

export interface CostAnalysis {
  id: string;
  category: string;
  totalCost: number;
  averageCost: number;
  costPerUnit: number;
  unitCount: number;
  startDate: string;
  endDate: string;
  analysisDate: string;
  createdBy: string;
  insights: string[];
  recommendations: string[];
  costBreakdown: CostBreakdown;
  trends: CostTrend[];
  drivers: CostDriver[];
}

export interface CostBreakdown {
  category: string;
  totalCost: number;
  subcategories: Array<{
    name: string;
    cost: number;
    percentage: number;
  }>;
  timePeriod: {
    startDate: string;
    endDate: string;
  };
  costPerUnit: number;
  unitCount: number;
}

export interface CostTrend {
  date: string;
  cost: number;
  unitCount: number;
  costPerUnit: number;
  changeFromPrevious: number;
  changePercentage: number;
}

export interface CostDriver {
  name: string;
  impact: number;
  percentage: number;
  description: string;
  recommendations: string[];
}

export interface CostCategory {
  id: string;
  name: string;
  description: string;
  parentCategory?: string;
  isActive: boolean;
  createdAt: string;
}

export interface CostOptimization {
  category: string;
  currentCost: number;
  potentialSavings: number;
  savingsPercentage: number;
  recommendations: Array<{
    title: string;
    description: string;
    potentialSavings: number;
    implementationCost: number;
    paybackPeriod: number;
    priority: 'HIGH' | 'MEDIUM' | 'LOW';
  }>;
  constraints: string[];
  timeline: string;
}

export interface CostAnalysisRequest {
  category: string;
  startDate: string;
  endDate: string;
  filters?: Record<string, any>;
}

export interface CostOptimizationRequest {
  category: string;
  budget: number;
  constraints?: Record<string, any>;
}

export interface BenchmarkRequest {
  category: string;
  region: string;
  organizationSize: string;
}

class CostAnalysisService {
  private baseUrl = '/cost-analysis';

  async performCostAnalysis(request: CostAnalysisRequest): Promise<CostAnalysis> {
    return apiService.post(`${this.baseUrl}/analyze`, request);
  }

  async getCostBreakdown(
    category: string,
    startDate?: string,
    endDate?: string
  ): Promise<CostBreakdown> {
    return apiService.get(`${this.baseUrl}/breakdown/${category}`, {
      startDate,
      endDate
    });
  }

  async getCostTrends(
    category: string,
    startDate?: string,
    endDate?: string,
    days: number = 30
  ): Promise<CostTrend[]> {
    return apiService.get(`${this.baseUrl}/trends/${category}`, {
      startDate,
      endDate,
      days
    });
  }

  async getCostDrivers(
    category?: string,
    startDate?: string,
    endDate?: string
  ): Promise<CostDriver[]> {
    return apiService.get(`${this.baseUrl}/drivers`, {
      category,
      startDate,
      endDate
    });
  }

  async getCostCategories(): Promise<CostCategory[]> {
    return apiService.get(`${this.baseUrl}/categories`);
  }

  async getCostOptimization(request: CostOptimizationRequest): Promise<CostOptimization> {
    return apiService.post(`${this.baseUrl}/optimize`, request);
  }

  async compareCosts(
    category: string,
    period1Start: string,
    period1End: string,
    period2Start: string,
    period2End: string
  ): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/comparison`, {
      category,
      period1Start,
      period1End,
      period2Start,
      period2End
    });
  }

  async getCostForecast(category: string, months: number): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/forecast`, { category, months });
  }

  async getCostVariance(
    category: string,
    startDate: string,
    endDate: string
  ): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/variance`, {
      category,
      startDate,
      endDate
    });
  }

  async benchmarkCosts(request: BenchmarkRequest): Promise<Record<string, any>> {
    return apiService.post(`${this.baseUrl}/benchmark`, request);
  }

  async getCostEfficiency(
    category: string,
    startDate?: string,
    endDate?: string
  ): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/efficiency`, {
      category,
      startDate,
      endDate
    });
  }
}

export const costAnalysisService = new CostAnalysisService();


