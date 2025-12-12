import { apiClient } from './apiClient';

export interface LocationHistory {
  id: number;
  entityType: string;
  entityId: number;
  entityName?: string;
  position: {
    type: 'Point';
    coordinates: [number, number];
  };
  latitude: number;
  longitude: number;
  altitude?: number;
  heading?: number;
  speed: number;
  accuracy: number;
  activityType: string;
  activityDescription?: string;
  timestamp: string;
  durationSeconds?: number;
  distanceFromPrevious?: number;
  isStationary: boolean;
  isSignificant: boolean;
  locationContext?: any;
  environmentalConditions?: any;
  metadata?: any;
  createdAt: string;
}

export interface LocationPattern {
  id: number;
  locationHistoryId?: number;
  entityType: string;
  entityId: number;
  patternType: string;
  patternName: string;
  patternDescription?: string;
  patternGeometry?: {
    type: 'Geometry';
    coordinates: any;
  };
  startTime: string;
  endTime: string;
  durationSeconds: number;
  distanceMeters: number;
  averageSpeed: number;
  maxSpeed: number;
  confidenceScore: number;
  frequency: number;
  isRecurring: boolean;
  isOptimal: boolean;
  optimizationSuggestions?: any;
  patternCharacteristics?: any;
  environmentalFactors?: any;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface LocationOptimization {
  id: number;
  locationPatternId: number;
  optimizationType: string;
  optimizationName: string;
  description: string;
  suggestedRoute?: {
    type: 'LineString';
    coordinates: [number, number][];
  };
  currentEfficiency: number;
  projectedEfficiency: number;
  timeSavingsSeconds?: number;
  distanceSavingsMeters?: number;
  resourceSavings?: any;
  priority: string;
  status: string;
  implementationDifficulty: string;
  estimatedImplementationTime?: number;
  costBenefitRatio?: number;
  riskLevel: string;
  affectedEntities?: any;
  implementationSteps?: any;
  successMetrics?: any;
  monitoringRequirements?: any;
  isImplemented: boolean;
  implementationDate?: string;
  implementationNotes?: string;
  actualEfficiencyGain?: number;
  feedback?: string;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface LocationHistoryRequest {
  entityType: string;
  entityId: number;
  entityName?: string;
  latitude: number;
  longitude: number;
  altitude?: number;
  heading?: number;
  speed: number;
  accuracy: number;
  activityType: string;
  activityDescription?: string;
  timestamp: string;
  durationSeconds?: number;
  locationContext?: any;
  environmentalConditions?: any;
  metadata?: any;
}

export interface LocationHistoryStatistics {
  totalLocations: number;
  stationaryLocations: number;
  significantLocations: number;
  avgSpeed: number;
  maxSpeed: number;
  avgAccuracy: number;
  uniqueEntityTypes: number;
  uniqueEntities: number;
}

export interface LocationPatternStatistics {
  totalPatterns: number;
  linearPatterns: number;
  circularPatterns: number;
  stationaryPatterns: number;
  routePatterns: number;
  searchPatterns: number;
  recurringPatterns: number;
  optimalPatterns: number;
  avgConfidence: number;
  avgSpeed: number;
  avgDistance: number;
}

export interface LocationOptimizationStatistics {
  totalOptimizations: number;
  pendingOptimizations: number;
  approvedOptimizations: number;
  inProgressOptimizations: number;
  completedOptimizations: number;
  implementedOptimizations: number;
  avgCurrentEfficiency: number;
  avgProjectedEfficiency: number;
  avgActualEfficiencyGain: number;
  totalTimeSavings: number;
  totalDistanceSavings: number;
}

export class LocationAnalyticsService {
  // Location History
  static async recordLocationHistory(request: LocationHistoryRequest): Promise<LocationHistory> {
    const response = await apiClient.post('/location-analytics/history', request);
    return response;
  }

  static async getLocationHistory(params?: {
    entityType?: string;
    entityId?: number;
    activityType?: string;
    startTime?: string;
    endTime?: string;
    stationaryOnly?: boolean;
    significantOnly?: boolean;
    page?: number;
    size?: number;
  }): Promise<LocationHistory[]> {
    const response = await apiClient.get('/location-analytics/history', { params });
    return response;
  }

  static async getLocationHistoryWithinBounds(
    minLon: number,
    minLat: number,
    maxLon: number,
    maxLat: number,
    startTime?: string,
    endTime?: string
  ): Promise<LocationHistory[]> {
    const response = await apiClient.get('/location-analytics/history/within-bounds', {
      params: { minLon, minLat, maxLon, maxLat, startTime, endTime }
    });
    return response;
  }

  static async getLocationHistoryNearPoint(
    longitude: number,
    latitude: number,
    radius: number = 1000,
    startTime?: string,
    endTime?: string
  ): Promise<LocationHistory[]> {
    const response = await apiClient.get('/location-analytics/history/near-point', {
      params: { longitude, latitude, radius, startTime, endTime }
    });
    return response;
  }

  // Location Patterns
  static async getLocationPatterns(params?: {
    entityType?: string;
    entityId?: number;
    patternType?: string;
    recurringOnly?: boolean;
    optimalOnly?: boolean;
    minConfidence?: number;
  }): Promise<LocationPattern[]> {
    const response = await apiClient.get('/location-analytics/patterns', { params });
    return response;
  }

  static async analyzePatternsForEntity(entityType: string, entityId: number): Promise<void> {
    await apiClient.post('/location-analytics/patterns/analyze', null, {
      params: { entityType, entityId }
    });
  }

  // Location Optimizations
  static async getLocationOptimizations(params?: {
    patternId?: number;
    optimizationType?: string;
    priority?: string;
    status?: string;
    implementedOnly?: boolean;
    highPriorityOnly?: boolean;
  }): Promise<LocationOptimization[]> {
    const response = await apiClient.get('/location-analytics/optimizations', { params });
    return response;
  }

  static async implementOptimization(
    optimizationId: number,
    notes?: string,
    actualEfficiencyGain?: number
  ): Promise<void> {
    await apiClient.post(`/location-analytics/optimizations/${optimizationId}/implement`, null, {
      params: { notes, actualEfficiencyGain }
    });
  }

  // Statistics
  static async getLocationHistoryStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<LocationHistoryStatistics> {
    const response = await apiClient.get('/location-analytics/statistics/history', { params });
    return response;
  }

  static async getPatternStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<LocationPatternStatistics> {
    const response = await apiClient.get('/location-analytics/statistics/patterns', { params });
    return response;
  }

  static async getOptimizationStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<LocationOptimizationStatistics> {
    const response = await apiClient.get('/location-analytics/statistics/optimizations', { params });
    return response;
  }

  static async getActivityTypeStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<any[]> {
    const response = await apiClient.get('/location-analytics/statistics/activity-types', { params });
    return response;
  }

  static async getEntityMovementStatistics(params?: {
    startDate?: string;
    endDate?: string;
  }): Promise<any[]> {
    const response = await apiClient.get('/location-analytics/statistics/entity-movement', { params });
    return response;
  }

  // Utility methods
  static formatSpeed(speed: number): string {
    if (speed < 1) {
      return `${(speed * 1000).toFixed(1)} mm/s`;
    } else if (speed < 1000) {
      return `${speed.toFixed(1)} m/s`;
    } else {
      return `${(speed / 1000).toFixed(1)} km/s`;
    }
  }

  static formatDistance(distance: number): string {
    if (distance < 1000) {
      return `${distance.toFixed(1)} m`;
    } else {
      return `${(distance / 1000).toFixed(1)} km`;
    }
  }

  static formatDuration(seconds: number): string {
    if (seconds < 60) {
      return `${seconds}s`;
    } else if (seconds < 3600) {
      return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
    } else {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      return `${hours}h ${minutes}m`;
    }
  }

  static formatEfficiency(efficiency: number): string {
    return `${(efficiency * 100).toFixed(1)}%`;
  }

  static getActivityTypeIcon(activityType: string): string {
    switch (activityType) {
      case 'WALKING':
        return '🚶';
      case 'RUNNING':
        return '🏃';
      case 'DRIVING':
        return '🚗';
      case 'FLYING':
        return '✈️';
      case 'BOATING':
        return '🚤';
      case 'CYCLING':
        return '🚴';
      case 'STATIONARY':
        return '⏸️';
      case 'SEARCH_AND_RESCUE':
        return '🔍';
      case 'MEDICAL_TREATMENT':
        return '🏥';
      case 'SUPPLY_DELIVERY':
        return '📦';
      case 'EVACUATION':
        return '🚨';
      case 'DAMAGE_ASSESSMENT':
        return '🔍';
      case 'EMERGENCY_RESPONSE':
        return '🚑';
      case 'FIRE_FIGHTING':
        return '🚒';
      case 'FLOOD_RESPONSE':
        return '🌊';
      case 'EARTHQUAKE_RESPONSE':
        return '🌍';
      case 'HURRICANE_RESPONSE':
        return '🌀';
      case 'TORNADO_RESPONSE':
        return '🌪️';
      default:
        return '📍';
    }
  }

  static getPatternTypeIcon(patternType: string): string {
    switch (patternType) {
      case 'LINEAR_MOVEMENT':
        return '➡️';
      case 'CIRCULAR_MOVEMENT':
        return '🔄';
      case 'RANDOM_MOVEMENT':
        return '🎲';
      case 'GRID_PATTERN':
        return '⬜';
      case 'SPIRAL_PATTERN':
        return '🌀';
      case 'ZIGZAG_PATTERN':
        return '⚡';
      case 'BACK_AND_FORTH':
        return '↔️';
      case 'STATIONARY_CLUSTER':
        return '⏸️';
      case 'WAITING_PATTERN':
        return '⏳';
      case 'WORK_STATION':
        return '🏢';
      case 'REST_AREA':
        return '🛋️';
      case 'COMMUTE_ROUTE':
        return '🛣️';
      case 'SUPPLY_ROUTE':
        return '📦';
      case 'PATROL_ROUTE':
        return '🚔';
      case 'EMERGENCY_ROUTE':
        return '🚨';
      case 'EVACUATION_ROUTE':
        return '🚪';
      case 'SEARCH_GRID':
        return '🔍';
      case 'SEARCH_SPIRAL':
        return '🌀';
      case 'EMERGENCY_RESPONSE':
        return '🚑';
      case 'RESCUE_OPERATION':
        return '🆘';
      case 'MEDICAL_RESPONSE':
        return '🏥';
      case 'FIRE_RESPONSE':
        return '🚒';
      case 'FLOOD_RESPONSE':
        return '🌊';
      case 'ANOMALY_DETECTED':
        return '⚠️';
      case 'DEVIATION_FROM_NORM':
        return '📈';
      case 'UNUSUAL_ACTIVITY':
        return '❓';
      case 'SUSPICIOUS_MOVEMENT':
        return '👁️';
      case 'OPTIMIZED_ROUTE':
        return '✅';
      case 'EFFICIENT_MOVEMENT':
        return '⚡';
      case 'TIME_OPTIMIZED':
        return '⏰';
      case 'DISTANCE_OPTIMIZED':
        return '📏';
      case 'RESOURCE_OPTIMIZED':
        return '💡';
      default:
        return '📊';
    }
  }

  static getOptimizationTypeIcon(optimizationType: string): string {
    switch (optimizationType) {
      case 'ROUTE_OPTIMIZATION':
        return '🛣️';
      case 'SHORTEST_PATH':
        return '📏';
      case 'FASTEST_PATH':
        return '⚡';
      case 'FUEL_EFFICIENT_ROUTE':
        return '⛽';
      case 'TIME_OPTIMIZED_ROUTE':
        return '⏰';
      case 'DISTANCE_OPTIMIZED_ROUTE':
        return '📐';
      case 'RESOURCE_ALLOCATION':
        return '📦';
      case 'PERSONNEL_DEPLOYMENT':
        return '👥';
      case 'EQUIPMENT_PLACEMENT':
        return '🔧';
      case 'SUPPLY_CHAIN_OPTIMIZATION':
        return '🚚';
      case 'INVENTORY_OPTIMIZATION':
        return '📋';
      case 'AREA_COVERAGE':
        return '🗺️';
      case 'SEARCH_PATTERN_OPTIMIZATION':
        return '🔍';
      case 'PATROL_ROUTE_OPTIMIZATION':
        return '🚔';
      case 'MONITORING_OPTIMIZATION':
        return '👁️';
      case 'EMERGENCY_RESPONSE_TIME':
        return '🚨';
      case 'RESCUE_OPERATION_EFFICIENCY':
        return '🆘';
      case 'MEDICAL_RESPONSE_OPTIMIZATION':
        return '🏥';
      case 'EVACUATION_OPTIMIZATION':
        return '🚪';
      case 'COMMUNICATION_NETWORK':
        return '📡';
      case 'RELAY_STATION_PLACEMENT':
        return '📻';
      case 'COORDINATION_POINT_OPTIMIZATION':
        return '🎯';
      case 'WEATHER_AVOIDANCE':
        return '🌤️';
      case 'TERRAIN_OPTIMIZATION':
        return '🏔️';
      case 'OBSTACLE_AVOIDANCE':
        return '🚧';
      case 'ACCESSIBILITY_OPTIMIZATION':
        return '♿';
      case 'WORKFLOW_OPTIMIZATION':
        return '⚙️';
      case 'PROCESS_OPTIMIZATION':
        return '🔄';
      case 'TASK_SEQUENCING':
        return '📝';
      case 'SCHEDULE_OPTIMIZATION':
        return '📅';
      case 'SAFETY_OPTIMIZATION':
        return '🛡️';
      case 'RISK_REDUCTION':
        return '⚠️';
      case 'HAZARD_AVOIDANCE':
        return '🚫';
      case 'EMERGENCY_PREPAREDNESS':
        return '🚨';
      case 'COST_REDUCTION':
        return '💰';
      case 'BUDGET_OPTIMIZATION':
        return '💳';
      case 'RESOURCE_EFFICIENCY':
        return '♻️';
      case 'WASTE_REDUCTION':
        return '♻️';
      case 'PERFORMANCE_IMPROVEMENT':
        return '📈';
      case 'THROUGHPUT_OPTIMIZATION':
        return '⚡';
      case 'LATENCY_REDUCTION':
        return '⏱️';
      case 'CAPACITY_OPTIMIZATION':
        return '📊';
      default:
        return '🔧';
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

  static getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'APPROVED':
        return 'text-blue-600 bg-blue-100';
      case 'IN_PROGRESS':
        return 'text-purple-600 bg-purple-100';
      case 'COMPLETED':
        return 'text-green-600 bg-green-100';
      case 'IMPLEMENTED':
        return 'text-green-600 bg-green-100';
      case 'REJECTED':
        return 'text-red-600 bg-red-100';
      case 'CANCELLED':
        return 'text-gray-600 bg-gray-100';
      case 'ON_HOLD':
        return 'text-orange-600 bg-orange-100';
      case 'NEEDS_REVISION':
        return 'text-yellow-600 bg-yellow-100';
      case 'FAILED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  static getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'VERY_EASY':
        return 'text-green-600 bg-green-100';
      case 'EASY':
        return 'text-green-600 bg-green-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'HARD':
        return 'text-orange-600 bg-orange-100';
      case 'VERY_HARD':
        return 'text-red-600 bg-red-100';
      case 'EXTREMELY_HARD':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  static getRiskColor(risk: string): string {
    switch (risk) {
      case 'VERY_LOW':
        return 'text-green-600 bg-green-100';
      case 'LOW':
        return 'text-green-600 bg-green-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'HIGH':
        return 'text-orange-600 bg-orange-100';
      case 'VERY_HIGH':
        return 'text-red-600 bg-red-100';
      case 'CRITICAL':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }
}



