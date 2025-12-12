import React, { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import { TerrainService, ElevationPoint, TerrainAnalysis, TerrainRoute } from '../../services/terrainService';
import { MapComponent } from '../map/MapComponent';

interface TerrainVisualizationProps {
  center: [number, number];
  zoom: number;
  showElevation?: boolean;
  showSlope?: boolean;
  showAccessibility?: boolean;
  showRoutes?: boolean;
  onTerrainAnalysis?: (analysis: TerrainAnalysis) => void;
  onRouteCalculated?: (routes: TerrainRoute[]) => void;
}

export const TerrainVisualization: React.FC<TerrainVisualizationProps> = ({
  center,
  zoom,
  showElevation = true,
  showSlope = false,
  showAccessibility = false,
  showRoutes = false,
  onTerrainAnalysis,
  onRouteCalculated
}) => {
  const mapRef = useRef<maplibregl.Map | null>(null);
  const isLoadingRef = useRef(false);
  const lastLoadParamsRef = useRef<string>('');
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  const [elevationPoints, setElevationPoints] = useState<ElevationPoint[]>([]);
  const [terrainAnalysis, setTerrainAnalysis] = useState<TerrainAnalysis[]>([]);
  const [routes, setRoutes] = useState<TerrainRoute[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Clear any pending debounce timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Wait a bit for map to be fully initialized
    if (mapRef.current && mapRef.current.loaded() && !isLoadingRef.current) {
      // Create a unique key for current load parameters
      const loadKey = `${center[0]}-${center[1]}-${zoom}-${showElevation}-${showSlope}-${showAccessibility}`;
      
      // Only load if parameters actually changed
      if (lastLoadParamsRef.current === loadKey) {
        return;
      }

      lastLoadParamsRef.current = loadKey;
      
      // Debounce to prevent rapid successive calls
      debounceTimerRef.current = setTimeout(() => {
        if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
          loadTerrainData();
        }
      }, 500);

      return () => {
        if (debounceTimerRef.current) {
          clearTimeout(debounceTimerRef.current);
        }
      };
    }
  }, [center, zoom, showElevation, showSlope, showAccessibility]);

  const loadTerrainData = async () => {
    // Prevent overlapping loads which can cause the loading UI to flicker
    if (isLoadingRef.current || loading) {
      console.warn('Terrain data load already in progress, skipping duplicate call');
      return;
    }

    if (!mapRef.current || !mapRef.current.loaded()) {
      console.warn('Map not loaded yet, skipping terrain data load');
      return;
    }

    isLoadingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      const bounds = mapRef.current.getBounds();
      if (!bounds) {
        throw new Error('Map bounds not available');
      }

      const minLon = bounds.getWest();
      const minLat = bounds.getSouth();
      const maxLon = bounds.getEast();
      const maxLat = bounds.getNorth();

      // Load elevation points
      if (showElevation) {
        const points = await TerrainService.getElevationPointsInBounds(minLon, minLat, maxLon, maxLat);
        if (points && Array.isArray(points) && points.length > 0) {
          setElevationPoints(points);
          addElevationLayer(points);
        } else {
          console.warn('No elevation points returned from API or invalid data format');
          // Don't show error if API returns empty array - this is normal for areas without data
          if (points === null || points === undefined) {
            setError('Failed to load elevation data from server');
          }
        }
      }

      // Load terrain analysis
      if (showSlope || showAccessibility) {
        const analysis = await TerrainService.findAccessibleAreas(0.4, 45);
        if (analysis && analysis.length > 0) {
          setTerrainAnalysis(analysis);
          addTerrainAnalysisLayer(analysis);
        }
      }

    } catch (err: any) {
      const errorMessage = err?.message || 'Failed to load terrain data';
      setError(errorMessage);
      console.error('Terrain data loading error:', err);
    } finally {
      setLoading(false);
      isLoadingRef.current = false;
    }
  };

  const addElevationLayer = (points: ElevationPoint[]) => {
    if (!mapRef.current || !points || !Array.isArray(points) || points.length === 0) {
      console.warn('Cannot add elevation layer: invalid points data', points);
      return;
    }

    // Check if map is fully loaded
    if (!mapRef.current.isStyleLoaded()) {
      console.warn('Map style not loaded yet, waiting...');
      mapRef.current.once('styledata', () => addElevationLayer(points));
      return;
    }

    const minElev = Math.min(...points.map(p => p.elevation));
    const maxElev = Math.max(...points.map(p => p.elevation));

    // Add elevation points as circles
    // Double-check map is still available and has getSource method
    if (!mapRef.current || typeof mapRef.current.getSource !== 'function') {
      console.warn('Map ref is null or not ready, cannot add elevation layer');
      return;
    }

    try {
      const existingSource = mapRef.current.getSource('elevation-points');
      if (existingSource) {
        if (mapRef.current.getLayer('elevation-points')) {
          mapRef.current.removeLayer('elevation-points');
        }
        if (mapRef.current.getLayer('elevation-labels')) {
          mapRef.current.removeLayer('elevation-labels');
        }
        mapRef.current.removeSource('elevation-points');
      }
    } catch (error) {
      // Source might not exist, which is fine
      console.log('No existing elevation source to remove:', error);
    }

    // Final check before adding source
    if (!mapRef.current || !mapRef.current.isStyleLoaded() || typeof mapRef.current.addSource !== 'function') {
      console.warn('Map not ready, cannot add elevation source');
      return;
    }

    try {
      mapRef.current.addSource('elevation-points', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: points.map(point => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [point.longitude, point.latitude]
          },
          properties: {
            elevation: point.elevation,
            source: point.source,
            accuracy: point.accuracy,
            resolution: point.resolution
          }
        }))
      }
    });

      if (!mapRef.current || typeof mapRef.current.addLayer !== 'function') return;

      mapRef.current.addLayer({
        id: 'elevation-points',
        type: 'circle',
        source: 'elevation-points',
        paint: {
          'circle-radius': 4,
          'circle-color': [
            'interpolate',
            ['linear'],
            ['get', 'elevation'],
            minElev, '#0066cc',
            maxElev, '#cc0000'
          ],
          'circle-opacity': 0.8
        }
      });

      // Add elevation labels
      if (!mapRef.current || typeof mapRef.current.addLayer !== 'function') return;

      mapRef.current.addLayer({
      id: 'elevation-labels',
      type: 'symbol',
      source: 'elevation-points',
      layout: {
        'text-field': ['format', ['get', 'elevation'], { 'font-scale': 0.8 }],
        'text-font': ['Open Sans Regular'],
        'text-offset': [0, -1.5],
        'text-anchor': 'center'
      },
      paint: {
        'text-color': '#000',
        'text-halo-color': '#fff',
        'text-halo-width': 1
      }
    });
    } catch (error) {
      console.error('Error adding elevation layer:', error);
      setError('Failed to add elevation layer to map');
    }
  };

  const addTerrainAnalysisLayer = (analysis: TerrainAnalysis[]) => {
    if (!mapRef.current || !analysis || !Array.isArray(analysis) || analysis.length === 0) {
      console.warn('Cannot add terrain analysis layer: invalid analysis data', analysis);
      return;
    }

    // Check if map is fully loaded
    if (!mapRef.current.isStyleLoaded()) {
      console.warn('Map style not loaded yet, waiting...');
      mapRef.current.once('styledata', () => addTerrainAnalysisLayer(analysis));
      return;
    }

    // Add terrain analysis areas
    // Double-check map is still available and has getSource method
    if (!mapRef.current || typeof mapRef.current.getSource !== 'function') {
      console.warn('Map ref is null or not ready, cannot add terrain analysis layer');
      return;
    }

    try {
      const existingSource = mapRef.current.getSource('terrain-analysis');
      if (existingSource) {
        if (mapRef.current.getLayer('terrain-analysis')) {
          mapRef.current.removeLayer('terrain-analysis');
        }
        mapRef.current.removeSource('terrain-analysis');
      }
    } catch (error) {
      // Source might not exist, which is fine
      console.log('No existing terrain analysis source to remove:', error);
    }

    // Final check before adding source
    if (!mapRef.current || !mapRef.current.isStyleLoaded() || typeof mapRef.current.addSource !== 'function') {
      console.warn('Map not ready, cannot add terrain analysis source');
      return;
    }

    try {
      // For demonstration, create simple rectangular areas
      // In a real implementation, you'd use the actual polygon data
      const features = analysis.map((item, index) => ({
        type: 'Feature',
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [center[0] - 0.01 + index * 0.005, center[1] - 0.01],
          [center[0] + 0.01 + index * 0.005, center[1] - 0.01],
          [center[0] + 0.01 + index * 0.005, center[1] + 0.01],
          [center[0] - 0.01 + index * 0.005, center[1] + 0.01],
          [center[0] - 0.01 + index * 0.005, center[1] - 0.01]
        ]]
      },
      properties: {
        id: item.id,
        analysisType: item.analysisType,
        accessibilityScore: item.accessibilityScore,
        floodRiskScore: item.floodRiskScore,
        slopeAverage: item.slopeAverage,
        slopeMaximum: item.slopeMaximum
      }
      }));

      if (!mapRef.current || typeof mapRef.current.addSource !== 'function') return;

      mapRef.current.addSource('terrain-analysis', {
        type: 'geojson',
        data: {
          type: 'FeatureCollection',
          features
        }
      });

      if (showAccessibility) {
        if (!mapRef.current || typeof mapRef.current.addLayer !== 'function') return;
        mapRef.current.addLayer({
        id: 'terrain-analysis',
        type: 'fill',
        source: 'terrain-analysis',
        paint: {
          'fill-color': [
            'interpolate',
            ['linear'],
            ['get', 'accessibilityScore'],
            0, '#ff0000',
            0.5, '#ffff00',
            1, '#00ff00'
          ],
          'fill-opacity': 0.6
        }
      });
    }

      if (showSlope) {
        if (!mapRef.current || typeof mapRef.current.addLayer !== 'function') return;
        mapRef.current.addLayer({
        id: 'terrain-slope',
        type: 'fill',
        source: 'terrain-analysis',
        paint: {
          'fill-color': [
            'interpolate',
            ['linear'],
            ['get', 'slopeMaximum'],
            0, '#00ff00',
            15, '#ffff00',
            30, '#ff8800',
            45, '#ff0000'
          ],
          'fill-opacity': 0.4
        }
      });
      }
    } catch (error) {
      console.error('Error adding terrain analysis layer:', error);
      setError('Failed to add terrain analysis layer to map');
    }
  };

  const calculateRoute = async (start: [number, number], end: [number, number]) => {
    if (!showRoutes) return;

    setLoading(true);
    try {
      const route = await TerrainService.calculateTerrainRoute(
        start[0], start[1], end[0], end[1]
      );
      
      if (route) {
        setRoutes([route]);
        addRouteLayer([route]);
        onRouteCalculated?.([route]);
      }
    } catch (err) {
      setError('Failed to calculate route');
      console.error('Route calculation error:', err);
    } finally {
      setLoading(false);
    }
  };

  const addRouteLayer = (routeData: TerrainRoute[]) => {
    if (!mapRef.current || !routeData || !Array.isArray(routeData) || routeData.length === 0) {
      console.warn('Cannot add route layer: invalid route data', routeData);
      return;
    }

    // Remove existing route layers
    ['route-line', 'route-points'].forEach(layerId => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current.removeLayer(layerId);
      }
    });
    ['route-source'].forEach(sourceId => {
      if (mapRef.current?.getSource(sourceId)) {
        mapRef.current.removeSource(sourceId);
      }
    });

    const features = routeData.map((route, index) => {
      if (!route.segments || !Array.isArray(route.segments) || route.segments.length === 0) {
        console.warn(`Route ${index} has invalid segments`, route);
        return null;
      }
      
      return {
        type: 'Feature',
        geometry: {
          type: 'LineString',
          coordinates: route.segments.map(segment => [
            segment.startPoint.longitude,
            segment.startPoint.latitude
          ]).concat([[
            route.segments[route.segments.length - 1].endPoint.longitude,
            route.segments[route.segments.length - 1].endPoint.latitude
          ]])
        },
        properties: {
          routeIndex: index,
          totalDistance: route.totalDistance,
          accessibilityScore: route.accessibilityScore,
          isAccessible: route.isAccessible
        }
      };
    }).filter((feature): feature is NonNullable<typeof feature> => feature !== null);

    if (features.length === 0) {
      console.warn('No valid route features to display');
      return;
    }

    mapRef.current.addSource('route-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features
      }
    });

    mapRef.current.addLayer({
      id: 'route-line',
      type: 'line',
      source: 'route-source',
      paint: {
        'line-color': [
          'case',
          ['get', 'isAccessible'], '#00ff00',
          '#ff0000'
        ],
        'line-width': 4,
        'line-opacity': 0.8
      }
    });

    // Add route points
    const routePoints = routeData.flatMap(route => {
      if (!route.segments || !Array.isArray(route.segments)) {
        return [];
      }
      return route.segments.map(segment => ({
        type: 'Feature',
        geometry: {
          type: 'Point',
          coordinates: [segment.startPoint.longitude, segment.startPoint.latitude]
        },
        properties: {
          slope: segment.slope,
          elevationGain: segment.elevationGain,
          elevationLoss: segment.elevationLoss
        }
      }));
    });

    mapRef.current.addSource('route-points', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: routePoints
      }
    });

    mapRef.current.addLayer({
      id: 'route-points',
      type: 'circle',
      source: 'route-points',
      paint: {
        'circle-radius': 3,
        'circle-color': [
          'interpolate',
          ['linear'],
          ['abs', ['get', 'slope']],
          0, '#00ff00',
          15, '#ffff00',
          30, '#ff8800',
          45, '#ff0000'
        ],
        'circle-opacity': 0.8
      }
    });
  };

  const handleMapLoad = (map: maplibregl.Map) => {
    mapRef.current = map;
    // Don't call loadTerrainData here - let useEffect handle it
    // This prevents duplicate calls when map loads
  };

  const handleMapClick = (event: maplibregl.MapMouseEvent) => {
    const { lng, lat } = event.lngLat;
    
    // Perform terrain analysis for clicked point
    if (onTerrainAnalysis) {
      TerrainService.getTerrainAnalysisForPoint(lng, lat)
        .then(analysis => {
          if (analysis) {
            onTerrainAnalysis(analysis);
          }
        })
        .catch(err => console.error('Terrain analysis error:', err));
    }
  };

  return (
    <div className="relative w-full h-full">
      <MapComponent
        center={center}
        zoom={zoom}
        onMapLoad={handleMapLoad}
        onMapClick={handleMapClick}
        className="w-full h-full"
      />
      
      {loading && (
        <div className="absolute top-4 left-4 bg-white p-2 rounded shadow">
          <div className="flex items-center space-x-2">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
            <span className="text-sm">Loading terrain data...</span>
          </div>
        </div>
      )}

      {error && (
        <div className="absolute top-4 right-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Terrain Controls */}
      <div className="absolute bottom-4 left-4 bg-white p-4 rounded shadow">
        <h3 className="font-semibold mb-2">Terrain Visualization</h3>
        <div className="space-y-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showElevation}
              onChange={(e) => {
                // Toggle elevation display
                if (e.target.checked && !isLoadingRef.current && !loading && mapRef.current && mapRef.current.loaded()) {
                  // Update lastLoadParamsRef to force reload
                  lastLoadParamsRef.current = '';
                  // Debounce the load
                  if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
                  }
                  debounceTimerRef.current = setTimeout(() => {
                    if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
                      loadTerrainData();
                    }
                  }, 300);
                } else if (!e.target.checked) {
                  if (mapRef.current?.getLayer('elevation-points')) {
                    mapRef.current.removeLayer('elevation-points');
                    mapRef.current.removeLayer('elevation-labels');
                    mapRef.current.removeSource('elevation-points');
                  }
                }
              }}
              className="mr-2"
            />
            Elevation Points
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showSlope}
              onChange={(e) => {
                // Toggle slope display
                if (e.target.checked && !isLoadingRef.current && !loading && mapRef.current && mapRef.current.loaded()) {
                  // Update lastLoadParamsRef to force reload
                  lastLoadParamsRef.current = '';
                  // Debounce the load
                  if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
                  }
                  debounceTimerRef.current = setTimeout(() => {
                    if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
                      loadTerrainData();
                    }
                  }, 300);
                } else if (!e.target.checked) {
                  if (mapRef.current?.getLayer('terrain-slope')) {
                    mapRef.current.removeLayer('terrain-slope');
                  }
                }
              }}
              className="mr-2"
            />
            Slope Analysis
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showAccessibility}
              onChange={(e) => {
                // Toggle accessibility display
                if (e.target.checked && !isLoadingRef.current && !loading && mapRef.current && mapRef.current.loaded()) {
                  // Update lastLoadParamsRef to force reload
                  lastLoadParamsRef.current = '';
                  // Debounce the load
                  if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
                  }
                  debounceTimerRef.current = setTimeout(() => {
                    if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
                      loadTerrainData();
                    }
                  }, 300);
                } else if (!e.target.checked) {
                  if (mapRef.current?.getLayer('terrain-analysis')) {
                    mapRef.current.removeLayer('terrain-analysis');
                  }
                }
              }}
              className="mr-2"
            />
            Accessibility
          </label>
        </div>
      </div>

      {/* Terrain Info Panel */}
      {terrainAnalysis.length > 0 && (
        <div className="absolute top-4 right-4 bg-white p-4 rounded shadow max-w-sm">
          <h3 className="font-semibold mb-2">Terrain Analysis</h3>
          <div className="space-y-1 text-sm">
            <div>Areas Analyzed: {terrainAnalysis.length}</div>
            <div>Avg Accessibility: {
              (terrainAnalysis.reduce((sum, a) => sum + a.accessibilityScore, 0) / terrainAnalysis.length).toFixed(2)
            }</div>
            <div>Max Slope: {
              Math.max(...terrainAnalysis.map(a => a.slopeMaximum)).toFixed(1)}°
            </div>
          </div>
        </div>
      )}
    </div>
  );
};



