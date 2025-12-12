import { apiService } from './api';

export interface GovernmentDisasterData {
  region: string;
  disasterType: string;
  severity: string;
  startTime: string;
  endTime?: string;
  description: string;
  affectedAreas: string[];
  metadata: Record<string, any>;
}

export interface GovernmentAlert {
  id: string;
  region: string;
  alertType: string;
  severity: string;
  message: string;
  issuedAt: string;
  expiresAt?: string;
  source: string;
}

export interface GovernmentResourceData {
  region: string;
  resourceType: string;
  availableQuantity: number;
  totalQuantity: number;
  locations: string[];
  specifications: Record<string, any>;
}

export interface GovernmentEvacuationData {
  region: string;
  routes: EvacuationRoute[];
  assemblyPoints: string[];
  instructions: Record<string, any>;
}

export interface EvacuationRoute {
  id: string;
  name: string;
  fromLocation: string;
  toLocation: string;
  waypoints: string[];
  status: string;
  capacity: number;
}

export interface GovernmentShelterData {
  region: string;
  shelters: Shelter[];
  totalCapacity: number;
  currentOccupancy: number;
}

export interface Shelter {
  id: string;
  name: string;
  address: string;
  capacity: number;
  currentOccupancy: number;
  amenities: string[];
  status: string;
}

export interface GovernmentIncidentReport {
  incidentId: string;
  incidentType: string;
  location: string;
  description: string;
  severity: string;
  reportedAt: string;
  reporterId: string;
  details: Record<string, any>;
}

export interface GovernmentEmergencyContact {
  id: string;
  name: string;
  department: string;
  phone: string;
  email: string;
  region: string;
  role: string;
}

export interface GovernmentComplianceStatus {
  organizationId: string;
  isCompliant: boolean;
  complianceIssues: string[];
  lastChecked: string;
  complianceLevel: string;
}

class GovernmentApiService {
  private baseUrl = '/integration/government';

  async getDisasterData(region: string, disasterType: string): Promise<GovernmentDisasterData> {
    return apiService.get(`${this.baseUrl}/disasters`, { region, disasterType });
  }

  async getActiveAlerts(region: string): Promise<GovernmentAlert[]> {
    return apiService.get(`${this.baseUrl}/alerts`, { region });
  }

  async getResourceAvailability(region: string, resourceType: string): Promise<GovernmentResourceData> {
    return apiService.get(`${this.baseUrl}/resources`, { region, resourceType });
  }

  async getEvacuationRoutes(region: string): Promise<GovernmentEvacuationData> {
    return apiService.get(`${this.baseUrl}/evacuation-routes`, { region });
  }

  async getShelterInformation(region: string): Promise<GovernmentShelterData> {
    return apiService.get(`${this.baseUrl}/shelters`, { region });
  }

  async reportIncident(report: GovernmentIncidentReport): Promise<boolean> {
    return apiService.post(`${this.baseUrl}/incidents`, report);
  }

  async getEmergencyContacts(region: string): Promise<GovernmentEmergencyContact[]> {
    return apiService.get(`${this.baseUrl}/emergency-contacts`, { region });
  }

  async checkCompliance(organizationId: string): Promise<GovernmentComplianceStatus> {
    return apiService.get(`${this.baseUrl}/compliance`, { organizationId });
  }

  async syncData(): Promise<void> {
    return apiService.post(`${this.baseUrl}/sync`);
  }
}

export const governmentApiService = new GovernmentApiService();


