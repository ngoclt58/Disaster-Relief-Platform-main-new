import React, { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import { SatelliteService, SatelliteImage, DamageAssessment } from '../../services/satelliteService';
import { MapComponent } from '../map/MapComponent';

interface SatelliteImageryViewerProps {
  center: [number, number];
  zoom: number;
  showImages?: boolean;
  showDamage?: boolean;
  selectedImage?: SatelliteImage;
  onImageSelect?: (image: SatelliteImage) => void;
  onDamageSelect?: (assessment: DamageAssessment) => void;
}

export const SatelliteImageryViewer: React.FC<SatelliteImageryViewerProps> = ({
  center,
  zoom,
  showImages = true,
  showDamage = true,
  selectedImage,
  onImageSelect,
  onDamageSelect
}) => {
  const mapRef = useRef<maplibregl.Map | null>(null);
  const isLoadingRef = useRef(false);
  const lastLoadParamsRef = useRef<string>('');
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  const [images, setImages] = useState<SatelliteImage[]>([]);
  const [damageAssessments, setDamageAssessments] = useState<DamageAssessment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Clear any pending debounce timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    if (!mapRef.current || !mapRef.current.loaded() || isLoadingRef.current) {
      return;
    }

    const loadKey = `${center[0]}-${center[1]}-${zoom}-${showImages}-${showDamage}`;
    if (lastLoadParamsRef.current === loadKey) {
      return;
    }
    lastLoadParamsRef.current = loadKey;

    debounceTimerRef.current = setTimeout(() => {
      if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
        loadSatelliteData();
      }
    }, 400);

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [center, zoom, showImages, showDamage]);

  const loadSatelliteData = async () => {
    if (!mapRef.current || !mapRef.current.loaded()) return;

    if (isLoadingRef.current || loading) {
      console.warn('Satellite data load already in progress, skipping duplicate call');
      return;
    }

    isLoadingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      const bounds = mapRef.current.getBounds();
      const minLon = bounds.getWest();
      const minLat = bounds.getSouth();
      const maxLon = bounds.getEast();
      const maxLat = bounds.getNorth();

      // Load satellite images
      if (showImages) {
        const satelliteImages = await SatelliteService.getImagesInBounds(minLon, minLat, maxLon, maxLat);
        setImages(satelliteImages);
        addImageLayers(satelliteImages);
      }

      // Load damage assessments
      if (showDamage) {
        const assessments = await SatelliteService.getDamageAssessmentsInBounds(minLon, minLat, maxLon, maxLat);
        setDamageAssessments(assessments);
        addDamageLayers(assessments);
      }

    } catch (err) {
      setError('Failed to load satellite data');
      console.error('Satellite data loading error:', err);
    } finally {
      setLoading(false);
      isLoadingRef.current = false;
    }
  };

  const addImageLayers = (satelliteImages: SatelliteImage[]) => {
    if (!mapRef.current || satelliteImages.length === 0) return;

    // Remove existing image layers
    ['satellite-images', 'satellite-labels'].forEach(layerId => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current.removeLayer(layerId);
      }
    });
    ['satellite-source'].forEach(sourceId => {
      if (mapRef.current?.getSource(sourceId)) {
        mapRef.current.removeSource(sourceId);
      }
    });

    // Add satellite image coverage areas
    mapRef.current.addSource('satellite-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: satelliteImages.map(image => ({
          type: 'Feature',
          geometry: {
            type: 'Polygon',
            coordinates: [[]] // This would be populated with actual coverage area coordinates
          },
          properties: {
            id: image.id,
            provider: image.provider,
            satelliteName: image.satelliteName,
            capturedAt: image.capturedAt,
            resolutionMeters: image.resolutionMeters,
            cloudCoverPercentage: image.cloudCoverPercentage,
            qualityScore: image.qualityScore,
            processingStatus: image.processingStatus,
            imageUrl: image.imageUrl,
            thumbnailUrl: image.thumbnailUrl
          }
        }))
      }
    });

    // Add coverage area polygons
    mapRef.current.addLayer({
      id: 'satellite-images',
      type: 'fill',
      source: 'satellite-source',
      paint: {
        'fill-color': [
          'case',
          ['==', ['get', 'provider'], 'LANDSAT'], '#2E8B57',
          ['==', ['get', 'provider'], 'SENTINEL'], '#4169E1',
          ['==', ['get', 'provider'], 'WORLDVIEW'], '#8A2BE2',
          ['==', ['get', 'provider'], 'PLEIADES'], '#FF1493',
          '#808080'
        ],
        'fill-opacity': 0.3
      }
    });

    // Add coverage area borders
    mapRef.current.addLayer({
      id: 'satellite-borders',
      type: 'line',
      source: 'satellite-source',
      paint: {
        'line-color': [
          'case',
          ['==', ['get', 'provider'], 'LANDSAT'], '#2E8B57',
          ['==', ['get', 'provider'], 'SENTINEL'], '#4169E1',
          ['==', ['get', 'provider'], 'WORLDVIEW'], '#8A2BE2',
          ['==', ['get', 'provider'], 'PLEIADES'], '#FF1493',
          '#808080'
        ],
        'line-width': 2,
        'line-opacity': 0.8
      }
    });

    // Add satellite image labels
    mapRef.current.addLayer({
      id: 'satellite-labels',
      type: 'symbol',
      source: 'satellite-source',
      layout: {
        'text-field': [
          'format',
          ['get', 'satelliteName'],
          { 'font-scale': 0.8 }
        ],
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
  };

  const addDamageLayers = (assessments: DamageAssessment[]) => {
    if (!mapRef.current || assessments.length === 0) return;

    // Remove existing damage layers
    ['damage-assessments', 'damage-labels'].forEach(layerId => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current.removeLayer(layerId);
      }
    });
    ['damage-source'].forEach(sourceId => {
      if (mapRef.current?.getSource(sourceId)) {
        mapRef.current.removeSource(sourceId);
      }
    });

    // Add damage assessment areas
    mapRef.current.addSource('damage-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: assessments.map(assessment => ({
          type: 'Feature',
          geometry: {
            type: 'Polygon',
            coordinates: [[]] // This would be populated with actual damage area coordinates
          },
          properties: {
            id: assessment.id,
            damageType: assessment.damageType,
            severity: assessment.severity,
            confidenceScore: assessment.confidenceScore,
            damagePercentage: assessment.damagePercentage,
            affectedAreaSqm: assessment.affectedAreaSqm,
            assessedAt: assessment.assessedAt,
            assessedBy: assessment.assessedBy
          }
        }))
      }
    });

    // Add damage areas
    mapRef.current.addLayer({
      id: 'damage-assessments',
      type: 'fill',
      source: 'damage-source',
      paint: {
        'fill-color': [
          'case',
          ['==', ['get', 'severity'], 'CATASTROPHIC'], '#ff0000',
          ['==', ['get', 'severity'], 'SEVERE'], '#ff4400',
          ['==', ['get', 'severity'], 'MODERATE'], '#ff8800',
          ['==', ['get', 'severity'], 'LIGHT'], '#ffff00',
          '#00ff00' // MINIMAL
        ],
        'fill-opacity': 0.6
      }
    });

    // Add damage area borders
    mapRef.current.addLayer({
      id: 'damage-borders',
      type: 'line',
      source: 'damage-source',
      paint: {
        'line-color': [
          'case',
          ['==', ['get', 'severity'], 'CATASTROPHIC'], '#ff0000',
          ['==', ['get', 'severity'], 'SEVERE'], '#ff4400',
          ['==', ['get', 'severity'], 'MODERATE'], '#ff8800',
          ['==', ['get', 'severity'], 'LIGHT'], '#ffff00',
          '#00ff00' // MINIMAL
        ],
        'line-width': 2,
        'line-opacity': 0.8
      }
    });

    // Add damage labels
    mapRef.current.addLayer({
      id: 'damage-labels',
      type: 'symbol',
      source: 'damage-source',
      layout: {
        'text-field': [
          'format',
          ['get', 'damageType'],
          { 'font-scale': 0.7 }
        ],
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
  };

  const handleMapLoad = (map: maplibregl.Map) => {
    mapRef.current = map;
    // Let useEffect handle initial load to avoid duplicate calls
  };

  const handleMapClick = (event: maplibregl.MapMouseEvent) => {
    if (!mapRef.current) return;

    const features = mapRef.current.queryRenderedFeatures(event.point, {
      layers: ['satellite-images', 'damage-assessments']
    });

    if (features.length > 0) {
      const feature = features[0];
      const properties = feature.properties;

      if (feature.layer.id === 'satellite-images') {
        const image = images.find(img => img.id === properties.id);
        if (image && onImageSelect) {
          onImageSelect(image);
        }
      } else if (feature.layer.id === 'damage-assessments') {
        const assessment = damageAssessments.find(ass => ass.id === properties.id);
        if (assessment && onDamageSelect) {
          onDamageSelect(assessment);
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
      
      {loading && (
        <div className="absolute top-4 left-4 bg-white p-2 rounded shadow">
          <div className="flex items-center space-x-2">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
            <span className="text-sm">Loading satellite data...</span>
          </div>
        </div>
      )}

      {error && (
        <div className="absolute top-4 right-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Satellite Data Controls */}
      <div className="absolute bottom-4 left-4 bg-white p-4 rounded shadow">
        <h3 className="font-semibold mb-2">Satellite Imagery</h3>
        <div className="space-y-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showImages}
              onChange={(e) => {
                if (e.target.checked) {
                  // Force reload with new params
                  lastLoadParamsRef.current = '';
                  if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
                  }
                  debounceTimerRef.current = setTimeout(() => {
                    if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
                      loadSatelliteData();
                    }
                  }, 300);
                } else {
                  if (mapRef.current?.getLayer('satellite-images')) {
                    mapRef.current.removeLayer('satellite-images');
                    mapRef.current.removeLayer('satellite-borders');
                    mapRef.current.removeLayer('satellite-labels');
                    mapRef.current.removeSource('satellite-source');
                  }
                }
              }}
              className="mr-2"
            />
            Satellite Images
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={showDamage}
              onChange={(e) => {
                if (e.target.checked) {
                  // Force reload with new params
                  lastLoadParamsRef.current = '';
                  if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
                  }
                  debounceTimerRef.current = setTimeout(() => {
                    if (!isLoadingRef.current && mapRef.current && mapRef.current.loaded()) {
                      loadSatelliteData();
                    }
                  }, 300);
                } else {
                  if (mapRef.current?.getLayer('damage-assessments')) {
                    mapRef.current.removeLayer('damage-assessments');
                    mapRef.current.removeLayer('damage-borders');
                    mapRef.current.removeLayer('damage-labels');
                    mapRef.current.removeSource('damage-source');
                  }
                }
              }}
              className="mr-2"
            />
            Damage Assessments
          </label>
        </div>
      </div>

      {/* Legend */}
      <div className="absolute top-4 right-4 bg-white p-4 rounded shadow max-w-sm">
        <h3 className="font-semibold mb-2">Legend</h3>
        
        {showImages && (
          <div className="mb-3">
            <h4 className="text-sm font-medium mb-1">Satellite Providers</h4>
            <div className="space-y-1 text-xs">
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-600 mr-2"></div>
                <span>Landsat</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-blue-600 mr-2"></div>
                <span>Sentinel</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-purple-600 mr-2"></div>
                <span>WorldView</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-pink-600 mr-2"></div>
                <span>Pleiades</span>
              </div>
            </div>
          </div>
        )}

        {showDamage && (
          <div>
            <h4 className="text-sm font-medium mb-1">Damage Severity</h4>
            <div className="space-y-1 text-xs">
              <div className="flex items-center">
                <div className="w-3 h-3 bg-red-600 mr-2"></div>
                <span>Catastrophic</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-orange-600 mr-2"></div>
                <span>Severe</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-yellow-600 mr-2"></div>
                <span>Moderate</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-600 mr-2"></div>
                <span>Light/Minimal</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Selected Image Info */}
      {selectedImage && (
        <div className="absolute bottom-4 right-4 bg-white p-4 rounded shadow max-w-sm">
          <h3 className="font-semibold mb-2">Selected Image</h3>
          <div className="space-y-1 text-sm">
            <div><strong>Provider:</strong> {selectedImage.provider}</div>
            <div><strong>Satellite:</strong> {selectedImage.satelliteName}</div>
            <div><strong>Captured:</strong> {new Date(selectedImage.capturedAt).toLocaleDateString()}</div>
            <div><strong>Resolution:</strong> {SatelliteService.formatResolution(selectedImage.resolutionMeters)}</div>
            {selectedImage.cloudCoverPercentage && (
              <div><strong>Cloud Cover:</strong> {selectedImage.cloudCoverPercentage.toFixed(1)}%</div>
            )}
            {selectedImage.qualityScore && (
              <div><strong>Quality:</strong> {(selectedImage.qualityScore * 100).toFixed(0)}%</div>
            )}
            <div><strong>Status:</strong> {selectedImage.processingStatus}</div>
          </div>
        </div>
      )}
    </div>
  );
};



