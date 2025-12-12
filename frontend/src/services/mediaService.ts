import { apiService } from './api';
import { useAuthStore } from '../store/authStore';

export interface MediaUploadResult {
  id: string;
  url: string;
  objectName: string;
}

export class MediaService {
  async uploadFile(file: File, metadata?: { takenAt?: Date; location?: { lat: number; lng: number } }): Promise<MediaUploadResult> {
    try {
      // Generate unique object name
      const timestamp = Date.now();
      const extension = file.name.split('.').pop();
      let objectName = `media/${timestamp}-${Math.random().toString(36).substring(2)}.${extension}`;
      
      // Get presigned URL (or direct upload URL for file system)
      const presignedData = await apiService.getPresignedUploadUrl(objectName, file.type);
      
      // Check if it's a direct upload endpoint (file system) or presigned URL (MinIO)
      if (presignedData.url.includes('/media/upload-direct')) {
        // File system storage - upload directly
        const RAW_API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
        // Backend already prefixes file-system URLs with context-path (/api),
        // so avoid generating "/api/api/..." when building the full URL.
        const API_BASE_ORIGIN = RAW_API_BASE_URL.replace(/\/api\/?$/, '');

        const { token } = useAuthStore.getState();
        const formData = new FormData();
        formData.append('file', file);
        formData.append('objectName', objectName);
        
        const uploadUrl = presignedData.url.startsWith('http')
          ? presignedData.url
          : `${API_BASE_ORIGIN}${presignedData.url}`;

        const response = await fetch(uploadUrl, {
          method: 'POST',
          headers: {
            ...(token && { 'Authorization': `Bearer ${token}` })
          },
          body: formData
        });
        
        if (!response.ok) {
          throw new Error(`Upload failed: ${response.status}`);
        }
        
        const uploadResult = await response.json();
        // Use the returned objectName from the server if available
        if (uploadResult.objectName) {
          objectName = uploadResult.objectName;
        }
      } else {
        // MinIO presigned URL - upload using PUT
        await apiService.uploadToMinIO(presignedData.url, file);
      }
      
      // Create media record in backend with metadata
      try {
        const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
        const { token } = useAuthStore.getState();
        const headers: HeadersInit = {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        };
        
        const response = await fetch(`${API_BASE_URL}/media`, {
          method: 'POST',
          headers,
          body: JSON.stringify({
            objectName,
            contentType: file.type,
            fileName: file.name,
            fileSize: file.size,
            takenAt: metadata?.takenAt?.toISOString(),
            location: metadata?.location ? {
              latitude: metadata.location.lat,
              longitude: metadata.location.lng
            } : undefined
          })
        });
        
        if (response.ok) {
          const mediaRecord = await response.json();
          return {
            id: mediaRecord.id || objectName,
            url: mediaRecord.url || `${API_BASE_URL.replace('/api', '')}/media/${objectName}`,
            objectName
          };
        } else {
          throw new Error(`Failed to create media record: ${response.status}`);
        }
      } catch (error) {
        // If backend endpoint doesn't exist yet, return basic info
        console.warn('Media record creation endpoint not available, returning basic info:', error);
        const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
        return {
          id: objectName,
          url: `${API_BASE_URL.replace('/api', '')}/media/${objectName}`,
          objectName
        };
      }
    } catch (error) {
      console.error('Media upload failed:', error);
      throw new Error('Failed to upload media file');
    }
  }

  async uploadImage(file: File, options?: {
    maxWidth?: number;
    maxHeight?: number;
    quality?: number;
  }): Promise<MediaUploadResult> {
    // Resize image if needed
    if (options?.maxWidth || options?.maxHeight) {
      file = await this.resizeImage(file, options);
    }
    
    return this.uploadFile(file);
  }

  private async resizeImage(file: File, options: {
    maxWidth?: number;
    maxHeight?: number;
    quality?: number;
  }): Promise<File> {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      const img = new Image();
      
      img.onload = () => {
        const { maxWidth = 1920, maxHeight = 1080, quality = 0.8 } = options;
        
        // Calculate new dimensions
        let { width, height } = img;
        if (width > maxWidth || height > maxHeight) {
          const ratio = Math.min(maxWidth / width, maxHeight / height);
          width *= ratio;
          height *= ratio;
        }
        
        canvas.width = width;
        canvas.height = height;
        
        // Draw and compress
        ctx?.drawImage(img, 0, 0, width, height);
        canvas.toBlob(
          (blob) => {
            if (blob) {
              const resizedFile = new File([blob], file.name, {
                type: file.type,
                lastModified: Date.now()
              });
              resolve(resizedFile);
            } else {
              reject(new Error('Failed to resize image'));
            }
          },
          file.type,
          quality
        );
      };
      
      img.onerror = () => reject(new Error('Failed to load image'));
      img.src = URL.createObjectURL(file);
    });
  }

  async getMediaUrl(mediaId: string): Promise<string> {
    // This would return the actual media URL from backend
    return `${process.env.REACT_APP_API_URL}/media/${mediaId}`;
  }

  async deleteMedia(mediaId: string): Promise<void> {
    // This would call backend to delete media
    throw new Error('Delete media not implemented');
  }
}

export const mediaService = new MediaService();



