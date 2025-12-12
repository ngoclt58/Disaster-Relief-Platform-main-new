const CACHE_NAME = 'drp-cache-v1';
const API_CACHE_NAME = 'drp-api-cache-v1';
const OUTBOX_DB = 'drp-outbox';
const OUTBOX_STORE = 'requests';

self.addEventListener('install', (event) => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    await cache.addAll([
      '/',
      '/index.html'
    ]);
    self.skipWaiting();
  })());
});

self.addEventListener('activate', (event) => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(k => ![CACHE_NAME, API_CACHE_NAME].includes(k)).map(k => caches.delete(k)));
    clients.claim();
  })());
});

// IndexedDB helpers
function openOutbox() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(OUTBOX_DB, 1);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(OUTBOX_STORE)) {
        db.createObjectStore(OUTBOX_STORE, { keyPath: 'id', autoIncrement: true });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function queueRequest(req) {
  const db = await openOutbox();
  const tx = db.transaction(OUTBOX_STORE, 'readwrite');
  const store = tx.objectStore(OUTBOX_STORE);
  const body = await req.clone().text();
  const record = {
    url: req.url,
    method: req.method,
    headers: Array.from(req.headers.entries()),
    body,
    ts: Date.now()
  };
  store.add(record);
  await tx.complete;
  db.close();
  if ('sync' in self.registration) {
    try { await self.registration.sync.register('dr-sync'); } catch (e) { /* ignore */ }
  }
}

async function replayOutbox() {
  const db = await openOutbox();
  const tx = db.transaction(OUTBOX_STORE, 'readwrite');
  const store = tx.objectStore(OUTBOX_STORE);
  const all = await new Promise((resolve) => {
    const items = [];
    const cur = store.openCursor();
    cur.onsuccess = (e) => {
      const cursor = e.target.result;
      if (cursor) { items.push({ key: cursor.key, value: cursor.value }); cursor.continue(); } else { resolve(items); }
    };
  });
  for (const { key, value } of all) {
    try {
      const headers = new Headers(value.headers);
      const res = await fetch(value.url, { method: value.method, headers, body: value.body });
      if (res.ok) {
        store.delete(key);
      }
    } catch (e) {
      // still offline
    }
  }
  await tx.complete;
  db.close();
}

self.addEventListener('sync', (event) => {
  if (event.tag === 'dr-sync') {
    event.waitUntil(replayOutbox());
  }
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);
  const isApi = url.pathname.startsWith('/api');
  const isGET = req.method === 'GET';

  if (isApi && isGET) {
    event.respondWith((async () => {
      try {
        const netRes = await fetch(req);
        const apiCache = await caches.open(API_CACHE_NAME);
        apiCache.put(req, netRes.clone());
        return netRes;
      } catch (e) {
        const cached = await caches.match(req);
        if (cached) return cached;
        throw e;
      }
    })());
    return;
  }

  if (isApi && !isGET) {
    event.respondWith((async () => {
      try {
        return await fetch(req);
      } catch (e) {
        await queueRequest(req);
        return new Response(JSON.stringify({ queued: true }), { status: 202, headers: { 'Content-Type': 'application/json' } });
      }
    })());
    return;
  }

  // static assets: cache-first
  event.respondWith((async () => {
    const cached = await caches.match(req);
    if (cached) return cached;
    try {
      const net = await fetch(req);
      const cache = await caches.open(CACHE_NAME);
      cache.put(req, net.clone());
      return net;
    } catch (e) {
      return new Response('Offline', { status: 503 });
    }
  })());
});





