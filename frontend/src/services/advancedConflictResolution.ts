import { offlineManager } from './offlineManager';

interface ConflictResolutionAlgorithm {
  entityType: string;
  priority: number;
  resolve: (localData: any, serverData: any) => 'local' | 'server' | 'merge' | 'manual';
}

interface ConflictMetrics {
  totalConflicts: number;
  resolvedAutomatically: number;
  resolvedManually: number;
  resolutionTime: number;
}

class AdvancedConflictResolution {
  private algorithms: Map<string, ConflictResolutionAlgorithm> = new Map();
  private metrics: ConflictMetrics = {
    totalConflicts: 0,
    resolvedAutomatically: 0,
    resolvedManually: 0,
    resolutionTime: 0
  };

  constructor() {
    this.initDefaultAlgorithms();
  }

  private initDefaultAlgorithms() {
    // Last-Write-Wins algorithm
    this.registerAlgorithm({
      entityType: 'needs',
      priority: 1,
      resolve: (localData, serverData) => {
        const localTimestamp = localData._timestamp || localData.updatedAt || 0;
        const serverTimestamp = serverData._timestamp || serverData.updatedAt || 0;
        return localTimestamp > serverTimestamp ? 'local' : 'server';
      }
    });

    // Field-level merge for tasks
    this.registerAlgorithm({
      entityType: 'tasks',
      priority: 1,
      resolve: (localData, serverData) => {
        // Check for critical field conflicts
        const criticalFields = ['status', 'priority', 'assignedTo'];
        const hasCriticalConflict = criticalFields.some(field => 
          localData[field] !== serverData[field]
        );

        if (hasCriticalConflict) {
          // Use timestamp for critical conflicts
          const localTimestamp = localData._timestamp || 0;
          const serverTimestamp = serverData._timestamp || 0;
          return localTimestamp > serverTimestamp ? 'local' : 'server';
        }

        // Merge non-critical fields
        return 'merge';
      }
    });

    // Smart merge for inventory
    this.registerAlgorithm({
      entityType: 'inventory',
      priority: 1,
      resolve: (localData, serverData) => {
        // For inventory, merge quantities and use latest timestamps for other fields
        const localQuantity = localData.quantity || 0;
        const serverQuantity = serverData.quantity || 0;
        
        // If quantities differ significantly, keep the one with more recent timestamp
        if (Math.abs(localQuantity - serverQuantity) > 10) {
          const localTimestamp = localData._timestamp || 0;
          const serverTimestamp = serverData._timestamp || 0;
          return localTimestamp > serverTimestamp ? 'local' : 'server';
        }

        // Otherwise, merge
        return 'merge';
      }
    });

    // Automatic server override for user profiles
    this.registerAlgorithm({
      entityType: 'users',
      priority: 1,
      resolve: () => 'server' // Always use server version for user data
    });
  }

  registerAlgorithm(algorithm: ConflictResolutionAlgorithm) {
    this.algorithms.set(algorithm.entityType, algorithm);
  }

  async resolveConflict(
    entityId: string,
    entityType: string,
    localData: any,
    serverData: any
  ): Promise<'local' | 'server' | 'merge' | 'manual'> {
    const startTime = Date.now();
    this.metrics.totalConflicts++;

    const algorithm = this.algorithms.get(entityType);
    
    if (!algorithm) {
      // No algorithm found, require manual resolution
      return 'manual';
    }

    const resolution = algorithm.resolve(localData, serverData);
    
    const resolutionTime = Date.now() - startTime;
    this.metrics.resolutionTime += resolutionTime;

    if (resolution === 'merge') {
      const mergedData = this.mergeData(localData, serverData, entityType);
      await offlineManager.storeLocalData(entityId, entityType, mergedData);
    }

    if (resolution !== 'manual') {
      this.metrics.resolvedAutomatically++;
    } else {
      this.metrics.resolvedManually++;
    }

    return resolution;
  }

  private mergeData(localData: any, serverData: any, entityType: string): any {
    const merged: any = { ...serverData };

    // Merge strategies based on entity type
    switch (entityType) {
      case 'tasks':
        // Keep local changes for non-critical fields
        Object.keys(localData).forEach(key => {
          if (!['status', 'priority', 'assignedTo', 'id', '_timestamp'].includes(key)) {
            merged[key] = localData[key];
          }
        });
        break;

      case 'inventory':
        // Merge quantities
        merged.quantity = Math.max(localData.quantity || 0, serverData.quantity || 0);
        // Merge other local changes
        Object.keys(localData).forEach(key => {
          if (!['quantity', 'id', '_timestamp'].includes(key)) {
            if (localData[key] !== serverData[key]) {
              merged[key] = localData[key];
            }
          }
        });
        break;

      case 'needs':
        // Prefer local description, server for status
        merged.description = localData.description || serverData.description;
        merged.status = serverData.status || localData.status; // Use server status
        merged.priority = Math.max(localData.priority || 0, serverData.priority || 0);
        break;

      default:
        // Default merge: local changes override server for non-conflicting fields
        Object.keys(localData).forEach(key => {
          if (!key.startsWith('_')) {
            merged[key] = localData[key];
          }
        });
    }

    merged._merged = true;
    merged._mergedAt = Date.now();
    merged._mergeStrategy = entityType;

    return merged;
  }

  getMetrics(): ConflictMetrics {
    return { ...this.metrics };
  }

  resetMetrics() {
    this.metrics = {
      totalConflicts: 0,
      resolvedAutomatically: 0,
      resolvedManually: 0,
      resolutionTime: 0
    };
  }
}

export const advancedConflictResolution = new AdvancedConflictResolution();
export default advancedConflictResolution;

