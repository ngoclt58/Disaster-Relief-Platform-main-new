import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { MapPin, Filter, Layers, Navigation, RefreshCw, AlertTriangle } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { apiService } from '../services/api';
import { realtimeService, RealtimeEventType } from '../services/realtimeService';

const MapPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<maplibregl.Map | null>(null);
  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const [requests, setRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    severity: 'all',
    type: 'all',
    status: 'all',
    shelter: searchParams.get('shelter') === 'true'
  });
  const [showFiltersPanel, setShowFiltersPanel] = useState(false);
  const [drawingBBox, setDrawingBBox] = useState(false);
  const bboxStartRef = useRef<maplibregl.LngLat | null>(null);
  const [bbox, setBbox] = useState<[number, number, number, number] | null>(null); // [minLng,minLat,maxLng,maxLat]
  const [showDemoData, setShowDemoData] = useState(true);
  const { token } = useAuthStore();
  const [visibleCount, setVisibleCount] = useState(0);

  useEffect(() => {
    if (map.current) return; // Initialize map only once

    map.current = new maplibregl.Map({
      container: mapContainer.current!,
      style: {
        version: 8,
        sources: {
          'osm': {
            type: 'raster',
            tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
            tileSize: 256,
            attribution: '© OpenStreetMap contributors'
          }
        },
        layers: [
          {
            id: 'osm',
            type: 'raster',
            source: 'osm'
          }
        ]
      },
      center: searchParams.get('lat') && searchParams.get('lng') 
        ? [parseFloat(searchParams.get('lng')!), parseFloat(searchParams.get('lat')!)]
        : [-95.7129, 37.0902], // Center on US to show demo markers or user location
      zoom: searchParams.get('lat') && searchParams.get('lng') ? 12 : 4
    });

    map.current.on('load', () => {
      setIsMapLoaded(true);
      // Add clustered GeoJSON source for needs
      map.current!.addSource('needs', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] },
        cluster: true,
        clusterMaxZoom: 14,
        clusterRadius: 50
      } as any);

      // Cluster circles
      map.current!.addLayer({
        id: 'clusters',
        type: 'circle',
        source: 'needs',
        filter: ['has', 'point_count'],
        paint: {
          'circle-color': [
            'step',
            ['get', 'point_count'],
            '#93c5fd',
            10, '#60a5fa',
            50, '#3b82f6',
            100, '#1d4ed8'
          ],
          'circle-radius': [
            'step',
            ['get', 'point_count'],
            15,
            10, 20,
            50, 25,
            100, 30
          ],
          'circle-stroke-color': '#fff',
          'circle-stroke-width': 2
        }
      });

      // Cluster count labels
      map.current!.addLayer({
        id: 'cluster-count',
        type: 'symbol',
        source: 'needs',
        filter: ['has', 'point_count'],
        layout: {
          'text-field': ['get', 'point_count_abbreviated'],
          'text-font': ['Open Sans Bold'],
          'text-size': 12
        },
        paint: {
          'text-color': '#111827'
        }
      });

      // Unclustered points with status/severity theming
      map.current!.addLayer({
        id: 'needs-points',
        type: 'circle',
        source: 'needs',
        filter: ['!', ['has', 'point_count']],
        paint: {
          'circle-color': [
            'case',
            ['==', ['get', 'status'], 'COMPLETED'], '#10b981',
            ['==', ['get', 'status'], 'IN_PROGRESS'], '#f59e0b',
            ['==', ['get', 'status'], 'OPEN'], ['case', ['>=', ['to-number', ['get', 'severity']], 5], '#ef4444', ['>=', ['to-number', ['get', 'severity']], 4], '#f97316', ['>=', ['to-number', ['get', 'severity']], 3], '#eab308', ['>=', ['to-number', ['get', 'severity']], 2], '#3b82f6', '#6b7280'],
            '#6b7280'
          ],
          'circle-radius': 7,
          'circle-stroke-color': '#fff',
          'circle-stroke-width': 1.5
        }
      });

      // Add click handler for markers
      map.current!.on('click', 'needs-points', (e) => {
        const feature = e.features![0];
        const geometry = feature.geometry;
        // Type guard for Point geometry
        if (geometry.type !== 'Point' || !('coordinates' in geometry)) {
          return;
        }
        const coords = geometry.coordinates as [number, number];
        const coordinates: [number, number] = [coords[0], coords[1]];
        const properties = feature.properties;
        
        // Create popup content
        const popupContent = `
          <div class="p-3">
            <h3 class="font-semibold text-gray-900 mb-2">${properties.category}</h3>
            <div class="space-y-1 text-sm">
              <p><span class="font-medium">Status:</span> ${properties.status}</p>
              <p><span class="font-medium">Severity:</span> ${properties.severity}/5</p>
              ${properties.description ? `<p><span class="font-medium">Description:</span> ${properties.description}</p>` : ''}
              ${properties.address ? `<p><span class="font-medium">Address:</span> ${properties.address}</p>` : ''}
            </div>
          </div>
        `;
        
        // Create popup
        new maplibregl.Popup()
          .setLngLat(coordinates)
          .setHTML(popupContent)
          .addTo(map.current!);
      });

      // Change cursor on hover
      map.current!.on('mouseenter', 'needs-points', () => {
        map.current!.getCanvas().style.cursor = 'pointer';
      });
      
      map.current!.on('mouseleave', 'needs-points', () => {
        map.current!.getCanvas().style.cursor = '';
      });

      // Simple bbox layer
      map.current!.addSource('bbox', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      } as any);
      map.current!.addLayer({
        id: 'bbox-fill',
        type: 'fill',
        source: 'bbox',
        paint: { 'fill-color': '#3b82f6', 'fill-opacity': 0.1 }
      });
      map.current!.addLayer({
        id: 'bbox-line',
        type: 'line',
        source: 'bbox',
        paint: { 'line-color': '#3b82f6', 'line-width': 2, 'line-dasharray': [2, 2] }
      });
    });

    // Add navigation controls
    map.current.addControl(new maplibregl.NavigationControl(), 'top-right');

    // Add geolocation control
    map.current.addControl(new maplibregl.GeolocateControl({
      positionOptions: {
        enableHighAccuracy: true
      },
      trackUserLocation: true
    }), 'top-right');

    return () => {
      if (map.current) {
        map.current.remove();
        map.current = null;
      }
    };
  }, []);

  // Demo markers for showcase
  const demoMarkers = [
    {
      id: 'demo-1',
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-74.006, 40.7128] // New York
      },
      properties: {
        id: 'demo-1',
        category: 'Medical Emergency',
        severity: 5,
        status: 'OPEN',
        description: 'Demo: Medical emergency in Manhattan',
        address: '123 Broadway, New York, NY'
      }
    },
    {
      id: 'demo-2',
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-87.6298, 41.8781] // Chicago
      },
      properties: {
        id: 'demo-2',
        category: 'Evacuation',
        severity: 4,
        status: 'IN_PROGRESS',
        description: 'Demo: Evacuation needed in downtown Chicago',
        address: '456 Michigan Ave, Chicago, IL'
      }
    },
    {
      id: 'demo-3',
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-122.4194, 37.7749] // San Francisco
      },
      properties: {
        id: 'demo-3',
        category: 'Food Request',
        severity: 3,
        status: 'OPEN',
        description: 'Demo: Food assistance needed in SF',
        address: '789 Market St, San Francisco, CA'
      }
    },
    {
      id: 'demo-4',
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-80.1918, 25.7617] // Miami
      },
      properties: {
        id: 'demo-4',
        category: 'Water Request',
        severity: 2,
        status: 'COMPLETED',
        description: 'Demo: Water supply request in Miami',
        address: '321 Ocean Dr, Miami, FL'
      }
    },
    {
      id: 'demo-5',
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-97.5164, 32.7767] // Dallas
      },
      properties: {
        id: 'demo-5',
        category: 'Shelter',
        severity: 3,
        status: 'OPEN',
        description: 'Demo: Emergency shelter needed in Dallas',
        address: '654 Main St, Dallas, TX'
      }
    }
  ];

  // Initialize shelter filter from URL params
  useEffect(() => {
    const shelterParam = searchParams.get('shelter');
    if (shelterParam === 'true' && !filters.shelter) {
      setFilters(prev => ({ ...prev, shelter: true }));
    }
  }, [searchParams]);

  // Fetch requests data
  useEffect(() => {
    fetchRequests();
  }, [filters, bbox]);

  const fetchRequests = async () => {
    // Prevent overlapping calls
    if (loading) {
      console.warn('Requests fetch already in progress, skipping duplicate call');
      return;
    }
    
    try {
      setLoading(true);
      const params: any = {};
      
      // Apply filters to API params (only when not 'all')
      if (filters.type !== 'all') {
        params.type = filters.type;
      }
      if (filters.status !== 'all') {
        params.status = filters.status;
      }
      if (filters.severity !== 'all') {
        params.severity = filters.severity;
      }
      
      // If shelter filter is active, filter by type "Shelter"
      if (filters.shelter) {
        params.type = 'Shelter';
      }
      
      if (bbox) {
        // bbox as "minLng,minLat,maxLng,maxLat"
        params.bbox = bbox.join(',');
      }
      const data = await apiService.getRequests(params);
      setRequests(data.content || []);
    } catch (error) {
      console.error('Failed to fetch requests:', error);
    } finally {
      setLoading(false);
    }
  };

  // Update vector layer data when requests change
  useEffect(() => {
    if (!map.current || !isMapLoaded) return;
    
    let filteredRequests = (requests || []) as any[];

    // Apply filters on client as an extra safety (in case backend ignores some filters)
    if (filters.shelter) {
      filteredRequests = filteredRequests.filter((r: any) => r.type === 'Shelter' || r.category === 'Shelter');
    }
    if (filters.type !== 'all') {
      filteredRequests = filteredRequests.filter((r: any) =>
        (r.type || r.category || '').toLowerCase() === filters.type.toLowerCase()
      );
    }
    if (filters.status !== 'all') {
      filteredRequests = filteredRequests.filter((r: any) => (r.status || '').toLowerCase() === filters.status.toLowerCase());
    }
    if (filters.severity !== 'all') {
      filteredRequests = filteredRequests.filter((r: any) => String(r.severity || '') === String(filters.severity));
    }
    if (bbox) {
      const [minLng, minLat, maxLng, maxLat] = bbox;
      filteredRequests = filteredRequests.filter((r: any) => {
        const coords = r.location?.coordinates;
        if (!coords || coords.length < 2) return false;
        const [lng, lat] = coords;
        return lng >= minLng && lng <= maxLng && lat >= minLat && lat <= maxLat;
      });
    }
    
    // Use real data if available, otherwise show demo markers
    const realFeatures = filteredRequests
      .filter((r: any) => r.location?.coordinates)
      .map((r: any) => ({
        type: 'Feature',
        geometry: {
          type: 'Point',
          coordinates: [r.location.coordinates[0], r.location.coordinates[1]]
        },
        properties: {
          id: r.id,
          category: r.category || r.type,
          severity: r.severity,
          status: r.status,
          description: r.description,
          address: r.address
        }
      }));
    
    // Use demo markers if enabled, otherwise use real data
    // Apply same filters to demo markers for consistent UX
    const demoFeatures = demoMarkers.filter((m: any) => {
      const p = m.properties || {};
      
      if (filters.shelter && p.category !== 'Shelter') {
        return false;
      }
      if (filters.type !== 'all') {
        // Map logical type to demo category text
        const typeToCategory: Record<string, string[]> = {
          food: ['Food Request'],
          water: ['Water Request'],
          medical: ['Medical Emergency'],
          evacuation: ['Evacuation'],
          shelter: ['Shelter'],
          sos: [],
          other: []
        };
        const allowedCategories = typeToCategory[filters.type] || [];
        if (allowedCategories.length && !allowedCategories.includes(p.category)) {
          return false;
        }
      }
      if (filters.severity !== 'all' && String(p.severity || '') !== String(filters.severity)) {
        return false;
      }
      if (filters.status !== 'all') {
        const statusMap: Record<string, string[]> = {
          new: ['OPEN'],
          assigned: ['OPEN'],
          in_progress: ['IN_PROGRESS'],
          completed: ['COMPLETED'],
          cancelled: []
        };
        const allowedStatuses = statusMap[filters.status] || [];
        if (allowedStatuses.length && !allowedStatuses.includes(p.status)) {
          return false;
        }
      }
      if (bbox) {
        const [minLng, minLat, maxLng, maxLat] = bbox;
        const coords = m.geometry?.coordinates;
        if (!coords || coords.length < 2) return false;
        const [lng, lat] = coords;
        if (!(lng >= minLng && lng <= maxLng && lat >= minLat && lat <= maxLat)) {
          return false;
        }
      }
      return true;
    });
    const features = showDemoData ? demoFeatures : realFeatures;
    
    const data = { type: 'FeatureCollection', features } as any;
    const src = map.current.getSource('needs') as maplibregl.GeoJSONSource;
    if (src) {
      src.setData(data as any);
    }
    setVisibleCount(features.length);
  }, [isMapLoaded, requests, showDemoData, filters.shelter, filters.type, filters.status, filters.severity, bbox]);

  // Set up real-time updates
  useEffect(() => {
    const unsubscribe = realtimeService.subscribe('needs.created' as RealtimeEventType, (event) => {
      console.log('New need created:', event.data);
      fetchRequests(); // Refresh requests
    });
    
    const unsubscribeUpdated = realtimeService.subscribe('needs.updated' as RealtimeEventType, (event) => {
      console.log('Need updated:', event.data);
      fetchRequests(); // Refresh requests
    });
    
    return () => {
      unsubscribe();
      unsubscribeUpdated();
    };
  }, []);

  const getSeverityColor = (severity: number) => {
    switch (severity) {
      case 5: return '#ef4444'; // red
      case 4: return '#f97316'; // orange
      case 3: return '#eab308'; // yellow
      case 2: return '#3b82f6'; // blue
      default: return '#6b7280'; // gray
    }
  };

  // BBox drawing handlers
  useEffect(() => {
    if (!map.current) return;
    const m = map.current;
    const onMouseDown = (e: maplibregl.MapMouseEvent) => {
      if (!drawingBBox) return;
      
      // Start drawing interaction
      bboxStartRef.current = e.lngLat;
      m.getCanvas().style.cursor = 'crosshair';
      // Temporarily disable drag-to-pan so drawing feels natural
      if (m.dragPan && m.dragPan.isEnabled()) {
        m.dragPan.disable();
      }

      const onMove = (ev: any) => {
        if (!bboxStartRef.current) return;
        const minLng = Math.min(bboxStartRef.current.lng, ev.lngLat.lng);
        const minLat = Math.min(bboxStartRef.current.lat, ev.lngLat.lat);
        const maxLng = Math.max(bboxStartRef.current.lng, ev.lngLat.lng);
        const maxLat = Math.max(bboxStartRef.current.lat, ev.lngLat.lat);
        const poly = {
          type: 'FeatureCollection',
          features: [
            {
              type: 'Feature',
              geometry: {
                type: 'Polygon',
                coordinates: [[
                  [minLng, minLat], [maxLng, minLat], [maxLng, maxLat], [minLng, maxLat], [minLng, minLat]
                ]]
              },
              properties: {}
            }
          ]
        } as any;
        (m.getSource('bbox') as maplibregl.GeoJSONSource)?.setData(poly);
      };
      const onUp = (ev: any) => {
        m.getCanvas().style.cursor = '';
        // Re-enable map panning
        if (m.dragPan && !m.dragPan.isEnabled()) {
          m.dragPan.enable();
        }
        m.off('mousemove', onMove);
        m.off('mouseup', onUp);
        if (!bboxStartRef.current) return;
        const minLng = Math.min(bboxStartRef.current.lng, ev.lngLat.lng);
        const minLat = Math.min(bboxStartRef.current.lat, ev.lngLat.lat);
        const maxLng = Math.max(bboxStartRef.current.lng, ev.lngLat.lng);
        const maxLat = Math.max(bboxStartRef.current.lat, ev.lngLat.lat);
        setBbox([minLng, minLat, maxLng, maxLat]);
        bboxStartRef.current = null;
        setDrawingBBox(false);
      };
      m.on('mousemove', onMove);
      m.on('mouseup', onUp);
    };
    m.on('mousedown', onMouseDown);
    return () => { m.off('mousedown', onMouseDown); };
  }, [drawingBBox]);

  return (
    <div className="h-screen flex flex-col">
      {/* Map Controls */}
      <div className="bg-white shadow-sm border-b p-4 flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <h2 className="text-lg font-semibold text-gray-900">
            {filters.shelter ? 'Shelter Map' : 'Live Map'}
          </h2>
          {filters.shelter && (
            <>
              <span className="px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm font-medium">
                Showing Shelters Only
              </span>
              <button 
                onClick={() => {
                  setFilters(prev => ({ ...prev, shelter: false }));
                  // Remove shelter param from URL
                  const newParams = new URLSearchParams(searchParams);
                  newParams.delete('shelter');
                  window.history.replaceState({}, '', `${window.location.pathname}${newParams.toString() ? '?' + newParams.toString() : ''}`);
                }}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-sm"
              >
                Clear Filter
              </button>
            </>
          )}
          <div className="flex items-center space-x-2">
            <button
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                showFiltersPanel
                  ? 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setShowFiltersPanel(v => !v)}
            >
              <Filter className="w-4 h-4 mr-1" />
              Filters
            </button>
            <button
              className="flex items-center px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
              onClick={() => {
                if (drawingBBox) {
                  // Cancel current drawing and clear existing bbox filter
                  setDrawingBBox(false);
                  if (bbox) {
                    setBbox(null);
                    if (map.current) {
                      const src = map.current.getSource('bbox') as maplibregl.GeoJSONSource;
                      src?.setData({ type: 'FeatureCollection', features: [] } as any);
                    }
                  }
                } else {
                  // Start drawing mode
                  setDrawingBBox(true);
                }
              }}
            >
              <Layers className="w-4 h-4 mr-1" />
              {drawingBBox ? 'Cancel BBox' : 'Draw BBox'}
            </button>
            <button 
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                showDemoData 
                  ? 'bg-blue-100 text-blue-700 hover:bg-blue-200' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setShowDemoData(!showDemoData)}
            >
              <MapPin className="w-4 h-4 mr-1" />
              {showDemoData ? 'Demo Data' : 'Live Data'}
            </button>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <div className="text-sm text-gray-600 flex items-center gap-3">
            <span>
              <span className="font-medium">{visibleCount}</span>{' '}
              {showDemoData ? 'demo' : 'active'} requests
            </span>
            {bbox && (
              <span className="px-2 py-1 bg-gray-100 rounded text-xs">BBox active</span>
            )}
          </div>
          <button 
            onClick={fetchRequests}
            disabled={loading}
            className="p-2 text-gray-400 hover:text-gray-600 disabled:opacity-50"
          >
            <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Filters panel */}
      {showFiltersPanel && (
        <div className="bg-white border-b px-4 py-3 flex flex-wrap items-center gap-4">
          <div className="flex items-center space-x-2">
            <label className="text-sm text-gray-600">Severity</label>
            <select
              className="border rounded-md px-2 py-1 text-sm"
              value={filters.severity}
              onChange={e => setFilters(prev => ({ ...prev, severity: e.target.value }))}
            >
              <option value="all">All</option>
              <option value="5">5 - Critical</option>
              <option value="4">4 - High</option>
              <option value="3">3 - Medium</option>
              <option value="2">2 - Low</option>
            </select>
          </div>
          <div className="flex items-center space-x-2">
            <label className="text-sm text-gray-600">Type</label>
            <select
              className="border rounded-md px-2 py-1 text-sm"
              value={filters.type}
              onChange={e => setFilters(prev => ({ ...prev, type: e.target.value }))}
            >
              <option value="all">All</option>
              <option value="food">Food</option>
              <option value="water">Water</option>
              <option value="medical">Medical</option>
              <option value="evacuation">Evacuation</option>
              <option value="shelter">Shelter</option>
              <option value="sos">SOS</option>
              <option value="other">Other</option>
            </select>
          </div>
          <div className="flex items-center space-x-2">
            <label className="text-sm text-gray-600">Status</label>
            <select
              className="border rounded-md px-2 py-1 text-sm"
              value={filters.status}
              onChange={e => setFilters(prev => ({ ...prev, status: e.target.value }))}
            >
              <option value="all">All</option>
              <option value="new">New</option>
              <option value="assigned">Assigned</option>
              <option value="in_progress">In progress</option>
              <option value="completed">Completed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
          <button
            className="ml-auto px-3 py-1 text-sm text-gray-600 hover:text-gray-900"
            onClick={() =>
              setFilters(prev => ({
                ...prev,
                severity: 'all',
                type: 'all',
                status: 'all'
              }))
            }
          >
            Clear filters
          </button>
        </div>
      )}

      {/* Map Container */}
      <div className="flex-1 relative">
        <div ref={mapContainer} className="w-full h-full" />
        {/* Real-time connection status */}
        {isMapLoaded && (
          <div className="absolute top-4 right-4 bg-white rounded-lg shadow-lg p-2">
            <div className="flex items-center space-x-2">
              <div className={`w-2 h-2 rounded-full ${realtimeService.isConnected() ? 'bg-green-500' : 'bg-red-500'}`}></div>
              <span className="text-xs text-gray-600">
                {realtimeService.isConnected() ? 'Live' : 'Disconnected'}
              </span>
            </div>
          </div>
        )}
        
        {/* Map Overlay Info */}
        <div className="absolute top-4 left-4 bg-white rounded-lg shadow-lg p-4 max-w-sm">
          <h3 className="font-semibold text-gray-900 mb-2">Legend</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <div className="w-4 h-4 rounded-full bg-red-500"></div>
              <span className="text-sm text-gray-700">Medical Emergency (5)</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-4 h-4 rounded-full bg-orange-500"></div>
              <span className="text-sm text-gray-700">Evacuation (4)</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-4 h-4 rounded-full bg-yellow-500"></div>
              <span className="text-sm text-gray-700">Food Request (3)</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-4 h-4 rounded-full bg-blue-500"></div>
              <span className="text-sm text-gray-700">Water Request (2)</span>
            </div>
          </div>
        </div>

        {/* Map Status */}
        {!isMapLoaded && (
          <div className="absolute inset-0 bg-gray-100 flex items-center justify-center">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
              <p className="text-gray-600">Loading map...</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MapPage;
