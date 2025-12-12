import { useAuthStore } from '../store/authStore';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface RequestOptions {
  method?: string;
  body?: any;
  params?: Record<string, any>;
  headers?: HeadersInit;
  responseType?: 'json' | 'blob' | 'text';
}

function buildUrl(path: string, params?: Record<string, any>): string {
  const base = `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
  if (!params) return base;
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `${base}?${query}` : base;
}

async function request<T = any>(path: string, options: RequestOptions = {}): Promise<T> {
  const { token } = useAuthStore.getState();
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(buildUrl(path, options.params), {
    method: options.method || 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (!response.ok) {
    const errorText = await response.text().catch(() => '');
    throw new Error(`API Error: ${response.status} ${response.statusText} ${errorText}`);
  }

  // handle no-content
  if (response.status === 204 || !response.headers.get('content-type')) {
    return undefined as T;
  }

  // Handle different response types
  if (options.responseType === 'blob') {
    return (await response.blob()) as T;
  }
  if (options.responseType === 'text') {
    return (await response.text()) as T;
  }

  return (await response.json()) as T;
}

export const apiClient = {
  get: <T = any>(path: string, options: Omit<RequestOptions, 'method' | 'body'> = {}) =>
    request<T>(path, { ...options, method: 'GET' }),
  post: <T = any>(path: string, body?: any, options: Omit<RequestOptions, 'method' | 'body'> = {}) =>
    request<T>(path, { ...options, method: 'POST', body }),
  put: <T = any>(path: string, body?: any, options: Omit<RequestOptions, 'method' | 'body'> = {}) =>
    request<T>(path, { ...options, method: 'PUT', body }),
  delete: <T = any>(path: string, options: Omit<RequestOptions, 'method' | 'body'> = {}) =>
    request<T>(path, { ...options, method: 'DELETE' }),
};


