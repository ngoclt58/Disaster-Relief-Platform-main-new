import React, { useState, useMemo, useRef } from 'react';
import { HeatmapVisualization } from '../components/heatmap/HeatmapVisualization';
import { HeatmapConfigurationPanel } from '../components/heatmap/HeatmapConfigurationPanel';
import { HeatmapService, HeatmapData, HeatmapLayer, HeatmapStatistics } from '../services/heatmapService';

export const HeatmapDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'visualization' | 'configuration' | 'statistics'>('visualization');
  const [selectedHeatmapType, setSelectedHeatmapType] = useState<string>('DISASTER_IMPACT');
  const [showHeatmap, setShowHeatmap] = useState(true);
  const [showPoints, setShowPoints] = useState(true);
  const [mapCenter, setMapCenter] = useState<[number, number]>([-74.0, 40.7]); // Default to NYC area
  const [mapZoom, setMapZoom] = useState(10);
  const [selectedPoint, setSelectedPoint] = useState<HeatmapData | null>(null);
  const [heatmapLayers, setHeatmapLayers] = useState<HeatmapLayer[]>([]);
  const [statistics, setStatistics] = useState<Map<string, HeatmapStatistics>>(new Map());

  const availableHeatmapTypes = HeatmapService.getAvailableHeatmapTypes();
  // Memoize heatmapTypes to prevent unnecessary re-renders
  const heatmapTypes = useMemo(() => availableHeatmapTypes.map(type => type.value), []);

  const handleHeatmapTypeSelect = (heatmapType: string) => {
    setSelectedHeatmapType(heatmapType);
  };

  const handlePointClick = (point: HeatmapData) => {
    setSelectedPoint(point);
  };

  const handleLayerGenerated = (layer: HeatmapLayer) => {
    setHeatmapLayers(prev => [...prev, layer]);
    setActiveTab('visualization');
  };

  const loadStatistics = async () => {
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30); // Last 30 days
    const endDate = new Date();

    try {
      const statsPromises = heatmapTypes.map(async type => {
        const stats = await HeatmapService.getHeatmapStatistics(
          type, startDate.toISOString(), endDate.toISOString()
        );
        return { type, stats };
      });

      const results = await Promise.all(statsPromises);
      const statsMap = new Map<string, HeatmapStatistics>();
      
      results.forEach(({ type, stats }) => {
        if (stats) {
          statsMap.set(type, stats);
        }
      });

      setStatistics(statsMap);
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  };

  const tabs = [
    { id: 'visualization', label: 'Heatmap Visualization', icon: '🔥' },
    { id: 'configuration', label: 'Layer Configuration', icon: '⚙️' },
    { id: 'statistics', label: 'Statistics', icon: '📊' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Heatmap Analytics</h1>
              <p className="text-gray-600">Visual representation of disaster impact, resource distribution, and response effectiveness</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-500">
                {selectedHeatmapType && (
                  <span className="inline-block px-2 py-1 bg-blue-100 text-blue-800 rounded">
                    {HeatmapService.getHeatmapTypeDisplayName(selectedHeatmapType)}
                  </span>
                )}
                {selectedPoint && (
                  <span className="inline-block px-2 py-1 bg-green-100 text-green-800 rounded ml-2">
                    Point Selected
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
                onClick={() => {
                  setActiveTab(tab.id as any);
                  if (tab.id === 'statistics') {
                    loadStatistics();
                  }
                }}
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
        {activeTab === 'visualization' && (
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
                        checked={showHeatmap}
                        onChange={(e) => setShowHeatmap(e.target.checked)}
                        className="mr-2"
                      />
                      <span className="text-sm">Show Heatmap</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={showPoints}
                        onChange={(e) => setShowPoints(e.target.checked)}
                        className="mr-2"
                      />
                      <span className="text-sm">Show Points</span>
                    </label>
                  </div>
                </div>

                {/* Selected Point Info */}
                {selectedPoint && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Selected Point</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Type:</span>
                        <span className="font-medium">
                          {HeatmapService.getHeatmapTypeIcon(selectedPoint.heatmapType)} {HeatmapService.getHeatmapTypeDisplayName(selectedPoint.heatmapType)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Intensity:</span>
                        <span className="font-medium">
                          {HeatmapService.formatIntensity(selectedPoint.intensity)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Weight:</span>
                        <span>{HeatmapService.formatWeight(selectedPoint.weight)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Radius:</span>
                        <span>{HeatmapService.formatRadius(selectedPoint.radius)}</span>
                      </div>
                      {selectedPoint.category && (
                        <div className="flex justify-between">
                          <span>Category:</span>
                          <span>{selectedPoint.category}</span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span>Created:</span>
                        <span>{new Date(selectedPoint.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Heatmap Layers */}
                {heatmapLayers.length > 0 && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Generated Layers</h3>
                    <div className="space-y-2">
                      {heatmapLayers.map(layer => (
                        <div key={layer.id} className="p-2 bg-gray-50 rounded text-sm">
                          <div className="font-medium">{layer.name}</div>
                          <div className="text-gray-600">
                            {HeatmapService.getHeatmapTypeDisplayName(layer.heatmapType)}
                          </div>
                          <div className="text-xs text-gray-500">
                            {layer.dataPointsCount} points
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right Panel - Map */}
            <div className="lg:col-span-3">
              <div className="bg-white rounded-lg shadow h-96 lg:h-[600px]">
                <HeatmapVisualization
                  center={mapCenter}
                  zoom={mapZoom}
                  heatmapTypes={heatmapTypes}
                  showHeatmap={showHeatmap}
                  showPoints={showPoints}
                  selectedHeatmapType={selectedHeatmapType}
                  onHeatmapTypeSelect={handleHeatmapTypeSelect}
                  onPointClick={handlePointClick}
                />
              </div>
            </div>
          </div>
        )}

        {activeTab === 'configuration' && (
          <div className="max-w-4xl mx-auto">
            <HeatmapConfigurationPanel onLayerGenerated={handleLayerGenerated} />
          </div>
        )}

        {activeTab === 'statistics' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {heatmapTypes.map(heatmapType => {
              const stats = statistics.get(heatmapType);
              const heatmapTypeInfo = availableHeatmapTypes.find(t => t.value === heatmapType);
              
              if (!stats || !heatmapTypeInfo) return null;

              return (
                <div key={heatmapType} className="bg-white p-6 rounded-lg shadow">
                  <div className="flex items-center space-x-3 mb-4">
                    <span className="text-2xl">{heatmapTypeInfo.icon}</span>
                    <div>
                      <h3 className="text-lg font-semibold">{heatmapTypeInfo.label}</h3>
                      <div 
                        className="w-4 h-4 rounded-full"
                        style={{ backgroundColor: heatmapTypeInfo.color }}
                      ></div>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span>Data Points:</span>
                      <span className="font-medium">{stats.pointCount}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Average Intensity:</span>
                      <span className="font-medium">
                        {HeatmapService.formatIntensity(stats.avgIntensity)}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Intensity Range:</span>
                      <span>
                        {HeatmapService.formatIntensity(stats.minIntensity)} - {HeatmapService.formatIntensity(stats.maxIntensity)}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Standard Deviation:</span>
                      <span>{HeatmapService.formatIntensity(stats.intensityStddev)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Average Weight:</span>
                      <span>{HeatmapService.formatWeight(stats.avgWeight)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Average Radius:</span>
                      <span>{HeatmapService.formatRadius(stats.avgRadius)}</span>
                    </div>
                  </div>

                  {/* Intensity Distribution Bar */}
                  <div className="mt-4">
                    <div className="text-sm text-gray-600 mb-2">Intensity Distribution</div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div 
                        className="h-2 rounded-full"
                        style={{ 
                          width: `${(stats.avgIntensity / stats.maxIntensity) * 100}%`,
                          backgroundColor: heatmapTypeInfo.color
                        }}
                      ></div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="bg-white border-t mt-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="text-sm text-gray-500">
            <p>
              <strong>Heatmap Analytics</strong> - Advanced data visualization for disaster relief operations 
              with real-time heatmap generation and interactive analysis capabilities.
            </p>
            <p className="mt-2">
              Features include multi-type heatmap visualization, configurable layer generation, 
              statistical analysis, and interactive point inspection for comprehensive data insights.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};



