import React, { useState, useEffect } from 'react';
import { IndoorNavigationService, IndoorMap, IndoorRoute } from '../services/indoorNavigationService';
import { IndoorMapViewer } from '../components/indoor/IndoorMapViewer';
import { IndoorNavigationPanel } from '../components/indoor/IndoorNavigationPanel';
import { Building, MapPin, Navigation, AlertTriangle, Users, Clock, Route } from 'lucide-react';

export const IndoorNavigationDashboard: React.FC = () => {
  const [indoorMaps, setIndoorMaps] = useState<IndoorMap[]>([]);
  const [selectedMap, setSelectedMap] = useState<IndoorMap | null>(null);
  const [selectedRoute, setSelectedRoute] = useState<IndoorRoute | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNodes, setShowNodes] = useState(true);
  const [showEdges, setShowEdges] = useState(true);
  const [showRoutes, setShowRoutes] = useState(false);
  const [showPositions, setShowPositions] = useState(false);

  useEffect(() => {
    loadIndoorMaps();
  }, []);

  const loadIndoorMaps = async () => {
    try {
      setLoading(true);
      setError(null);
      const maps = await IndoorNavigationService.getAllIndoorMaps({ activeOnly: true });
      setIndoorMaps(maps);
      if (maps.length > 0) {
        setSelectedMap(maps[0]);
      }
    } catch (err) {
      console.error('Error loading indoor maps:', err);
      setError('Failed to load indoor maps');
    } finally {
      setLoading(false);
    }
  };

  const handleMapSelect = (map: IndoorMap) => {
    setSelectedMap(map);
    setSelectedRoute(null);
  };

  const handleRouteSelect = (route: IndoorRoute) => {
    setSelectedRoute(route);
    setShowRoutes(true);
  };

  const handleNodeSelect = (node: any) => {
    console.log('Selected node:', node);
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
              <h1 className="text-2xl font-bold text-gray-900">Indoor Navigation</h1>
              <p className="text-gray-600">GPS-denied environment navigation for buildings and shelters</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Building className="w-5 h-5 text-gray-400" />
                <span className="text-sm text-gray-600">
                  {indoorMaps.length} Active Maps
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Map Selection Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
              <h2 className="text-lg font-semibold mb-4">Select Map</h2>
              <div className="space-y-2">
                {indoorMaps.map(map => (
                  <button
                    key={map.id}
                    onClick={() => handleMapSelect(map)}
                    className={`w-full text-left p-3 rounded-md border ${
                      selectedMap?.id === map.id
                        ? 'border-blue-500 bg-blue-50 text-blue-700'
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <div className="flex items-center space-x-2">
                      <Building className="w-4 h-4" />
                      <div>
                        <p className="font-medium">{map.name}</p>
                        <p className="text-sm text-gray-600">{map.facilityName}</p>
                        <p className="text-xs text-gray-500">Floor {map.floorNumber}</p>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Map Controls */}
            <div className="bg-white rounded-lg shadow-sm p-4">
              <h3 className="text-md font-semibold mb-3">Map Controls</h3>
              <div className="space-y-3">
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={showNodes}
                    onChange={(e) => setShowNodes(e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm">Show Nodes</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={showEdges}
                    onChange={(e) => setShowEdges(e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm">Show Edges</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={showRoutes}
                    onChange={(e) => setShowRoutes(e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm">Show Routes</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={showPositions}
                    onChange={(e) => setShowPositions(e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm">Show Positions</span>
                </label>
              </div>
            </div>
          </div>

          {/* Main Content */}
          <div className="lg:col-span-3">
            {selectedMap ? (
              <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                {/* Map Viewer */}
                <div className="xl:col-span-2">
                  <div className="bg-white rounded-lg shadow-sm p-4">
                    <h2 className="text-lg font-semibold mb-4">Map View</h2>
                    <div className="h-96">
                      <IndoorMapViewer
                        mapId={selectedMap.id}
                        onNodeSelect={handleNodeSelect}
                        onRouteSelect={handleRouteSelect}
                        showNodes={showNodes}
                        showEdges={showEdges}
                        showRoutes={showRoutes}
                        showPositions={showPositions}
                        className="w-full h-full"
                      />
                    </div>
                  </div>
                </div>

                {/* Navigation Panel */}
                <div className="xl:col-span-1">
                  <IndoorNavigationPanel
                    mapId={selectedMap.id}
                    onRouteSelect={handleRouteSelect}
                    className="h-full"
                  />
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-sm p-8 text-center">
                <Building className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No Map Selected</h3>
                <p className="text-gray-600">Select an indoor map from the sidebar to begin navigation.</p>
              </div>
            )}

            {/* Selected Route Details */}
            {selectedRoute && (
              <div className="mt-6 bg-white rounded-lg shadow-sm p-4">
                <h3 className="text-lg font-semibold mb-4">Route Details</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="flex items-center space-x-2">
                    <Navigation className="w-5 h-5 text-blue-600" />
                    <div>
                      <p className="text-sm font-medium">Distance</p>
                      <p className="text-lg">{selectedRoute.totalDistance.toFixed(1)}m</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Clock className="w-5 h-5 text-green-600" />
                    <div>
                      <p className="text-sm font-medium">Estimated Time</p>
                      <p className="text-lg">{Math.round(selectedRoute.estimatedTime / 60)}min</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Route className="w-5 h-5 text-purple-600" />
                    <div>
                      <p className="text-sm font-medium">Difficulty</p>
                      <p className="text-lg capitalize">{selectedRoute.difficultyLevel.toLowerCase()}</p>
                    </div>
                  </div>
                </div>
                
                {selectedRoute.instructions && (
                  <div className="mt-4">
                    <h4 className="text-md font-medium mb-2">Turn-by-Turn Instructions</h4>
                    <div className="bg-gray-50 rounded-md p-3">
                      <pre className="text-sm text-gray-700 whitespace-pre-wrap">
                        {JSON.stringify(selectedRoute.instructions, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};



