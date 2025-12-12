import { apiClient } from './apiClient';

export interface SatelliteImage {
  id: number;
  imageUrl: string;
  thumbnailUrl?: string;
  provider: string;
  satelliteName?: string;
  capturedAt: string;
  resolutionMeters: number;
  cloudCoverPercentage?: number;
  sunElevationAngle?: number;
  sunAzimuthAngle?: number;
  imageBands?: string;
  metadata?: string;
  processingStatus: string;
  qualityScore?: number;
}

export interface DamageAssessment {
  id: number;
  satelliteImageId: number;
  damageType: string;
  severity: string;
  confidenceScore: number;
  damagePercentage?: number;
  affectedAreaSqm?: number;
  preDisasterImageId?: number;
  changeDetectionScore?: number;
  analysisAlgorithm?: string;
  analysisParameters?: string;
  assessedAt: string;
  assessedBy?: string;
  notes?: string;
}

export interface SatelliteImageStatistics {
  totalImages: number;
  avgResolution: number;
  avgCloudCover: number;
  avgQuality: number;
  earliestCapture: string;
  latestCapture: string;
}

export interface DamageAssessmentStatistics {
  totalAssessments: number;
  avgConfidence: number;
  avgDamagePercentage: number;
  totalAffectedArea: number;
}

export interface SatelliteImageRequest {
  imageUrl: string;
  thumbnailUrl?: string;
  minLon: number;
  minLat: number;
  maxLon: number;
  maxLat: number;
  provider: string;
  satelliteName?: string;
  capturedAt: string;
  resolutionMeters: number;
  cloudCoverPercentage?: number;
  sunElevationAngle?: number;
  sunAzimuthAngle?: number;
  imageBands?: string;
  metadata?: string;
}

export interface DamageAssessmentRequest {
  satelliteImageId: number;
  damageCoordinates: Array<{ longitude: number; latitude: number }>;
  preDisasterImageId?: number;
  analysisAlgorithm?: string;
  analysisParameters?: string;
  assessedBy?: string;
  notes?: string;
}

export class SatelliteService {
  /**
   * Add satellite image
   */
  static async addSatelliteImage(imageData: SatelliteImageRequest): Promise<SatelliteImage> {
    try {
      const response = await apiClient.post('/satellite/images', imageData);
      return response.data;
    } catch (error) {
      console.error('Failed to add satellite image:', error);
      throw error;
    }
  }

  /**
   * Get satellite images within bounds
   */
  static async getImagesInBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<SatelliteImage[]> {
    try {
      const response = await apiClient.get('/satellite/images/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get satellite images:', error);
      return [];
    }
  }

  /**
   * Get satellite images by provider
   */
  static async getImagesByProvider(provider: string): Promise<SatelliteImage[]> {
    try {
      const response = await apiClient.get(`/satellite/images/provider/${provider}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get images by provider:', error);
      return [];
    }
  }

  /**
   * Get most recent satellite image for a point
   */
  static async getMostRecentImage(longitude: number, latitude: number): Promise<SatelliteImage | null> {
    try {
      const response = await apiClient.get('/satellite/images/recent', {
        params: { longitude, latitude }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get most recent image:', error);
      return null;
    }
  }

  /**
   * Get satellite image statistics
   */
  static async getImageStatistics(startDate: string, endDate: string): Promise<SatelliteImageStatistics | null> {
    try {
      const response = await apiClient.get('/satellite/images/statistics', {
        params: { startDate, endDate }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get image statistics:', error);
      return null;
    }
  }

  /**
   * Update processing status
   */
  static async updateProcessingStatus(imageId: number, status: string): Promise<void> {
    try {
      await apiClient.put(`/satellite/images/${imageId}/status`, null, {
        params: { status }
      });
    } catch (error) {
      console.error('Failed to update processing status:', error);
      throw error;
    }
  }

  /**
   * Perform damage assessment
   */
  static async performDamageAssessment(assessmentData: DamageAssessmentRequest): Promise<DamageAssessment> {
    try {
      const response = await apiClient.post('/satellite/damage-assessment', assessmentData);
      return response.data;
    } catch (error) {
      console.error('Failed to perform damage assessment:', error);
      throw error;
    }
  }

  /**
   * Get damage assessments within bounds
   */
  static async getDamageAssessmentsInBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<DamageAssessment[]> {
    try {
      const response = await apiClient.get('/satellite/damage-assessment/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get damage assessments:', error);
      return [];
    }
  }

  /**
   * Get damage assessments by type
   */
  static async getDamageAssessmentsByType(damageType: string): Promise<DamageAssessment[]> {
    try {
      const response = await apiClient.get(`/satellite/damage-assessment/type/${damageType}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get damage assessments by type:', error);
      return [];
    }
  }

  /**
   * Get damage assessment statistics
   */
  static async getDamageStatistics(startDate: string, endDate: string): Promise<DamageAssessmentStatistics | null> {
    try {
      const response = await apiClient.get('/satellite/damage-assessment/statistics', {
        params: { startDate, endDate }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get damage statistics:', error);
      return null;
    }
  }

  /**
   * Perform automated damage detection
   */
  static async performAutomatedDamageDetection(
    satelliteImageId: number, algorithm: string
  ): Promise<DamageAssessment[]> {
    try {
      const response = await apiClient.post('/satellite/damage-assessment/automated', null, {
        params: { satelliteImageId, algorithm }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to perform automated damage detection:', error);
      return [];
    }
  }

  /**
   * Get damage severity color
   */
  static getDamageSeverityColor(severity: string): string {
    switch (severity) {
      case 'MINIMAL': return '#00ff00'; // Green
      case 'LIGHT': return '#ffff00'; // Yellow
      case 'MODERATE': return '#ff8800'; // Orange
      case 'SEVERE': return '#ff4400'; // Red-orange
      case 'CATASTROPHIC': return '#ff0000'; // Red
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get damage type icon
   */
  static getDamageTypeIcon(damageType: string): string {
    switch (damageType) {
      case 'BUILDING_COLLAPSE': return '🏢';
      case 'FLOODING': return '🌊';
      case 'FIRE': return '🔥';
      case 'LANDSLIDE': return '⛰️';
      case 'DEBRIS': return '🗑️';
      case 'INFRASTRUCTURE': return '🛣️';
      case 'VEGETATION': return '🌳';
      case 'EROSION': return '🏔️';
      case 'CONTAMINATION': return '☢️';
      default: return '⚠️';
    }
  }

  /**
   * Get provider color
   */
  static getProviderColor(provider: string): string {
    switch (provider) {
      case 'LANDSAT': return '#2E8B57'; // Sea green
      case 'SENTINEL': return '#4169E1'; // Royal blue
      case 'MODIS': return '#FF6347'; // Tomato
      case 'SPOT': return '#32CD32'; // Lime green
      case 'WORLDVIEW': return '#8A2BE2'; // Blue violet
      case 'PLEIADES': return '#FF1493'; // Deep pink
      case 'KOMPSAT': return '#00CED1'; // Dark turquoise
      case 'PLANET': return '#FFD700'; // Gold
      case 'MAXAR': return '#DC143C'; // Crimson
      default: return '#808080'; // Gray
    }
  }

  /**
   * Format resolution for display
   */
  static formatResolution(resolutionMeters: number): string {
    if (resolutionMeters < 1) {
      return `${(resolutionMeters * 100).toFixed(0)}cm`;
    } else if (resolutionMeters < 1000) {
      return `${resolutionMeters.toFixed(1)}m`;
    } else {
      return `${(resolutionMeters / 1000).toFixed(1)}km`;
    }
  }

  /**
   * Format area for display
   */
  static formatArea(areaSqm: number): string {
    if (areaSqm < 10000) {
      return `${areaSqm.toFixed(0)} m²`;
    } else if (areaSqm < 1000000) {
      return `${(areaSqm / 10000).toFixed(1)} ha`;
    } else {
      return `${(areaSqm / 1000000).toFixed(1)} km²`;
    }
  }

  /**
   * Calculate image quality score color
   */
  static getQualityScoreColor(score: number): string {
    if (score >= 0.8) return '#00ff00'; // Green - excellent
    if (score >= 0.6) return '#ffff00'; // Yellow - good
    if (score >= 0.4) return '#ff8800'; // Orange - fair
    return '#ff0000'; // Red - poor
  }

  /**
   * Get confidence score color
   */
  static getConfidenceScoreColor(score: number): string {
    if (score >= 0.8) return '#00ff00'; // Green - high confidence
    if (score >= 0.6) return '#ffff00'; // Yellow - medium confidence
    if (score >= 0.4) return '#ff8800'; // Orange - low confidence
    return '#ff0000'; // Red - very low confidence
  }

  /**
   * Get processing status color
   */
  static getProcessingStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return '#00ff00'; // Green
      case 'PROCESSING': return '#ffff00'; // Yellow
      case 'PENDING': return '#808080'; // Gray
      case 'FAILED': return '#ff0000'; // Red
      case 'CANCELLED': return '#ff8800'; // Orange
      case 'ARCHIVED': return '#4169E1'; // Blue
      default: return '#808080'; // Gray
    }
  }

  /**
   * Generate satellite image overlay for map
   */
  static generateImageOverlay(image: SatelliteImage): any {
    return {
      type: 'raster',
      source: {
        type: 'raster',
        tiles: [image.imageUrl],
        tileSize: 256
      },
      paint: {
        'raster-opacity': 0.7
      }
    };
  }

  /**
   * Generate damage assessment overlay for map
   */
  static generateDamageOverlay(assessments: DamageAssessment[]): any {
    return {
      type: 'fill',
      source: {
        type: 'geojson',
        data: {
          type: 'FeatureCollection',
          features: assessments.map(assessment => ({
            type: 'Feature',
            geometry: {
              type: 'Polygon',
              coordinates: [[]] // This would be populated with actual coordinates
            },
            properties: {
              id: assessment.id,
              damageType: assessment.damageType,
              severity: assessment.severity,
              confidenceScore: assessment.confidenceScore,
              damagePercentage: assessment.damagePercentage
            }
          }))
        }
      },
      paint: {
        'fill-color': [
          'case',
          ['==', ['get', 'severity'], 'CATASTROPHIC'], '#ff0000',
          ['==', ['get', 'severity'], 'SEVERE'], '#ff4400',
          ['==', ['get', 'severity'], 'MODERATE'], '#ff8800',
          ['==', ['get', 'severity'], 'LIGHT'], '#ffff00',
          '#00ff00' // MINIMAL
        ],
        'fill-opacity': 0.6
      }
    };
  }
}



