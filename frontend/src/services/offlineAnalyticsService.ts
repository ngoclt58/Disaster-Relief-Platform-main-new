import { offlineManager } from './offlineManager';

interface AnalyticsEvent {
  id: string;
  type: string;
  category: string;
  data: any;
  timestamp: number;
  sessionId: string;
}

interface AnalyticsMetrics {
  totalEvents: number;
  categories: Record<string, number>;
  timeRange: {
    start: number;
    end: number;
  };
}

interface AnalyticsReport {
  id: string;
  title: string;
  type: string;
  data: any;
  generatedAt: number;
  sentToServer: boolean;
}

class OfflineAnalyticsService {
  private events: Map<string, AnalyticsEvent> = new Map();
  private reports: Map<string, AnalyticsReport> = new Map();
  private sessionId = this.generateSessionId();
  private maxEventsInMemory = 1000;

  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackEvent(type: string, category: string, data: any = {}) {
    const event: AnalyticsEvent = {
      id: `event_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      type,
      category,
      data,
      timestamp: Date.now(),
      sessionId: this.sessionId
    };

    this.events.set(event.id, event);

    // Store in IndexedDB for persistence
    try {
      await offlineManager.storeLocalData(event.id, 'analytics_event', event);
    } catch (error) {
      console.error('Failed to store analytics event:', error);
    }

    // Clean up old events from memory if needed
    if (this.events.size > this.maxEventsInMemory) {
      const oldestEvent = Array.from(this.events.values())
        .sort((a, b) => a.timestamp - b.timestamp)[0];
      this.events.delete(oldestEvent.id);
    }
  }

  async getUserBehavior(data: any) {
    return this.trackEvent('user_behavior', 'user', data);
  }

  async getPerformanceMetric(data: any) {
    return this.trackEvent('performance', 'system', data);
  }

  async getError(error: Error | string, context?: any) {
    const errorData = {
      message: error instanceof Error ? error.message : error,
      stack: error instanceof Error ? error.stack : undefined,
      context
    };
    return this.trackEvent('error', 'system', errorData);
  }

  async getFeatureUsage(feature: string, action: string, data?: any) {
    return this.trackEvent('feature_usage', 'user', {
      feature,
      action,
      ...data
    });
  }

  async getSyncEvent(status: string, data?: any) {
    return this.trackEvent('sync', 'system', {
      status,
      ...data
    });
  }

  getMetrics(timeRange?: { start: number; end: number }): AnalyticsMetrics {
    const range = timeRange || {
      start: Date.now() - 24 * 60 * 60 * 1000, // Last 24 hours
      end: Date.now()
    };

    const eventsInRange = Array.from(this.events.values())
      .filter(event => event.timestamp >= range.start && event.timestamp <= range.end);

    const categories: Record<string, number> = {};
    eventsInRange.forEach(event => {
      categories[event.category] = (categories[event.category] || 0) + 1;
    });

    return {
      totalEvents: eventsInRange.length,
      categories,
      timeRange: range
    };
  }

  async generateReport(
    title: string,
    type: string,
    data: any,
    sendToServer: boolean = true
  ): Promise<AnalyticsReport> {
    const report: AnalyticsReport = {
      id: `report_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      title,
      type,
      data,
      generatedAt: Date.now(),
      sentToServer: false
    };

    this.reports.set(report.id, report);

    // Store in IndexedDB
    try {
      await offlineManager.storeLocalData(report.id, 'analytics_report', report);
    } catch (error) {
      console.error('Failed to store analytics report:', error);
    }

    // Send to server if online
    if (sendToServer && offlineManager.isOnlineStatus) {
      try {
        await this.sendReportToServer(report);
        report.sentToServer = true;
      } catch (error) {
        console.error('Failed to send report to server:', error);
        // Queue for later sync
        await offlineManager.queueAction(
          'POST',
          '/api/analytics/reports',
          'POST',
          report
        );
      }
    }

    return report;
  }

  async generateDailySummary(): Promise<AnalyticsReport> {
    const metrics = this.getMetrics();
    const eventsByType = this.getEventsByType();
    const topCategories = this.getTopCategories(5);

    const summary = {
      date: new Date().toISOString().split('T')[0],
      metrics,
      eventsByType,
      topCategories,
      sessionId: this.sessionId
    };

    return this.generateReport(
      'Daily Analytics Summary',
      'daily_summary',
      summary,
      true
    );
  }

  async generateUsageReport(feature?: string): Promise<AnalyticsReport> {
    const events = Array.from(this.events.values());
    const filteredEvents = feature
      ? events.filter(e => e.data.feature === feature)
      : events;

    const usage = this.analyzeUsage(filteredEvents);

    return this.generateReport(
      `Usage Report${feature ? ` - ${feature}` : ''}`,
      'usage',
      usage,
      true
    );
  }

  async generatePerformanceReport(): Promise<AnalyticsReport> {
    const events = Array.from(this.events.values())
      .filter(e => e.category === 'system' && e.type === 'performance');

    const metrics = this.analyzePerformance(events);

    return this.generateReport(
      'Performance Report',
      'performance',
      metrics,
      true
    );
  }

  private async sendReportToServer(report: AnalyticsReport): Promise<void> {
    // Mock API call - replace with actual implementation
    return new Promise((resolve, reject) => {
      // Simulate network delay
      setTimeout(() => {
        resolve();
      }, 500);
    });
  }

  private getEventsByType(): Record<string, number> {
    const types: Record<string, number> = {};
    Array.from(this.events.values()).forEach(event => {
      types[event.type] = (types[event.type] || 0) + 1;
    });
    return types;
  }

  private getTopCategories(limit: number): Array<{ category: string; count: number }> {
    const categories: Record<string, number> = {};
    Array.from(this.events.values()).forEach(event => {
      categories[event.category] = (categories[event.category] || 0) + 1;
    });

    return Object.entries(categories)
      .sort((a, b) => b[1] - a[1])
      .slice(0, limit)
      .map(([category, count]) => ({ category, count }));
  }

  private analyzeUsage(events: AnalyticsEvent[]) {
    const actions: Record<string, number> = {};
    const times: number[] = [];

    events.forEach(event => {
      if (event.data.action) {
        actions[event.data.action] = (actions[event.data.action] || 0) + 1;
      }
      times.push(event.timestamp);
    });

    const sessionDuration = times.length > 0
      ? Math.max(...times) - Math.min(...times)
      : 0;

    return {
      totalInteractions: events.length,
      actions,
      sessionDuration,
      averageTimeBetweenEvents: times.length > 1
        ? sessionDuration / (times.length - 1)
        : 0
    };
  }

  private analyzePerformance(events: AnalyticsEvent[]) {
    const metrics: any = {};

    events.forEach(event => {
      if (event.data.loadTime) {
        metrics.pageLoadTime = (metrics.pageLoadTime || 0) + event.data.loadTime;
      }
      if (event.data.apiResponseTime) {
        metrics.apiResponseTime = (metrics.apiResponseTime || 0) + event.data.apiResponseTime;
      }
      if (event.data.memoryUsage) {
        metrics.memoryUsage = (metrics.memoryUsage || 0) + event.data.memoryUsage;
      }
    });

    const count = events.length;
    return {
      averagePageLoadTime: count > 0 ? metrics.pageLoadTime / count : 0,
      averageApiResponseTime: count > 0 ? metrics.apiResponseTime / count : 0,
      averageMemoryUsage: count > 0 ? metrics.memoryUsage / count : 0,
      totalEvents: count
    };
  }

  async getPendingReports(): Promise<AnalyticsReport[]> {
    const localReports = await offlineManager.getLocalDataByType('analytics_report');
    return localReports.filter((r: any) => !r.sentToServer) as AnalyticsReport[];
  }

  async sendPendingReports() {
    const pendingReports = await this.getPendingReports();
    
    for (const report of pendingReports) {
      try {
        await this.sendReportToServer(report);
        report.sentToServer = true;
        await offlineManager.storeLocalData(report.id, 'analytics_report', report);
      } catch (error) {
        console.error('Failed to send report:', report.id, error);
      }
    }
  }

  async clearOldEvents(maxAge: number = 7 * 24 * 60 * 60 * 1000) {
    const cutoff = Date.now() - maxAge;
    const oldEvents = Array.from(this.events.values())
      .filter(event => event.timestamp < cutoff);

    for (const event of oldEvents) {
      this.events.delete(event.id);
    }

    return oldEvents.length;
  }

  getEvents(): AnalyticsEvent[] {
    return Array.from(this.events.values());
  }

  getReports(): AnalyticsReport[] {
    return Array.from(this.reports.values());
  }
}

export const offlineAnalyticsService = new OfflineAnalyticsService();
export default offlineAnalyticsService;

