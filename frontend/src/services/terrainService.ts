import { apiClient } from './apiClient';

export interface ElevationPoint {
  longitude: number;
  latitude: number;
  elevation: number;
  source: string;
  accuracy?: number;
  resolution?: number;
}

export interface ElevationStatistics {
  minElevation: number;
  maxElevation: number;
  avgElevation: number;
  elevationStddev: number;
  pointCount: number;
}

export interface TerrainAnalysis {
  id: number;
  analysisType: string;
  minElevation: number;
  maxElevation: number;
  avgElevation: number;
  elevationVariance: number;
  slopeAverage: number;
  slopeMaximum: number;
  aspectAverage: number;
  roughnessIndex: number;
  accessibilityScore: number;
  floodRiskScore: number;
  analysisData: string;
}

export interface TerrainRoute {
  startPoint: { longitude: number; latitude: number };
  endPoint: { longitude: number; latitude: number };
  segments: RouteSegment[];
  totalDistance: number;
  totalElevationGain: number;
  totalElevationLoss: number;
  maxSlope: number;
  avgSlope: number;
  difficultyScore: number;
  accessibilityScore: number;
  isAccessible: boolean;
}

export interface RouteSegment {
  startPoint: { longitude: number; latitude: number };
  endPoint: { longitude: number; latitude: number };
  distance: number;
  slope: number;
  elevationGain: number;
  elevationLoss: number;
}

export interface TerrainAnalysisRequest {
  coordinates: Array<{ longitude: number; latitude: number }>;
  analysisType: string;
}

export interface TerrainRouteRequest {
  startLongitude: number;
  startLatitude: number;
  endLongitude: number;
  endLatitude: number;
  searchRadius: number;
  maxSlope: number;
  minAccessibilityScore: number;
  waypointOffsetDistance: number;
  maxAlternativeRoutes: number;
}

export class TerrainService {
  /**
   * Get elevation at a specific point
   */
  static async getElevation(longitude: number, latitude: number): Promise<number | null> {
    try {
      const response = await apiClient.get<{ elevation: number }>(`/terrain/elevation`, {
        params: { longitude, latitude }
      });
      return response?.elevation || null;
    } catch (error) {
      console.error('Failed to get elevation:', error);
      return null;
    }
  }

  /**
   * Get elevation points within a bounding box
   */
  static async getElevationPointsInBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<ElevationPoint[]> {
    try {
      const response = await apiClient.get<ElevationPoint[]>('/terrain/elevation/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response || [];
    } catch (error) {
      console.error('Failed to get elevation points:', error);
      return [];
    }
  }

  /**
   * Get elevation statistics for an area
   */
  static async getElevationStatistics(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<ElevationStatistics | null> {
    try {
      const response = await apiClient.get<ElevationStatistics>('/terrain/elevation/statistics', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response || null;
    } catch (error) {
      console.error('Failed to get elevation statistics:', error);
      return null;
    }
  }

  /**
   * Perform terrain analysis for an area
   */
  static async performTerrainAnalysis(
    coordinates: Array<{ longitude: number; latitude: number }>,
    analysisType: string
  ): Promise<TerrainAnalysis | null> {
    try {
      const response = await apiClient.post<TerrainAnalysis>('/terrain/analysis', {
        coordinates,
        analysisType
      });
      return response || null;
    } catch (error) {
      console.error('Failed to perform terrain analysis:', error);
      return null;
    }
  }

  /**
   * Get terrain analysis for a point
   */
  static async getTerrainAnalysisForPoint(
    longitude: number, latitude: number
  ): Promise<TerrainAnalysis | null> {
    try {
      const response = await apiClient.get<TerrainAnalysis>('/terrain/analysis/point', {
        params: { longitude, latitude }
      });
      return response || null;
    } catch (error) {
      console.error('Failed to get terrain analysis:', error);
      return null;
    }
  }

  /**
   * Find accessible areas
   */
  static async findAccessibleAreas(
    minAccessibilityScore: number = 0.7,
    maxSlope: number = 15
  ): Promise<TerrainAnalysis[]> {
    try {
      const response = await apiClient.get<TerrainAnalysis[]>('/terrain/analysis/accessible', {
        params: { minAccessibilityScore, maxSlope }
      });
      return response || [];
    } catch (error) {
      console.error('Failed to find accessible areas:', error);
      return [];
    }
  }

  /**
   * Calculate terrain-aware route
   */
  static async calculateTerrainRoute(
    startLon: number, startLat: number,
    endLon: number, endLat: number,
    options: Partial<TerrainRouteRequest> = {}
  ): Promise<TerrainRoute | null> {
    try {
      const request: TerrainRouteRequest = {
        startLongitude: startLon,
        startLatitude: startLat,
        endLongitude: endLon,
        endLatitude: endLat,
        searchRadius: options.searchRadius || 1000,
        maxSlope: options.maxSlope || 15,
        minAccessibilityScore: options.minAccessibilityScore || 0.7,
        waypointOffsetDistance: options.waypointOffsetDistance || 500,
        maxAlternativeRoutes: options.maxAlternativeRoutes || 3
      };

      const response = await apiClient.post<TerrainRoute>('/terrain/routing', request);
      return response || null;
    } catch (error) {
      console.error('Failed to calculate terrain route:', error);
      return null;
    }
  }

  /**
   * Find alternative routes
   */
  static async findAlternativeRoutes(
    startLon: number, startLat: number,
    endLon: number, endLat: number,
    options: Partial<TerrainRouteRequest> = {}
  ): Promise<TerrainRoute[]> {
    try {
      const request: TerrainRouteRequest = {
        startLongitude: startLon,
        startLatitude: startLat,
        endLongitude: endLon,
        endLatitude: endLat,
        searchRadius: options.searchRadius || 1000,
        maxSlope: options.maxSlope || 15,
        minAccessibilityScore: options.minAccessibilityScore || 0.7,
        waypointOffsetDistance: options.waypointOffsetDistance || 500,
        maxAlternativeRoutes: options.maxAlternativeRoutes || 3
      };

      const response = await apiClient.post<TerrainRoute[]>('/terrain/routing/alternatives', request);
      return response || [];
    } catch (error) {
      console.error('Failed to find alternative routes:', error);
      return [];
    }
  }

  /**
   * Calculate distance between two points
   */
  static calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371000; // Earth's radius in meters
    const lat1Rad = (lat1 * Math.PI) / 180;
    const lat2Rad = (lat2 * Math.PI) / 180;
    const deltaLat = ((lat2 - lat1) * Math.PI) / 180;
    const deltaLon = ((lon2 - lon1) * Math.PI) / 180;

    const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
              Math.cos(lat1Rad) * Math.cos(lat2Rad) *
              Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }

  /**
   * Calculate slope between two points
   */
  static calculateSlope(
    lat1: number, lon1: number, elev1: number,
    lat2: number, lon2: number, elev2: number
  ): number {
    const distance = this.calculateDistance(lat1, lon1, lat2, lon2);
    if (distance === 0) return 0;
    
    const elevationDiff = elev2 - elev1;
    return (Math.atan(elevationDiff / distance) * 180) / Math.PI;
  }

  /**
   * Generate elevation color based on height
   */
  static getElevationColor(elevation: number, minElev: number, maxElev: number): string {
    if (minElev === maxElev) return '#808080';
    
    const normalized = (elevation - minElev) / (maxElev - minElev);
    
    // Color scheme: blue (low) -> green -> yellow -> red (high)
    if (normalized < 0.2) {
      // Blue to cyan
      const intensity = normalized / 0.2;
      return `rgb(${Math.floor(0 * intensity)}, ${Math.floor(100 * intensity)}, ${Math.floor(255 * intensity)})`;
    } else if (normalized < 0.4) {
      // Cyan to green
      const intensity = (normalized - 0.2) / 0.2;
      return `rgb(${Math.floor(0 + 100 * intensity)}, ${Math.floor(100 + 155 * intensity)}, ${Math.floor(255 - 100 * intensity)})`;
    } else if (normalized < 0.6) {
      // Green to yellow
      const intensity = (normalized - 0.4) / 0.2;
      return `rgb(${Math.floor(100 + 155 * intensity)}, ${Math.floor(255)}, ${Math.floor(155 - 155 * intensity)})`;
    } else if (normalized < 0.8) {
      // Yellow to orange
      const intensity = (normalized - 0.6) / 0.2;
      return `rgb(${Math.floor(255)}, ${Math.floor(255 - 100 * intensity)}, ${Math.floor(0)})`;
    } else {
      // Orange to red
      const intensity = (normalized - 0.8) / 0.2;
      return `rgb(${Math.floor(255)}, ${Math.floor(155 - 155 * intensity)}, ${Math.floor(0)})`;
    }
  }

  /**
   * Generate slope color based on steepness
   */
  static getSlopeColor(slope: number): string {
    const absSlope = Math.abs(slope);
    
    if (absSlope < 5) return '#00ff00'; // Green - gentle
    if (absSlope < 15) return '#ffff00'; // Yellow - moderate
    if (absSlope < 30) return '#ff8800'; // Orange - steep
    return '#ff0000'; // Red - very steep
  }

  /**
   * Generate accessibility color based on score
   */
  static getAccessibilityColor(score: number): string {
    if (score >= 0.8) return '#00ff00'; // Green - highly accessible
    if (score >= 0.6) return '#ffff00'; // Yellow - moderately accessible
    if (score >= 0.4) return '#ff8800'; // Orange - limited accessibility
    return '#ff0000'; // Red - not accessible
  }
}



