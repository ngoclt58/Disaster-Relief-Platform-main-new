export type StoreName = 'requests' | 'tasks' | 'inventory';

const DB_NAME = 'drp-cache-db';
const DB_VERSION = 1;

export function openDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains('requests')) db.createObjectStore('requests', { keyPath: 'id' });
      if (!db.objectStoreNames.contains('tasks')) db.createObjectStore('tasks', { keyPath: 'id' });
      if (!db.objectStoreNames.contains('inventory')) db.createObjectStore('inventory', { keyPath: 'id' });
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export async function putAll<T extends { id: string }>(store: StoreName, items: T[]) {
  const db = await openDb();
  const tx = db.transaction(store, 'readwrite');
  const s = tx.objectStore(store);
  for (const item of items) {
    s.put(item);
  }
  await new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

export async function getAll<T>(store: StoreName): Promise<T[]> {
  const db = await openDb();
  const tx = db.transaction(store, 'readonly');
  const s = tx.objectStore(store);
  const result: T[] = await new Promise((resolve, reject) => {
    const items: T[] = [] as any;
    const cur = s.openCursor();
    cur.onsuccess = (e: any) => {
      const cursor = e.target.result;
      if (cursor) { items.push(cursor.value); cursor.continue(); } else { resolve(items); }
    };
    cur.onerror = () => reject(cur.error);
    tx.onerror = () => reject(tx.error);
  });
  await new Promise<void>((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
  return result;
}





