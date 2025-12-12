import React, { useState, useEffect } from 'react';
import { 
  LocationAnalyticsService, 
  LocationHistory, 
  LocationPattern, 
  LocationOptimization,
  LocationHistoryStatistics,
  LocationPatternStatistics,
  LocationOptimizationStatistics
} from '../services/locationAnalyticsService';
import { 
  MapPin, 
  TrendingUp, 
  Target, 
  BarChart3, 
  Activity, 
  Clock, 
  Navigation,
  AlertTriangle,
  CheckCircle,
  RefreshCw,
  Filter,
  Search,
  Download,
  Settings,
  Eye,
  Play,
  Pause,
  RotateCcw
} from 'lucide-react';

export const LocationAnalyticsDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'history' | 'patterns' | 'optimizations'>('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Statistics
  const [historyStats, setHistoryStats] = useState<LocationHistoryStatistics | null>(null);
  const [patternStats, setPatternStats] = useState<LocationPatternStatistics | null>(null);
  const [optimizationStats, setOptimizationStats] = useState<LocationOptimizationStatistics | null>(null);
  
  // Data
  const [locationHistory, setLocationHistory] = useState<LocationHistory[]>([]);
  const [patterns, setPatterns] = useState<LocationPattern[]>([]);
  const [optimizations, setOptimizations] = useState<LocationOptimization[]>([]);
  
  // Filters
  const [filters, setFilters] = useState({
    entityType: '',
    startDate: '',
    endDate: '',
    activityType: '',
    patternType: '',
    optimizationType: '',
    priority: '',
    status: ''
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Load statistics
      const [historyStatsData, patternStatsData, optimizationStatsData] = await Promise.all([
        LocationAnalyticsService.getLocationHistoryStatistics(),
        LocationAnalyticsService.getPatternStatistics(),
        LocationAnalyticsService.getOptimizationStatistics()
      ]);
      
      setHistoryStats(historyStatsData);
      setPatternStats(patternStatsData);
      setOptimizationStats(optimizationStatsData);
      
      // Load initial data
      await loadLocationHistory();
      await loadPatterns();
      await loadOptimizations();
      
    } catch (err) {
      console.error('Error loading dashboard data:', err);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const loadLocationHistory = async () => {
    try {
      const data = await LocationAnalyticsService.getLocationHistory({
        entityType: filters.entityType || undefined,
        startTime: filters.startDate || undefined,
        endTime: filters.endDate || undefined,
        activityType: filters.activityType || undefined
      });
      setLocationHistory(data);
    } catch (err) {
      console.error('Error loading location history:', err);
    }
  };

  const loadPatterns = async () => {
    try {
      const data = await LocationAnalyticsService.getLocationPatterns({
        entityType: filters.entityType || undefined,
        patternType: filters.patternType || undefined
      });
      setPatterns(data);
    } catch (err) {
      console.error('Error loading patterns:', err);
    }
  };

  const loadOptimizations = async () => {
    try {
      const data = await LocationAnalyticsService.getLocationOptimizations({
        optimizationType: filters.optimizationType || undefined,
        priority: filters.priority || undefined,
        status: filters.status || undefined
      });
      setOptimizations(data);
    } catch (err) {
      console.error('Error loading optimizations:', err);
    }
  };

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const handleApplyFilters = () => {
    loadLocationHistory();
    loadPatterns();
    loadOptimizations();
  };

  const handleAnalyzePatterns = async (entityType: string, entityId: number) => {
    try {
      await LocationAnalyticsService.analyzePatternsForEntity(entityType, entityId);
      await loadPatterns();
      await loadOptimizations();
    } catch (err) {
      console.error('Error analyzing patterns:', err);
    }
  };

  const handleImplementOptimization = async (optimizationId: number) => {
    try {
      await LocationAnalyticsService.implementOptimization(optimizationId);
      await loadOptimizations();
    } catch (err) {
      console.error('Error implementing optimization:', err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-red-600 text-center">
          <AlertTriangle className="w-8 h-8 mx-auto mb-2" />
          <p>{error}</p>
          <button
            onClick={loadDashboardData}
            className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Location Analytics</h1>
              <p className="text-gray-600">Track movement patterns for optimization</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Activity className="w-5 h-5 text-gray-400" />
                <span className="text-sm text-gray-600">
                  {historyStats?.totalLocations || 0} Locations Tracked
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Tab Navigation */}
        <div className="mb-6">
          <nav className="flex space-x-8">
            <button
              onClick={() => setActiveTab('overview')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'overview'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <BarChart3 className="w-4 h-4" />
                <span>Overview</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'history'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <MapPin className="w-4 h-4" />
                <span>Location History</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('patterns')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'patterns'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <Target className="w-4 h-4" />
                <span>Patterns</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('optimizations')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'optimizations'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <TrendingUp className="w-4 h-4" />
                <span>Optimizations</span>
              </div>
            </button>
          </nav>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Entity Type</label>
              <select
                value={filters.entityType}
                onChange={(e) => handleFilterChange('entityType', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Types</option>
                <option value="PERSON">Person</option>
                <option value="VEHICLE">Vehicle</option>
                <option value="EQUIPMENT">Equipment</option>
                <option value="DRONE">Drone</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
              <input
                type="date"
                value={filters.startDate}
                onChange={(e) => handleFilterChange('startDate', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
              <input
                type="date"
                value={filters.endDate}
                onChange={(e) => handleFilterChange('endDate', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            
            <div className="flex items-end">
              <button
                onClick={handleApplyFilters}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Apply Filters
              </button>
            </div>
          </div>
        </div>

        {/* Tab Content */}
        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Statistics Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <MapPin className="w-8 h-8 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Total Locations</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {historyStats?.totalLocations || 0}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Target className="w-8 h-8 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Patterns Detected</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {patternStats?.totalPatterns || 0}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <TrendingUp className="w-8 h-8 text-purple-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Optimizations</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {optimizationStats?.totalOptimizations || 0}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <CheckCircle className="w-8 h-8 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Implemented</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {optimizationStats?.implementedOptimizations || 0}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Activity</h3>
              <div className="space-y-3">
                {locationHistory.slice(0, 5).map((location) => (
                  <div key={location.id} className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
                    <div className="flex-shrink-0">
                      <span className="text-2xl">
                        {LocationAnalyticsService.getActivityTypeIcon(location.activityType)}
                      </span>
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-900">
                        {location.entityName || `${location.entityType} ${location.entityId}`}
                      </p>
                      <p className="text-xs text-gray-500">
                        {location.activityType} • {LocationAnalyticsService.formatSpeed(location.speed)} • 
                        {new Date(location.timestamp).toLocaleString()}
                      </p>
                    </div>
                    <div className="flex-shrink-0">
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        location.isStationary ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                      }`}>
                        {location.isStationary ? 'Stationary' : 'Moving'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'history' && (
          <div className="bg-white rounded-lg shadow">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Location History</h3>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                {locationHistory.map((location) => (
                  <div key={location.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex items-center space-x-2">
                          <span className="text-2xl">
                            {LocationAnalyticsService.getActivityTypeIcon(location.activityType)}
                          </span>
                          <div>
                            <p className="font-medium">{location.entityName || `${location.entityType} ${location.entityId}`}</p>
                            <p className="text-sm text-gray-500">{location.activityType}</p>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <div className="text-sm text-gray-600">
                          {LocationAnalyticsService.formatSpeed(location.speed)}
                        </div>
                        <div className="text-sm text-gray-600">
                          {new Date(location.timestamp).toLocaleString()}
                        </div>
                      </div>
                    </div>
                    
                    <div className="mt-2 flex items-center justify-between text-xs text-gray-500">
                      <span>Accuracy: {location.accuracy.toFixed(1)}m</span>
                      <span>Lat: {location.latitude.toFixed(6)}, Lng: {location.longitude.toFixed(6)}</span>
                      <span className={`px-2 py-1 rounded-full ${
                        location.isStationary ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                      }`}>
                        {location.isStationary ? 'Stationary' : 'Moving'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'patterns' && (
          <div className="bg-white rounded-lg shadow">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Detected Patterns</h3>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                {patterns.map((pattern) => (
                  <div key={pattern.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex items-center space-x-2">
                          <span className="text-2xl">
                            {LocationAnalyticsService.getPatternTypeIcon(pattern.patternType)}
                          </span>
                          <div>
                            <p className="font-medium">{pattern.patternName}</p>
                            <p className="text-sm text-gray-500">{pattern.patternType}</p>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <div className="text-sm text-gray-600">
                          Confidence: {(pattern.confidenceScore * 100).toFixed(1)}%
                        </div>
                        <div className="text-sm text-gray-600">
                          {LocationAnalyticsService.formatDistance(pattern.distanceMeters)}
                        </div>
                      </div>
                    </div>
                    
                    <div className="mt-2 flex items-center justify-between text-xs text-gray-500">
                      <span>Duration: {LocationAnalyticsService.formatDuration(pattern.durationSeconds)}</span>
                      <span>Speed: {LocationAnalyticsService.formatSpeed(pattern.averageSpeed)}</span>
                      <div className="flex space-x-2">
                        {pattern.isRecurring && (
                          <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full">Recurring</span>
                        )}
                        {pattern.isOptimal && (
                          <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full">Optimal</span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'optimizations' && (
          <div className="bg-white rounded-lg shadow">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Optimization Suggestions</h3>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                {optimizations.map((optimization) => (
                  <div key={optimization.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex items-center space-x-2">
                          <span className="text-2xl">
                            {LocationAnalyticsService.getOptimizationTypeIcon(optimization.optimizationType)}
                          </span>
                          <div>
                            <p className="font-medium">{optimization.optimizationName}</p>
                            <p className="text-sm text-gray-500">{optimization.optimizationType}</p>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <div className="text-sm text-gray-600">
                          Efficiency: {LocationAnalyticsService.formatEfficiency(optimization.currentEfficiency)} → 
                          {LocationAnalyticsService.formatEfficiency(optimization.projectedEfficiency)}
                        </div>
                        <div className="text-sm text-gray-600">
                          {optimization.timeSavingsSeconds && 
                            `Time Savings: ${LocationAnalyticsService.formatDuration(optimization.timeSavingsSeconds)}`
                          }
                        </div>
                      </div>
                    </div>
                    
                    <div className="mt-2 flex items-center justify-between text-xs text-gray-500">
                      <span className={`px-2 py-1 rounded-full ${LocationAnalyticsService.getPriorityColor(optimization.priority)}`}>
                        {optimization.priority}
                      </span>
                      <span className={`px-2 py-1 rounded-full ${LocationAnalyticsService.getStatusColor(optimization.status)}`}>
                        {optimization.status}
                      </span>
                      <span className={`px-2 py-1 rounded-full ${LocationAnalyticsService.getDifficultyColor(optimization.implementationDifficulty)}`}>
                        {optimization.implementationDifficulty}
                      </span>
                      <span className={`px-2 py-1 rounded-full ${LocationAnalyticsService.getRiskColor(optimization.riskLevel)}`}>
                        {optimization.riskLevel}
                      </span>
                    </div>
                    
                    <div className="mt-3">
                      <p className="text-sm text-gray-700">{optimization.description}</p>
                    </div>
                    
                    {!optimization.isImplemented && (
                      <div className="mt-3 flex justify-end">
                        <button
                          onClick={() => handleImplementOptimization(optimization.id)}
                          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                        >
                          Implement
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};



