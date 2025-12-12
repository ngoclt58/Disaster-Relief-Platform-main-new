interface OfflineAction {
  id?: number;
  type: string;
  url: string;
  method: string;
  data: any;
  timestamp: number;
  retryCount?: number;
  maxRetries?: number;
}

interface ConflictResolution {
  id?: number;
  entityId: string;
  entityType: string;
  localData: any;
  serverData: any;
  resolution: 'local' | 'server' | 'merge' | 'manual';
  resolvedAt?: number;
}

interface ApiCacheEntry {
  endpoint: string;
  data: any;
  timestamp: number;
}

class OfflineManager {
  private db: IDBDatabase | null = null;
  private isOnline: boolean = navigator.onLine;
  private syncInProgress: boolean = false;

  constructor() {
    this.init();
    this.setupEventListeners();
  }

  private async init() {
    try {
      this.db = await this.openDB();
      console.log('Offline manager initialized');
    } catch (error) {
      console.error('Failed to initialize offline manager:', error);
    }
  }

  private openDB(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('DisasterReliefDB', 2);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result);
      
      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        // API Cache store
        if (!db.objectStoreNames.contains('apiCache')) {
          const store = db.createObjectStore('apiCache', { keyPath: 'endpoint' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }
        
        // Offline Queue store
        if (!db.objectStoreNames.contains('offlineQueue')) {
          const store = db.createObjectStore('offlineQueue', { 
            keyPath: 'id', 
            autoIncrement: true 
          });
          store.createIndex('timestamp', 'timestamp', { unique: false });
          store.createIndex('type', 'type', { unique: false });
        }
        
        // Conflict Resolution store
        if (!db.objectStoreNames.contains('conflictResolution')) {
          const store = db.createObjectStore('conflictResolution', { 
            keyPath: 'id', 
            autoIncrement: true 
          });
          store.createIndex('entityId', 'entityId', { unique: false });
          store.createIndex('entityType', 'entityType', { unique: false });
        }
        
        // Local Data store for entities
        if (!db.objectStoreNames.contains('localData')) {
          const store = db.createObjectStore('localData', { 
            keyPath: 'id' 
          });
          store.createIndex('type', 'type', { unique: false });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }
      };
    });
  }

  private setupEventListeners() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.triggerSync();
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
    });

    // Listen for service worker messages
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('message', (event) => {
        this.handleServiceWorkerMessage(event.data);
      });
    }
  }

  private handleServiceWorkerMessage(data: any) {
    switch (data.type) {
      case 'SYNC_COMPLETE':
        this.syncInProgress = false;
        this.notifySyncComplete();
        break;
      case 'SYNC_FAILED':
        this.syncInProgress = false;
        this.notifySyncFailed(data.error);
        break;
    }
  }

  // Queue offline action
  async queueAction(type: string, url: string, method: string, data: any): Promise<number> {
    if (!this.db) {
      throw new Error('Database not initialized');
    }

    const action: OfflineAction = {
      type,
      url,
      method,
      data,
      timestamp: Date.now(),
      retryCount: 0,
      maxRetries: 3
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineQueue'], 'readwrite');
      const store = transaction.objectStore('offlineQueue');
      const request = store.add(action);

      request.onsuccess = () => {
        const id = request.result as number;
        console.log('Action queued:', id, action);
        
        // Register for background sync
        if ('serviceWorker' in navigator) {
          navigator.serviceWorker.ready.then(registration => {
            // Safe check for sync manager
            const syncManager = (registration as any).sync;
            if (syncManager && typeof syncManager.register === 'function') {
              syncManager.register('background-sync');
            }
          });
        }
        
        resolve(id);
      };

      request.onerror = () => reject(request.error);
    });
  }

  // Get queued actions
  async getQueuedActions(): Promise<OfflineAction[]> {
    if (!this.db) return [];

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineQueue'], 'readonly');
      const store = transaction.objectStore('offlineQueue');
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Remove action from queue
  async removeAction(id: number): Promise<void> {
    if (!this.db) return;

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineQueue'], 'readwrite');
      const store = transaction.objectStore('offlineQueue');
      const request = store.delete(id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Cache API response
  async cacheApiResponse(endpoint: string, data: any): Promise<void> {
    if (!this.db) return;

    const entry: ApiCacheEntry = {
      endpoint,
      data,
      timestamp: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const request = store.put(entry);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Get cached API response
  async getCachedResponse(endpoint: string): Promise<any | null> {
    if (!this.db) return null;

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readonly');
      const store = transaction.objectStore('apiCache');
      const request = store.get(endpoint);

      request.onsuccess = () => {
        const result = request.result;
        if (result && this.isCacheValid(result.timestamp)) {
          resolve(result.data);
        } else {
          resolve(null);
        }
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Store local data
  async storeLocalData(id: string, type: string, data: any): Promise<void> {
    if (!this.db) return;

    const entry = {
      id,
      type,
      data,
      timestamp: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['localData'], 'readwrite');
      const store = transaction.objectStore('localData');
      const request = store.put(entry);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Get local data
  async getLocalData(id: string): Promise<any | null> {
    if (!this.db) return null;

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['localData'], 'readonly');
      const store = transaction.objectStore('localData');
      const request = store.get(id);

      request.onsuccess = () => {
        const result = request.result;
        resolve(result ? result.data : null);
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Get all local data by type
  async getLocalDataByType(type: string): Promise<any[]> {
    if (!this.db) return [];

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['localData'], 'readonly');
      const store = transaction.objectStore('localData');
      const index = store.index('type');
      const request = index.getAll(type);

      request.onsuccess = () => {
        const results = request.result;
        resolve(results.map(r => ({ ...r.data, _localId: r.id })));
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Handle conflict resolution
  async handleConflict(
    entityId: string, 
    entityType: string, 
    localData: any, 
    serverData: any
  ): Promise<ConflictResolution> {
    if (!this.db) {
      throw new Error('Database not initialized');
    }

    const conflict: ConflictResolution = {
      entityId,
      entityType,
      localData,
      serverData,
      resolution: 'manual', // Default to manual resolution
      resolvedAt: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['conflictResolution'], 'readwrite');
      const store = transaction.objectStore('conflictResolution');
      const request = store.add(conflict);

      request.onsuccess = () => {
        const id = request.result as number;
        resolve({ ...conflict, id });
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Get pending conflicts
  async getPendingConflicts(): Promise<ConflictResolution[]> {
    if (!this.db) return [];

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['conflictResolution'], 'readonly');
      const store = transaction.objectStore('conflictResolution');
      const request = store.getAll();

      request.onsuccess = () => {
        const conflicts = request.result.filter(c => c.resolution === 'manual');
        resolve(conflicts);
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Resolve conflict
  async resolveConflict(id: number, resolution: 'local' | 'server' | 'merge', mergedData?: any): Promise<void> {
    if (!this.db) return;

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['conflictResolution'], 'readwrite');
      const store = transaction.objectStore('conflictResolution');
      const getRequest = store.get(id);

      getRequest.onsuccess = () => {
        const conflict = getRequest.result;
        if (conflict) {
          conflict.resolution = resolution;
          conflict.resolvedAt = Date.now();
          
          if (resolution === 'merge' && mergedData) {
            conflict.mergedData = mergedData;
          }

          const updateRequest = store.put(conflict);
          updateRequest.onsuccess = () => resolve();
          updateRequest.onerror = () => reject(updateRequest.error);
        } else {
          reject(new Error('Conflict not found'));
        }
      };
      getRequest.onerror = () => reject(getRequest.error);
    });
  }

  // Trigger sync
  async triggerSync(): Promise<void> {
    if (!this.isOnline || this.syncInProgress) return;

    this.syncInProgress = true;
    console.log('Starting offline sync...');

    try {
      const actions = await this.getQueuedActions();
      
      for (const action of actions) {
        try {
          await this.syncAction(action);
          await this.removeAction(action.id!);
        } catch (error) {
          console.error('Failed to sync action:', action, error);
          
          // Increment retry count
          action.retryCount = (action.retryCount || 0) + 1;
          
          if (action.retryCount >= (action.maxRetries || 3)) {
            console.error('Max retries exceeded for action:', action);
            await this.removeAction(action.id!);
          }
        }
      }

      this.syncInProgress = false;
      this.notifySyncComplete();
    } catch (error) {
      this.syncInProgress = false;
      this.notifySyncFailed(error);
    }
  }

  private async syncAction(action: OfflineAction): Promise<void> {
    const response = await fetch(action.url, {
      method: action.method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${action.data.token || ''}`
      },
      body: action.method !== 'GET' ? JSON.stringify(action.data) : undefined
    });

    if (!response.ok) {
      throw new Error(`Sync failed: ${response.status}`);
    }

    return response.json();
  }

  private isCacheValid(timestamp: number): boolean {
    const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    return Date.now() - timestamp < CACHE_DURATION;
  }

  private notifySyncComplete() {
    window.dispatchEvent(new CustomEvent('offlineSyncComplete'));
  }

  private notifySyncFailed(error: any) {
    window.dispatchEvent(new CustomEvent('offlineSyncFailed', { detail: error }));
  }

  // Public API
  get isOnlineStatus(): boolean {
    return this.isOnline;
  }

  get isSyncInProgress(): boolean {
    return this.syncInProgress;
  }

  // Clear all offline data
  async clearAllData(): Promise<void> {
    if (!this.db) return;

    const stores = ['apiCache', 'offlineQueue', 'conflictResolution', 'localData'];
    
    for (const storeName of stores) {
      const transaction = this.db.transaction([storeName], 'readwrite');
      const store = transaction.objectStore(storeName);
      await store.clear();
    }
  }
}

export const offlineManager = new OfflineManager();
export default offlineManager;



