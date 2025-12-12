import React, { useEffect, useRef } from 'react';
import maplibregl from 'maplibre-gl';

interface MapComponentProps {
  center: [number, number];
  zoom: number;
  className?: string;
  onMapLoad?: (map: maplibregl.Map) => void;
  onMapClick?: (event: maplibregl.MapMouseEvent) => void;
}

export const MapComponent: React.FC<MapComponentProps> = ({
  center,
  zoom,
  className = 'w-full h-full',
  onMapLoad,
  onMapClick,
}) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: 'https://demotiles.maplibre.org/style.json',
      center,
      zoom,
    });

    mapRef.current = map;

    map.on('load', () => {
      if (onMapLoad) {
        onMapLoad(map);
      }
    });

    if (onMapClick) {
      map.on('click', onMapClick);
    }

    return () => {
      map.remove();
      mapRef.current = null;
    };
  }, [center[0], center[1], zoom, onMapLoad, onMapClick]);

  return <div ref={containerRef} className={className} />;
};


