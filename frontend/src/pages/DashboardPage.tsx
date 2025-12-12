import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { apiService } from '../services/api';

import NeedsForm from '../components/NeedsForm';
import { 
  MapPin, 
  Users, 
  AlertTriangle, 
  CheckCircle, 
  Clock,
  TrendingUp,
  Activity,
  Plus
} from 'lucide-react';
import StockAlerts from '../components/StockAlerts';

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [stats, setStats] = useState({
    activeRequests: 0,
    completedToday: 0,
    activeHelpers: 0,
    responseTime: '0h'
  });
  const [recentRequests, setRecentRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(false); // Changed to false to prevent initial blocking
  const [showNeedsForm, setShowNeedsForm] = useState(false);

  useEffect(() => {
    console.log('[DashboardPage] Component mounted, calling fetchDashboardData');
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    console.log('[DashboardPage] fetchDashboardData called!');
    console.log('[DashboardPage] Current loading state:', loading);
    
    // Prevent overlapping calls
    if (loading) {
      console.warn('Dashboard data fetch already in progress, skipping duplicate call');
      return;
    }
    
    try {
      console.log('[DashboardPage] Setting loading to true');
      setLoading(true);
      
      // Fetch dashboard stats and recent requests in parallel
      const [statsData, requestsData] = await Promise.all([
        apiService.get<{
          activeRequests: number;
          completedToday: number;
          activeHelpers: number;
          responseTime: string;
        }>('/dashboard/stats').catch(() => ({
          activeRequests: 0,
          completedToday: 0,
          activeHelpers: 0,
          responseTime: '0h'
        })),
        apiService.getRequests({ page: 0, size: 5 }).then((data) => {
          console.log('[DashboardPage] Successfully fetched requests:', data);
          return data;
        }).catch((err) => {
          console.error('[DashboardPage] Failed to fetch recent requests:', err);
          return { content: [], totalElements: 0 };
        })
      ]);
      
      setRecentRequests(requestsData.content || []);
      
      // Use real stats from backend
      setStats({
        activeRequests: statsData.activeRequests || 0,
        completedToday: statsData.completedToday || 0,
        activeHelpers: statsData.activeHelpers || 0,
        responseTime: statsData.responseTime || '0h'
      });
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
      // Set default values on error
      setStats({
        activeRequests: 0,
        completedToday: 0,
        activeHelpers: 0,
        responseTime: '0h'
      });
    } finally {
      console.log('[DashboardPage] Setting loading to false');
      setLoading(false);
    }
  };

  const getSeverityColor = (severity: number) => {
    switch (severity) {
      case 5: return 'bg-red-100 text-red-800';
      case 4: return 'bg-orange-100 text-orange-800';
      case 3: return 'bg-yellow-100 text-yellow-800';
      case 2: return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const updateRequestStatus = async (requestId: string, newStatus: string) => {
    try {
      console.log(`[DashboardPage] Updating request ${requestId} status to ${newStatus}`);
      
      // Call API to update status
      await apiService.patch(`/requests/${requestId}/status`, { status: newStatus });
      
      console.log(`[DashboardPage] Successfully updated request ${requestId} status to ${newStatus}`);
      
      // Refresh the dashboard data to show updated status
      await fetchDashboardData();
      
    } catch (error) {
      console.error(`[DashboardPage] Failed to update request ${requestId} status:`, error);
      // You could add a toast notification here for better UX
      alert(`Failed to update request status: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  };

  const handleFindShelter = async () => {
    try {
      // Try to get user's current location
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async (position) => {
            const { latitude, longitude } = position.coords;
            // Navigate to map with shelter filter
            navigate(`/map?shelter=true&lat=${latitude}&lng=${longitude}`);
          },
          () => {
            // If location access denied, just navigate to map
            navigate('/map?shelter=true');
          }
        );
      } else {
        // Geolocation not supported, navigate to map
        navigate('/map?shelter=true');
      }
    } catch (error) {
      console.error('Error finding shelter:', error);
      navigate('/map?shelter=true');
    }
  };

  const statsData = [
    { name: 'Active Requests', value: stats.activeRequests.toString(), icon: AlertTriangle, color: 'text-red-600' },
    { name: 'Completed Today', value: stats.completedToday.toString(), icon: CheckCircle, color: 'text-green-600' },
    { name: 'Active Helpers', value: stats.activeHelpers.toString(), icon: Users, color: 'text-blue-600' },
    { name: 'Response Time', value: stats.responseTime, icon: Clock, color: 'text-yellow-600' },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      <div className="space-y-8 p-6">
        {/* Welcome Header - Modern Design */}
        <div className="relative overflow-hidden bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 shadow-2xl rounded-2xl">
          <div className="absolute inset-0 bg-black opacity-10"></div>
          <div className="relative px-8 py-12">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-4xl font-bold text-white mb-2">
                  Welcome back, {user?.fullName}!
                </h1>
                <p className="text-blue-100 text-lg">
                  Here's what's happening in your area today.
                </p>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
                  <Activity className="w-12 h-12 text-white" />
                </div>
              </div>
            </div>
          </div>
          {/* Decorative elements */}
          <div className="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white bg-opacity-10 rounded-full"></div>
          <div className="absolute bottom-0 left-0 -mb-8 -ml-8 w-32 h-32 bg-white bg-opacity-5 rounded-full"></div>
        </div>

        {/* Stats Grid - Modern Cards */}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {statsData.map((stat, index) => {
            const Icon = stat.icon;
            const gradients = [
              'from-red-500 to-pink-500',
              'from-green-500 to-emerald-500', 
              'from-blue-500 to-cyan-500',
              'from-yellow-500 to-orange-500'
            ];
            return (
              <div key={stat.name} className="group relative">
                <div className="absolute -inset-0.5 bg-gradient-to-r from-pink-600 to-purple-600 rounded-2xl blur opacity-25 group-hover:opacity-75 transition duration-1000 group-hover:duration-200"></div>
                <div className="relative bg-white rounded-2xl shadow-xl p-6 hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-600 mb-1">
                        {stat.name}
                      </p>
                      <p className="text-3xl font-bold text-gray-900">
                        {stat.value}
                      </p>
                    </div>
                    <div className={`p-3 rounded-xl bg-gradient-to-r ${gradients[index]} shadow-lg`}>
                      <Icon className="h-6 w-6 text-white" />
                    </div>
                  </div>
                  <div className="mt-4 flex items-center">
                    <TrendingUp className="h-4 w-4 text-green-500 mr-1" />
                    <span className="text-sm text-green-600 font-medium">+12% from last week</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Recent Requests - Modern Design */}
          <div className="bg-white shadow-2xl rounded-2xl overflow-hidden">
            <div className="bg-gradient-to-r from-slate-800 to-slate-900 px-6 py-4">
              <h3 className="text-xl font-bold text-white flex items-center">
                <MapPin className="w-5 h-5 mr-2" />
                Recent Requests
              </h3>
            </div>
            <div className="p-6">
              {loading ? (
                <div className="flex items-center justify-center h-32">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                </div>
              ) : (
                <div className="space-y-4">
                  {recentRequests.map((request, index) => (
                    <div key={request.id} className="group relative">
                      <div className="absolute -inset-0.5 bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl blur opacity-0 group-hover:opacity-25 transition duration-300"></div>
                      <div className="relative bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-4 hover:shadow-lg transition-all duration-300">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-4">
                            <div className="flex-shrink-0">
                              <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
                                <MapPin className="h-5 w-5 text-white" />
                              </div>
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-bold text-gray-900 truncate">
                                {request.category}
                              </p>
                              <p className="text-xs text-gray-600 truncate">
                                {request.address || 'Location not specified'}
                              </p>
                            </div>
                          </div>
                          <div className="flex items-center space-x-3">
                            <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold ${getSeverityColor(request.severity || 3)}`}>
                              Severity {request.severity || 3}
                            </span>
                            <select 
                              value={request.status || 'new'}
                              onChange={(e) => updateRequestStatus(request.id, e.target.value)}
                              className="text-xs border-2 border-gray-300 rounded-lg px-3 py-1 bg-white hover:border-blue-500 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                            >
                              <option value="new">New</option>
                              <option value="assigned">Assigned</option>
                              <option value="in_progress">In Progress</option>
                              <option value="completed">Completed</option>
                              <option value="cancelled">Cancelled</option>
                            </select>
                            <span className="text-xs text-gray-500 font-medium">
                              {new Date(request.createdAt).toLocaleTimeString()}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                  {recentRequests.length === 0 && (
                    <div className="text-center py-12">
                      <MapPin className="mx-auto h-12 w-12 text-gray-400" />
                      <h3 className="mt-2 text-sm font-medium text-gray-900">No recent requests</h3>
                      <p className="mt-1 text-sm text-gray-500">Get started by creating a new request.</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Quick Actions - Modern Design */}
          <div className="bg-white shadow-2xl rounded-2xl overflow-hidden">
            <div className="bg-gradient-to-r from-emerald-800 to-teal-900 px-6 py-4">
              <h3 className="text-xl font-bold text-white flex items-center">
                <Activity className="w-5 h-5 mr-2" />
                Quick Actions
              </h3>
            </div>
            <div className="p-6">
              <div className="grid grid-cols-2 gap-4">
                <button 
                  onClick={() => setShowNeedsForm(true)}
                  className="group relative overflow-hidden bg-gradient-to-r from-blue-500 to-blue-600 text-white p-6 rounded-xl hover:from-blue-600 hover:to-blue-700 transition-all duration-300 transform hover:scale-105 hover:shadow-xl"
                >
                  <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
                  <div className="relative">
                    <Plus className="h-8 w-8 mx-auto mb-3" />
                    <span className="text-sm font-bold">Create Need</span>
                  </div>
                </button>
                <button 
                  onClick={() => navigate('/tasks')}
                  className="group relative overflow-hidden bg-gradient-to-r from-green-500 to-emerald-600 text-white p-6 rounded-xl hover:from-green-600 hover:to-emerald-700 transition-all duration-300 transform hover:scale-105 hover:shadow-xl"
                  title="View available tasks to help"
                >
                  <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
                  <div className="relative">
                    <Users className="h-8 w-8 mx-auto mb-3" />
                    <span className="text-sm font-bold">Help Someone</span>
                  </div>
                </button>
                <button 
                  onClick={handleFindShelter}
                  className="group relative overflow-hidden bg-gradient-to-r from-orange-500 to-yellow-600 text-white p-6 rounded-xl hover:from-orange-600 hover:to-yellow-700 transition-all duration-300 transform hover:scale-105 hover:shadow-xl"
                  title="Find nearby emergency shelters"
                >
                  <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
                  <div className="relative">
                    <MapPin className="h-8 w-8 mx-auto mb-3" />
                    <span className="text-sm font-bold">Find Shelter</span>
                  </div>
                </button>
                <button 
                  onClick={() => navigate('/map')}
                  className="group relative overflow-hidden bg-gradient-to-r from-purple-500 to-indigo-600 text-white p-6 rounded-xl hover:from-purple-600 hover:to-indigo-700 transition-all duration-300 transform hover:scale-105 hover:shadow-xl"
                  title="View interactive map"
                >
                  <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
                  <div className="relative">
                    <Activity className="h-8 w-8 mx-auto mb-3" />
                    <span className="text-sm font-bold">View Map</span>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Stock Alerts - Modern Design */}
        <div className="bg-white shadow-2xl rounded-2xl overflow-hidden">
          <div className="bg-gradient-to-r from-red-800 to-pink-900 px-6 py-4">
            <h3 className="text-xl font-bold text-white flex items-center">
              <AlertTriangle className="w-5 h-5 mr-2" />
              Stock Alerts
            </h3>
          </div>
          <div className="p-6">
            <StockAlerts />
          </div>
        </div>

        {/* Activity Chart - Modern Design */}
        <ActivityChartComponent />

        {/* Needs Form Modal */}
        {showNeedsForm && (
          <NeedsForm
            onSuccess={() => {
              setShowNeedsForm(false);
              fetchDashboardData(); // Refresh data
            }}
            onCancel={() => setShowNeedsForm(false)}
          />
        )}
      </div>
    </div>
  );
};

// Activity Chart Component
const ActivityChartComponent: React.FC = () => {
  const [activityData, setActivityData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchActivityData = async () => {
      try {
        setLoading(true);
        const data = await apiService.get<any[]>('/dashboard/activity');
        // Handle both plain array and wrapped { data: [...] }
        const normalized = Array.isArray(data)
          ? data
          : (data && Array.isArray((data as any).data) ? (data as any).data : []);
        setActivityData(normalized || []);
      } catch (error) {
        console.error('Failed to fetch activity data:', error);
        setActivityData([]);
      } finally {
        setLoading(false);
      }
    };

    fetchActivityData();
  }, []);

  return (
    <div className="bg-white shadow-2xl rounded-2xl overflow-hidden">
      <div className="bg-gradient-to-r from-indigo-800 to-purple-900 px-6 py-4">
        <h3 className="text-xl font-bold text-white flex items-center">
          <TrendingUp className="w-5 h-5 mr-2" />
          Activity Overview
        </h3>
      </div>
      <div className="p-6">
        <div className="bg-gradient-to-br from-gray-50 to-blue-50 rounded-xl p-6">
          {loading ? (
            <div className="h-64 flex items-center justify-center">
              <div className="text-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-500">Loading activity data...</p>
              </div>
            </div>
          ) : activityData.length > 0 ? (
            <div className="w-full">
              {/* Bar chart with fixed height container */}
              <div className="relative h-48 mb-4">
                <div className="absolute inset-0 flex items-end justify-between space-x-1">
                  {(() => {
                    const maxValue = Math.max(...activityData.map(d => Number(d.value) || 0), 1);
                    return activityData.map((item, index) => {
                      const value = Number(item.value) || 0;
                      // Calculate height as percentage of container, with minimum 4px for visibility
                      const heightPercent = maxValue > 0 ? (value / maxValue) * 100 : 0;
                      const minHeightPx = value > 0 ? 4 : 0;
                      const barHeight = Math.max((heightPercent / 100) * 192, minHeightPx); // 192px = h-48 (12rem)
                      
                      return (
                        <div 
                          key={index} 
                          className="flex-1 flex flex-col items-center justify-end h-full"
                          style={{ maxWidth: `${100 / activityData.length}%` }}
                        >
                          <div 
                            className="w-full bg-gradient-to-t from-blue-600 to-blue-400 rounded-t-lg hover:from-blue-700 hover:to-blue-500 transition-all duration-300 cursor-pointer relative group shadow-lg hover:shadow-xl transform hover:scale-105"
                            style={{ 
                              height: `${barHeight}px`,
                              minHeight: value > 0 ? '4px' : '0px'
                            }}
                            title={`${item.label || 'Activity'}: ${value} on ${new Date(item.date).toLocaleDateString()}`}
                          >
                            {/* Value label on hover or always visible for small bars */}
                            {barHeight < 20 && value > 0 && (
                              <div className="absolute -top-6 left-1/2 transform -translate-x-1/2 text-xs font-semibold text-blue-700 whitespace-nowrap">
                                {value}
                              </div>
                            )}
                          </div>
                          {/* Value label below bar (for larger bars) */}
                          {barHeight >= 20 && (
                            <div className="text-xs font-semibold text-blue-700 mt-1">
                              {value}
                            </div>
                          )}
                          {/* Date label */}
                          <div className="text-xs text-gray-500 mt-1 truncate w-full text-center">
                            {new Date(item.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                          </div>
                        </div>
                      );
                    });
                  })()}
                </div>
              </div>
            </div>
          ) : (
            <div className="h-64 flex items-center justify-center">
              <div className="text-center">
                <div className="w-16 h-16 bg-gradient-to-r from-gray-400 to-gray-500 rounded-full flex items-center justify-center mx-auto mb-4">
                  <TrendingUp className="h-8 w-8 text-white" />
                </div>
                <h3 className="text-lg font-semibold text-gray-700 mb-2">All Stock Levels Good</h3>
                <p className="text-gray-500">No low stock alerts at this time.</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;