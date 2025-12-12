import React, { useState, useEffect } from 'react';
import { IndoorNavigationService, IndoorMap, IndoorNode, IndoorRoute } from '../../services/indoorNavigationService';
import { MapPin, Navigation, Building, ArrowUpRightFromCircle, ArrowUpDown, AlertTriangle, Search, Route, Clock, Users } from 'lucide-react';

interface IndoorNavigationPanelProps {
  mapId: number;
  onRouteSelect?: (route: IndoorRoute) => void;
  className?: string;
}

export const IndoorNavigationPanel: React.FC<IndoorNavigationPanelProps> = ({
  mapId,
  onRouteSelect,
  className = ''
}) => {
  const [indoorMap, setIndoorMap] = useState<IndoorMap | null>(null);
  const [nodes, setNodes] = useState<IndoorNode[]>([]);
  const [routes, setRoutes] = useState<IndoorRoute[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedFromNode, setSelectedFromNode] = useState<IndoorNode | null>(null);
  const [selectedToNode, setSelectedToNode] = useState<IndoorNode | null>(null);
  const [routeType, setRouteType] = useState<string>('SHORTEST_PATH');
  const [calculatingRoute, setCalculatingRoute] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadIndoorMapData();
  }, [mapId]);

  const loadIndoorMapData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [mapData, nodesData, routesData] = await Promise.all([
        IndoorNavigationService.getIndoorMap(mapId),
        IndoorNavigationService.getIndoorNodes(mapId),
        IndoorNavigationService.getIndoorRoutes(mapId)
      ]);

      setIndoorMap(mapData);
      setNodes(nodesData);
      setRoutes(routesData);
    } catch (err) {
      console.error('Error loading indoor map data:', err);
      setError('Failed to load indoor map data');
    } finally {
      setLoading(false);
    }
  };

  const handleCalculateRoute = async () => {
    if (!selectedFromNode || !selectedToNode) return;

    try {
      setCalculatingRoute(true);
      const route = await IndoorNavigationService.calculateRoute(
        mapId,
        selectedFromNode.id,
        selectedToNode.id,
        routeType,
        'user' // This should come from auth context
      );
      
      setRoutes(prev => [route, ...prev]);
      if (onRouteSelect) {
        onRouteSelect(route);
      }
    } catch (err) {
      console.error('Error calculating route:', err);
      setError('Failed to calculate route');
    } finally {
      setCalculatingRoute(false);
    }
  };

  const normalizedQuery = searchQuery.toLowerCase();

  const filteredNodes = nodes.filter(node => {
    const name = (node.name || '').toLowerCase();
    const nodeId = typeof node.nodeId === 'string'
      ? node.nodeId.toLowerCase()
      : '';

    if (!normalizedQuery) {
      return true;
    }

    return name.includes(normalizedQuery) || nodeId.includes(normalizedQuery);
  });

  const getNodeIcon = (nodeType: string) => {
    switch (nodeType) {
      case 'STAIRS':
        return <ArrowUpRightFromCircle className="w-4 h-4" />;
      case 'ELEVATOR':
        return <ArrowUpDown className="w-4 h-4" />;
      case 'EMERGENCY_EXIT':
        return <AlertTriangle className="w-4 h-4" />;
      default:
        return <MapPin className="w-4 h-4" />;
    }
  };

  const getRouteTypeLabel = (type: string) => {
    switch (type) {
      case 'SHORTEST_PATH':
        return 'Shortest Path';
      case 'FASTEST_PATH':
        return 'Fastest Path';
      case 'ACCESSIBLE_PATH':
        return 'Accessible Path';
      case 'EMERGENCY_EVACUATION':
        return 'Emergency Evacuation';
      default:
        return type;
    }
  };

  const getDifficultyColor = (level: string) => {
    switch (level) {
      case 'EASY':
        return 'text-green-600 bg-green-100';
      case 'MODERATE':
        return 'text-yellow-600 bg-yellow-100';
      case 'DIFFICULT':
        return 'text-orange-600 bg-orange-100';
      case 'EXPERT':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className={`flex items-center justify-center h-64 ${className}`}>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`flex items-center justify-center h-64 ${className}`}>
        <div className="text-red-600 text-center">
          <AlertTriangle className="w-8 h-8 mx-auto mb-2" />
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-white rounded-lg shadow-lg p-4 ${className}`}>
      <div className="mb-4">
        <h2 className="text-lg font-semibold mb-2">Indoor Navigation</h2>
        {indoorMap && (
          <div className="text-sm text-gray-600">
            <p className="font-medium">{indoorMap.name}</p>
            <p>{indoorMap.facilityName} - Floor {indoorMap.floorNumber}</p>
          </div>
        )}
      </div>

      {/* Route Calculation */}
      <div className="mb-6">
        <h3 className="text-md font-medium mb-3">Calculate Route</h3>
        
        {/* From Node Selection */}
        <div className="mb-3">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            From
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search nodes..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="mt-2 max-h-32 overflow-y-auto">
            {filteredNodes.map(node => (
              <button
                key={node.id}
                onClick={() => setSelectedFromNode(node)}
                className={`w-full text-left p-2 rounded-md mb-1 flex items-center space-x-2 ${
                  selectedFromNode?.id === node.id
                    ? 'bg-blue-100 text-blue-700'
                    : 'hover:bg-gray-100'
                }`}
              >
                {getNodeIcon(node.nodeType)}
                <div>
                  <p className="text-sm font-medium">{node.name || node.nodeId}</p>
                  <p className="text-xs text-gray-500">{node.nodeType}</p>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* To Node Selection */}
        <div className="mb-3">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            To
          </label>
          <div className="max-h-32 overflow-y-auto">
            {filteredNodes.map(node => (
              <button
                key={node.id}
                onClick={() => setSelectedToNode(node)}
                className={`w-full text-left p-2 rounded-md mb-1 flex items-center space-x-2 ${
                  selectedToNode?.id === node.id
                    ? 'bg-blue-100 text-blue-700'
                    : 'hover:bg-gray-100'
                }`}
              >
                {getNodeIcon(node.nodeType)}
                <div>
                  <p className="text-sm font-medium">{node.name || node.nodeId}</p>
                  <p className="text-xs text-gray-500">{node.nodeType}</p>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Route Type Selection */}
        <div className="mb-3">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Route Type
          </label>
          <select
            value={routeType}
            onChange={(e) => setRouteType(e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="SHORTEST_PATH">Shortest Path</option>
            <option value="FASTEST_PATH">Fastest Path</option>
            <option value="ACCESSIBLE_PATH">Accessible Path</option>
            <option value="EMERGENCY_EVACUATION">Emergency Evacuation</option>
          </select>
        </div>

        {/* Calculate Route Button */}
        <button
          onClick={handleCalculateRoute}
          disabled={!selectedFromNode || !selectedToNode || calculatingRoute}
          className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
        >
          {calculatingRoute ? (
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          ) : (
            <Navigation className="w-4 h-4" />
          )}
          <span>{calculatingRoute ? 'Calculating...' : 'Calculate Route'}</span>
        </button>
      </div>

      {/* Routes List */}
      <div>
        <h3 className="text-md font-medium mb-3">Recent Routes</h3>
        <div className="space-y-2 max-h-64 overflow-y-auto">
          {routes.map(route => (
            <div
              key={route.id}
              className="p-3 border border-gray-200 rounded-md hover:bg-gray-50 cursor-pointer"
              onClick={() => onRouteSelect?.(route)}
            >
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center space-x-2">
                  <Route className="w-4 h-4 text-blue-600" />
                  <span className="text-sm font-medium">{route.name || 'Generated Route'}</span>
                </div>
                <span className={`text-xs px-2 py-1 rounded-full ${getDifficultyColor(route.difficultyLevel)}`}>
                  {route.difficultyLevel}
                </span>
              </div>
              
              <div className="text-xs text-gray-600 space-y-1">
                <div className="flex items-center space-x-4">
                  <span className="flex items-center space-x-1">
                    <Navigation className="w-3 h-3" />
                    <span>{route.totalDistance.toFixed(1)}m</span>
                  </span>
                  <span className="flex items-center space-x-1">
                    <Clock className="w-3 h-3" />
                    <span>{Math.round(route.estimatedTime / 60)}min</span>
                  </span>
                </div>
                <p className="text-xs text-gray-500">
                  {getRouteTypeLabel(route.routeType)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Map Statistics */}
      <div className="mt-6 pt-4 border-t border-gray-200">
        <h3 className="text-md font-medium mb-3">Map Statistics</h3>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div className="flex items-center space-x-2">
            <MapPin className="w-4 h-4 text-green-600" />
            <span>Nodes: {nodes.length}</span>
          </div>
          <div className="flex items-center space-x-2">
            <Route className="w-4 h-4 text-blue-600" />
            <span>Routes: {routes.length}</span>
          </div>
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-4 h-4 text-red-600" />
            <span>Emergency Exits: {nodes.filter(n => n.isEmergencyExit).length}</span>
          </div>
          <div className="flex items-center space-x-2">
            <Users className="w-4 h-4 text-purple-600" />
            <span>Capacity: {nodes.reduce((sum, n) => sum + (n.capacity || 0), 0)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};



