import React, { useState } from 'react';
import { TerrainVisualization } from '../components/terrain/TerrainVisualization';
import { TerrainAnalysisPanel } from '../components/terrain/TerrainAnalysisPanel';
import { TerrainRoutingPanel } from '../components/terrain/TerrainRoutingPanel';
import { TerrainAnalysis, TerrainRoute, ElevationStatistics } from '../services/terrainService';

export const TerrainDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'visualization' | 'analysis' | 'routing'>('visualization');
  const [selectedAnalysis, setSelectedAnalysis] = useState<TerrainAnalysis | null>(null);
  const [selectedRoute, setSelectedRoute] = useState<TerrainRoute | null>(null);
  const [elevationStats, setElevationStats] = useState<ElevationStatistics | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>([-74.0, 40.7]); // Default to NYC area
  const [mapZoom, setMapZoom] = useState(10);

  const handleAnalysisComplete = (analysis: TerrainAnalysis) => {
    setSelectedAnalysis(analysis);
    setActiveTab('visualization');
  };

  const handleStatisticsUpdate = (stats: ElevationStatistics) => {
    setElevationStats(stats);
  };

  const handleRouteCalculated = (routes: TerrainRoute[]) => {
    if (routes.length > 0) {
      setSelectedRoute(routes[0]);
      setActiveTab('visualization');
    }
  };

  const handleRouteSelected = (route: TerrainRoute) => {
    setSelectedRoute(route);
  };

  const handleMapClick = (analysis: TerrainAnalysis) => {
    setSelectedAnalysis(analysis);
  };

  const tabs = [
    { id: 'visualization', label: '3D Visualization', icon: '🗺️' },
    { id: 'analysis', label: 'Terrain Analysis', icon: '📊' },
    { id: 'routing', label: 'Terrain Routing', icon: '🛣️' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">3D Terrain Analysis</h1>
              <p className="text-gray-600">Advanced geospatial analysis with elevation data and terrain-aware routing</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-500">
                {selectedAnalysis && (
                  <span className="inline-block px-2 py-1 bg-blue-100 text-blue-800 rounded">
                    Analysis: {selectedAnalysis.analysisType}
                  </span>
                )}
                {selectedRoute && (
                  <span className="inline-block px-2 py-1 bg-green-100 text-green-800 rounded ml-2">
                    Route: {selectedRoute.isAccessible ? 'Accessible' : 'Not Accessible'}
                  </span>
                )}
              </div>
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
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Panel - Controls */}
          <div className="lg:col-span-1">
            {activeTab === 'visualization' && (
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

                {/* Analysis Results Summary */}
                {selectedAnalysis && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Analysis Results</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Type:</span>
                        <span className="font-medium">{selectedAnalysis.analysisType}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Elevation Range:</span>
                        <span>{selectedAnalysis.minElevation?.toFixed(0)}m - {selectedAnalysis.maxElevation?.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Max Slope:</span>
                        <span>{selectedAnalysis.slopeMaximum?.toFixed(1)}°</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Accessibility:</span>
                        <span className={`font-medium ${
                          (selectedAnalysis.accessibilityScore || 0) >= 0.7 ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {((selectedAnalysis.accessibilityScore || 0) * 100).toFixed(0)}%
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Flood Risk:</span>
                        <span className={`font-medium ${
                          (selectedAnalysis.floodRiskScore || 0) >= 0.7 ? 'text-red-600' : 'text-green-600'
                        }`}>
                          {((selectedAnalysis.floodRiskScore || 0) * 100).toFixed(0)}%
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Route Results Summary */}
                {selectedRoute && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Route Information</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Distance:</span>
                        <span>{selectedRoute.totalDistance < 1000 
                          ? `${selectedRoute.totalDistance.toFixed(0)}m` 
                          : `${(selectedRoute.totalDistance / 1000).toFixed(1)}km`}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Elevation Gain:</span>
                        <span>+{selectedRoute.totalElevationGain.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Max Slope:</span>
                        <span>{selectedRoute.maxSlope.toFixed(1)}°</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Accessibility:</span>
                        <span className={`font-medium ${
                          selectedRoute.accessibilityScore >= 0.7 ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {(selectedRoute.accessibilityScore * 100).toFixed(0)}%
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Difficulty:</span>
                        <span className={`font-medium ${
                          selectedRoute.difficultyScore <= 2 ? 'text-green-600' :
                          selectedRoute.difficultyScore <= 4 ? 'text-yellow-600' : 'text-red-600'
                        }`}>
                          {selectedRoute.difficultyScore.toFixed(1)}
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Elevation Statistics */}
                {elevationStats && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Elevation Statistics</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Min Elevation:</span>
                        <span>{elevationStats.minElevation?.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Max Elevation:</span>
                        <span>{elevationStats.maxElevation?.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Average:</span>
                        <span>{elevationStats.avgElevation?.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Std Dev:</span>
                        <span>{elevationStats.elevationStddev?.toFixed(0)}m</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Data Points:</span>
                        <span>{elevationStats.pointCount}</span>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'analysis' && (
              <TerrainAnalysisPanel
                onAnalysisComplete={handleAnalysisComplete}
                onStatisticsUpdate={handleStatisticsUpdate}
              />
            )}

            {activeTab === 'routing' && (
              <TerrainRoutingPanel
                onRouteCalculated={handleRouteCalculated}
                onRouteSelected={handleRouteSelected}
              />
            )}
          </div>

          {/* Right Panel - Map Visualization */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow h-96 lg:h-[600px]">
              <TerrainVisualization
                center={mapCenter}
                zoom={mapZoom}
                showElevation={true}
                showSlope={true}
                showAccessibility={true}
                showRoutes={!!selectedRoute}
                onTerrainAnalysis={handleMapClick}
                onRouteCalculated={handleRouteCalculated}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-white border-t mt-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="text-sm text-gray-500">
            <p>
              <strong>3D Terrain Analysis</strong> - Advanced geospatial analysis with elevation data, 
              terrain-aware routing, and accessibility assessment for disaster relief operations.
            </p>
            <p className="mt-2">
              Features include elevation point visualization, slope analysis, flood risk assessment, 
              and intelligent routing that considers terrain constraints for optimal emergency response.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};



