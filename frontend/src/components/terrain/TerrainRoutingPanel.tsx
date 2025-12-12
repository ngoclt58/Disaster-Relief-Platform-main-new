import React, { useState } from 'react';
import { TerrainService, TerrainRoute } from '../../services/terrainService';

interface TerrainRoutingPanelProps {
  onRouteCalculated?: (routes: TerrainRoute[]) => void;
  onRouteSelected?: (route: TerrainRoute) => void;
}

export const TerrainRoutingPanel: React.FC<TerrainRoutingPanelProps> = ({
  onRouteCalculated,
  onRouteSelected
}) => {
  const [startLon, setStartLon] = useState(0);
  const [startLat, setStartLat] = useState(0);
  const [endLon, setEndLon] = useState(0);
  const [endLat, setEndLat] = useState(0);
  const [maxSlope, setMaxSlope] = useState(15);
  const [minAccessibility, setMinAccessibility] = useState(0.7);
  const [searchRadius, setSearchRadius] = useState(1000);
  const [isCalculating, setIsCalculating] = useState(false);
  const [routes, setRoutes] = useState<TerrainRoute[]>([]);
  const [selectedRoute, setSelectedRoute] = useState<TerrainRoute | null>(null);
  const [error, setError] = useState<string | null>(null);

  const calculateRoute = async () => {
    if (startLon === 0 && startLat === 0 && endLon === 0 && endLat === 0) {
      setError('Please enter valid start and end coordinates');
      return;
    }

    setIsCalculating(true);
    setError(null);

    try {
      const route = await TerrainService.calculateTerrainRoute(
        startLon, startLat, endLon, endLat, {
          maxSlope,
          minAccessibilityScore: minAccessibility,
          searchRadius
        }
      );

      if (route) {
        setRoutes([route]);
        setSelectedRoute(route);
        onRouteCalculated?.([route]);
        onRouteSelected?.(route);
      } else {
        setError('No accessible route found');
      }
    } catch (err) {
      setError('Failed to calculate route');
      console.error('Route calculation error:', err);
    } finally {
      setIsCalculating(false);
    }
  };

  const findAlternativeRoutes = async () => {
    if (startLon === 0 && startLat === 0 && endLon === 0 && endLat === 0) {
      setError('Please enter valid start and end coordinates');
      return;
    }

    setIsCalculating(true);
    setError(null);

    try {
      const alternativeRoutes = await TerrainService.findAlternativeRoutes(
        startLon, startLat, endLon, endLat, {
          maxSlope,
          minAccessibilityScore: minAccessibility,
          searchRadius,
          maxAlternativeRoutes: 5
        }
      );

      setRoutes(alternativeRoutes);
      if (alternativeRoutes.length > 0) {
        setSelectedRoute(alternativeRoutes[0]);
        onRouteCalculated?.(alternativeRoutes);
        onRouteSelected?.(alternativeRoutes[0]);
      } else {
        setError('No alternative routes found');
      }
    } catch (err) {
      setError('Failed to find alternative routes');
      console.error('Alternative routes error:', err);
    } finally {
      setIsCalculating(false);
    }
  };

  const selectRoute = (route: TerrainRoute) => {
    setSelectedRoute(route);
    onRouteSelected?.(route);
  };

  const getDifficultyColor = (score: number) => {
    if (score <= 2) return 'text-green-600';
    if (score <= 4) return 'text-yellow-600';
    if (score <= 6) return 'text-orange-600';
    return 'text-red-600';
  };

  const getAccessibilityColor = (score: number) => {
    if (score >= 0.8) return 'text-green-600';
    if (score >= 0.6) return 'text-yellow-600';
    if (score >= 0.4) return 'text-orange-600';
    return 'text-red-600';
  };

  const formatDistance = (meters: number) => {
    if (meters < 1000) {
      return `${meters.toFixed(0)}m`;
    } else {
      return `${(meters / 1000).toFixed(1)}km`;
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg max-w-4xl">
      <h2 className="text-xl font-bold mb-4">Terrain-Aware Routing</h2>

      {/* Route Input */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Start Point
          </label>
          <div className="space-y-2">
            <input
              type="number"
              step="any"
              placeholder="Longitude"
              value={startLon}
              onChange={(e) => setStartLon(parseFloat(e.target.value) || 0)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="number"
              step="any"
              placeholder="Latitude"
              value={startLat}
              onChange={(e) => setStartLat(parseFloat(e.target.value) || 0)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            End Point
          </label>
          <div className="space-y-2">
            <input
              type="number"
              step="any"
              placeholder="Longitude"
              value={endLon}
              onChange={(e) => setEndLon(parseFloat(e.target.value) || 0)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="number"
              step="any"
              placeholder="Latitude"
              value={endLat}
              onChange={(e) => setEndLat(parseFloat(e.target.value) || 0)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </div>

      {/* Routing Options */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Max Slope (°)
          </label>
          <input
            type="number"
            min="0"
            max="90"
            value={maxSlope}
            onChange={(e) => setMaxSlope(parseFloat(e.target.value) || 0)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Min Accessibility
          </label>
          <input
            type="number"
            min="0"
            max="1"
            step="0.1"
            value={minAccessibility}
            onChange={(e) => setMinAccessibility(parseFloat(e.target.value) || 0)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Search Radius (m)
          </label>
          <input
            type="number"
            min="100"
            max="5000"
            value={searchRadius}
            onChange={(e) => setSearchRadius(parseFloat(e.target.value) || 1000)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-2 mb-4">
        <button
          onClick={calculateRoute}
          disabled={isCalculating}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isCalculating ? 'Calculating...' : 'Calculate Route'}
        </button>
        <button
          onClick={findAlternativeRoutes}
          disabled={isCalculating}
          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Find Alternatives
        </button>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Route Results */}
      {routes.length > 0 && (
        <div className="mb-4">
          <h3 className="font-semibold mb-2">Available Routes ({routes.length})</h3>
          <div className="space-y-2 max-h-64 overflow-y-auto">
            {routes.map((route, index) => (
              <div
                key={index}
                className={`p-3 border rounded-md cursor-pointer transition-colors ${
                  selectedRoute === route
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => selectRoute(route)}
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="font-medium">Route {index + 1}</div>
                    <div className="text-sm text-gray-600">
                      Distance: {formatDistance(route.totalDistance)}
                    </div>
                    <div className="text-sm text-gray-600">
                      Elevation: +{route.totalElevationGain.toFixed(0)}m / -{route.totalElevationLoss.toFixed(0)}m
                    </div>
                  </div>
                  <div className="text-right">
                    <div className={`text-sm font-medium ${getAccessibilityColor(route.accessibilityScore)}`}>
                      {(route.accessibilityScore * 100).toFixed(0)}% Accessible
                    </div>
                    <div className={`text-sm ${getDifficultyColor(route.difficultyScore)}`}>
                      Difficulty: {route.difficultyScore.toFixed(1)}
                    </div>
                    <div className="text-sm text-gray-600">
                      Max Slope: {route.maxSlope.toFixed(1)}°
                    </div>
                    {route.isAccessible ? (
                      <span className="inline-block px-2 py-1 text-xs bg-green-100 text-green-800 rounded">
                        Accessible
                      </span>
                    ) : (
                      <span className="inline-block px-2 py-1 text-xs bg-red-100 text-red-800 rounded">
                        Not Accessible
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Selected Route Details */}
      {selectedRoute && (
        <div className="p-4 bg-gray-50 rounded-md">
          <h3 className="font-semibold mb-2">Selected Route Details</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="font-medium">Total Distance:</span>
              <div className="text-gray-600">{formatDistance(selectedRoute.totalDistance)}</div>
            </div>
            <div>
              <span className="font-medium">Elevation Gain:</span>
              <div className="text-gray-600">+{selectedRoute.totalElevationGain.toFixed(0)}m</div>
            </div>
            <div>
              <span className="font-medium">Elevation Loss:</span>
              <div className="text-gray-600">-{selectedRoute.totalElevationLoss.toFixed(0)}m</div>
            </div>
            <div>
              <span className="font-medium">Max Slope:</span>
              <div className="text-gray-600">{selectedRoute.maxSlope.toFixed(1)}°</div>
            </div>
            <div>
              <span className="font-medium">Average Slope:</span>
              <div className="text-gray-600">{selectedRoute.avgSlope.toFixed(1)}°</div>
            </div>
            <div>
              <span className="font-medium">Difficulty Score:</span>
              <div className={`font-medium ${getDifficultyColor(selectedRoute.difficultyScore)}`}>
                {selectedRoute.difficultyScore.toFixed(1)}
              </div>
            </div>
            <div>
              <span className="font-medium">Accessibility Score:</span>
              <div className={`font-medium ${getAccessibilityColor(selectedRoute.accessibilityScore)}`}>
                {(selectedRoute.accessibilityScore * 100).toFixed(1)}%
              </div>
            </div>
            <div>
              <span className="font-medium">Status:</span>
              <div className={selectedRoute.isAccessible ? 'text-green-600' : 'text-red-600'}>
                {selectedRoute.isAccessible ? 'Accessible' : 'Not Accessible'}
              </div>
            </div>
          </div>

          {/* Route Segments */}
          <div className="mt-4">
            <h4 className="font-medium mb-2">Route Segments ({selectedRoute.segments.length})</h4>
            <div className="max-h-32 overflow-y-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-1">Segment</th>
                    <th className="text-left py-1">Distance</th>
                    <th className="text-left py-1">Slope</th>
                    <th className="text-left py-1">Elev. Gain</th>
                    <th className="text-left py-1">Elev. Loss</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedRoute.segments.map((segment, index) => (
                    <tr key={index} className="border-b">
                      <td className="py-1">{index + 1}</td>
                      <td className="py-1">{formatDistance(segment.distance)}</td>
                      <td className="py-1">{segment.slope.toFixed(1)}°</td>
                      <td className="py-1">+{segment.elevationGain.toFixed(0)}m</td>
                      <td className="py-1">-{segment.elevationLoss.toFixed(0)}m</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Instructions */}
      <div className="text-sm text-gray-600 mt-4">
        <p className="mb-2">
          <strong>Instructions:</strong>
        </p>
        <ul className="list-disc list-inside space-y-1">
          <li>Enter start and end coordinates for your route</li>
          <li>Adjust routing parameters based on your requirements</li>
          <li>Click "Calculate Route" for a single optimal route</li>
          <li>Click "Find Alternatives" for multiple route options</li>
          <li>Select a route to view detailed information</li>
        </ul>
      </div>
    </div>
  );
};



