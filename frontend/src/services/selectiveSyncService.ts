import { offlineManager } from './offlineManager';

interface SyncPriority {
  entityType: string;
  priority: number;
  maxSize: number;
  syncInterval: number;
}

interface BandwidthEstimate {
  available: number; // bytes per second
  estimatedSpeed: 'slow' | 'medium' | 'fast';
}

interface SyncPlan {
  priorityOrder: string[];
  estimatedTime: number;
  estimatedSize: number;
}

class SelectiveSyncService {
  private priorities: Map<string, SyncPriority> = new Map();
  private currentBandwidth: BandwidthEstimate = {
    available: 1024 * 100, // 100KB/s default
    estimatedSpeed: 'medium'
  };

  constructor() {
    this.initDefaultPriorities();
    this.estimateBandwidth();
  }

  private initDefaultPriorities() {
    // High priority entities
    this.registerPriority({
      entityType: 'needs',
      priority: 10,
      maxSize: 1024 * 50, // 50KB
      syncInterval: 1000 // 1 second
    });

    this.registerPriority({
      entityType: 'tasks',
      priority: 9,
      maxSize: 1024 * 50,
      syncInterval: 2000 // 2 seconds
    });

    // Medium priority
    this.registerPriority({
      entityType: 'inventory',
      priority: 6,
      maxSize: 1024 * 100, // 100KB
      syncInterval: 5000 // 5 seconds
    });

    this.registerPriority({
      entityType: 'deliveries',
      priority: 5,
      maxSize: 1024 * 50,
      syncInterval: 5000
    });

    // Low priority
    this.registerPriority({
      entityType: 'analytics',
      priority: 3,
      maxSize: 1024 * 200, // 200KB
      syncInterval: 30000 // 30 seconds
    });

    this.registerPriority({
      entityType: 'logs',
      priority: 1,
      maxSize: 1024 * 500, // 500KB
      syncInterval: 60000 // 60 seconds
    });
  }

  registerPriority(priority: SyncPriority) {
    this.priorities.set(priority.entityType, priority);
  }

  async createSyncPlan(entityTypes?: string[]): Promise<SyncPlan> {
    const queuedActions = await offlineManager.getQueuedActions();
    const localData = await offlineManager.getLocalDataByType('all');

    // Group actions by entity type
    const actionsByType = new Map<string, any[]>();
    queuedActions.forEach(action => {
      const type = this.extractEntityType(action.url);
      if (!actionsByType.has(type)) {
        actionsByType.set(type, []);
      }
      actionsByType.get(type)!.push(action);
    });

    // Calculate priorities and sizes
    const syncItems: Array<{ type: string; priority: number; size: number }> = [];

    const typesToProcess = entityTypes || Array.from(this.priorities.keys());
    
    for (const type of typesToProcess) {
      const priorityConfig = this.priorities.get(type);
      if (!priorityConfig) continue;

      const actions = actionsByType.get(type) || [];
      const size = this.estimateActionSize(actions);

      if (size > 0) {
        syncItems.push({
          type,
          priority: priorityConfig.priority,
          size: Math.min(size, priorityConfig.maxSize)
        });
      }
    }

    // Sort by priority
    syncItems.sort((a, b) => b.priority - a.priority);

    const priorityOrder = syncItems.map(item => item.type);
    const estimatedSize = syncItems.reduce((sum, item) => sum + item.size, 0);
    const estimatedTime = estimatedSize / this.currentBandwidth.available;

    return {
      priorityOrder,
      estimatedTime,
      estimatedSize
    };
  }

  async syncSelective(entityTypes?: string[], onProgress?: (progress: number) => void) {
    const plan = await this.createSyncPlan(entityTypes);
    const totalItems = plan.priorityOrder.length;
    let completedItems = 0;

    for (const type of plan.priorityOrder) {
      try {
        await this.syncEntityType(type);
        completedItems++;
        onProgress?.(completedItems / totalItems);
      } catch (error) {
        console.error(`Failed to sync ${type}:`, error);
      }
    }
  }

  async syncByPriority(priorityThreshold: number = 5) {
    const plan = await this.createSyncPlan();
    const highPriorityTypes = plan.priorityOrder.filter(type => {
      const config = this.priorities.get(type);
      return config && config.priority >= priorityThreshold;
    });

    await this.syncSelective(highPriorityTypes);
  }

  async syncByBandwidth(availableBandwidth: number) {
    this.currentBandwidth.available = availableBandwidth;
    
    if (availableBandwidth < 1024 * 10) { // < 10KB/s
      this.currentBandwidth.estimatedSpeed = 'slow';
      // Only sync critical data
      await this.syncByPriority(8);
    } else if (availableBandwidth < 1024 * 100) { // < 100KB/s
      this.currentBandwidth.estimatedSpeed = 'medium';
      // Sync high and medium priority data
      await this.syncByPriority(5);
    } else {
      this.currentBandwidth.estimatedSpeed = 'fast';
      // Sync all data
      await this.syncSelective();
    }
  }

  private async syncEntityType(entityType: string) {
    const queuedActions = await offlineManager.getQueuedActions();
    const priorityConfig = this.priorities.get(entityType);
    
    if (!priorityConfig) return;

    const typeActions = queuedActions.filter(action => 
      this.extractEntityType(action.url) === entityType
    );

    for (const action of typeActions) {
      try {
        await offlineManager.triggerSync();
      } catch (error) {
        console.error(`Failed to sync action ${action.id}:`, error);
      }
    }
  }

  private estimateBandwidth() {
    // Use Navigation Timing API to estimate bandwidth
    if ('performance' in window && 'timing' in performance) {
      const perfTiming = performance.timing;
      const resourceTiming = performance.getEntriesByType('resource') as PerformanceResourceTiming[];
      
      if (resourceTiming.length > 0) {
        const totalSize = resourceTiming.reduce((sum, entry) => 
          sum + (entry.transferSize || 0), 0
        );
        const totalTime = perfTiming.loadEventEnd - perfTiming.navigationStart;
        
        if (totalTime > 0) {
          this.currentBandwidth.available = totalSize / (totalTime / 1000);
          
          if (this.currentBandwidth.available < 1024 * 10) {
            this.currentBandwidth.estimatedSpeed = 'slow';
          } else if (this.currentBandwidth.available < 1024 * 100) {
            this.currentBandwidth.estimatedSpeed = 'medium';
          } else {
            this.currentBandwidth.estimatedSpeed = 'fast';
          }
        }
      }
    }
  }

  private estimateActionSize(actions: any[]): number {
    return actions.reduce((size, action) => {
      const dataSize = JSON.stringify(action.data).length;
      const urlSize = action.url.length;
      return size + dataSize + urlSize;
    }, 0);
  }

  private extractEntityType(url: string): string {
    if (url.includes('/needs')) return 'needs';
    if (url.includes('/tasks')) return 'tasks';
    if (url.includes('/inventory')) return 'inventory';
    if (url.includes('/deliveries')) return 'deliveries';
    if (url.includes('/analytics')) return 'analytics';
    if (url.includes('/logs')) return 'logs';
    if (url.includes('/users')) return 'users';
    return 'unknown';
  }

  getBandwidthEstimate(): BandwidthEstimate {
    return { ...this.currentBandwidth };
  }

  getPriorities(): Map<string, SyncPriority> {
    return new Map(this.priorities);
  }
}

export const selectiveSyncService = new SelectiveSyncService();
export default selectiveSyncService;



