import React, { useState, useEffect } from 'react';
import { 
  Globe, 
  Cloud, 
  MessageCircle, 
  Wifi, 
  Truck, 
  Activity,
  AlertTriangle,
  CheckCircle,
  Clock,
  MapPin,
  TrendingUp,
  Users,
  BarChart3,
  Settings,
  RefreshCw
} from 'lucide-react';
import { governmentApiService, GovernmentAlert, GovernmentDisasterData } from '../services/governmentApiService';
import { weatherService, WeatherData, WeatherAlert } from '../services/weatherService';
import { socialMediaService, SocialMediaPost, SocialMediaAlert } from '../services/socialMediaService';
import { iotService, IoTDevice, IoTAlert } from '../services/iotService';
import { logisticsService, LogisticsShipment, LogisticsProvider } from '../services/logisticsService';

const IntegrationDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'government' | 'weather' | 'social' | 'iot' | 'logistics'>('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Data states
  const [governmentAlerts, setGovernmentAlerts] = useState<GovernmentAlert[]>([]);
  const [weatherData, setWeatherData] = useState<WeatherData | null>(null);
  const [weatherAlerts, setWeatherAlerts] = useState<WeatherAlert[]>([]);
  const [socialMediaPosts, setSocialMediaPosts] = useState<SocialMediaPost[]>([]);
  const [socialAlerts, setSocialAlerts] = useState<SocialMediaAlert[]>([]);
  const [iotDevices, setIotDevices] = useState<IoTDevice[]>([]);
  const [iotAlerts, setIotAlerts] = useState<IoTAlert[]>([]);
  const [logisticsShipments, setLogisticsShipments] = useState<LogisticsShipment[]>([]);
  const [logisticsProviders, setLogisticsProviders] = useState<LogisticsProvider[]>([]);

  useEffect(() => {
    loadIntegrationData();
  }, []);

  const loadIntegrationData = async () => {
    try {
      setLoading(true);
      
      // Load data from all integration services
      const [
        alerts,
        weather,
        weatherAlertsData,
        socialPosts,
        socialAlertsData,
        devices,
        iotAlertsData,
        providers
      ] = await Promise.all([
        governmentApiService.getActiveAlerts('current-region'),
        weatherService.getCurrentWeather('current-location'),
        weatherService.getWeatherAlerts('current-location'),
        socialMediaService.searchPosts('disaster relief', undefined, 10),
        socialMediaService.getAlerts('twitter'),
        iotService.getDevices(),
        iotService.getDeviceAlerts('device-1'),
        logisticsService.getProviders('emergency', 'current-region')
      ]);
      
      setGovernmentAlerts(alerts);
      setWeatherData(weather);
      setWeatherAlerts(weatherAlertsData);
      setSocialMediaPosts(socialPosts);
      setSocialAlerts(socialAlertsData);
      setIotDevices(devices);
      setIotAlerts(iotAlertsData);
      setLogisticsProviders(providers);
      
    } catch (err) {
      setError('Failed to load integration data');
      console.error('Error loading integration data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    loadIntegrationData();
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'online':
      case 'operational':
        return 'text-green-600 bg-green-100';
      case 'warning':
      case 'alert':
        return 'text-yellow-600 bg-yellow-100';
      case 'error':
      case 'offline':
      case 'critical':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'low':
        return 'text-blue-600 bg-blue-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'high':
        return 'text-orange-600 bg-orange-100';
      case 'critical':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
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
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <AlertTriangle className="h-5 w-5 text-red-400" />
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error</h3>
            <div className="mt-2 text-sm text-red-700">{error}</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Integration & Interoperability</h1>
          <p className="text-gray-600">External system integration and data synchronization</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={handleRefresh}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </button>
          <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
            <Settings className="h-4 w-4 mr-2" />
            Configure
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'overview', name: 'Overview', icon: BarChart3 },
            { id: 'government', name: 'Government APIs', icon: Globe },
            { id: 'weather', name: 'Weather Services', icon: Cloud },
            { id: 'social', name: 'Social Media', icon: MessageCircle },
            { id: 'iot', name: 'IoT Devices', icon: Wifi },
            { id: 'logistics', name: 'Logistics', icon: Truck }
          ].map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
              >
                <Icon className="h-4 w-4 mr-2" />
                {tab.name}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="mt-6">
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Integration Status Cards */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Globe className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Government APIs</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {governmentAlerts.length} Active Alerts
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Cloud className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Weather Services</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {weatherData ? `${weatherData.temperature}°C` : 'N/A'}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <MessageCircle className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Social Media</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {socialMediaPosts.length} Posts
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Wifi className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">IoT Devices</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {iotDevices.length} Connected
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'government' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Government API Integration</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Official disaster management systems</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {governmentAlerts.map((alert) => (
                  <li key={alert.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {alert.alertType}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getSeverityColor(alert.severity)}`}>
                              {alert.severity}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <p className="text-sm text-gray-500">{alert.message}</p>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Region: {alert.region}</span>
                            <span className="ml-4">Issued: {formatDateTime(alert.issuedAt)}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'weather' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Weather Services Integration</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Real-time weather data and alerts</p>
              </div>
              {weatherData && (
                <div className="px-4 py-4 sm:px-6">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center">
                      <p className="text-2xl font-bold text-blue-600">{weatherData.temperature}°C</p>
                      <p className="text-sm text-gray-500">Temperature</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-green-600">{weatherData.humidity}%</p>
                      <p className="text-sm text-gray-500">Humidity</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-purple-600">{weatherData.windSpeed} km/h</p>
                      <p className="text-sm text-gray-500">Wind Speed</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-orange-600">{weatherData.condition}</p>
                      <p className="text-sm text-gray-500">Condition</p>
                    </div>
                  </div>
                </div>
              )}
              {weatherAlerts.length > 0 && (
                <div className="px-4 py-4 sm:px-6 border-t">
                  <h4 className="text-md font-medium text-gray-900 mb-3">Weather Alerts</h4>
                  <ul className="space-y-2">
                    {weatherAlerts.map((alert) => (
                      <li key={alert.id} className="flex items-center justify-between p-3 bg-yellow-50 rounded-md">
                        <div>
                          <p className="text-sm font-medium text-gray-900">{alert.title}</p>
                          <p className="text-sm text-gray-500">{alert.description}</p>
                        </div>
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getSeverityColor(alert.severity)}`}>
                          {alert.severity}
                        </span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'social' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Social Media Monitoring</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Track and analyze social media for disaster information</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {socialMediaPosts.map((post) => (
                  <li key={post.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            @{post.author}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(post.sentiment)}`}>
                              {post.sentiment}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <p className="text-sm text-gray-500">{post.content}</p>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Platform: {post.platform}</span>
                            <span className="ml-4">Likes: {post.likes}</span>
                            <span className="ml-4">Shares: {post.shares}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'iot' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">IoT Device Integration</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Connected sensors, drones, and IoT devices</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {iotDevices.map((device) => (
                  <li key={device.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {device.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(device.status)}`}>
                              {device.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Type: {device.type}</span>
                            <span className="ml-4">Location: {device.location}</span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Last Seen: {formatDateTime(device.lastSeen)}</span>
                            <span className="ml-4">Firmware: {device.firmwareVersion}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'logistics' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Logistics Integration</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Third-party delivery and logistics providers</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {logisticsProviders.map((provider) => (
                  <li key={provider.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {provider.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex">
                            <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                              {provider.rating}/5
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Type: {provider.type}</span>
                            <span className="ml-4">Regions: {provider.regions.join(', ')}</span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Services: {provider.serviceTypes.join(', ')}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default IntegrationDashboard;


