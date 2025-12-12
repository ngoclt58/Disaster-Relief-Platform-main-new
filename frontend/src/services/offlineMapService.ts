import { apiClient } from './apiClient';

export interface OfflineMapCache {
  id: number;
  name: string;
  description?: string;
  regionId: string;
  regionName: string;
  bounds: {
    type: 'Polygon';
    coordinates: number[][][];
  };
  zoomLevels: number[];
  mapType: string;
  tileSource: string;
  tileFormat: string;
  status: string;
  priority: string;
  totalTiles: number;
  downloadedTiles: number;
  cacheSizeBytes: number;
  estimatedSizeBytes?: number;
  downloadProgress: number;
  downloadStartedAt?: string;
  downloadCompletedAt?: string;
  lastAccessedAt?: string;
  expiresAt?: string;
  isCompressed: boolean;
  compressionRatio?: number;
  metadata?: any;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
}

export interface OfflineMapTile {
  id: number;
  offlineMapCacheId: number;
  z: number;
  x: number;
  y: number;
  tileKey: string;
  tileUrl: string;
  filePath?: string;
  fileSizeBytes?: number;
  status: string;
  downloadAttempts: number;
  lastDownloadAttempt?: string;
  lastAccessedAt?: string;
  checksum?: string;
  isCompressed: boolean;
  compressionRatio?: number;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface OfflineMapDownload {
  id: number;
  offlineMapCacheId: number;
  downloadId: string;
  status: string;
  totalTiles: number;
  downloadedTiles: number;
  failedTiles: number;
  progressPercentage: number;
  downloadSpeedBytesPerSec?: number;
  estimatedCompletionTime?: string;
  startedAt: string;
  completedAt?: string;
  errorMessage?: string;
  retryCount: number;
  maxRetries: number;
  downloadConfig?: any;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface OfflineMapCacheRequest {
  name: string;
  description?: string;
  regionId: string;
  regionName: string;
  boundsCoordinates: Array<{ longitude: number; latitude: number }>;
  zoomLevels: number[];
  mapType: string;
  tileSource: string;
  tileFormat?: string;
  priority?: string;
  estimatedSizeBytes?: number;
  isCompressed?: boolean;
  metadata?: any;
  createdBy: string;
  autoStart?: boolean;
}

export interface OfflineMapCacheStatistics {
  totalCaches: number;
  completedCaches: number;
  downloadingCaches: number;
  failedCaches: number;
  pendingCaches: number;
  totalSizeBytes: number;
  avgDownloadProgress: number;
}

export interface OfflineMapCacheRegionStatistics {
  regionId: string;
  regionName: string;
  cacheCount: number;
  totalSizeBytes: number;
  avgDownloadProgress: number;
}

export class OfflineMapService {
  // Offline Map Caches
  static async createOfflineMapCache(request: OfflineMapCacheRequest): Promise<OfflineMapCache> {
    const response = await apiClient.post('/offline-maps/caches', request);
    return response;
  }

  static async getOfflineMapCache(cacheId: number): Promise<OfflineMapCache> {
    const response = await apiClient.get(`/offline-maps/caches/${cacheId}`);
    return response;
  }

  static async getAllOfflineMapCaches(params?: {
    regionId?: string;
    status?: string;
    mapType?: string;
    priority?: string;
  }): Promise<OfflineMapCache[]> {
    const response = await apiClient.get('/offline-maps/caches', { params });
    return response;
  }

  static async getCachesWithinBounds(
    minLon: number,
    minLat: number,
    maxLon: number,
    maxLat: number
  ): Promise<OfflineMapCache[]> {
    const response = await apiClient.get('/offline-maps/caches/within-bounds', {
      params: { minLon, minLat, maxLon, maxLat }
    });
    return response;
  }

  static async getCachesContainingPoint(
    longitude: number,
    latitude: number
  ): Promise<OfflineMapCache[]> {
    const response = await apiClient.get('/offline-maps/caches/containing-point', {
      params: { longitude, latitude }
    });
    return response;
  }

  // Cache Management
  static async startCacheDownload(cacheId: number): Promise<void> {
    await apiClient.post(`/offline-maps/caches/${cacheId}/start-download`);
  }

  static async pauseCacheDownload(cacheId: number): Promise<void> {
    await apiClient.post(`/offline-maps/caches/${cacheId}/pause-download`);
  }

  static async resumeCacheDownload(cacheId: number): Promise<void> {
    await apiClient.post(`/offline-maps/caches/${cacheId}/resume-download`);
  }

  static async deleteOfflineMapCache(cacheId: number): Promise<void> {
    await apiClient.delete(`/offline-maps/caches/${cacheId}`);
  }

  static async cleanupExpiredCaches(): Promise<void> {
    await apiClient.post('/offline-maps/caches/cleanup');
  }

  // Statistics
  static async getCacheStatistics(cacheId: number): Promise<any> {
    const response = await apiClient.get(`/offline-maps/caches/${cacheId}/statistics`);
    return response;
  }

  static async getGlobalStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<OfflineMapCacheStatistics> {
    const response = await apiClient.get('/offline-maps/statistics', { params });
    return response;
  }

  static async getRegionalStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<OfflineMapCacheRegionStatistics[]> {
    const response = await apiClient.get('/offline-maps/statistics/regions', { params });
    return response;
  }

  // Tiles
  static async getCacheTiles(
    cacheId: number,
    params?: {
      zoomLevel?: number;
      status?: string;
      page?: number;
      size?: number;
    }
  ): Promise<OfflineMapTile[]> {
    const response = await apiClient.get(`/offline-maps/tiles/${cacheId}`, { params });
    return response;
  }

  static async downloadTile(
    cacheId: number,
    z: number,
    x: number,
    y: number
  ): Promise<Blob> {
    const response = await apiClient.get<Blob>(`/offline-maps/tiles/${cacheId}/download`, {
      params: { z, x, y },
      responseType: 'blob'
    });
    return response;
  }

  // Utility methods
  static formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  static formatDownloadProgress(progress: number): string {
    return (progress * 100).toFixed(1) + '%';
  }

  static getStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'text-green-600 bg-green-100';
      case 'DOWNLOADING':
        return 'text-blue-600 bg-blue-100';
      case 'FAILED':
        return 'text-red-600 bg-red-100';
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'PAUSED':
        return 'text-orange-600 bg-orange-100';
      case 'EXPIRED':
        return 'text-gray-600 bg-gray-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  static getPriorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL':
        return 'text-red-600 bg-red-100';
      case 'HIGH':
        return 'text-orange-600 bg-orange-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'LOW':
        return 'text-green-600 bg-green-100';
      case 'BACKGROUND':
        return 'text-gray-600 bg-gray-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  static getMapTypeIcon(mapType: string): string {
    switch (mapType) {
      case 'SATELLITE':
        return '🛰️';
      case 'STREET_MAP':
        return '🗺️';
      case 'TERRAIN':
        return '🏔️';
      case 'HYBRID':
        return '🔀';
      case 'TOPOGRAPHIC':
        return '📊';
      case 'AERIAL':
        return '✈️';
      case 'NIGHT':
        return '🌙';
      case 'TRAFFIC':
        return '🚦';
      case 'WEATHER':
        return '🌤️';
      case 'DISASTER_OVERLAY':
        return '⚠️';
      default:
        return '🗺️';
    }
  }
}



