import { apiService } from './api';
import { offlineManager } from './offlineManager';

interface OfflineApiOptions {
  cache?: boolean;
  offlineQueue?: boolean;
  conflictResolution?: boolean;
}

class OfflineApiService {
  private defaultOptions: OfflineApiOptions = {
    cache: true,
    offlineQueue: true,
    conflictResolution: true
  };

  // GET request with offline support
  async get<T>(url: string, options: OfflineApiOptions = {}): Promise<T> {
    const opts = { ...this.defaultOptions, ...options };
    
    if (offlineManager.isOnlineStatus) {
      try {
        const response = await apiService.get<T>(url);
        
        // Cache the response if caching is enabled
        if (opts.cache) {
          await offlineManager.cacheApiResponse(url, response);
        }
        
        return response;
      } catch (error) {
        // If online request fails, try cache
        if (opts.cache) {
          const cached = await offlineManager.getCachedResponse(url);
          if (cached) {
            return cached;
          }
        }
        throw error;
      }
    } else {
      // Offline - try cache first
      if (opts.cache) {
        const cached = await offlineManager.getCachedResponse(url);
        if (cached) {
          return cached;
        }
      }
      
      // Try local data
      const localData = await offlineManager.getLocalData(url);
      if (localData) {
        return localData;
      }
      
      throw new Error('Data not available offline');
    }
  }

  // POST request with offline queuing
  async post<T>(url: string, data: any, options: OfflineApiOptions = {}): Promise<T> {
    const opts = { ...this.defaultOptions, ...options };
    
    if (offlineManager.isOnlineStatus) {
      try {
        const response = await apiService.post<T>(url, data);
        return response;
      } catch (error) {
        // If online request fails and queuing is enabled, queue for later
        if (opts.offlineQueue) {
          await offlineManager.queueAction('POST', url, 'POST', data);
          throw new Error('Request queued for offline sync');
        }
        throw error;
      }
    } else {
      // Offline - queue the request
      if (opts.offlineQueue) {
        await offlineManager.queueAction('POST', url, 'POST', data);
        
        // Store locally for immediate UI updates
        const localId = `local_${Date.now()}`;
        await offlineManager.storeLocalData(localId, this.getEntityType(url), data);
        
        return { id: localId, ...data, _isLocal: true } as T;
      }
      
      throw new Error('Cannot perform POST request offline');
    }
  }

  // PUT request with offline queuing
  async put<T>(url: string, data: any, options: OfflineApiOptions = {}): Promise<T> {
    const opts = { ...this.defaultOptions, ...options };
    
    if (offlineManager.isOnlineStatus) {
      try {
        const response = await apiService.put<T>(url, data);
        return response;
      } catch (error) {
        if (opts.offlineQueue) {
          await offlineManager.queueAction('PUT', url, 'PUT', data);
          throw new Error('Request queued for offline sync');
        }
        throw error;
      }
    } else {
      if (opts.offlineQueue) {
        await offlineManager.queueAction('PUT', url, 'PUT', data);
        
        // Update local data
        const entityId = this.extractEntityId(url);
        await offlineManager.storeLocalData(entityId, this.getEntityType(url), data);
        
        return { ...data, _isLocal: true } as T;
      }
      
      throw new Error('Cannot perform PUT request offline');
    }
  }

  // DELETE request with offline queuing
  async delete<T>(url: string, options: OfflineApiOptions = {}): Promise<T> {
    const opts = { ...this.defaultOptions, ...options };
    
    if (offlineManager.isOnlineStatus) {
      try {
        const response = await apiService.delete<T>(url);
        return response;
      } catch (error) {
        if (opts.offlineQueue) {
          await offlineManager.queueAction('DELETE', url, 'DELETE', {});
          throw new Error('Request queued for offline sync');
        }
        throw error;
      }
    } else {
      if (opts.offlineQueue) {
        await offlineManager.queueAction('DELETE', url, 'DELETE', {});
        
        // Mark as deleted locally
        const entityId = this.extractEntityId(url);
        await offlineManager.storeLocalData(entityId, this.getEntityType(url), { 
          _deleted: true, 
          _deletedAt: Date.now() 
        });
        
        return { _deleted: true } as T;
      }
      
      throw new Error('Cannot perform DELETE request offline');
    }
  }

  // PATCH request with offline queuing
  async patch<T>(url: string, data: any, options: OfflineApiOptions = {}): Promise<T> {
    const opts = { ...this.defaultOptions, ...options };
    
    if (offlineManager.isOnlineStatus) {
      try {
        const response = await apiService.patch<T>(url, data);
        return response;
      } catch (error) {
        if (opts.offlineQueue) {
          await offlineManager.queueAction('PATCH', url, 'PATCH', data);
          throw new Error('Request queued for offline sync');
        }
        throw error;
      }
    } else {
      if (opts.offlineQueue) {
        await offlineManager.queueAction('PATCH', url, 'PATCH', data);
        
        // Update local data
        const entityId = this.extractEntityId(url);
        const existingData = await offlineManager.getLocalData(entityId);
        const updatedData = { ...existingData, ...data, _isLocal: true };
        await offlineManager.storeLocalData(entityId, this.getEntityType(url), updatedData);
        
        return updatedData as T;
      }
      
      throw new Error('Cannot perform PATCH request offline');
    }
  }

  // Get offline data by type
  async getOfflineData<T>(type: string): Promise<T[]> {
    return await offlineManager.getLocalDataByType(type);
  }

  // Get queued actions
  async getQueuedActions() {
    return await offlineManager.getQueuedActions();
  }

  // Get pending conflicts
  async getPendingConflicts() {
    return await offlineManager.getPendingConflicts();
  }

  // Resolve conflict
  async resolveConflict(id: number, resolution: 'local' | 'server' | 'merge', mergedData?: any) {
    return await offlineManager.resolveConflict(id, resolution, mergedData);
  }

  // Trigger sync
  async triggerSync() {
    return await offlineManager.triggerSync();
  }

  // Check if online
  get isOnline(): boolean {
    return offlineManager.isOnlineStatus;
  }

  // Check if sync in progress
  get isSyncInProgress(): boolean {
    return offlineManager.isSyncInProgress;
  }

  // Helper methods
  private getEntityType(url: string): string {
    if (url.includes('/needs')) return 'needs';
    if (url.includes('/tasks')) return 'tasks';
    if (url.includes('/inventory')) return 'inventory';
    if (url.includes('/users')) return 'users';
    if (url.includes('/deliveries')) return 'deliveries';
    return 'unknown';
  }

  private extractEntityId(url: string): string {
    const parts = url.split('/');
    return parts[parts.length - 1] || parts[parts.length - 2] || 'unknown';
  }

  // Specific API methods with offline support
  async getNeeds(filters: any = {}) {
    const url = `/api/requests?${new URLSearchParams(filters).toString()}`;
    return this.get(url);
  }

  async createNeed(needData: any) {
    return this.post('/api/resident/needs', needData);
  }

  async updateNeed(id: string, needData: any) {
    return this.patch(`/api/needs/${id}`, needData);
  }

  async deleteNeed(id: string) {
    return this.delete(`/api/needs/${id}`);
  }

  async getTasks(filters: any = {}) {
    const url = `/api/tasks/mine?${new URLSearchParams(filters).toString()}`;
    return this.get(url);
  }

  async createTask(taskData: any) {
    return this.post('/api/tasks', taskData);
  }

  async updateTask(id: string, taskData: any) {
    return this.patch(`/api/tasks/${id}`, taskData);
  }

  async claimTask(id: string) {
    return this.post(`/api/tasks/${id}:claim`, {});
  }

  async assignTask(id: string, assigneeId: string) {
    return this.post(`/api/tasks/${id}:assign`, { assigneeId });
  }

  async getInventoryStock(filters: any = {}) {
    const url = `/api/inventory/stock?${new URLSearchParams(filters).toString()}`;
    return this.get(url);
  }

  async updateStock(stockData: any) {
    return this.put('/api/inventory/stock', stockData);
  }

  async createDelivery(deliveryData: any) {
    return this.post('/api/deliveries', deliveryData);
  }

  async getProfile() {
    return this.get('/api/auth/me');
  }

  async updateProfile(profileData: any) {
    return this.post('/api/resident/profile', profileData);
  }

  // Upload with offline support
  async uploadFile(file: File, metadata?: any) {
    if (offlineManager.isOnlineStatus) {
      // Online upload
      try {
        const presignedData = await apiService.getPresignedUploadUrl(
          file.name, 
          file.type
        );
        await apiService.uploadToMinIO(presignedData.url, file);
        return presignedData;
      } catch (error) {
        // Queue for offline upload
        await offlineManager.queueAction('UPLOAD', '/api/media/upload', 'POST', {
          file: file.name,
          type: file.type,
          metadata
        });
        
        // Store file locally
        const localId = `upload_${Date.now()}`;
        await offlineManager.storeLocalData(localId, 'upload', {
          file: file.name,
          type: file.type,
          metadata,
          _isLocal: true
        });
        
        throw new Error('Upload queued for offline sync');
      }
    } else {
      // Offline - store file info
      const localId = `upload_${Date.now()}`;
      await offlineManager.storeLocalData(localId, 'upload', {
        file: file.name,
        type: file.type,
        metadata,
        _isLocal: true
      });
      
      await offlineManager.queueAction('UPLOAD', '/api/media/upload', 'POST', {
        file: file.name,
        type: file.type,
        metadata
      });
      
      return { id: localId, _isLocal: true };
    }
  }
}

export const offlineApiService = new OfflineApiService();
export default offlineApiService;



