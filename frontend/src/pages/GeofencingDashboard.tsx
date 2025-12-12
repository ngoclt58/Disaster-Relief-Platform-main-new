import React, { useState, useEffect } from 'react';
import { GeofenceMap } from '../components/geofencing/GeofenceMap';
import { GeofenceManagement } from '../components/geofencing/GeofenceManagement';
import { GeofencingService, Geofence, GeofenceEvent, GeofenceAlert } from '../services/geofencingService';

export const GeofencingDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'map' | 'management' | 'events' | 'alerts'>('map');
  const [geofences, setGeofences] = useState<Geofence[]>([]);
  const [events, setEvents] = useState<GeofenceEvent[]>([]);
  const [alerts, setAlerts] = useState<GeofenceAlert[]>([]);
  const [selectedGeofence, setSelectedGeofence] = useState<Geofence | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<GeofenceEvent | null>(null);
  const [selectedAlert, setSelectedAlert] = useState<GeofenceAlert | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>([-74.0, 40.7]);
  const [mapZoom, setMapZoom] = useState(10);
  const [showGeofences, setShowGeofences] = useState(true);
  const [showEvents, setShowEvents] = useState(true);
  const [showAlerts, setShowAlerts] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);

    try {
      const [geofencesData, alertsData] = await Promise.all([
        GeofencingService.getActiveGeofences().catch(() => []),
        GeofencingService.getActiveAlerts().catch(() => [])
      ]);

      // Ensure we always set arrays, never undefined or null
      setGeofences(Array.isArray(geofencesData) ? geofencesData : []);
      setAlerts(Array.isArray(alertsData) ? alertsData : []);

      // Load events for selected geofence if any
      if (selectedGeofence) {
        const eventsData = await GeofencingService.getGeofenceEvents(selectedGeofence.id).catch(() => []);
        setEvents(Array.isArray(eventsData) ? eventsData : []);
      } else {
        setEvents([]);
      }
    } catch (err) {
      setError('Failed to load geofencing data');
      console.error('Data loading error:', err);
      // Ensure arrays are set even on error
      setGeofences([]);
      setAlerts([]);
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  const handleGeofenceCreated = (geofence: Geofence) => {
    setGeofences(prev => [...prev, geofence]);
    setActiveTab('map');
  };

  const handleGeofenceUpdated = (geofence: Geofence) => {
    setGeofences(prev => prev.map(g => g.id === geofence.id ? geofence : g));
    setActiveTab('map');
  };

  const handleGeofenceDeleted = (geofenceId: number) => {
    setGeofences(prev => prev.filter(g => g.id !== geofenceId));
    if (selectedGeofence?.id === geofenceId) {
      setSelectedGeofence(null);
    }
    setActiveTab('map');
  };

  const handleGeofenceClick = (geofence: Geofence) => {
    setSelectedGeofence(geofence);
    loadGeofenceEvents(geofence.id);
  };

  const handleEventClick = (event: GeofenceEvent) => {
    setSelectedEvent(event);
  };

  const handleAlertClick = (alert: GeofenceAlert) => {
    setSelectedAlert(alert);
  };

  const loadGeofenceEvents = async (geofenceId: number) => {
    try {
      const eventsData = await GeofencingService.getGeofenceEvents(geofenceId).catch(() => []);
      setEvents(Array.isArray(eventsData) ? eventsData : []);
    } catch (err) {
      console.error('Failed to load geofence events:', err);
      setEvents([]);
    }
  };

  const handleAcknowledgeAlert = async (alertId: number) => {
    try {
      await GeofencingService.acknowledgeAlert(alertId, 'current_user');
      setAlerts(prev => prev.map(alert => 
        alert.id === alertId 
          ? { ...alert, status: 'ACKNOWLEDGED', acknowledgedAt: new Date().toISOString(), acknowledgedBy: 'current_user' }
          : alert
      ));
    } catch (err) {
      console.error('Failed to acknowledge alert:', err);
    }
  };

  const handleResolveAlert = async (alertId: number) => {
    try {
      await GeofencingService.resolveAlert(alertId, 'current_user', 'Resolved via dashboard');
      setAlerts(prev => prev.map(alert => 
        alert.id === alertId 
          ? { ...alert, status: 'RESOLVED', resolvedAt: new Date().toISOString(), resolvedBy: 'current_user' }
          : alert
      ));
    } catch (err) {
      console.error('Failed to resolve alert:', err);
    }
  };

  const tabs = [
    { id: 'map', label: 'Geofence Map', icon: '🗺️' },
    { id: 'management', label: 'Geofence Management', icon: '⚙️' },
    { id: 'events', label: 'Events', icon: '📋' },
    { id: 'alerts', label: 'Alerts', icon: '🚨' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Geofencing & Monitoring</h1>
              <p className="text-gray-600">Automated alerts and actions based on geographic boundaries</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-500">
                {(geofences?.length || 0)} geofences • {(events?.length || 0)} events • {(alerts?.length || 0)} alerts
              </div>
              <button
                onClick={loadData}
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Loading...' : 'Refresh'}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex space-x-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'map' && (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Left Panel - Controls */}
            <div className="lg:col-span-1">
              <div className="space-y-4">
                <div className="bg-white p-4 rounded-lg shadow">
                  <h3 className="font-semibold mb-3">Map Controls</h3>
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Center Coordinates
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        <input
                          type="number"
                          step="any"
                          placeholder="Longitude"
                          value={mapCenter[0]}
                          onChange={(e) => setMapCenter([parseFloat(e.target.value) || 0, mapCenter[1]])}
                          className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Latitude"
                          value={mapCenter[1]}
                          onChange={(e) => setMapCenter([mapCenter[0], parseFloat(e.target.value) || 0])}
                          className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Zoom Level
                      </label>
                      <input
                        type="range"
                        min="1"
                        max="18"
                        value={mapZoom}
                        onChange={(e) => setMapZoom(parseInt(e.target.value))}
                        className="w-full"
                      />
                      <div className="text-xs text-gray-500 text-center">{mapZoom}</div>
                    </div>
                  </div>
                </div>

                <div className="bg-white p-4 rounded-lg shadow">
                  <h3 className="font-semibold mb-3">Display Options</h3>
                  <div className="space-y-2">
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={showGeofences}
                        onChange={(e) => setShowGeofences(e.target.checked)}
                        className="mr-2"
                      />
                      <span className="text-sm">Show Geofences</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={showEvents}
                        onChange={(e) => setShowEvents(e.target.checked)}
                        className="mr-2"
                      />
                      <span className="text-sm">Show Events</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={showAlerts}
                        onChange={(e) => setShowAlerts(e.target.checked)}
                        className="mr-2"
                      />
                      <span className="text-sm">Show Alerts</span>
                    </label>
                  </div>
                </div>

                {/* Selected Geofence Info */}
                {selectedGeofence && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Selected Geofence</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Name:</span>
                        <span className="font-medium">{selectedGeofence.name}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Type:</span>
                        <span className="font-medium">
                          {GeofencingService.getGeofenceTypeIcon(selectedGeofence.geofenceType)} {GeofencingService.getGeofenceTypeDisplayName(selectedGeofence.geofenceType)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Priority:</span>
                        <span className="font-medium">{selectedGeofence.priority}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Status:</span>
                        <span className={selectedGeofence.isActive ? 'text-green-600' : 'text-red-600'}>
                          {selectedGeofence.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Events:</span>
                        <span>{events?.length || 0}</span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Selected Event Info */}
                {selectedEvent && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Selected Event</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Type:</span>
                        <span className="font-medium">
                          {GeofencingService.getEventTypeIcon(selectedEvent.eventType)} {selectedEvent.eventType}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Severity:</span>
                        <span className="font-medium">{selectedEvent.severity}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Entity:</span>
                        <span>{selectedEvent.entityName || selectedEvent.entityType}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Occurred:</span>
                        <span>{new Date(selectedEvent.occurredAt).toLocaleString()}</span>
                      </div>
                      {selectedEvent.confidenceScore && (
                        <div className="flex justify-between">
                          <span>Confidence:</span>
                          <span>{GeofencingService.formatConfidenceScore(selectedEvent.confidenceScore)}</span>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right Panel - Map */}
            <div className="lg:col-span-3">
              <div className="bg-white rounded-lg shadow h-96 lg:h-[600px]">
                <GeofenceMap
                  center={mapCenter}
                  zoom={mapZoom}
                  geofences={geofences}
                  events={events}
                  alerts={alerts}
                  showGeofences={showGeofences}
                  showEvents={showEvents}
                  showAlerts={showAlerts}
                  selectedGeofenceId={selectedGeofence?.id}
                  onGeofenceClick={handleGeofenceClick}
                  onEventClick={handleEventClick}
                  onAlertClick={handleAlertClick}
                />
              </div>
            </div>
          </div>
        )}

        {activeTab === 'management' && (
          <div className="max-w-4xl mx-auto">
            <GeofenceManagement
              onGeofenceCreated={handleGeofenceCreated}
              onGeofenceUpdated={handleGeofenceUpdated}
              onGeofenceDeleted={handleGeofenceDeleted}
            />
          </div>
        )}

        {activeTab === 'events' && (
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b">
              <h3 className="text-lg font-semibold">Geofence Events</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Event</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Geofence</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entity</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Severity</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Occurred</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {events && events.length > 0 ? events.map((event) => (
                    <tr key={event.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <span className="text-lg mr-2">
                            {GeofencingService.getEventTypeIcon(event.eventType)}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{event.eventType}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {geofences.find(g => g.id === event.geofenceId)?.name || 'Unknown'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {event.entityName || event.entityType}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          event.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                          event.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                          event.severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                          'bg-green-100 text-green-800'
                        }`}>
                          {event.severity}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {new Date(event.occurredAt).toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          event.isProcessed ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                        }`}>
                          {event.isProcessed ? 'Processed' : 'Pending'}
                        </span>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan={6} className="px-6 py-4 text-center text-gray-500">
                        No events found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'alerts' && (
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b">
              <h3 className="text-lg font-semibold">Geofence Alerts</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Alert</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Geofence</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Severity</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {alerts && alerts.length > 0 ? alerts.map((alert) => (
                    <tr key={alert.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <span className="text-lg mr-2">
                            {GeofencingService.getAlertTypeIcon(alert.alertType)}
                          </span>
                          <div>
                            <div className="text-sm font-medium text-gray-900">{alert.title}</div>
                            <div className="text-sm text-gray-500">{alert.message}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {geofences.find(g => g.id === alert.geofenceId)?.name || 'Unknown'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          alert.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                          alert.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                          alert.severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                          'bg-green-100 text-green-800'
                        }`}>
                          {alert.severity}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          alert.status === 'ACTIVE' ? 'bg-red-100 text-red-800' :
                          alert.status === 'ACKNOWLEDGED' ? 'bg-yellow-100 text-yellow-800' :
                          alert.status === 'RESOLVED' ? 'bg-green-100 text-green-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {alert.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {new Date(alert.createdAt).toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          {alert.status === 'ACTIVE' && (
                            <button
                              onClick={() => handleAcknowledgeAlert(alert.id)}
                              className="text-blue-600 hover:text-blue-900"
                            >
                              Acknowledge
                            </button>
                          )}
                          {alert.status === 'ACKNOWLEDGED' && (
                            <button
                              onClick={() => handleResolveAlert(alert.id)}
                              className="text-green-600 hover:text-green-900"
                            >
                              Resolve
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan={6} className="px-6 py-4 text-center text-gray-500">
                        No alerts found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="bg-white border-t mt-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="text-sm text-gray-500">
            <p>
              <strong>Geofencing & Monitoring</strong> - Automated alerts and actions based on geographic boundaries 
              with real-time event detection and intelligent alert management.
            </p>
            <p className="mt-2">
              Features include geofence creation and management, real-time event monitoring, 
              automated alert generation, and comprehensive analytics for disaster response operations.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};



