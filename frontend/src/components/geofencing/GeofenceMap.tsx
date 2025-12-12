import React, { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import { GeofencingService, Geofence, GeofenceEvent, GeofenceAlert } from '../../services/geofencingService';
import { MapComponent } from '../map/MapComponent';

interface GeofenceMapProps {
  center: [number, number];
  zoom: number;
  geofences: Geofence[];
  events: GeofenceEvent[];
  alerts: GeofenceAlert[];
  showGeofences: boolean;
  showEvents: boolean;
  showAlerts: boolean;
  selectedGeofenceId?: number;
  onGeofenceClick?: (geofence: Geofence) => void;
  onEventClick?: (event: GeofenceEvent) => void;
  onAlertClick?: (alert: GeofenceAlert) => void;
}

export const GeofenceMap: React.FC<GeofenceMapProps> = ({
  center,
  zoom,
  geofences,
  events,
  alerts,
  showGeofences,
  showEvents,
  showAlerts,
  selectedGeofenceId,
  onGeofenceClick,
  onEventClick,
  onAlertClick
}) => {
  const mapRef = useRef<maplibregl.Map | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);

  useEffect(() => {
    if (mapRef.current && mapLoaded && mapRef.current.loaded() && mapRef.current.isStyleLoaded()) {
      updateMapLayers();
    } else if (mapRef.current && mapLoaded) {
      // Wait for style to load
      const checkStyle = () => {
        if (mapRef.current && mapRef.current.isStyleLoaded()) {
          updateMapLayers();
        } else {
          setTimeout(checkStyle, 100);
        }
      };
      checkStyle();
    }
  }, [geofences, events, alerts, showGeofences, showEvents, showAlerts, selectedGeofenceId, mapLoaded]);

  const handleMapLoad = (map: maplibregl.Map) => {
    mapRef.current = map;
    setMapLoaded(true);
    updateMapLayers();
  };

  const updateMapLayers = () => {
    if (!mapRef.current || !mapRef.current.loaded() || !mapRef.current.isStyleLoaded()) {
      console.warn('Map not ready, skipping layer update');
      return;
    }

    // Check if map has required methods
    if (typeof mapRef.current.getLayer !== 'function' || typeof mapRef.current.getSource !== 'function') {
      console.warn('Map methods not available, skipping layer update');
      return;
    }

    // Remove existing layers
    const layerIds = [
      'geofence-fill', 'geofence-border', 'geofence-events', 'geofence-alerts'
    ];
    layerIds.forEach(layerId => {
      try {
        if (mapRef.current && mapRef.current.getLayer(layerId)) {
          mapRef.current.removeLayer(layerId);
        }
      } catch (error) {
        console.warn(`Failed to remove layer ${layerId}:`, error);
      }
    });

    // Remove dynamic geofence layers (by type)
    if (geofences && geofences.length > 0) {
      geofences.forEach(geofence => {
        const type = geofence.geofenceType.toLowerCase();
        const fillLayerId = `geofence-${type}-fill`;
        const borderLayerId = `geofence-${type}-border`;
        try {
          if (mapRef.current && mapRef.current.getLayer(fillLayerId)) {
            mapRef.current.removeLayer(fillLayerId);
          }
          if (mapRef.current && mapRef.current.getLayer(borderLayerId)) {
            mapRef.current.removeLayer(borderLayerId);
          }
        } catch (error) {
          console.warn(`Failed to remove geofence layer:`, error);
        }
      });
    }

    const sourceIds = [
      'geofence-source', 'geofence-events-source', 'geofence-alerts-source'
    ];
    sourceIds.forEach(sourceId => {
      try {
        if (mapRef.current && mapRef.current.getSource(sourceId)) {
          mapRef.current.removeSource(sourceId);
        }
      } catch (error) {
        console.warn(`Failed to remove source ${sourceId}:`, error);
      }
    });

    // Remove dynamic geofence sources (by type)
    if (geofences && geofences.length > 0) {
      geofences.forEach(geofence => {
        const type = geofence.geofenceType.toLowerCase();
        const sourceId = `geofence-${type}-source`;
        try {
          if (mapRef.current && mapRef.current.getSource(sourceId)) {
            mapRef.current.removeSource(sourceId);
          }
        } catch (error) {
          console.warn(`Failed to remove geofence source:`, error);
        }
      });
    }

    // Add geofence layers
    if (showGeofences && geofences.length > 0) {
      addGeofenceLayers();
    }

    // Add event layers
    if (showEvents && events.length > 0) {
      addEventLayers();
    }

    // Add alert layers
    if (showAlerts && alerts.length > 0) {
      addAlertLayers();
    }
  };

  const addGeofenceLayers = () => {
    if (!mapRef.current || !mapRef.current.loaded() || !mapRef.current.isStyleLoaded()) {
      console.warn('Map not ready, cannot add geofence layers');
      return;
    }

    if (typeof mapRef.current.addSource !== 'function' || typeof mapRef.current.addLayer !== 'function') {
      console.warn('Map methods not available, cannot add geofence layers');
      return;
    }

    // Group geofences by type
    const geofencesByType = geofences.reduce((acc, geofence) => {
      if (!acc[geofence.geofenceType]) {
        acc[geofence.geofenceType] = [];
      }
      acc[geofence.geofenceType].push(geofence);
      return acc;
    }, {} as Record<string, Geofence[]>);

    Object.entries(geofencesByType).forEach(([type, typeGeofences]) => {
      const sourceId = `geofence-${type.toLowerCase()}-source`;
      const fillLayerId = `geofence-${type.toLowerCase()}-fill`;
      const borderLayerId = `geofence-${type.toLowerCase()}-border`;

      // Add source
      mapRef.current!.addSource(sourceId, {
        type: 'geojson',
        data: {
          type: 'FeatureCollection',
          features: typeGeofences.map(geofence => ({
            type: 'Feature',
            geometry: {
              type: 'Polygon',
              coordinates: [[]] // This would be populated with actual boundary coordinates
            },
            properties: {
              id: geofence.id,
              name: geofence.name,
              geofenceType: geofence.geofenceType,
              priority: geofence.priority,
              isActive: geofence.isActive
            }
          }))
        }
      });

      // Add fill layer
      mapRef.current!.addLayer({
        id: fillLayerId,
        type: 'fill',
        source: sourceId,
        paint: {
          'fill-color': GeofencingService.getGeofenceTypeColor(type),
          'fill-opacity': selectedGeofenceId ? 0.2 : 0.3
        }
      });

      // Add border layer
      mapRef.current!.addLayer({
        id: borderLayerId,
        type: 'line',
        source: sourceId,
        paint: {
          'line-color': GeofencingService.getGeofenceTypeColor(type),
          'line-width': selectedGeofenceId ? 3 : 2,
          'line-opacity': 0.8
        }
      });

      // Add click handler
      mapRef.current!.on('click', fillLayerId, (e) => {
        const feature = e.features?.[0];
        if (feature) {
          const geofence = geofences.find(g => g.id === feature.properties.id);
          if (geofence) {
            onGeofenceClick?.(geofence);
          }
        }
      });
    });
  };

  const addEventLayers = () => {
    if (!mapRef.current || !mapRef.current.loaded() || !mapRef.current.isStyleLoaded()) {
      console.warn('Map not ready, cannot add event layers');
      return;
    }

    if (typeof mapRef.current.addSource !== 'function' || typeof mapRef.current.addLayer !== 'function') {
      console.warn('Map methods not available, cannot add event layers');
      return;
    }

    const sourceId = 'geofence-events-source';
    const layerId = 'geofence-events';

    // Add source
    mapRef.current.addSource(sourceId, {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: events.map(event => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [event.longitude, event.latitude]
          },
          properties: {
            id: event.id,
            eventType: event.eventType,
            severity: event.severity,
            entityType: event.entityType,
            entityName: event.entityName,
            occurredAt: event.occurredAt
          }
        }))
      }
    });

    // Add circle layer
    mapRef.current.addLayer({
      id: layerId,
      type: 'circle',
      source: sourceId,
      paint: {
        'circle-radius': [
          'interpolate',
          ['linear'],
          ['get', 'severity'],
          0, 4, // INFO
          1, 6, // LOW
          2, 8, // MEDIUM
          3, 10, // HIGH
          4, 12  // CRITICAL
        ],
        'circle-color': [
          'case',
          ['==', ['get', 'severity'], 'CRITICAL'], '#ff0000',
          ['==', ['get', 'severity'], 'HIGH'], '#ff4400',
          ['==', ['get', 'severity'], 'MEDIUM'], '#ff8800',
          ['==', ['get', 'severity'], 'LOW'], '#ffff00',
          '#00ff00' // INFO
        ],
        'circle-opacity': 0.8,
        'circle-stroke-width': 2,
        'circle-stroke-color': '#ffffff'
      }
    });

    // Add click handler
    mapRef.current.on('click', layerId, (e) => {
      const feature = e.features?.[0];
      if (feature) {
        const event = events.find(e => e.id === feature.properties.id);
        if (event) {
          onEventClick?.(event);
        }
      }
    });
  };

  const addAlertLayers = () => {
    if (!mapRef.current || !mapRef.current.loaded() || !mapRef.current.isStyleLoaded()) {
      console.warn('Map not ready, cannot add alert layers');
      return;
    }

    if (typeof mapRef.current.addSource !== 'function' || typeof mapRef.current.addLayer !== 'function') {
      console.warn('Map methods not available, cannot add alert layers');
      return;
    }

    const sourceId = 'geofence-alerts-source';
    const layerId = 'geofence-alerts';

    // Add source
    mapRef.current.addSource(sourceId, {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: alerts.map(alert => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [0, 0] // This would be populated with actual coordinates
          },
          properties: {
            id: alert.id,
            alertType: alert.alertType,
            severity: alert.severity,
            status: alert.status,
            title: alert.title,
            createdAt: alert.createdAt
          }
        }))
      }
    });

    // Add symbol layer
    mapRef.current.addLayer({
      id: layerId,
      type: 'symbol',
      source: sourceId,
      layout: {
        'icon-image': 'alert-icon',
        'icon-size': 1.5,
        'icon-allow-overlap': true
      },
      paint: {
        'icon-color': [
          'case',
          ['==', ['get', 'severity'], 'CRITICAL'], '#ff0000',
          ['==', ['get', 'severity'], 'HIGH'], '#ff4400',
          ['==', ['get', 'severity'], 'MEDIUM'], '#ff8800',
          ['==', ['get', 'severity'], 'LOW'], '#ffff00',
          '#00ff00' // INFO
        ]
      }
    });

    // Add click handler
    mapRef.current.on('click', layerId, (e) => {
      const feature = e.features?.[0];
      if (feature) {
        const alert = alerts.find(a => a.id === feature.properties.id);
        if (alert) {
          onAlertClick?.(alert);
        }
      }
    });
  };

  const handleMapClick = (event: maplibregl.MapMouseEvent) => {
    // Handle map clicks for geofence selection
    if (onGeofenceClick) {
      const features = mapRef.current?.queryRenderedFeatures(event.point, {
        layers: geofences.map(g => `geofence-${g.geofenceType.toLowerCase()}-fill`)
      });

      if (features && features.length > 0) {
        const feature = features[0];
        const geofence = geofences.find(g => g.id === feature.properties.id);
        if (geofence) {
          onGeofenceClick(geofence);
        }
      }
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
      
      {/* Map Controls */}
      <div className="absolute top-4 right-4 bg-white p-4 rounded shadow">
        <h3 className="font-semibold mb-3">Geofence Controls</h3>
        
        <div className="space-y-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showGeofences}
              onChange={() => {
                // This would be handled by parent component
              }}
              className="mr-2"
            />
            <span className="text-sm">Show Geofences</span>
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showEvents}
              onChange={() => {
                // This would be handled by parent component
              }}
              className="mr-2"
            />
            <span className="text-sm">Show Events</span>
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showAlerts}
              onChange={() => {
                // This would be handled by parent component
              }}
              className="mr-2"
            />
            <span className="text-sm">Show Alerts</span>
          </label>
        </div>
      </div>

      {/* Legend */}
      <div className="absolute bottom-4 right-4 bg-white p-4 rounded shadow">
        <h3 className="font-semibold mb-2">Geofence Types</h3>
        <div className="space-y-1 text-sm">
          {geofences.map(geofence => (
            <div key={geofence.id} className="flex items-center space-x-2">
              <div 
                className="w-3 h-3 rounded"
                style={{ backgroundColor: GeofencingService.getGeofenceTypeColor(geofence.geofenceType) }}
              ></div>
              <span>{GeofencingService.getGeofenceTypeDisplayName(geofence.geofenceType)}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Event Legend */}
      {showEvents && (
        <div className="absolute bottom-4 left-4 bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2">Event Severity</h3>
          <div className="space-y-1 text-sm">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-red-500"></div>
              <span>Critical</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-orange-500"></div>
              <span>High</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
              <span>Medium</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-green-500"></div>
              <span>Low/Info</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};



