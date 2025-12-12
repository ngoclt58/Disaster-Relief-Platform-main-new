import React, { useEffect, useState, useRef } from 'react';
import maplibregl from 'maplibre-gl';
import { IndoorNavigationService, IndoorMap, IndoorNode, IndoorEdge, IndoorRoute } from '../../services/indoorNavigationService';
import { MapPin, Navigation, Building, ArrowUpRightFromCircle, ArrowUpDown, AlertTriangle } from 'lucide-react';

interface IndoorMapViewerProps {
  mapId: number;
  onNodeSelect?: (node: IndoorNode) => void;
  onRouteSelect?: (route: IndoorRoute) => void;
  showNodes?: boolean;
  showEdges?: boolean;
  showRoutes?: boolean;
  showPositions?: boolean;
  className?: string;
}

export const IndoorMapViewer: React.FC<IndoorMapViewerProps> = ({
  mapId,
  onNodeSelect,
  onRouteSelect,
  showNodes = true,
  showEdges = true,
  showRoutes = false,
  showPositions = false,
  className = ''
}) => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<maplibregl.Map | null>(null);
  const [indoorMap, setIndoorMap] = useState<IndoorMap | null>(null);
  const [nodes, setNodes] = useState<IndoorNode[]>([]);
  const [edges, setEdges] = useState<IndoorEdge[]>([]);
  const [routes, setRoutes] = useState<IndoorRoute[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadIndoorMapData();
  }, [mapId]);

  useEffect(() => {
    if (indoorMap && mapContainer.current && !map.current) {
      initializeMap();
    }
  }, [indoorMap]);

  useEffect(() => {
    if (map.current && nodes.length > 0) {
      // Fit bounds to nodes if map is already initialized
      if (nodes.length > 0) {
        const bbox = nodes.reduce((acc: [number, number, number, number], node) => {
          const coords = (node.position as any)?.coordinates as [number, number] | undefined;
          if (!coords || !Array.isArray(coords) || coords.length < 2) {
            return acc;
          }
          const [lon, lat] = coords;
          acc[0] = Math.min(acc[0], lon);
          acc[1] = Math.min(acc[1], lat);
          acc[2] = Math.max(acc[2], lon);
          acc[3] = Math.max(acc[3], lat);
          return acc;
        }, [Infinity, Infinity, -Infinity, -Infinity] as [number, number, number, number]);
        
        if (isFinite(bbox[0])) {
          map.current.fitBounds(bbox, { padding: 50 });
        }
      }
      addNodesToMap();
    }
  }, [nodes, showNodes]);

  useEffect(() => {
    if (map.current && edges.length > 0) {
      addEdgesToMap();
    }
  }, [edges, showEdges]);

  useEffect(() => {
    if (map.current && routes.length > 0) {
      addRoutesToMap();
    }
  }, [routes, showRoutes]);

  const loadIndoorMapData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [mapData, nodesData, edgesData, routesData] = await Promise.all([
        IndoorNavigationService.getIndoorMap(mapId),
        IndoorNavigationService.getIndoorNodes(mapId),
        showEdges ? IndoorNavigationService.getIndoorEdges(mapId) : Promise.resolve([]),
        showRoutes ? IndoorNavigationService.getIndoorRoutes(mapId) : Promise.resolve([])
      ]);

      setIndoorMap(mapData);
      setNodes(nodesData);
      setEdges(edgesData);
      setRoutes(routesData);
    } catch (err) {
      console.error('Error loading indoor map data:', err);
      setError('Failed to load indoor map data');
    } finally {
      setLoading(false);
    }
  };

  const initializeMap = () => {
    if (!indoorMap || !mapContainer.current) return;

    // Calculate map bounds from nodes if available, otherwise use default
    let bbox: [number, number, number, number] = [0, 0, 1, 1];
    if (nodes.length > 0) {
      bbox = nodes.reduce((acc: [number, number, number, number], node) => {
        const coords = (node.position as any)?.coordinates as [number, number] | undefined;
        if (!coords || !Array.isArray(coords) || coords.length < 2) {
          return acc;
        }
        const [lon, lat] = coords;
        acc[0] = Math.min(acc[0], lon);
        acc[1] = Math.min(acc[1], lat);
        acc[2] = Math.max(acc[2], lon);
        acc[3] = Math.max(acc[3], lat);
        return acc;
      }, [Infinity, Infinity, -Infinity, -Infinity] as [number, number, number, number]);
      
      // If no valid bounds found, use default
      if (!isFinite(bbox[0])) {
        bbox = [0, 0, 1, 1];
      }
    }

    map.current = new maplibregl.Map({
      container: mapContainer.current,
      style: {
        version: 8,
        sources: {},
        layers: []
      },
      bounds: bbox,
      fitBoundsOptions: { padding: 50 }
    });

    map.current.on('load', () => {
      addMapLayers();
    });

    map.current.on('click', (e) => {
      const features = map.current!.queryRenderedFeatures(e.point, {
        layers: ['indoor-nodes']
      });
      
      if (features.length > 0) {
        const nodeId = features[0].properties?.nodeId;
        const node = nodes.find(n => n.nodeId === nodeId);
        if (node && onNodeSelect) {
          onNodeSelect(node);
        }
      }
    });
  };

  const addMapLayers = () => {
    if (!map.current || !indoorMap) return;

    // Add map image if available
    if (indoorMap.mapImageUrl) {
      // Calculate coordinates from nodes if available, otherwise use default
      let imageCoordinates: [[number, number], [number, number], [number, number], [number, number]] = [[0, 0], [1, 0], [1, 1], [0, 1]];
      if (nodes.length > 0) {
        const bbox = nodes.reduce((acc: [number, number, number, number], node) => {
          const coords = (node.position as any)?.coordinates as [number, number] | undefined;
          if (!coords || !Array.isArray(coords) || coords.length < 2) {
            return acc;
          }
          const [lon, lat] = coords;
          acc[0] = Math.min(acc[0], lon);
          acc[1] = Math.min(acc[1], lat);
          acc[2] = Math.max(acc[2], lon);
          acc[3] = Math.max(acc[3], lat);
          return acc;
        }, [Infinity, Infinity, -Infinity, -Infinity] as [number, number, number, number]);
        
        if (isFinite(bbox[0])) {
          // Convert bbox [minLng, minLat, maxLng, maxLat] to image coordinates
          imageCoordinates = [
            [bbox[0], bbox[1]], // bottom-left
            [bbox[2], bbox[1]], // bottom-right
            [bbox[2], bbox[3]], // top-right
            [bbox[0], bbox[3]]  // top-left
          ];
        }
      }
      
      map.current.addSource('indoor-map-image', {
        type: 'image',
        url: indoorMap.mapImageUrl,
        coordinates: imageCoordinates
      });

      map.current.addLayer({
        id: 'indoor-map-background',
        type: 'raster',
        source: 'indoor-map-image',
        paint: { 'raster-opacity': 0.8 }
      });
    }

    // Add nodes source
    map.current.addSource('indoor-nodes', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: nodes
          .filter(node => {
            const coords = (node.position as any)?.coordinates as [number, number] | undefined;
            return coords && Array.isArray(coords) && coords.length >= 2;
          })
          .map(node => ({
            type: 'Feature',
            geometry: node.position,
            properties: {
              nodeId: node.nodeId,
              name: node.name,
              nodeType: node.nodeType,
              isAccessible: node.isAccessible,
              isEmergencyExit: node.isEmergencyExit,
              isElevator: node.isElevator,
              isStairs: node.isStairs,
              floorLevel: node.floorLevel
            }
          }))
      }
    });

    // Add edges source
    map.current.addSource('indoor-edges', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: edges.map(edge => ({
          type: 'Feature',
          geometry: edge.path || {
            type: 'LineString',
            coordinates: [
              [edge.fromNodeId, 0], // Placeholder coordinates
              [edge.toNodeId, 0]
            ]
          },
          properties: {
            edgeId: edge.edgeId,
            name: edge.name,
            edgeType: edge.edgeType,
            isAccessible: edge.isAccessible,
            isEmergencyRoute: edge.isEmergencyRoute,
            distance: edge.distance
          }
        }))
      }
    });

    // Add routes source
    map.current.addSource('indoor-routes', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: routes.map(route => ({
          type: 'Feature',
          geometry: route.path,
          properties: {
            routeId: route.routeId,
            name: route.name,
            routeType: route.routeType,
            totalDistance: route.totalDistance,
            estimatedTime: route.estimatedTime,
            difficultyLevel: route.difficultyLevel
          }
        }))
      }
    });
  };

  const addNodesToMap = () => {
    if (!map.current || !showNodes) return;

    // Ensure style is fully loaded before adding layers
    if (!map.current.isStyleLoaded()) {
      map.current.once('styledata', () => addNodesToMap());
      return;
    }

    // Avoid adding duplicate layers
    if (map.current.getLayer('indoor-nodes')) {
      return;
    }

    // Add node markers
    try {
      map.current.addLayer({
        id: 'indoor-nodes',
        type: 'circle',
        source: 'indoor-nodes',
        paint: {
          'circle-radius': 8,
          'circle-color': [
            'case',
            ['get', 'isEmergencyExit'], '#ef4444',
            ['get', 'isElevator'], '#3b82f6',
            ['get', 'isStairs'], '#f59e0b',
            '#10b981'
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff'
        }
      });
    } catch (err) {
      console.error('Error adding indoor node layer:', err);
      return;
    }

    // Add node labels
    try {
      map.current.addLayer({
        id: 'indoor-node-labels',
        type: 'symbol',
        source: 'indoor-nodes',
        layout: {
          'text-field': ['get', 'name'],
          'text-font': ['Open Sans Regular'],
          'text-size': 12,
          'text-offset': [0, -2]
        },
        paint: {
          'text-color': '#374151',
          'text-halo-color': '#ffffff',
          'text-halo-width': 2
        }
      });
    } catch (err) {
      console.error('Error adding indoor node label layer:', err);
    }
  };

  const addEdgesToMap = () => {
    if (!map.current || !showEdges) return;

    if (!map.current.isStyleLoaded()) {
      map.current.once('styledata', () => addEdgesToMap());
      return;
    }

    if (map.current.getLayer('indoor-edges')) {
      return;
    }

    // Add edge lines
    try {
      map.current.addLayer({
        id: 'indoor-edges',
        type: 'line',
        source: 'indoor-edges',
        paint: {
          'line-width': 3,
          'line-color': [
            'case',
            ['get', 'isEmergencyRoute'], '#ef4444',
            ['get', 'isAccessible'], '#10b981',
            '#6b7280'
          ],
          'line-opacity': 0.8
        }
      });
    } catch (err) {
      console.error('Error adding indoor edge layer:', err);
    }
  };

  const addRoutesToMap = () => {
    if (!map.current || !showRoutes) return;

    if (!map.current.isStyleLoaded()) {
      map.current.once('styledata', () => addRoutesToMap());
      return;
    }

    if (map.current.getLayer('indoor-routes')) {
      return;
    }

    // Add route lines
    try {
      map.current.addLayer({
        id: 'indoor-routes',
        type: 'line',
        source: 'indoor-routes',
        paint: {
          'line-width': 4,
          'line-color': '#8b5cf6',
          'line-opacity': 0.9,
          'line-dasharray': [2, 2]
        }
      });
    } catch (err) {
      console.error('Error adding indoor route layer:', err);
    }
  };

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
    <div className={`relative ${className}`}>
      <div ref={mapContainer} className="w-full h-full rounded-lg" />
      
      {/* Map Controls */}
      <div className="absolute top-4 right-4 bg-white rounded-lg shadow-lg p-2 space-y-2">
        <div className="flex items-center space-x-2 text-sm">
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            <span>Accessible</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-red-500 rounded-full"></div>
            <span>Emergency Exit</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
            <span>Elevator</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
            <span>Stairs</span>
          </div>
        </div>
      </div>

      {/* Map Info */}
      {indoorMap && (
        <div className="absolute bottom-4 left-4 bg-white rounded-lg shadow-lg p-3">
          <h3 className="font-semibold text-lg">{indoorMap.name}</h3>
          <p className="text-sm text-gray-600">{indoorMap.facilityName}</p>
          <p className="text-sm text-gray-500">Floor {indoorMap.floorNumber}</p>
          <div className="mt-2 text-xs text-gray-500">
            <p>Nodes: {nodes.length}</p>
            <p>Edges: {edges.length}</p>
            <p>Routes: {routes.length}</p>
          </div>
        </div>
      )}
    </div>
  );
};



