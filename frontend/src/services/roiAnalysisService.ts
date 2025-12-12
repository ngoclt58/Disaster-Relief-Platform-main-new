import { apiService } from './api';

export interface ROIAnalysis {
  id: string;
  name: string;
  description: string;
  analysisType: string;
  projectId: string;
  parameters: Record<string, any>;
  userId: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  status: string;
  isActive: boolean;
}

export interface ROIMetrics {
  projectId: string;
  startDate: string;
  endDate: string;
  totalInvestment: number;
  totalReturns: number;
  netProfit: number;
  roiPercentage: number;
  paybackPeriod: number;
  npv: number;
  irr: number;
  generatedAt: string;
}

export interface CostBenefitAnalysis {
  id: string;
  projectId: string;
  costs: CostItem[];
  benefits: BenefitItem[];
  totalCosts: number;
  totalBenefits: number;
  netBenefit: number;
  benefitCostRatio: number;
  generatedAt: string;
}

export interface CostItem {
  id: string;
  name: string;
  category: string;
  amount: number;
  currency: string;
  description: string;
}

export interface BenefitItem {
  id: string;
  name: string;
  category: string;
  value: number;
  currency: string;
  description: string;
}

export interface EffectivenessMetrics {
  projectId: string;
  metricType: string;
  parameters: Record<string, any>;
  effectivenessScore: number;
  targetAchievement: number;
  efficiencyRatio: number;
  qualityScore: number;
  timelinessScore: number;
  generatedAt: string;
}

export interface ImpactAssessment {
  id: string;
  projectId: string;
  impactType: string;
  criteria: Record<string, any>;
  impactScore: number;
  socialImpact: number;
  economicImpact: number;
  environmentalImpact: number;
  longTermImpact: number;
  generatedAt: string;
}

export interface PerformanceBenchmark {
  id: string;
  projectId: string;
  benchmarkType: string;
  parameters: Record<string, any>;
  currentPerformance: number;
  industryAverage: number;
  bestPractice: number;
  performanceGap: number;
  improvementPotential: number;
  generatedAt: string;
}

export interface ValueForMoneyAnalysis {
  id: string;
  projectId: string;
  parameters: Record<string, any>;
  valueScore: number;
  costEffectiveness: number;
  efficiency: number;
  economy: number;
  valueForMoneyRating: string;
  generatedAt: string;
}

export interface ROIComparison {
  id: string;
  projectIds: string[];
  comparisonType: string;
  parameters: Record<string, any>;
  comparisons: ProjectROI[];
  bestPerformer: string;
  worstPerformer: string;
  averageROI: number;
  generatedAt: string;
}

export interface ProjectROI {
  projectId: string;
  projectName: string;
  roiPercentage: number;
  totalInvestment: number;
  netProfit: number;
  ranking: number;
}

export interface ROITrend {
  id: string;
  projectId: string;
  startDate: string;
  endDate: string;
  trendData: TrendDataPoint[];
  trendDirection: string;
  trendStrength: number;
  forecast: Record<string, any>;
  generatedAt: string;
}

export interface TrendDataPoint {
  date: string;
  roiPercentage: number;
  investment: number;
  returns: number;
}

export interface ROIAnalytics {
  projectId: string;
  totalAnalyses: number;
  averageROI: number;
  bestROI: number;
  worstROI: number;
  trendDirection: string;
  lastAnalyzed: string;
}

class ROIAnalysisService {
  // Controllers are mapped under /analytics/roi; apiService adds /api prefix
  private baseUrl = '/analytics/roi';

  async createAnalysis(name: string, description: string, analysisType: string, projectId: string, parameters: Record<string, any>, userId: string): Promise<ROIAnalysis> {
    return apiService.post<ROIAnalysis>(`${this.baseUrl}/analyses`, { name, description, analysisType, projectId, parameters, userId });
  }

  async executeAnalysis(analysisId: string): Promise<ROIAnalysis> {
    return apiService.post<ROIAnalysis>(`${this.baseUrl}/analyses/${analysisId}/execute`, {});
  }

  async getAnalysis(analysisId: string): Promise<ROIAnalysis> {
    return apiService.get<ROIAnalysis>(`${this.baseUrl}/analyses/${analysisId}`);
  }

  async getUserAnalyses(userId: string): Promise<ROIAnalysis[]> {
    try {
      const response = await apiService.get<ROIAnalysis[]>(`${this.baseUrl}/analyses`, { userId });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get user analyses:', error);
      return [];
    }
  }

  async calculateROI(projectId: string, startDate: string, endDate: string, parameters: Record<string, any>): Promise<ROIMetrics> {
    return apiService.post<ROIMetrics>(`${this.baseUrl}/calculate`, { projectId, startDate, endDate, parameters });
  }

  async performCostBenefitAnalysis(projectId: string, costs: CostItem[], benefits: BenefitItem[]): Promise<CostBenefitAnalysis> {
    return apiService.post<CostBenefitAnalysis>(`${this.baseUrl}/cost-benefit`, { projectId, costs, benefits });
  }

  async measureEffectiveness(projectId: string, metricType: string, parameters: Record<string, any>): Promise<EffectivenessMetrics> {
    return apiService.post<EffectivenessMetrics>(`${this.baseUrl}/effectiveness`, { projectId, metricType, parameters });
  }

  async assessImpact(projectId: string, impactType: string, criteria: Record<string, any>): Promise<ImpactAssessment> {
    return apiService.post<ImpactAssessment>(`${this.baseUrl}/impact`, { projectId, impactType, criteria });
  }

  async benchmarkPerformance(projectId: string, benchmarkType: string, parameters: Record<string, any>): Promise<PerformanceBenchmark> {
    return apiService.post<PerformanceBenchmark>(`${this.baseUrl}/benchmark`, { projectId, benchmarkType, parameters });
  }

  async analyzeValueForMoney(projectId: string, parameters: Record<string, any>): Promise<ValueForMoneyAnalysis> {
    return apiService.post<ValueForMoneyAnalysis>(`${this.baseUrl}/value-for-money`, { projectId, parameters });
  }

  async compareROI(projectIds: string[], comparisonType: string, parameters: Record<string, any>): Promise<ROIComparison> {
    return apiService.post<ROIComparison>(`${this.baseUrl}/compare`, { projectIds, comparisonType, parameters });
  }

  async analyzeTrends(projectId: string, startDate: string, endDate: string): Promise<ROITrend> {
    return apiService.post<ROITrend>(`${this.baseUrl}/trends`, { projectId, startDate, endDate });
  }

  async getROIAnalytics(projectId: string): Promise<ROIAnalytics> {
    return apiService.get<ROIAnalytics>(`${this.baseUrl}/analytics`, { projectId });
  }

  async deleteAnalysis(analysisId: string): Promise<void> {
    return apiService.delete<void>(`${this.baseUrl}/analyses/${analysisId}`);
  }
}

export const roiAnalysisService = new ROIAnalysisService();
