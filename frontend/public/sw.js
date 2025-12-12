const CACHE_NAME = 'disaster-relief-v1';
const STATIC_CACHE = 'static-v1';
const DYNAMIC_CACHE = 'dynamic-v1';
const API_CACHE = 'api-v1';

// Static assets to cache immediately
const STATIC_ASSETS = [
  '/',
  '/static/js/bundle.js',
  '/static/css/main.css',
  '/manifest.json',
  '/favicon.ico'
];

// API endpoints that should be cached
const API_ENDPOINTS = [
  '/api/auth/me',
  '/api/inventory/hubs',
  '/api/inventory/items',
  '/api/requests',
  '/api/tasks/mine'
];

// Install event - cache static assets
self.addEventListener('install', (event) => {
  console.log('Service Worker installing...');
  
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('Caching static assets');
        return cache.addAll(STATIC_ASSETS);
      })
      .then(() => {
        console.log('Static assets cached');
        return self.skipWaiting();
      })
      .catch((error) => {
        console.error('Failed to cache static assets:', error);
      })
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
  console.log('Service Worker activating...');
  
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (cacheName !== STATIC_CACHE && 
                cacheName !== DYNAMIC_CACHE && 
                cacheName !== API_CACHE) {
              console.log('Deleting old cache:', cacheName);
              return caches.delete(cacheName);
            }
          })
        );
      })
      .then(() => {
        console.log('Service Worker activated');
        return self.clients.claim();
      })
  );
});

// Fetch event - implement caching strategies
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);
  
  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }
  
  // Handle different types of requests
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(handleApiRequest(request));
  } else if (url.pathname.startsWith('/static/') || 
             url.pathname.endsWith('.js') || 
             url.pathname.endsWith('.css') ||
             url.pathname.endsWith('.png') ||
             url.pathname.endsWith('.jpg') ||
             url.pathname.endsWith('.svg')) {
    event.respondWith(handleStaticRequest(request));
  } else {
    event.respondWith(handlePageRequest(request));
  }
});

// Handle API requests with network-first strategy
async function handleApiRequest(request) {
  const url = new URL(request.url);
  
  // Only cache GET requests
  if (request.method !== 'GET') {
    return fetch(request);
  }
  
  try {
    // Try network first
    const networkResponse = await fetch(request);
    
    if (networkResponse.ok) {
      // Cache successful responses - use request object, not string
      const cache = await caches.open(API_CACHE);
      try {
        await cache.put(request, networkResponse.clone());
      } catch (cacheError) {
        console.warn('Failed to cache response:', cacheError);
        // Continue even if caching fails
      }
      
      // Store in IndexedDB for offline access
      try {
        await storeApiResponse(url.pathname, networkResponse.clone());
      } catch (dbError) {
        console.warn('Failed to store in IndexedDB:', dbError);
        // Continue even if IndexedDB fails
      }
    }
    
    return networkResponse;
  } catch (error) {
    console.log('Network failed, trying cache for:', url.pathname);
    
    // Try cache - use request object
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    // Try IndexedDB
    try {
      const indexedResponse = await getApiResponse(url.pathname);
      if (indexedResponse) {
        return new Response(JSON.stringify(indexedResponse), {
          headers: { 'Content-Type': 'application/json' }
        });
      }
    } catch (dbError) {
      console.warn('Failed to get from IndexedDB:', dbError);
    }
    
    // Return offline fallback
    return new Response(JSON.stringify({
      error: 'Offline',
      message: 'This data is not available offline'
    }), {
      status: 503,
      headers: { 'Content-Type': 'application/json' }
    });
  }
}

// Handle static assets with cache-first strategy
async function handleStaticRequest(request) {
  const cachedResponse = await caches.match(request);
  
  if (cachedResponse) {
    return cachedResponse;
  }
  
  try {
    const networkResponse = await fetch(request);
    
    if (networkResponse.ok) {
      const cache = await caches.open(STATIC_CACHE);
      cache.put(request, networkResponse.clone());
    }
    
    return networkResponse;
  } catch (error) {
    console.error('Failed to fetch static asset:', request.url);
    return new Response('Asset not available offline', { status: 503 });
  }
}

// Handle page requests with network-first strategy
async function handlePageRequest(request) {
  try {
    const networkResponse = await fetch(request);
    return networkResponse;
  } catch (error) {
    // Return cached version or offline page
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    // Return offline page
    return caches.match('/offline.html') || new Response('Offline', { status: 503 });
  }
}

// Background sync for offline actions
self.addEventListener('sync', (event) => {
  console.log('Background sync triggered:', event.tag);
  
  if (event.tag === 'background-sync') {
    event.waitUntil(doBackgroundSync());
  }
});

// Handle push notifications
self.addEventListener('push', (event) => {
  console.log('Push notification received');
  
  const options = {
    body: event.data ? event.data.text() : 'New update available',
    icon: '/icon-192x192.png',
    badge: '/badge-72x72.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    },
    actions: [
      {
        action: 'explore',
        title: 'View Details',
        icon: '/icon-192x192.png'
      },
      {
        action: 'close',
        title: 'Close',
        icon: '/icon-192x192.png'
      }
    ]
  };
  
  event.waitUntil(
    self.registration.showNotification('Disaster Relief Platform', options)
  );
});

// Handle notification clicks
self.addEventListener('notificationclick', (event) => {
  console.log('Notification clicked:', event.action);
  
  event.notification.close();
  
  if (event.action === 'explore') {
    event.waitUntil(
      clients.openWindow('/')
    );
  }
});

// IndexedDB operations for offline storage
async function storeApiResponse(endpoint, response) {
  try {
    // Clone response to read it without consuming the original
    const clonedResponse = response.clone();
    const data = await clonedResponse.json();
    const db = await openDB();
    const transaction = db.transaction(['apiCache'], 'readwrite');
    const store = transaction.objectStore('apiCache');
    
    await store.put({
      endpoint,
      data,
      timestamp: Date.now()
    });
  } catch (error) {
    // Don't log VersionError as it's handled in openDB
    if (error.name !== 'VersionError') {
      console.error('Failed to store API response:', error);
    }
  }
}

async function getApiResponse(endpoint) {
  try {
    const db = await openDB();
    const transaction = db.transaction(['apiCache'], 'readonly');
    const store = transaction.objectStore('apiCache');
    
    const result = await store.get(endpoint);
    return result ? result.data : null;
  } catch (error) {
    console.error('Failed to get API response:', error);
    return null;
  }
}

async function openDB() {
  return new Promise((resolve, reject) => {
    // Use version 2 to match existing database or handle upgrade
    const request = indexedDB.open('DisasterReliefDB', 2);
    
    request.onerror = () => {
      // If version error, try to delete and recreate
      if (request.error && request.error.name === 'VersionError') {
        console.warn('Version mismatch, attempting to delete and recreate database');
        const deleteRequest = indexedDB.deleteDatabase('DisasterReliefDB');
        deleteRequest.onsuccess = () => {
          // Retry with version 2
          const retryRequest = indexedDB.open('DisasterReliefDB', 2);
          retryRequest.onsuccess = () => resolve(retryRequest.result);
          retryRequest.onerror = () => reject(retryRequest.error);
          retryRequest.onupgradeneeded = (event) => {
            setupDatabase(event.target.result);
          };
        };
        deleteRequest.onerror = () => reject(deleteRequest.error);
      } else {
        reject(request.error);
      }
    };
    
    request.onsuccess = () => resolve(request.result);
    
    request.onupgradeneeded = (event) => {
      setupDatabase(event.target.result);
    };
  });
}

function setupDatabase(db) {
  if (!db.objectStoreNames.contains('apiCache')) {
    db.createObjectStore('apiCache', { keyPath: 'endpoint' });
  }
  
  if (!db.objectStoreNames.contains('offlineQueue')) {
    const store = db.createObjectStore('offlineQueue', { 
      keyPath: 'id', 
      autoIncrement: true 
    });
    store.createIndex('timestamp', 'timestamp', { unique: false });
    store.createIndex('type', 'type', { unique: false });
  }
  
  if (!db.objectStoreNames.contains('conflictResolution')) {
    const store = db.createObjectStore('conflictResolution', { 
      keyPath: 'id', 
      autoIncrement: true 
    });
    store.createIndex('entityId', 'entityId', { unique: false });
    store.createIndex('entityType', 'entityType', { unique: false });
  }
}

// Background sync implementation
async function doBackgroundSync() {
  console.log('Performing background sync...');
  
  try {
    const db = await openDB();
    const transaction = db.transaction(['offlineQueue'], 'readonly');
    const store = transaction.objectStore('offlineQueue');
    const queue = await store.getAll();
    
    for (const item of queue) {
      try {
        await syncOfflineAction(item);
        await removeFromQueue(item.id);
      } catch (error) {
        console.error('Failed to sync item:', item, error);
      }
    }
    
    console.log('Background sync completed');
  } catch (error) {
    console.error('Background sync failed:', error);
  }
}

async function syncOfflineAction(item) {
  const { type, data, url, method } = item;
  
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${data.token}`
    },
    body: method !== 'GET' ? JSON.stringify(data) : undefined
  });
  
  if (!response.ok) {
    throw new Error(`Sync failed: ${response.status}`);
  }
  
  return response;
}

async function removeFromQueue(id) {
  const db = await openDB();
  const transaction = db.transaction(['offlineQueue'], 'readwrite');
  const store = transaction.objectStore('offlineQueue');
  await store.delete(id);
}

// Message handling for communication with main thread
self.addEventListener('message', (event) => {
  const { type, data } = event.data;
  
  switch (type) {
    case 'SKIP_WAITING':
      self.skipWaiting();
      break;
    case 'CACHE_API_RESPONSE':
      storeApiResponse(data.endpoint, data.response);
      break;
    case 'QUEUE_OFFLINE_ACTION':
      queueOfflineAction(data);
      break;
    case 'CLEAR_CACHE':
      clearCache(data.cacheName);
      break;
  }
});

async function queueOfflineAction(action) {
  try {
    const db = await openDB();
    const transaction = db.transaction(['offlineQueue'], 'readwrite');
    const store = transaction.objectStore('offlineQueue');
    
    await store.add({
      ...action,
      timestamp: Date.now()
    });
    
    // Register for background sync
    await self.registration.sync.register('background-sync');
  } catch (error) {
    console.error('Failed to queue offline action:', error);
  }
}

async function clearCache(cacheName) {
  try {
    const cache = await caches.open(cacheName);
    const keys = await cache.keys();
    await Promise.all(keys.map(key => cache.delete(key)));
  } catch (error) {
    console.error('Failed to clear cache:', error);
  }
}



