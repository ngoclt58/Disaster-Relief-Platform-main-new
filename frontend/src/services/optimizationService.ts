import { apiService } from './api';

// Dynamic Routing
export interface Route {
  id: string;
  originLat: string;
  originLon: string;
  destinations: string[];
  optimization: RouteOptimization;
  estimatedDuration: number;
  estimatedDistance: number;
  priority: number;
  createdAt: string;
  updatedAt?: string;
  reoptimizedAt?: string;
}

export interface RouteOptimization {
  id: string;
  waypoints: Waypoint[];
  totalDistance: number;
  totalDuration: number;
  trafficImpact: number;
  weatherImpact: number;
  priorityAdjustment: number;
  confidence: number;
  optimizedAt: string;
}

export interface Waypoint {
  latitude: string;
  longitude: string;
  type: string;
  order: number;
}

// Smart Inventory
export interface InventoryOptimization {
  itemId: string;
  analyzedAt: string;
  currentStock: number;
  minThreshold: number;
  predictedDemand: number;
  recommendedOrder: number;
  economicOrderQuantity: number;
  reorderPoint: number;
  confidence: number;
  estimatedCost: number;
}

export interface StockOptimization {
  optimizedAt: string;
  recommendations: ReorderRecommendation[];
  totalEstimatedCost: number;
}

export interface ReorderRecommendation {
  itemId: string;
  currentStock: number;
  recommendedQuantity: number;
  priority: string;
  estimatedCost: number;
}

export interface ReorderRule {
  id: string;
  itemId: string;
  ruleType: string;
  parameters: Record<string, any>;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
}

// Resource Allocation
export interface ResourceAllocation {
  id: string;
  allocatedAt: string;
  plans: AllocationPlan[];
  totalAllocated: number;
  allocationEfficiency: number;
  unmetNeeds: string[];
}

export interface AllocationPlan {
  needId: string;
  resourceType: string;
  location: string;
  quantity: number;
  percentageMet: number;
  severity: number;
  priority: string;
}

export interface ResourceNeed {
  id: string;
  resourceType: string;
  quantity: number;
  severity: number;
  urgency: number;
  location: string;
}

// Load Balancing
export interface WorkloadAssignment {
  id: string;
  assignedAt: string;
  taskAssignments: TaskAssignment[];
  workloadDistribution: Record<string, number>;
  balanceScore: number;
  totalTasksAssigned: number;
}

export interface TaskAssignment {
  taskId: string;
  workerId: string;
  taskName: string;
  taskType: string;
  estimatedDuration: number;
  priority: number;
  location: string;
}

export interface Worker {
  id: string;
  name: string;
  skills: string[];
  currentTasks: string[];
  location: string;
}

export interface TaskItem {
  id: string;
  name: string;
  type: string;
  estimatedDuration: number;
  priority: number;
  requiredSkills: string[];
  location: string;
}

class OptimizationService {
  private baseUrl = '/optimization';

  // Dynamic Routing
  async createRoute(originLat: string, originLon: string, destinations: string[], constraints?: Record<string, any>): Promise<Route> {
    return apiService.post(`${this.baseUrl}/routing/routes`, {
      originLat,
      originLon,
      destinations,
      constraints
    });
  }

  async reoptimizeRoute(routeId: string, newConditions: Record<string, any>): Promise<Route> {
    return apiService.post(`${this.baseUrl}/routing/routes/${routeId}/reoptimize`, newConditions);
  }

  async getRoutes(): Promise<Route[]> {
    return apiService.get(`${this.baseUrl}/routing/routes`);
  }

  async getRoute(routeId: string): Promise<Route> {
    return apiService.get(`${this.baseUrl}/routing/routes/${routeId}`);
  }

  // Smart Inventory
  async analyzeInventory(itemId: string, currentStock: number, minThreshold: number, historicalData?: Record<string, any>): Promise<InventoryOptimization> {
    return apiService.post(`${this.baseUrl}/inventory/analyze`, historicalData, {
      params: { itemId, currentStock, minThreshold }
    });
  }

  async optimizeStock(currentStock: Record<string, number>, itemData?: Record<string, Record<string, any>>): Promise<StockOptimization> {
    return apiService.post(`${this.baseUrl}/inventory/optimize`, {
      currentStock,
      itemData
    });
  }

  async createReorderRule(itemId: string, ruleType: string, parameters: Record<string, any>): Promise<ReorderRule> {
    return apiService.post(`${this.baseUrl}/inventory/rules`, parameters, {
      params: { itemId, ruleType }
    });
  }

  async getReorderRules(): Promise<ReorderRule[]> {
    return apiService.get(`${this.baseUrl}/inventory/rules`);
  }

  // Resource Allocation
  async allocateResources(needs: ResourceNeed[], availableResources: Record<string, number>): Promise<ResourceAllocation> {
    return apiService.post(`${this.baseUrl}/allocation/allocate`, {
      needs,
      availableResources
    });
  }

  async getAllocation(allocationId: string): Promise<ResourceAllocation> {
    return apiService.get(`${this.baseUrl}/allocation/allocations/${allocationId}`);
  }

  async getAllocations(): Promise<ResourceAllocation[]> {
    return apiService.get(`${this.baseUrl}/allocation/allocations`);
  }

  // Load Balancing
  async balanceWorkload(availableWorkers: Worker[], pendingTasks: TaskItem[]): Promise<WorkloadAssignment> {
    return apiService.post(`${this.baseUrl}/load-balancing/balance`, {
      availableWorkers,
      pendingTasks
    });
  }

  async getAssignment(assignmentId: string): Promise<WorkloadAssignment> {
    return apiService.get(`${this.baseUrl}/load-balancing/assignments/${assignmentId}`);
  }

  async getAssignments(): Promise<WorkloadAssignment[]> {
    return apiService.get(`${this.baseUrl}/load-balancing/assignments`);
  }
}

export const optimizationService = new OptimizationService();
export default optimizationService;



