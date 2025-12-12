import { apiService } from './api';

export interface Dashboard {
  id: string;
  name: string;
  description: string;
  userId: string;
  userRole: string;
  isPublic: boolean;
  layout: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

export interface DashboardWidget {
  id: string;
  dashboardId: string;
  widgetType: string;
  title: string;
  configuration: Record<string, any>;
  position: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  isVisible: boolean;
}

export interface WidgetData {
  widgetId: string;
  data: any[];
  metadata: Record<string, any>;
  generatedAt: string;
}

export interface DashboardTemplate {
  id: string;
  name: string;
  description: string;
  userRole: string;
  template: Record<string, any>;
  createdBy: string;
  createdAt: string;
  isPublic: boolean;
}

export interface DashboardAnalytics {
  dashboardId: string;
  viewCount: number;
  uniqueViewers: number;
  lastViewed: string;
  popularWidgets: string[];
  userEngagement: Record<string, any>;
}

class CustomDashboardService {
  // Backend controllers are mapped under /analytics/...; apiService already prefixes /api
  private baseUrl = '/analytics/dashboards';

  async createDashboard(name: string, description: string, userId: string, userRole: string, isPublic: boolean = false, layout: Record<string, any> = {}): Promise<Dashboard> {
    return apiService.post<Dashboard>(`${this.baseUrl}`, { name, description, userId, userRole, isPublic, layout });
  }

  async updateDashboard(dashboardId: string, name: string, description: string, layout: Record<string, any>, isPublic: boolean = false): Promise<Dashboard> {
    return apiService.put<Dashboard>(`${this.baseUrl}/${dashboardId}`, { name, description, layout, isPublic });
  }

  async getDashboard(dashboardId: string): Promise<Dashboard> {
    return apiService.get<Dashboard>(`${this.baseUrl}/${dashboardId}`);
  }

  async getUserDashboards(userId: string, userRole: string): Promise<Dashboard[]> {
    try {
      const response = await apiService.get<Dashboard[]>(`${this.baseUrl}`, { userId, userRole });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get user dashboards:', error);
      return [];
    }
  }

  async getPublicDashboards(): Promise<Dashboard[]> {
    try {
      const response = await apiService.get<Dashboard[]>(`${this.baseUrl}/public`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get public dashboards:', error);
      return [];
    }
  }

  async addWidget(dashboardId: string, widgetType: string, title: string, configuration: Record<string, any>, position: Record<string, any>): Promise<DashboardWidget> {
    return apiService.post<DashboardWidget>(`${this.baseUrl}/${dashboardId}/widgets`, { widgetType, title, configuration, position });
  }

  async updateWidget(widgetId: string, title: string, configuration: Record<string, any>, position: Record<string, any>, isVisible: boolean = true): Promise<DashboardWidget> {
    return apiService.put<DashboardWidget>(`${this.baseUrl}/widgets/${widgetId}`, { title, configuration, position, isVisible });
  }

  async getDashboardWidgets(dashboardId: string): Promise<DashboardWidget[]> {
    try {
      const response = await apiService.get<DashboardWidget[]>(`${this.baseUrl}/${dashboardId}/widgets`);
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get dashboard widgets:', error);
      return [];
    }
  }

  async getWidget(widgetId: string): Promise<DashboardWidget> {
    return apiService.get<DashboardWidget>(`${this.baseUrl}/widgets/${widgetId}`);
  }

  async getWidgetData(widgetId: string, filters: Record<string, any> = {}): Promise<WidgetData> {
    return apiService.get<WidgetData>(`${this.baseUrl}/widgets/${widgetId}/data`, filters);
  }

  async removeWidget(widgetId: string): Promise<void> {
    return apiService.delete<void>(`${this.baseUrl}/widgets/${widgetId}`);
  }

  async createTemplate(name: string, description: string, userRole: string, template: Record<string, any>, createdBy: string): Promise<DashboardTemplate> {
    return apiService.post<DashboardTemplate>(`${this.baseUrl}/templates`, { name, description, userRole, template, createdBy });
  }

  async getTemplates(userRole: string): Promise<DashboardTemplate[]> {
    return apiService.get<DashboardTemplate[]>(`${this.baseUrl}/templates`, { userRole });
  }

  async cloneDashboard(dashboardId: string, newName: string, userId: string): Promise<Dashboard> {
    return apiService.post<Dashboard>(`${this.baseUrl}/${dashboardId}/clone`, { newName, userId });
  }

  async shareDashboard(dashboardId: string, userId: string, permission: string): Promise<void> {
    return apiService.post<void>(`${this.baseUrl}/${dashboardId}/share`, { userId, permission });
  }

  async getDashboardAnalytics(dashboardId: string): Promise<DashboardAnalytics> {
    return apiService.get<DashboardAnalytics>(`${this.baseUrl}/${dashboardId}/analytics`);
  }

  async deleteDashboard(dashboardId: string): Promise<void> {
    return apiService.delete<void>(`${this.baseUrl}/${dashboardId}`);
  }
}

export const customDashboardService = new CustomDashboardService();
