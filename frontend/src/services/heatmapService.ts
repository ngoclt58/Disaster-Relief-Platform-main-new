import { apiClient } from './apiClient';

export interface HeatmapData {
  id: number;
  longitude: number;
  latitude: number;
  heatmapType: string;
  intensity: number;
  weight: number;
  radius: number;
  category?: string;
  metadata?: string;
  sourceId?: number;
  sourceType?: string;
  createdAt: string;
}

export interface HeatmapLayer {
  id: number;
  name: string;
  description?: string;
  heatmapType: string;
  tileUrlTemplate: string;
  minZoom: number;
  maxZoom: number;
  tileSize: number;
  dataPointsCount: number;
  intensityMin: number;
  intensityMax: number;
  intensityAvg: number;
  configurationId?: number;
  generationParameters?: string;
  fileSizeBytes?: number;
  isPublic: boolean;
  expiresAt?: string;
  createdAt: string;
}

export interface HeatmapStatistics {
  pointCount: number;
  avgIntensity: number;
  minIntensity: number;
  maxIntensity: number;
  intensityStddev: number;
  avgWeight: number;
  avgRadius: number;
}

export interface HeatmapTileData {
  layerId: number;
  minLon: number;
  minLat: number;
  maxLon: number;
  maxLat: number;
  points: HeatmapTilePoint[];
  statistics: HeatmapStatistics;
  heatmapType: string;
}

export interface HeatmapTilePoint {
  longitude: number;
  latitude: number;
  intensity: number;
  weight: number;
  radius: number;
  category?: string;
}

export interface HeatmapDataRequest {
  longitude: number;
  latitude: number;
  heatmapType: string;
  intensity: number;
  weight: number;
  radius: number;
  category?: string;
  metadata?: string;
  sourceId?: number;
  sourceType?: string;
}

export interface HeatmapLayerRequest {
  name: string;
  description?: string;
  heatmapType: string;
  configurationId?: number;
  bounds?: {
    minLon: number;
    minLat: number;
    maxLon: number;
    maxLat: number;
  };
  startDate?: string;
  endDate?: string;
  generationParameters?: string;
  isPublic: boolean;
  expiresAt?: string;
}

export class HeatmapService {
  /**
   * Add heatmap data point
   */
  static async addHeatmapData(data: HeatmapDataRequest): Promise<HeatmapData> {
    try {
      const response = await apiClient.post('/heatmap/data', data);
      return response.data;
    } catch (error) {
      console.error('Failed to add heatmap data:', error);
      throw error;
    }
  }

  /**
   * Bulk add heatmap data points
   */
  static async bulkAddHeatmapData(dataList: HeatmapDataRequest[]): Promise<HeatmapData[]> {
    try {
      const response = await apiClient.post('/heatmap/data/bulk', dataList);
      return response.data;
    } catch (error) {
      console.error('Failed to bulk add heatmap data:', error);
      throw error;
    }
  }

  /**
   * Get heatmap data within bounds
   */
  static async getHeatmapDataInBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<HeatmapData[]> {
    try {
      const response = await apiClient.get('/heatmap/data/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap data in bounds:', error);
      return [];
    }
  }

  /**
   * Get heatmap data by type
   */
  static async getHeatmapDataByType(heatmapType: string): Promise<HeatmapData[]> {
    try {
      const response = await apiClient.get(`/heatmap/data/type/${heatmapType}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap data by type:', error);
      return [];
    }
  }

  /**
   * Get heatmap data by type and bounds
   */
  static async getHeatmapDataByTypeAndBounds(
    heatmapType: string,
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<HeatmapData[]> {
    try {
      const response = await apiClient.get(`/heatmap/data/type/${heatmapType}/bounds`, {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap data by type and bounds:', error);
      return [];
    }
  }

  /**
   * Get heatmap statistics
   */
  static async getHeatmapStatistics(
    heatmapType: string, startDate: string, endDate: string
  ): Promise<HeatmapStatistics | null> {
    try {
      const response = await apiClient.get('/heatmap/data/statistics', {
        params: { type: heatmapType, startDate, endDate }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap statistics:', error);
      return null;
    }
  }

  /**
   * Generate heatmap layer
   */
  static async generateHeatmapLayer(layerData: HeatmapLayerRequest): Promise<HeatmapLayer> {
    try {
      const response = await apiClient.post('/heatmap/layers', layerData);
      return response.data;
    } catch (error) {
      console.error('Failed to generate heatmap layer:', error);
      throw error;
    }
  }

  /**
   * Get heatmap layer
   */
  static async getHeatmapLayer(layerId: number): Promise<HeatmapLayer | null> {
    try {
      const response = await apiClient.get(`/heatmap/layers/${layerId}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap layer:', error);
      return null;
    }
  }

  /**
   * Get heatmap layers by type
   */
  static async getHeatmapLayersByType(heatmapType: string): Promise<HeatmapLayer[]> {
    try {
      const response = await apiClient.get(`/heatmap/layers/type/${heatmapType}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap layers by type:', error);
      return [];
    }
  }

  /**
   * Get public heatmap layers
   */
  static async getPublicHeatmapLayers(): Promise<HeatmapLayer[]> {
    try {
      const response = await apiClient.get('/heatmap/layers/public');
      return response.data;
    } catch (error) {
      console.error('Failed to get public heatmap layers:', error);
      return [];
    }
  }

  /**
   * Get heatmap layers within bounds
   */
  static async getHeatmapLayersInBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<HeatmapLayer[]> {
    try {
      const response = await apiClient.get('/heatmap/layers/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get heatmap layers in bounds:', error);
      return [];
    }
  }

  /**
   * Generate heatmap tiles
   */
  static async generateHeatmapTiles(
    layerId: number,
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<HeatmapTileData | null> {
    try {
      const response = await apiClient.get(`/heatmap/tiles/${layerId}`, {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to generate heatmap tiles:', error);
      return null;
    }
  }

  /**
   * Get heatmap type color
   */
  static getHeatmapTypeColor(heatmapType: string): string {
    switch (heatmapType) {
      case 'DISASTER_IMPACT': return '#ff0000'; // Red
      case 'RESOURCE_DISTRIBUTION': return '#0000ff'; // Blue
      case 'RESPONSE_EFFECTIVENESS': return '#00ff00'; // Green
      case 'NEEDS_DENSITY': return '#ff00ff'; // Magenta
      case 'TASK_CONCENTRATION': return '#ffff00'; // Yellow
      case 'VOLUNTEER_ACTIVITY': return '#00ffff'; // Cyan
      case 'INFRASTRUCTURE_DAMAGE': return '#ff8800'; // Orange
      case 'POPULATION_DENSITY': return '#8800ff'; // Purple
      case 'EVACUATION_ROUTES': return '#ff0088'; // Pink
      case 'EMERGENCY_SERVICES': return '#88ff00'; // Lime
      case 'SUPPLY_CHAINS': return '#0088ff'; // Light Blue
      case 'COMMUNICATION_HUBS': return '#ff8800'; // Orange
      case 'MEDICAL_FACILITIES': return '#ff0000'; // Red
      case 'SHELTER_CAPACITY': return '#00ff88'; // Spring Green
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get heatmap type icon
   */
  static getHeatmapTypeIcon(heatmapType: string): string {
    switch (heatmapType) {
      case 'DISASTER_IMPACT': return '💥';
      case 'RESOURCE_DISTRIBUTION': return '📦';
      case 'RESPONSE_EFFECTIVENESS': return '⚡';
      case 'NEEDS_DENSITY': return '🚨';
      case 'TASK_CONCENTRATION': return '📋';
      case 'VOLUNTEER_ACTIVITY': return '👥';
      case 'INFRASTRUCTURE_DAMAGE': return '🏗️';
      case 'POPULATION_DENSITY': return '👤';
      case 'EVACUATION_ROUTES': return '🚪';
      case 'EMERGENCY_SERVICES': return '🚑';
      case 'SUPPLY_CHAINS': return '🚚';
      case 'COMMUNICATION_HUBS': return '📡';
      case 'MEDICAL_FACILITIES': return '🏥';
      case 'SHELTER_CAPACITY': return '🏠';
      default: return '🔥';
    }
  }

  /**
   * Get intensity color based on value
   */
  static getIntensityColor(intensity: number, minIntensity: number = 0, maxIntensity: number = 1): string {
    if (maxIntensity === minIntensity) return '#808080';
    
    const normalized = (intensity - minIntensity) / (maxIntensity - minIntensity);
    
    // Color scheme: blue (low) -> green -> yellow -> red (high)
    if (normalized < 0.25) {
      const intensity = normalized / 0.25;
      return `rgb(${Math.floor(0 + 0 * intensity)}, ${Math.floor(0 + 100 * intensity)}, ${Math.floor(255 * intensity)})`;
    } else if (normalized < 0.5) {
      const intensity = (normalized - 0.25) / 0.25;
      return `rgb(${Math.floor(0 + 0 * intensity)}, ${Math.floor(100 + 155 * intensity)}, ${Math.floor(255 - 255 * intensity)})`;
    } else if (normalized < 0.75) {
      const intensity = (normalized - 0.5) / 0.25;
      return `rgb(${Math.floor(0 + 255 * intensity)}, ${Math.floor(255)}, ${Math.floor(0)})`;
    } else {
      const intensity = (normalized - 0.75) / 0.25;
      return `rgb(${Math.floor(255)}, ${Math.floor(255 - 255 * intensity)}, ${Math.floor(0)})`;
    }
  }

  /**
   * Generate heatmap source for MapLibre
   */
  static generateHeatmapSource(data: HeatmapData[]): any {
    return {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: data.map(point => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [point.longitude, point.latitude]
          },
          properties: {
            intensity: point.intensity,
            weight: point.weight,
            radius: point.radius,
            category: point.category
          }
        }))
      }
    };
  }

  /**
   * Generate heatmap layer for MapLibre
   */
  static generateHeatmapLayerConfig(heatmapType: string, colorScheme: string = 'default'): any {
    const baseColor = this.getHeatmapTypeColor(heatmapType);
    
    return {
      id: `heatmap-${heatmapType.toLowerCase()}`,
      type: 'heatmap',
      source: `heatmap-${heatmapType.toLowerCase()}-source`,
      paint: {
        'heatmap-weight': ['interpolate', ['linear'], ['get', 'intensity'], 0, 0, 1, 1],
        'heatmap-intensity': ['interpolate', ['linear'], ['zoom'], 0, 1, 9, 3],
        'heatmap-color': [
          'interpolate',
          ['linear'],
          ['heatmap-density'],
          0, 'rgba(0, 0, 255, 0)',
          0.1, 'rgba(0, 0, 255, 0.1)',
          0.3, 'rgba(0, 255, 0, 0.3)',
          0.5, 'rgba(255, 255, 0, 0.5)',
          0.7, 'rgba(255, 165, 0, 0.7)',
          1, 'rgba(255, 0, 0, 0.9)'
        ],
        'heatmap-radius': ['interpolate', ['linear'], ['zoom'], 0, 2, 9, 20],
        'heatmap-opacity': 0.6
      }
    };
  }

  /**
   * Generate circle layer for individual points
   */
  static generateCircleLayer(heatmapType: string): any {
    const baseColor = this.getHeatmapTypeColor(heatmapType);
    
    return {
      id: `circles-${heatmapType.toLowerCase()}`,
      type: 'circle',
      source: `heatmap-${heatmapType.toLowerCase()}-source`,
      paint: {
        'circle-radius': ['interpolate', ['linear'], ['get', 'intensity'], 0, 2, 1, 10],
        'circle-color': [
          'interpolate',
          ['linear'],
          ['get', 'intensity'],
          0, 'rgba(0, 0, 255, 0.5)',
          0.5, 'rgba(255, 255, 0, 0.7)',
          1, 'rgba(255, 0, 0, 0.9)'
        ],
        'circle-opacity': 0.8
      }
    };
  }

  /**
   * Format intensity for display
   */
  static formatIntensity(intensity: number): string {
    return `${(intensity * 100).toFixed(1)}%`;
  }

  /**
   * Format weight for display
   */
  static formatWeight(weight: number): string {
    return weight.toFixed(2);
  }

  /**
   * Format radius for display
   */
  static formatRadius(radius: number): string {
    if (radius < 1000) {
      return `${radius.toFixed(0)}m`;
    } else {
      return `${(radius / 1000).toFixed(1)}km`;
    }
  }

  /**
   * Get heatmap type display name
   */
  static getHeatmapTypeDisplayName(heatmapType: string): string {
    switch (heatmapType) {
      case 'DISASTER_IMPACT': return 'Disaster Impact';
      case 'RESOURCE_DISTRIBUTION': return 'Resource Distribution';
      case 'RESPONSE_EFFECTIVENESS': return 'Response Effectiveness';
      case 'NEEDS_DENSITY': return 'Needs Density';
      case 'TASK_CONCENTRATION': return 'Task Concentration';
      case 'VOLUNTEER_ACTIVITY': return 'Volunteer Activity';
      case 'INFRASTRUCTURE_DAMAGE': return 'Infrastructure Damage';
      case 'POPULATION_DENSITY': return 'Population Density';
      case 'EVACUATION_ROUTES': return 'Evacuation Routes';
      case 'EMERGENCY_SERVICES': return 'Emergency Services';
      case 'SUPPLY_CHAINS': return 'Supply Chains';
      case 'COMMUNICATION_HUBS': return 'Communication Hubs';
      case 'MEDICAL_FACILITIES': return 'Medical Facilities';
      case 'SHELTER_CAPACITY': return 'Shelter Capacity';
      default: return heatmapType.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    }
  }

  /**
   * Get available heatmap types
   */
  static getAvailableHeatmapTypes(): Array<{ value: string; label: string; icon: string; color: string }> {
    return [
      { value: 'DISASTER_IMPACT', label: 'Disaster Impact', icon: '💥', color: '#ff0000' },
      { value: 'RESOURCE_DISTRIBUTION', label: 'Resource Distribution', icon: '📦', color: '#0000ff' },
      { value: 'RESPONSE_EFFECTIVENESS', label: 'Response Effectiveness', icon: '⚡', color: '#00ff00' },
      { value: 'NEEDS_DENSITY', label: 'Needs Density', icon: '🚨', color: '#ff00ff' },
      { value: 'TASK_CONCENTRATION', label: 'Task Concentration', icon: '📋', color: '#ffff00' },
      { value: 'VOLUNTEER_ACTIVITY', label: 'Volunteer Activity', icon: '👥', color: '#00ffff' },
      { value: 'INFRASTRUCTURE_DAMAGE', label: 'Infrastructure Damage', icon: '🏗️', color: '#ff8800' },
      { value: 'POPULATION_DENSITY', label: 'Population Density', icon: '👤', color: '#8800ff' },
      { value: 'EVACUATION_ROUTES', label: 'Evacuation Routes', icon: '🚪', color: '#ff0088' },
      { value: 'EMERGENCY_SERVICES', label: 'Emergency Services', icon: '🚑', color: '#88ff00' },
      { value: 'SUPPLY_CHAINS', label: 'Supply Chains', icon: '🚚', color: '#0088ff' },
      { value: 'COMMUNICATION_HUBS', label: 'Communication Hubs', icon: '📡', color: '#ff8800' },
      { value: 'MEDICAL_FACILITIES', label: 'Medical Facilities', icon: '🏥', color: '#ff0000' },
      { value: 'SHELTER_CAPACITY', label: 'Shelter Capacity', icon: '🏠', color: '#00ff88' }
    ];
  }
}



