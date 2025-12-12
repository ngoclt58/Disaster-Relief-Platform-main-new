import { apiService } from './api';

export interface IoTDevice {
  id: string;
  name: string;
  type: string;
  status: string;
  location: string;
  latitude: number;
  longitude: number;
  capabilities: Record<string, any>;
  lastSeen: string;
  firmwareVersion: string;
}

export interface IoTDeviceData {
  deviceId: string;
  dataType: string;
  startTime: string;
  endTime: string;
  dataPoints: DataPoint[];
  metadata: Record<string, any>;
}

export interface DataPoint {
  timestamp: string;
  value: number;
  unit: string;
  additionalData: Record<string, any>;
}

export interface DroneMission {
  id: string;
  missionType: string;
  area: string;
  status: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  parameters: Record<string, any>;
  waypoints: string[];
  pilotId: string;
}

export interface SensorReading {
  id: string;
  sensorId: string;
  sensorType: string;
  value: number;
  unit: string;
  timestamp: string;
  location: string;
  metadata: Record<string, any>;
}

export interface IoTAlert {
  id: string;
  deviceId: string;
  alertType: string;
  severity: string;
  message: string;
  createdAt: string;
  location: string;
  data: Record<string, any>;
}

export interface IoTDeviceStatus {
  deviceId: string;
  status: string;
  batteryLevel: number;
  signalStrength: string;
  lastUpdate: string;
  diagnostics: Record<string, any>;
}

export interface IoTDeviceAnalytics {
  deviceId: string;
  timeRange: string;
  performanceMetrics: Record<string, any>;
  trends: string[];
  usageStatistics: Record<string, any>;
  generatedAt: string;
}

class IoTService {
  private baseUrl = '/integration/iot';

  async getDevices(deviceType?: string, status?: string): Promise<IoTDevice[]> {
    return apiService.get(`${this.baseUrl}/devices`, { deviceType, status });
  }

  async getDeviceData(deviceId: string, dataType: string, startTime: string, endTime: string): Promise<IoTDeviceData> {
    return apiService.get(`${this.baseUrl}/devices/${deviceId}/data`, { dataType, startTime, endTime });
  }

  async controlDevice(deviceId: string, action: string, parameters: Record<string, any>): Promise<boolean> {
    return apiService.post(`${this.baseUrl}/devices/${deviceId}/control`, { action, parameters });
  }

  async getDroneMissions(status?: string): Promise<DroneMission[]> {
    return apiService.get(`${this.baseUrl}/drones/missions`, { status });
  }

  async createDroneMission(missionType: string, area: string, parameters: Record<string, any>): Promise<DroneMission> {
    return apiService.post(`${this.baseUrl}/drones/missions`, { missionType, area, parameters });
  }

  async getSensorReadings(sensorId: string, sensorType: string, startTime: string, endTime: string): Promise<SensorReading[]> {
    return apiService.get(`${this.baseUrl}/sensors/${sensorId}/readings`, { sensorType, startTime, endTime });
  }

  async getDeviceAlerts(deviceId: string, alertType?: string): Promise<IoTAlert[]> {
    return apiService.get<IoTAlert[]>(`${this.baseUrl}/devices/${deviceId}/alerts`, { alertType });
  }

  async getDeviceStatus(deviceId: string): Promise<IoTDeviceStatus> {
    return apiService.get(`${this.baseUrl}/devices/${deviceId}/status`);
  }

  async getDeviceAnalytics(deviceId: string, timeRange: string): Promise<IoTDeviceAnalytics> {
    return apiService.get(`${this.baseUrl}/devices/${deviceId}/analytics`, { timeRange });
  }

  async subscribeToDeviceEvents(deviceId: string, eventType: string, callbackUrl: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/subscriptions`, { deviceId, eventType, callbackUrl });
  }
}

export const iotService = new IoTService();


