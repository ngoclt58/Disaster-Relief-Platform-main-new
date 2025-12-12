import React, { useState } from 'react';
import { MapIcon, PackageIcon, TargetIcon, BarChart3Icon, RefreshCw } from 'lucide-react';
import { optimizationService } from '../services/optimizationService';

const OptimizationDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'routing' | 'inventory' | 'allocation' | 'load-balancing'>('overview');
  
  // Mock data for demonstration
  const mockRoutes = [
    { id: '1', destinations: 3, distance: 45.2, duration: 120, priority: 85 },
    { id: '2', destinations: 5, distance: 78.5, duration: 180, priority: 92 },
  ];

  const mockInventory = [
    { itemId: 'item1', currentStock: 45, minThreshold: 50, recommendedOrder: 25, priority: 'HIGH' },
    { itemId: 'item2', currentStock: 120, minThreshold: 50, recommendedOrder: 0, priority: 'LOW' },
  ];

  const mockAllocations = [
    { id: 'alloc1', totalAllocated: 150, efficiency: 87.5, unmetNeeds: 2 },
    { id: 'alloc2', totalAllocated: 230, efficiency: 92.0, unmetNeeds: 0 },
  ];

  const mockLoadBalancing = [
    { assignmentId: 'lb1', tasksAssigned: 12, balanceScore: 85.5 },
    { assignmentId: 'lb2', tasksAssigned: 8, balanceScore: 91.2 },
  ];

  return (
    <div className="min-h-full">
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">
            Intelligent Resource Optimization
          </h1>
          <p className="mt-2 text-sm text-gray-600">
            AI-powered routing, inventory, allocation, and load balancing
          </p>
        </div>
      </header>

      <main>
        <div className="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'overview', name: 'Overview', icon: BarChart3Icon },
                { id: 'routing', name: 'Dynamic Routing', icon: MapIcon },
                { id: 'inventory', name: 'Smart Inventory', icon: PackageIcon },
                { id: 'allocation', name: 'Resource Allocation', icon: TargetIcon },
                { id: 'load-balancing', name: 'Load Balancing', icon: RefreshCw }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
                >
                  <tab.icon className="h-4 w-4 mr-2" />
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>

          <div className="mt-6">
            {activeTab === 'overview' && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <MapIcon className="h-6 w-6 text-blue-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Active Routes</p>
                        <p className="text-2xl font-semibold text-gray-900">{mockRoutes.length}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <PackageIcon className="h-6 w-6 text-green-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Reorder Alerts</p>
                        <p className="text-2xl font-semibold text-gray-900">{mockInventory.filter(i => i.recommendedOrder > 0).length}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <TargetIcon className="h-6 w-6 text-yellow-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Avg Efficiency</p>
                        <p className="text-2xl font-semibold text-gray-900">
                          {(mockAllocations.reduce((sum, a) => sum + a.efficiency, 0) / mockAllocations.length).toFixed(1)}%
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <RefreshCw className="h-6 w-6 text-purple-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Balance Score</p>
                        <p className="text-2xl font-semibold text-gray-900">
                          {(mockLoadBalancing.reduce((sum, lb) => sum + lb.balanceScore, 0) / mockLoadBalancing.length).toFixed(1)}%
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'routing' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Dynamic Routing</h2>
                <div className="space-y-4">
                  {mockRoutes.map(route => (
                    <div key={route.id} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">Route {route.id}</h3>
                        <span className="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">
                          {route.destinations} destinations
                        </span>
                      </div>
                      <div className="mt-2 flex gap-4 text-sm">
                        <span>Distance: {route.distance} km</span>
                        <span>Duration: {route.duration} min</span>
                        <span>Priority: {route.priority}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'inventory' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Smart Inventory Management</h2>
                <div className="space-y-4">
                  {mockInventory.map(item => (
                    <div key={item.itemId} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">{item.itemId}</h3>
                        <span className={`px-2 py-1 rounded text-xs ${
                          item.priority === 'HIGH' ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                        }`}>
                          {item.priority}
                        </span>
                      </div>
                      <div className="mt-2 flex gap-4 text-sm">
                        <span>Stock: {item.currentStock}/{item.minThreshold}</span>
                        {item.recommendedOrder > 0 && (
                          <span className="text-red-600 font-medium">Recommended: {item.recommendedOrder}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'allocation' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Resource Allocation</h2>
                <div className="space-y-4">
                  {mockAllocations.map(alloc => (
                    <div key={alloc.id} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">{alloc.id}</h3>
                        <span className="text-lg font-bold">{alloc.totalAllocated} units</span>
                      </div>
                      <div className="mt-2 flex gap-4 text-sm">
                        <span>Efficiency: {alloc.efficiency.toFixed(1)}%</span>
                        {alloc.unmetNeeds > 0 && (
                          <span className="text-red-600">Unmet needs: {alloc.unmetNeeds}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'load-balancing' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Load Balancing</h2>
                <div className="space-y-4">
                  {mockLoadBalancing.map(lb => (
                    <div key={lb.assignmentId} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">{lb.assignmentId}</h3>
                        <div className="flex items-center gap-2">
                          <div className="w-32 bg-gray-200 rounded-full h-2">
                            <div 
                              className="bg-green-600 h-2 rounded-full"
                              style={{ width: `${lb.balanceScore}%` }}
                            ></div>
                          </div>
                          <span className="font-bold">{lb.balanceScore.toFixed(1)}</span>
                        </div>
                      </div>
                      <div className="mt-2 text-sm text-gray-600">
                        Tasks assigned: {lb.tasksAssigned}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default OptimizationDashboard;

