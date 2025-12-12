import { apiClient } from './apiClient';

export interface IndoorMap {
  id: number;
  name: string;
  description?: string;
  facilityId: number;
  facilityName: string;
  floorNumber: number;
  floorName?: string;
  mapType: string;
  coordinateSystem: string;
  scaleFactor: number;
  mapImageUrl?: string;
  mapData?: any;
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
}

export interface IndoorNode {
  id: number;
  indoorMapId: number;
  nodeId: string;
  name?: string;
  description?: string;
  position: {
    type: 'Point';
    coordinates: [number, number];
  };
  localX: number;
  localY: number;
  nodeType: string;
  isAccessible: boolean;
  accessibilityFeatures?: any;
  capacity?: number;
  currentOccupancy: number;
  isEmergencyExit: boolean;
  isElevator: boolean;
  isStairs: boolean;
  floorLevel?: number;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface IndoorEdge {
  id: number;
  indoorMapId: number;
  fromNodeId: number;
  toNodeId: number;
  edgeId: string;
  name?: string;
  description?: string;
  path?: {
    type: 'LineString';
    coordinates: [number, number][];
  };
  edgeType: string;
  isAccessible: boolean;
  isBidirectional: boolean;
  distance: number;
  width?: number;
  height?: number;
  weight: number;
  maxSpeed?: number;
  accessibilityFeatures?: any;
  isEmergencyRoute: boolean;
  isRestricted: boolean;
  restrictionType?: string;
  metadata?: any;
  createdAt: string;
  updatedAt?: string;
}

export interface IndoorPosition {
  id: number;
  indoorMapId: number;
  entityType: string;
  entityId: number;
  entityName?: string;
  position: {
    type: 'Point';
    coordinates: [number, number];
  };
  localX: number;
  localY: number;
  floorLevel: number;
  heading?: number;
  speed?: number;
  accuracy: number;
  positioningMethod: string;
  timestamp: string;
  isValid: boolean;
  metadata?: any;
  createdAt: string;
}

export interface IndoorRoute {
  id: number;
  indoorMapId: number;
  fromNodeId: number;
  toNodeId: number;
  routeId: string;
  name?: string;
  description?: string;
  path: {
    type: 'LineString';
    coordinates: [number, number][];
  };
  routeType: string;
  totalDistance: number;
  estimatedTime: number;
  difficultyLevel: string;
  isAccessible: boolean;
  isEmergencyRoute: boolean;
  isRestricted: boolean;
  accessLevel?: string;
  waypoints?: any;
  instructions?: any;
  metadata?: any;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
}

export interface IndoorMapRequest {
  name: string;
  description?: string;
  facilityId: number;
  facilityName: string;
  floorNumber: number;
  floorName?: string;
  boundsCoordinates: Array<{ longitude: number; latitude: number }>;
  mapType: string;
  coordinateSystem?: string;
  scaleFactor?: number;
  mapImageUrl?: string;
  mapData?: any;
  isActive?: boolean;
  createdBy: string;
}

export interface IndoorNodeRequest {
  indoorMapId: number;
  nodeId: string;
  name?: string;
  description?: string;
  longitude: number;
  latitude: number;
  localX: number;
  localY: number;
  nodeType: string;
  isAccessible?: boolean;
  accessibilityFeatures?: any;
  capacity?: number;
  currentOccupancy?: number;
  isEmergencyExit?: boolean;
  isElevator?: boolean;
  isStairs?: boolean;
  floorLevel?: number;
  metadata?: any;
}

export interface IndoorEdgeRequest {
  indoorMapId: number;
  edgeId: string;
  name?: string;
  description?: string;
  fromNodeId: number;
  toNodeId: number;
  pathCoordinates?: Array<{ longitude: number; latitude: number }>;
  edgeType: string;
  isAccessible?: boolean;
  isBidirectional?: boolean;
  distance: number;
  width?: number;
  height?: number;
  weight?: number;
  maxSpeed?: number;
  accessibilityFeatures?: any;
  isEmergencyRoute?: boolean;
  isRestricted?: boolean;
  restrictionType?: string;
  metadata?: any;
}

export interface IndoorPositionRequest {
  indoorMapId: number;
  entityType: string;
  entityId: number;
  entityName?: string;
  longitude: number;
  latitude: number;
  localX: number;
  localY: number;
  floorLevel: number;
  heading?: number;
  speed?: number;
  accuracy: number;
  positioningMethod: string;
  timestamp?: string;
  isValid?: boolean;
  metadata?: any;
}

export class IndoorNavigationService {
  // Indoor Maps
  static async createIndoorMap(request: IndoorMapRequest): Promise<IndoorMap> {
    const response = await apiClient.post('/indoor/maps', request);
    return response;
  }

  static async updateIndoorMap(mapId: number, request: IndoorMapRequest): Promise<IndoorMap> {
    const response = await apiClient.put(`/indoor/maps/${mapId}`, request);
    return response;
  }

  static async getIndoorMap(mapId: number): Promise<IndoorMap> {
    const response = await apiClient.get(`/indoor/maps/${mapId}`);
    return response;
  }

  static async getAllIndoorMaps(params?: {
    facilityId?: number;
    mapType?: string;
    floorNumber?: number;
    activeOnly?: boolean;
  }): Promise<IndoorMap[]> {
    try {
      const response = await apiClient.get<IndoorMap[]>('/indoor/maps', { params });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get indoor maps:', error);
      return [];
    }
  }

  // Indoor Nodes
  static async createIndoorNode(mapId: number, request: IndoorNodeRequest): Promise<IndoorNode> {
    const response = await apiClient.post(`/indoor/maps/${mapId}/nodes`, request);
    return response;
  }

  static async getIndoorNodes(mapId: number, params?: {
    nodeType?: string;
    floorLevel?: number;
    accessibleOnly?: boolean;
  }): Promise<IndoorNode[]> {
    try {
      const response = await apiClient.get<IndoorNode[]>(`/indoor/maps/${mapId}/nodes`, { params });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get indoor nodes:', error);
      return [];
    }
  }

  // Indoor Edges
  static async createIndoorEdge(mapId: number, request: IndoorEdgeRequest): Promise<IndoorEdge> {
    const response = await apiClient.post(`/indoor/maps/${mapId}/edges`, request);
    return response;
  }

  static async getIndoorEdges(mapId: number, params?: {
    edgeType?: string;
    accessibleOnly?: boolean;
    bidirectionalOnly?: boolean;
  }): Promise<IndoorEdge[]> {
    try {
      const response = await apiClient.get<IndoorEdge[]>(`/indoor/maps/${mapId}/edges`, { params });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get indoor edges:', error);
      return [];
    }
  }

  // Indoor Positioning
  static async recordIndoorPosition(request: IndoorPositionRequest): Promise<IndoorPosition> {
    const response = await apiClient.post('/indoor/positions', request);
    return response;
  }

  static async getIndoorPositions(params?: {
    entityType?: string;
    entityId?: number;
    mapId?: number;
    floorLevel?: number;
    positioningMethod?: string;
  }): Promise<IndoorPosition[]> {
    try {
      const response = await apiClient.get<IndoorPosition[]>('/indoor/positions', { params });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get indoor positions:', error);
      return [];
    }
  }

  static async getLatestPosition(entityType: string, entityId: number): Promise<IndoorPosition> {
    const response = await apiClient.get('/indoor/positions/latest', {
      params: { entityType, entityId }
    });
    return response;
  }

  // Indoor Routing
  static async calculateRoute(
    mapId: number,
    fromNodeId: number,
    toNodeId: number,
    routeType: string = 'SHORTEST_PATH',
    createdBy: string
  ): Promise<IndoorRoute> {
    const response = await apiClient.post(`/indoor/maps/${mapId}/routes/calculate`, null, {
      params: { fromNodeId, toNodeId, routeType, createdBy }
    });
    return response;
  }

  static async getIndoorRoutes(mapId: number, params?: {
    routeType?: string;
    accessibleOnly?: boolean;
    emergencyOnly?: boolean;
  }): Promise<IndoorRoute[]> {
    try {
      const response = await apiClient.get<IndoorRoute[]>(`/indoor/maps/${mapId}/routes`, { params });
      return Array.isArray(response) ? response : [];
    } catch (error) {
      console.error('Failed to get indoor routes:', error);
      return [];
    }
  }

  static async findNearestNode(
    mapId: number,
    longitude: number,
    latitude: number,
    radius: number = 100
  ): Promise<IndoorNode> {
    const response = await apiClient.get(`/indoor/maps/${mapId}/nodes/nearest`, {
      params: { longitude, latitude, radius }
    });
    return response;
  }

  // Statistics
  static async getMapStatistics(mapId: number): Promise<any> {
    const response = await apiClient.get(`/indoor/maps/${mapId}/statistics`);
    return response;
  }

  static async getNodeStatistics(mapId: number): Promise<any> {
    const response = await apiClient.get(`/indoor/maps/${mapId}/nodes/statistics`);
    return response;
  }

  static async getEdgeStatistics(mapId: number): Promise<any> {
    const response = await apiClient.get(`/indoor/maps/${mapId}/edges/statistics`);
    return response;
  }

  static async getPositionStatistics(
    mapId: number,
    startDate?: string,
    endDate?: string
  ): Promise<any> {
    const response = await apiClient.get(`/indoor/maps/${mapId}/positions/statistics`, {
      params: { startDate, endDate }
    });
    return response;
  }
}



