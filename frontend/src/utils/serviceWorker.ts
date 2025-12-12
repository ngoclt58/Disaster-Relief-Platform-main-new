// Service Worker Registration and Management

interface ServiceWorkerConfig {
  onUpdate?: (registration: ServiceWorkerRegistration) => void;
  onSuccess?: (registration: ServiceWorkerRegistration) => void;
  onOfflineReady?: () => void;
}

export function registerServiceWorker(config: ServiceWorkerConfig = {}) {
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      const swUrl = `${process.env.PUBLIC_URL}/sw.js`;

      navigator.serviceWorker
        .register(swUrl)
        .then((registration) => {
          console.log('SW registered: ', registration);
          config.onSuccess?.(registration);

          // Check for updates
          registration.addEventListener('updatefound', () => {
            const newWorker = registration.installing;
            if (newWorker) {
              newWorker.addEventListener('statechange', () => {
                if (newWorker.state === 'installed') {
                  if (navigator.serviceWorker.controller) {
                    // New content is available, show update prompt
                    config.onUpdate?.(registration);
                  } else {
                    // Content is cached for offline use
                    config.onOfflineReady?.();
                  }
                }
              });
            }
          });
        })
        .catch((error) => {
          console.log('SW registration failed: ', error);
        });
    });

    // Listen for service worker updates
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      window.location.reload();
    });
  }
}

export function unregisterServiceWorker() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready.then((registration) => {
      registration.unregister();
    });
  }
}

// Service Worker Communication
export class ServiceWorkerMessenger {
  private sw: ServiceWorker | null = null;

  constructor() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then((registration) => {
        this.sw = registration.active;
      });

      navigator.serviceWorker.addEventListener('message', (event) => {
        this.handleMessage(event.data);
      });
    }
  }

  private handleMessage(data: any) {
    switch (data.type) {
      case 'CACHE_UPDATED':
        console.log('Cache updated:', data.cacheName);
        break;
      case 'SYNC_COMPLETE':
        console.log('Background sync completed');
        break;
      case 'SYNC_FAILED':
        console.error('Background sync failed:', data.error);
        break;
    }
  }

  // Send message to service worker
  postMessage(type: string, data: any = {}) {
    if (this.sw) {
      this.sw.postMessage({ type, data });
    } else {
      navigator.serviceWorker.ready.then((registration) => {
        if (registration.active) {
          registration.active.postMessage({ type, data });
        }
      });
    }
  }

  // Cache API response
  cacheApiResponse(endpoint: string, response: Response) {
    this.postMessage('CACHE_API_RESPONSE', { endpoint, response });
  }

  // Queue offline action
  queueOfflineAction(action: any) {
    this.postMessage('QUEUE_OFFLINE_ACTION', action);
  }

  // Clear cache
  clearCache(cacheName: string) {
    this.postMessage('CLEAR_CACHE', { cacheName });
  }

  // Request background sync
  requestBackgroundSync() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then((registration) => {
        // Safe check for sync manager
        const syncManager = (registration as any).sync;
        if (syncManager && typeof syncManager.register === 'function') {
          syncManager.register('background-sync');
        }
      });
    }
  }
}

export const swMessenger = new ServiceWorkerMessenger();

// PWA Installation
export class PWAInstaller {
  private deferredPrompt: any = null;
  private isInstalled = false;

  constructor() {
    this.setupInstallPrompt();
    this.checkInstallationStatus();
  }

  private setupInstallPrompt() {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.deferredPrompt = e;
    });

    window.addEventListener('appinstalled', () => {
      this.isInstalled = true;
      this.deferredPrompt = null;
    });
  }

  private checkInstallationStatus() {
    // Check if app is already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      this.isInstalled = true;
    }
  }

  async install(): Promise<boolean> {
    if (!this.deferredPrompt) {
      return false;
    }

    this.deferredPrompt.prompt();
    const { outcome } = await this.deferredPrompt.userChoice;
    
    if (outcome === 'accepted') {
      this.deferredPrompt = null;
      return true;
    }
    
    return false;
  }

  get canInstall(): boolean {
    return !!this.deferredPrompt && !this.isInstalled;
  }

  get isAppInstalled(): boolean {
    return this.isInstalled;
  }
}

export const pwaInstaller = new PWAInstaller();

// Offline Detection
export class OfflineDetector {
  private isOnline = navigator.onLine;
  private listeners: ((isOnline: boolean) => void)[] = [];

  constructor() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.notifyListeners();
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.notifyListeners();
    });
  }

  addListener(callback: (isOnline: boolean) => void) {
    this.listeners.push(callback);
  }

  removeListener(callback: (isOnline: boolean) => void) {
    this.listeners = this.listeners.filter(l => l !== callback);
  }

  private notifyListeners() {
    this.listeners.forEach(callback => callback(this.isOnline));
  }

  get onlineStatus(): boolean {
    return this.isOnline;
  }
}

export const offlineDetector = new OfflineDetector();

// Cache Management
export class CacheManager {
  async getCacheSize(): Promise<number> {
    if (!('caches' in window)) return 0;

    let totalSize = 0;
    const cacheNames = await caches.keys();
    
    for (const cacheName of cacheNames) {
      const cache = await caches.open(cacheName);
      const keys = await cache.keys();
      
      for (const key of keys) {
        const response = await cache.match(key);
        if (response) {
          const blob = await response.blob();
          totalSize += blob.size;
        }
      }
    }
    
    return totalSize;
  }

  async clearAllCaches(): Promise<void> {
    if (!('caches' in window)) return;

    const cacheNames = await caches.keys();
    await Promise.all(cacheNames.map(name => caches.delete(name)));
  }

  async clearCache(cacheName: string): Promise<void> {
    if (!('caches' in window)) return;

    await caches.delete(cacheName);
  }

  async getCacheInfo(): Promise<{ name: string; size: number; keys: number }[]> {
    if (!('caches' in window)) return [];

    const cacheNames = await caches.keys();
    const cacheInfo = [];

    for (const cacheName of cacheNames) {
      const cache = await caches.open(cacheName);
      const keys = await cache.keys();
      let size = 0;

      for (const key of keys) {
        const response = await cache.match(key);
        if (response) {
          const blob = await response.blob();
          size += blob.size;
        }
      }

      cacheInfo.push({
        name: cacheName,
        size,
        keys: keys.length
      });
    }

    return cacheInfo;
  }
}

export const cacheManager = new CacheManager();



