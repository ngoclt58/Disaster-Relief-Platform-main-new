import { apiService } from './api';

export interface Budget {
  id: string;
  name: string;
  description: string;
  totalAmount: number;
  spentAmount: number;
  remainingAmount: number;
  category: string;
  status: 'ACTIVE' | 'CLOSED' | 'SUSPENDED';
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  startDate: string;
  endDate: string;
}

export interface BudgetTransaction {
  id: string;
  budgetId: string;
  description: string;
  amount: number;
  type: 'EXPENSE' | 'INCOME' | 'ADJUSTMENT';
  category: string;
  createdBy: string;
  createdAt: string;
  referenceId?: string;
}

export interface BudgetSummary {
  totalBudget: number;
  totalSpent: number;
  totalRemaining: number;
  utilizationRate: number;
  transactionCount: number;
  averageTransactionAmount: number;
  topCategories: Array<{
    category: string;
    amount: number;
    percentage: number;
  }>;
}

export interface BudgetAnalytics {
  spendingTrends: Array<{
    date: string;
    amount: number;
  }>;
  categoryBreakdown: Array<{
    category: string;
    amount: number;
    percentage: number;
  }>;
  monthlyComparison: Array<{
    month: string;
    budgeted: number;
    actual: number;
    variance: number;
  }>;
  alerts: Array<{
    type: 'WARNING' | 'CRITICAL' | 'INFO';
    message: string;
    threshold: number;
    currentValue: number;
  }>;
}

export interface BudgetAlert {
  id: string;
  budgetId: string;
  type: 'WARNING' | 'CRITICAL' | 'INFO';
  message: string;
  threshold: number;
  currentValue: number;
  createdAt: string;
  acknowledged: boolean;
}

export type TransactionType = 'EXPENSE' | 'INCOME' | 'ADJUSTMENT';

export interface CreateBudgetRequest {
  name: string;
  description: string;
  totalAmount: number;
  category: string;
  startDate: string;
  endDate: string;
}

export interface RecordTransactionRequest {
  description: string;
  amount: number;
  type: TransactionType;
  category: string;
  referenceId?: string;
}

export interface UpdateBudgetRequest {
  name?: string;
  description?: string;
  totalAmount?: number;
  endDate?: string;
}

export interface CloseBudgetRequest {
  reason: string;
}

class BudgetTrackingService {
  private baseUrl = '/budget-tracking';

  async createBudget(request: CreateBudgetRequest): Promise<Budget> {
    return apiService.post(`${this.baseUrl}/create`, request);
  }

  async recordTransaction(budgetId: string, request: RecordTransactionRequest): Promise<BudgetTransaction> {
    return apiService.post(`${this.baseUrl}/${budgetId}/transactions`, request);
  }

  async getBudget(budgetId: string): Promise<Budget> {
    return apiService.get(`${this.baseUrl}/${budgetId}`);
  }

  async getBudgetTransactions(budgetId: string, limit: number = 50): Promise<BudgetTransaction[]> {
    return apiService.get(`${this.baseUrl}/${budgetId}/transactions`, { limit });
  }

  async getBudgetAlerts(budgetId: string): Promise<BudgetAlert[]> {
    return apiService.get(`${this.baseUrl}/${budgetId}/alerts`);
  }

  async getUserBudgets(): Promise<Budget[]> {
    return apiService.get(`${this.baseUrl}/my-budgets`);
  }

  async getBudgetsByCategory(category: string): Promise<Budget[]> {
    return apiService.get(`${this.baseUrl}/category/${category}`);
  }

  async updateBudget(budgetId: string, request: UpdateBudgetRequest): Promise<Budget> {
    return apiService.put(`${this.baseUrl}/${budgetId}`, request);
  }

  async closeBudget(budgetId: string, request: CloseBudgetRequest): Promise<Budget> {
    return apiService.post(`${this.baseUrl}/${budgetId}/close`, request);
  }

  async getBudgetSummary(budgetId: string): Promise<BudgetSummary> {
    return apiService.get(`${this.baseUrl}/${budgetId}/summary`);
  }

  async getBudgetAnalytics(budgetId: string): Promise<BudgetAnalytics> {
    return apiService.get(`${this.baseUrl}/${budgetId}/analytics`);
  }
}

export const budgetTrackingService = new BudgetTrackingService();


