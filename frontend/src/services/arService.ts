import { apiService } from './api';

interface ARMarker {
  id: string;
  type: string;
  position: {
    latitude: number;
    longitude: number;
    altitude: number;
  };
  rotation: {
    x: number;
    y: number;
    z: number;
  };
  data: any;
}

interface ARDamageAssessment {
  id: string;
  type: 'minor' | 'moderate' | 'severe' | 'critical';
  location: {
    latitude: number;
    longitude: number;
    altitude: number;
  };
  photoUrl?: string;
  notes: string;
  assessedBy: string;
  assessedAt: number;
}

interface ARNavigationTarget {
  id: string;
  name: string;
  location: {
    latitude: number;
    longitude: number;
  };
  iconUrl?: string;
}

class ARService {
  private isSupported = this.checkARSupport();
  private currentMarkers: Map<string, ARMarker> = new Map();
  private cameraStream: MediaStream | null = null;

  private checkARSupport(): boolean {
    // Check for WebXR API support
    if ('xr' in navigator) {
      return true;
    }
    
    // Check for getCameraMediaStream (fallback)
    if ('mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices) {
      return true;
    }
    
    return false;
  }

  async initializeAR(): Promise<boolean> {
    if (!this.isSupported) {
      throw new Error('AR is not supported on this device');
    }

    try {
      // Request camera permission
      this.cameraStream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: 'environment' // Use back camera for AR
        }
      });
      return true;
    } catch (error) {
      console.error('Failed to initialize AR:', error);
      return false;
    }
  }

  async addMarker(marker: ARMarker) {
    this.currentMarkers.set(marker.id, marker);
    
    // Send to server for persistence
    try {
      await apiService.createARMarker(marker);
    } catch (error) {
      console.error('Failed to sync marker to server:', error);
    }
  }

  async getMarkersNearby(latitude: number, longitude: number, radius: number = 100): Promise<ARMarker[]> {
    const markers = Array.from(this.currentMarkers.values());
    
    // Filter markers within radius (in meters)
    const nearbyMarkers = markers.filter(marker => {
      const distance = this.calculateDistance(
        latitude,
        longitude,
        marker.position.latitude,
        marker.position.longitude
      );
      return distance <= radius;
    });

    return nearbyMarkers;
  }

  async performDamageAssessment(assessment: ARDamageAssessment): Promise<ARDamageAssessment> {
    // Store locally first
    const id = `damage_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const savedAssessment = { ...assessment, id };
    
    // Send to server
    try {
      await apiService.createDamageAssessment(savedAssessment);
    } catch (error) {
      console.error('Failed to sync damage assessment:', error);
    }

    return savedAssessment;
  }

  async startNavigation(target: ARNavigationTarget) {
    // Use device GPS for navigation
    if ('geolocation' in navigator) {
      const watchId = navigator.geolocation.watchPosition(
        (position) => {
          this.onPositionUpdate(position, target);
        },
        (error) => {
          console.error('Navigation error:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
      );
      
      return watchId;
    }
    
    throw new Error('Geolocation not supported');
  }

  stopNavigation(watchId: number) {
    navigator.geolocation.clearWatch(watchId);
  }

  private onPositionUpdate(position: GeolocationPosition, target: ARNavigationTarget) {
    const currentLocation = {
      latitude: position.coords.latitude,
      longitude: position.coords.longitude
    };

    const distance = this.calculateDistance(
      currentLocation.latitude,
      currentLocation.longitude,
      target.location.latitude,
      target.location.longitude
    );

    const bearing = this.calculateBearing(
      currentLocation.latitude,
      currentLocation.longitude,
      target.location.latitude,
      target.location.longitude
    );

    // Dispatch custom event for UI updates
    window.dispatchEvent(new CustomEvent('ar-navigation-update', {
      detail: {
        currentLocation,
        target,
        distance,
        bearing
      }
    }));
  }

  async capturePhoto(): Promise<Blob | null> {
    if (!this.cameraStream) {
      throw new Error('Camera not initialized');
    }

    try {
      const track = this.cameraStream.getVideoTracks()[0];
      const imageCapture = new (window as any).ImageCapture(track);
      const bitmap = await imageCapture.grabFrame();
      
      const canvas = document.createElement('canvas');
      canvas.width = bitmap.width;
      canvas.height = bitmap.height;
      const ctx = canvas.getContext('2d');
      
      if (ctx) {
        ctx.drawImage(bitmap, 0, 0);
        return new Promise<Blob>((resolve, reject) => {
          canvas.toBlob((blob) => {
            if (blob) {
              resolve(blob);
            } else {
              reject(new Error('Failed to convert to blob'));
            }
          }, 'image/jpeg');
        });
      }
    } catch (error) {
      console.error('Failed to capture photo:', error);
      return null;
    }

    return null;
  }

  private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }

  private calculateBearing(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const y = Math.sin(Δλ) * Math.cos(φ2);
    const x = Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ);

    return Math.atan2(y, x) * 180 / Math.PI;
  }

  cleanup() {
    if (this.cameraStream) {
      this.cameraStream.getTracks().forEach(track => track.stop());
      this.cameraStream = null;
    }
    this.currentMarkers.clear();
  }

  getIsSupported(): boolean {
    return this.isSupported;
  }
}

export const arService = new ARService();
export default arService;



