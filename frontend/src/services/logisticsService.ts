import { apiService } from './api';

export interface LogisticsItem {
  id: string;
  name: string;
  description: string;
  weight: number;
  length: number;
  width: number;
  height: number;
  quantity: number;
  category: string;
  fragile: boolean;
  hazardous: boolean;
}

export interface LogisticsQuote {
  id: string;
  origin: string;
  destination: string;
  totalCost: number;
  currency: string;
  estimatedDays: number;
  serviceType: string;
  availableProviders: string[];
  validUntil: string;
  breakdown: Record<string, any>;
}

export interface LogisticsShipment {
  id: string;
  quoteId: string;
  providerId: string;
  status: string;
  trackingNumber: string;
  recipientName: string;
  recipientAddress: string;
  recipientPhone: string;
  createdAt: string;
  estimatedDelivery?: string;
  specialInstructions: Record<string, any>;
}

export interface LogisticsTracking {
  shipmentId: string;
  status: string;
  currentLocation: string;
  lastUpdate: string;
  events: TrackingEvent[];
  estimatedDelivery?: string;
}

export interface TrackingEvent {
  timestamp: string;
  location: string;
  status: string;
  description: string;
}

export interface LogisticsProvider {
  id: string;
  name: string;
  type: string;
  serviceTypes: string[];
  regions: string[];
  rating: number;
  contactInfo: string;
  capabilities: Record<string, any>;
}

export interface LogisticsRoute {
  id: string;
  origin: string;
  destination: string;
  waypoints: string[];
  totalDistance: number;
  estimatedTime: number;
  priority: string;
  restrictions: string[];
  metadata: Record<string, any>;
}

export interface LogisticsInventory {
  providerId: string;
  itemType: string;
  availableQuantity: number;
  reservedQuantity: number;
  locations: string[];
  lastUpdated: string;
}

export interface LogisticsDeliverySchedule {
  region: string;
  serviceType: string;
  availableSlots: DeliverySlot[];
  restrictions: Record<string, any>;
}

export interface DeliverySlot {
  startTime: string;
  endTime: string;
  capacity: number;
  status: string;
}

export interface LogisticsAnalytics {
  providerId: string;
  timeRange: string;
  totalShipments: number;
  averageDeliveryTime: number;
  successRate: number;
  performanceMetrics: Record<string, any>;
  generatedAt: string;
}

class LogisticsService {
  private baseUrl = '/integration/logistics';

  async getQuote(origin: string, destination: string, items: LogisticsItem[], serviceType: string): Promise<LogisticsQuote> {
    return apiService.post(`${this.baseUrl}/quotes`, { origin, destination, items, serviceType });
  }

  async createShipment(quoteId: string, recipientName: string, recipientAddress: string, recipientPhone: string, specialInstructions: Record<string, any>): Promise<LogisticsShipment> {
    return apiService.post(`${this.baseUrl}/shipments`, { quoteId, recipientName, recipientAddress, recipientPhone, specialInstructions });
  }

  async getTrackingInfo(shipmentId: string): Promise<LogisticsTracking> {
    return apiService.get(`${this.baseUrl}/shipments/${shipmentId}/tracking`);
  }

  async getProviders(serviceType: string, region: string): Promise<LogisticsProvider[]> {
    return apiService.get(`${this.baseUrl}/providers`, { serviceType, region });
  }

  async getOptimalRoute(origin: string, destination: string, waypoints: string[], priority: string): Promise<LogisticsRoute> {
    return apiService.post(`${this.baseUrl}/routes/optimize`, { origin, destination, waypoints, priority });
  }

  async getInventory(providerId: string, itemType: string): Promise<LogisticsInventory> {
    return apiService.get(`${this.baseUrl}/providers/${providerId}/inventory`, { itemType });
  }

  async getDeliverySchedule(region: string, serviceType: string): Promise<LogisticsDeliverySchedule> {
    return apiService.get(`${this.baseUrl}/delivery-schedule`, { region, serviceType });
  }

  async updateShipmentStatus(shipmentId: string, status: string, notes?: string): Promise<boolean> {
    return apiService.put(`${this.baseUrl}/shipments/${shipmentId}/status`, { status, notes });
  }

  async getAnalytics(providerId: string, timeRange: string): Promise<LogisticsAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, { providerId, timeRange });
  }

  async subscribeToShipmentUpdates(shipmentId: string, callbackUrl: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/subscriptions`, { shipmentId, callbackUrl });
  }
}

export const logisticsService = new LogisticsService();


