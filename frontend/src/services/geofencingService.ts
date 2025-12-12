import { apiClient } from './apiClient';

export interface Geofence {
  id: number;
  name: string;
  description?: string;
  geofenceType: string;
  priority: string;
  isActive: boolean;
  bufferDistanceMeters?: number;
  checkIntervalSeconds: number;
  alertThreshold: number;
  cooldownPeriodSeconds: number;
  notificationChannels?: string;
  autoActions?: string;
  metadata?: string;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
  lastCheckedAt?: string;
  lastAlertAt?: string;
}

export interface GeofenceEvent {
  id: number;
  geofenceId: number;
  eventType: string;
  longitude: number;
  latitude: number;
  entityType: string;
  entityId?: number;
  entityName?: string;
  eventData?: string;
  severity: string;
  confidenceScore?: number;
  occurredAt: string;
  detectedAt: string;
  processedAt?: string;
  isProcessed: boolean;
  processingNotes?: string;
}

export interface GeofenceAlert {
  id: number;
  geofenceId: number;
  alertType: string;
  title: string;
  message: string;
  severity: string;
  status: string;
  triggeredByEventId?: number;
  alertData?: string;
  notificationChannels?: string;
  autoActionsTriggered?: string;
  assignedTo?: string;
  createdAt: string;
  acknowledgedAt?: string;
  acknowledgedBy?: string;
  resolvedAt?: string;
  resolvedBy?: string;
  resolutionNotes?: string;
  escalatedAt?: string;
  escalatedTo?: string;
  escalationReason?: string;
}

export interface GeofenceRequest {
  name: string;
  description?: string;
  boundaryCoordinates: Array<{ longitude: number; latitude: number }>;
  geofenceType: string;
  priority: string;
  isActive: boolean;
  bufferDistanceMeters?: number;
  checkIntervalSeconds?: number;
  alertThreshold?: number;
  cooldownPeriodSeconds?: number;
  notificationChannels?: string;
  autoActions?: string;
  metadata?: string;
  createdBy: string;
}

export interface GeofenceCheckRequest {
  longitude: number;
  latitude: number;
  entityType: string;
  entityId?: number;
  entityName?: string;
}

export interface GeofenceCheckResult {
  isInGeofence: boolean;
  geofenceId?: number;
  geofenceName?: string;
  eventId?: number;
  eventType?: string;
}

export class GeofencingService {
  /**
   * Create a new geofence
   */
  static async createGeofence(geofenceData: GeofenceRequest): Promise<Geofence> {
    try {
      const response = await apiClient.post('/geofencing/geofences', geofenceData);
      return response.data;
    } catch (error) {
      console.error('Failed to create geofence:', error);
      throw error;
    }
  }

  /**
   * Update an existing geofence
   */
  static async updateGeofence(geofenceId: number, geofenceData: GeofenceRequest): Promise<Geofence> {
    try {
      const response = await apiClient.put(`/geofencing/geofences/${geofenceId}`, geofenceData);
      return response.data;
    } catch (error) {
      console.error('Failed to update geofence:', error);
      throw error;
    }
  }

  /**
   * Delete a geofence
   */
  static async deleteGeofence(geofenceId: number): Promise<void> {
    try {
      await apiClient.delete(`/geofencing/geofences/${geofenceId}`);
    } catch (error) {
      console.error('Failed to delete geofence:', error);
      throw error;
    }
  }

  /**
   * Get geofence by ID
   */
  static async getGeofence(geofenceId: number): Promise<Geofence | null> {
    try {
      const response = await apiClient.get(`/geofencing/geofences/${geofenceId}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get geofence:', error);
      return null;
    }
  }

  /**
   * Get all geofences
   */
  static async getAllGeofences(): Promise<Geofence[]> {
    try {
      const response = await apiClient.get('/geofencing/geofences');
      return response.data;
    } catch (error) {
      console.error('Failed to get geofences:', error);
      return [];
    }
  }

  /**
   * Get active geofences
   */
  static async getActiveGeofences(): Promise<Geofence[]> {
    try {
      const response = await apiClient.get('/geofencing/geofences/active');
      const data = response?.data;
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Failed to get active geofences:', error);
      return [];
    }
  }

  /**
   * Get geofences by type
   */
  static async getGeofencesByType(geofenceType: string): Promise<Geofence[]> {
    try {
      const response = await apiClient.get(`/geofencing/geofences/type/${geofenceType}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get geofences by type:', error);
      return [];
    }
  }

  /**
   * Get geofences within bounds
   */
  static async getGeofencesWithinBounds(
    minLon: number, minLat: number, maxLon: number, maxLat: number
  ): Promise<Geofence[]> {
    try {
      const response = await apiClient.get('/geofencing/geofences/bounds', {
        params: { minLon, minLat, maxLon, maxLat }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get geofences within bounds:', error);
      return [];
    }
  }

  /**
   * Check if a point is within any geofence
   */
  static async checkPointInGeofences(checkData: GeofenceCheckRequest): Promise<GeofenceCheckResult> {
    try {
      const response = await apiClient.post('/geofencing/check', checkData);
      return response.data;
    } catch (error) {
      console.error('Failed to check point in geofences:', error);
      return { isInGeofence: false };
    }
  }

  /**
   * Process geofence events
   */
  static async processGeofenceEvents(): Promise<void> {
    try {
      await apiClient.post('/geofencing/process-events');
    } catch (error) {
      console.error('Failed to process geofence events:', error);
      throw error;
    }
  }

  /**
   * Get geofence events
   */
  static async getGeofenceEvents(geofenceId: number): Promise<GeofenceEvent[]> {
    try {
      const response = await apiClient.get(`/geofencing/geofences/${geofenceId}/events`);
      const data = response?.data;
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Failed to get geofence events:', error);
      return [];
    }
  }

  /**
   * Get geofence alerts
   */
  static async getGeofenceAlerts(geofenceId: number): Promise<GeofenceAlert[]> {
    try {
      const response = await apiClient.get(`/geofencing/geofences/${geofenceId}/alerts`);
      return response.data;
    } catch (error) {
      console.error('Failed to get geofence alerts:', error);
      return [];
    }
  }

  /**
   * Get active alerts
   */
  static async getActiveAlerts(): Promise<GeofenceAlert[]> {
    try {
      const response = await apiClient.get('/geofencing/alerts/active');
      const data = response?.data;
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Failed to get active alerts:', error);
      return [];
    }
  }

  /**
   * Acknowledge an alert
   */
  static async acknowledgeAlert(alertId: number, acknowledgedBy: string): Promise<void> {
    try {
      await apiClient.post(`/geofencing/alerts/${alertId}/acknowledge`, null, {
        params: { acknowledgedBy }
      });
    } catch (error) {
      console.error('Failed to acknowledge alert:', error);
      throw error;
    }
  }

  /**
   * Resolve an alert
   */
  static async resolveAlert(alertId: number, resolvedBy: string, resolutionNotes?: string): Promise<void> {
    try {
      await apiClient.post(`/geofencing/alerts/${alertId}/resolve`, null, {
        params: { resolvedBy, resolutionNotes }
      });
    } catch (error) {
      console.error('Failed to resolve alert:', error);
      throw error;
    }
  }

  /**
   * Get geofence type color
   */
  static getGeofenceTypeColor(geofenceType: string): string {
    switch (geofenceType) {
      case 'DISASTER_ZONE': return '#ff0000'; // Red
      case 'EVACUATION_ZONE': return '#ff8800'; // Orange
      case 'RESTRICTED_ZONE': return '#ff0000'; // Red
      case 'RESOURCE_DEPOT': return '#0000ff'; // Blue
      case 'EMERGENCY_SHELTER': return '#00ff00'; // Green
      case 'MEDICAL_FACILITY': return '#ff00ff'; // Magenta
      case 'COMMUNICATION_HUB': return '#ffff00'; // Yellow
      case 'SUPPLY_ROUTE': return '#00ffff'; // Cyan
      case 'INFRASTRUCTURE': return '#8800ff'; // Purple
      case 'POPULATION_DENSITY': return '#ff0088'; // Pink
      case 'VULNERABLE_AREA': return '#ff4400'; // Red-orange
      case 'RESPONSE_BASE': return '#88ff00'; // Lime
      case 'CHECKPOINT': return '#0088ff'; // Light blue
      case 'QUARANTINE_ZONE': return '#ff0000'; // Red
      case 'RECOVERY_ZONE': return '#00ff88'; // Spring green
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get geofence type icon
   */
  static getGeofenceTypeIcon(geofenceType: string): string {
    switch (geofenceType) {
      case 'DISASTER_ZONE': return '💥';
      case 'EVACUATION_ZONE': return '🚨';
      case 'RESTRICTED_ZONE': return '🚫';
      case 'RESOURCE_DEPOT': return '📦';
      case 'EMERGENCY_SHELTER': return '🏠';
      case 'MEDICAL_FACILITY': return '🏥';
      case 'COMMUNICATION_HUB': return '📡';
      case 'SUPPLY_ROUTE': return '🛣️';
      case 'INFRASTRUCTURE': return '🏗️';
      case 'POPULATION_DENSITY': return '👥';
      case 'VULNERABLE_AREA': return '⚠️';
      case 'RESPONSE_BASE': return '🏕️';
      case 'CHECKPOINT': return '🔍';
      case 'QUARANTINE_ZONE': return '🔒';
      case 'RECOVERY_ZONE': return '🔄';
      default: return '📍';
    }
  }

  /**
   * Get priority color
   */
  static getPriorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return '#ff0000'; // Red
      case 'HIGH': return '#ff8800'; // Orange
      case 'MEDIUM': return '#ffff00'; // Yellow
      case 'LOW': return '#00ff00'; // Green
      case 'INFO': return '#0000ff'; // Blue
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get severity color
   */
  static getSeverityColor(severity: string): string {
    switch (severity) {
      case 'CRITICAL': return '#ff0000'; // Red
      case 'HIGH': return '#ff4400'; // Red-orange
      case 'MEDIUM': return '#ff8800'; // Orange
      case 'LOW': return '#ffff00'; // Yellow
      case 'INFO': return '#00ff00'; // Green
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get status color
   */
  static getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return '#ff0000'; // Red
      case 'ACKNOWLEDGED': return '#ff8800'; // Orange
      case 'IN_PROGRESS': return '#ffff00'; // Yellow
      case 'RESOLVED': return '#00ff00'; // Green
      case 'ESCALATED': return '#ff00ff'; // Magenta
      case 'CANCELLED': return '#808080'; // Gray
      case 'EXPIRED': return '#404040'; // Dark gray
      default: return '#808080'; // Gray
    }
  }

  /**
   * Get event type icon
   */
  static getEventTypeIcon(eventType: string): string {
    switch (eventType) {
      case 'ENTRY': return '➡️';
      case 'EXIT': return '⬅️';
      case 'DWELL': return '⏱️';
      case 'PROXIMITY': return '📍';
      case 'VIOLATION': return '⚠️';
      case 'EMERGENCY': return '🚨';
      case 'RESOURCE_DEPLETION': return '📉';
      case 'CAPACITY_EXCEEDED': return '📊';
      case 'MAINTENANCE_REQUIRED': return '🔧';
      case 'STATUS_CHANGE': return '🔄';
      default: return '📋';
    }
  }

  /**
   * Get alert type icon
   */
  static getAlertTypeIcon(alertType: string): string {
    switch (alertType) {
      case 'BOUNDARY_VIOLATION': return '🚫';
      case 'CAPACITY_EXCEEDED': return '📊';
      case 'RESOURCE_DEPLETION': return '📉';
      case 'EMERGENCY_DETECTED': return '🚨';
      case 'UNAUTHORIZED_ACCESS': return '🔒';
      case 'MAINTENANCE_REQUIRED': return '🔧';
      case 'STATUS_CHANGE': return '🔄';
      case 'THRESHOLD_EXCEEDED': return '📈';
      case 'TIME_BASED_ALERT': return '⏰';
      default: return '⚠️';
    }
  }

  /**
   * Format confidence score for display
   */
  static formatConfidenceScore(score: number): string {
    return `${(score * 100).toFixed(1)}%`;
  }

  /**
   * Format time duration for display
   */
  static formatTimeDuration(seconds: number): string {
    if (seconds < 60) {
      return `${seconds}s`;
    } else if (seconds < 3600) {
      return `${Math.floor(seconds / 60)}m`;
    } else if (seconds < 86400) {
      return `${Math.floor(seconds / 3600)}h`;
    } else {
      return `${Math.floor(seconds / 86400)}d`;
    }
  }

  /**
   * Get geofence type display name
   */
  static getGeofenceTypeDisplayName(geofenceType: string): string {
    switch (geofenceType) {
      case 'DISASTER_ZONE': return 'Disaster Zone';
      case 'EVACUATION_ZONE': return 'Evacuation Zone';
      case 'RESTRICTED_ZONE': return 'Restricted Zone';
      case 'RESOURCE_DEPOT': return 'Resource Depot';
      case 'EMERGENCY_SHELTER': return 'Emergency Shelter';
      case 'MEDICAL_FACILITY': return 'Medical Facility';
      case 'COMMUNICATION_HUB': return 'Communication Hub';
      case 'SUPPLY_ROUTE': return 'Supply Route';
      case 'INFRASTRUCTURE': return 'Infrastructure';
      case 'POPULATION_DENSITY': return 'Population Density';
      case 'VULNERABLE_AREA': return 'Vulnerable Area';
      case 'RESPONSE_BASE': return 'Response Base';
      case 'CHECKPOINT': return 'Checkpoint';
      case 'QUARANTINE_ZONE': return 'Quarantine Zone';
      case 'RECOVERY_ZONE': return 'Recovery Zone';
      default: return geofenceType.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    }
  }

  /**
   * Get available geofence types
   */
  static getAvailableGeofenceTypes(): Array<{ value: string; label: string; icon: string; color: string }> {
    return [
      { value: 'DISASTER_ZONE', label: 'Disaster Zone', icon: '💥', color: '#ff0000' },
      { value: 'EVACUATION_ZONE', label: 'Evacuation Zone', icon: '🚨', color: '#ff8800' },
      { value: 'RESTRICTED_ZONE', label: 'Restricted Zone', icon: '🚫', color: '#ff0000' },
      { value: 'RESOURCE_DEPOT', label: 'Resource Depot', icon: '📦', color: '#0000ff' },
      { value: 'EMERGENCY_SHELTER', label: 'Emergency Shelter', icon: '🏠', color: '#00ff00' },
      { value: 'MEDICAL_FACILITY', label: 'Medical Facility', icon: '🏥', color: '#ff00ff' },
      { value: 'COMMUNICATION_HUB', label: 'Communication Hub', icon: '📡', color: '#ffff00' },
      { value: 'SUPPLY_ROUTE', label: 'Supply Route', icon: '🛣️', color: '#00ffff' },
      { value: 'INFRASTRUCTURE', label: 'Infrastructure', icon: '🏗️', color: '#8800ff' },
      { value: 'POPULATION_DENSITY', label: 'Population Density', icon: '👥', color: '#ff0088' },
      { value: 'VULNERABLE_AREA', label: 'Vulnerable Area', icon: '⚠️', color: '#ff4400' },
      { value: 'RESPONSE_BASE', label: 'Response Base', icon: '🏕️', color: '#88ff00' },
      { value: 'CHECKPOINT', label: 'Checkpoint', icon: '🔍', color: '#0088ff' },
      { value: 'QUARANTINE_ZONE', label: 'Quarantine Zone', icon: '🔒', color: '#ff0000' },
      { value: 'RECOVERY_ZONE', label: 'Recovery Zone', icon: '🔄', color: '#00ff88' }
    ];
  }

  /**
   * Get available priorities
   */
  static getAvailablePriorities(): Array<{ value: string; label: string; color: string }> {
    return [
      { value: 'CRITICAL', label: 'Critical', color: '#ff0000' },
      { value: 'HIGH', label: 'High', color: '#ff8800' },
      { value: 'MEDIUM', label: 'Medium', color: '#ffff00' },
      { value: 'LOW', label: 'Low', color: '#00ff00' },
      { value: 'INFO', label: 'Info', color: '#0000ff' }
    ];
  }

  /**
   * Generate geofence source for MapLibre
   */
  static generateGeofenceSource(geofences: Geofence[]): any {
    return {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: geofences.map(geofence => ({
          type: 'Feature',
          geometry: {
            type: 'Polygon',
            coordinates: [[]] // This would be populated with actual boundary coordinates
          },
          properties: {
            id: geofence.id,
            name: geofence.name,
            geofenceType: geofence.geofenceType,
            priority: geofence.priority,
            isActive: geofence.isActive,
            bufferDistanceMeters: geofence.bufferDistanceMeters
          }
        }))
      }
    };
  }

  /**
   * Generate geofence layer for MapLibre
   */
  static generateGeofenceLayer(geofenceType: string): any {
    const baseColor = this.getGeofenceTypeColor(geofenceType);
    
    return {
      id: `geofence-${geofenceType.toLowerCase()}`,
      type: 'fill',
      source: `geofence-${geofenceType.toLowerCase()}-source`,
      paint: {
        'fill-color': baseColor,
        'fill-opacity': 0.3
      }
    };
  }

  /**
   * Generate geofence border layer for MapLibre
   */
  static generateGeofenceBorderLayer(geofenceType: string): any {
    const baseColor = this.getGeofenceTypeColor(geofenceType);
    
    return {
      id: `geofence-border-${geofenceType.toLowerCase()}`,
      type: 'line',
      source: `geofence-${geofenceType.toLowerCase()}-source`,
      paint: {
        'line-color': baseColor,
        'line-width': 2,
        'line-opacity': 0.8
      }
    };
  }
}



