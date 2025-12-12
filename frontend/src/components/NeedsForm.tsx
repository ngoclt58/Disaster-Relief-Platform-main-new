import React, { useState, useRef } from 'react';
import { apiService } from '../services/api';
import { mediaService } from '../services/mediaService';
import { 
  MapPin, 
  Camera, 
  FileText, 
  AlertTriangle, 
  Send,
  X,
  Image as ImageIcon
} from 'lucide-react';

interface NeedsFormProps {
  onSuccess?: () => void;
  onCancel?: () => void;
}

interface CategorizationResult {
  confidence: number;
  suggestedType?: string;
  suggestedSeverity?: number;
  reasoning?: string;
}

const NeedsForm: React.FC<NeedsFormProps> = ({ onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    category: '',
    description: '',
    severity: 3,
    urgency: 'medium',
    location: {
      lat: 0,
      lng: 0,
      address: ''
    },
    media: [] as File[]
  });
  const [uploading, setUploading] = useState(false);
  const [locationError, setLocationError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const categories = [
    { value: 'medical', label: 'Medical Emergency' },
    { value: 'food', label: 'Food & Water' },
    { value: 'shelter', label: 'Shelter' },
    { value: 'evacuation', label: 'Evacuation' },
    { value: 'communication', label: 'Communication' },
    { value: 'other', label: 'Other' }
  ];

  const severityLevels = [
    { value: 1, label: 'Low', color: 'bg-blue-500' },
    { value: 2, label: 'Medium', color: 'bg-yellow-500' },
    { value: 3, label: 'High', color: 'bg-orange-500' },
    { value: 4, label: 'Critical', color: 'bg-red-500' },
    { value: 5, label: 'Emergency', color: 'bg-red-700' }
  ];

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by this browser');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        setFormData(prev => ({
          ...prev,
          location: {
            ...prev.location,
            lat: latitude,
            lng: longitude
          }
        }));
        setLocationError('');
        
        // Reverse geocoding to get address (optional - depends on Mapbox token)
        const mapboxToken = process.env.REACT_APP_MAPBOX_TOKEN;
        if (!mapboxToken) {
          console.warn('Mapbox token (REACT_APP_MAPBOX_TOKEN) is not configured. Skipping reverse geocoding.');
          return;
        }

        fetch(`https://api.mapbox.com/geocoding/v5/mapbox.places/${longitude},${latitude}.json?access_token=${mapboxToken}`)
          .then(response => response.json())
          .then(data => {
            if (data.features && data.features.length > 0) {
              setFormData(prev => ({
                ...prev,
                location: {
                  ...prev.location,
                  address: data.features[0].place_name
                }
              }));
            }
          })
          .catch(error => console.error('Geocoding error:', error));
      },
      (error) => {
        setLocationError('Unable to retrieve your location');
        console.error('Geolocation error:', error);
      }
    );
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    setFormData(prev => ({
      ...prev,
      media: [...prev.media, ...files].slice(0, 5) // Limit to 5 files
    }));
  };

  const removeFile = (index: number) => {
    setFormData(prev => ({
      ...prev,
      media: prev.media.filter((_, i) => i !== index)
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.category || !formData.description) {
      alert('Please fill in all required fields');
      return;
    }

    if (formData.location.lat === 0 && formData.location.lng === 0) {
      alert('Please get your current location');
      return;
    }

    try {
      setUploading(true);
      
      // Upload media files
      const mediaIds = [];
      for (const file of formData.media) {
        const uploadResult = await mediaService.uploadImage(file, {
          maxWidth: 1920,
          maxHeight: 1080,
          quality: 0.8
        });
        mediaIds.push(uploadResult.id);
      }

      // Use AI categorization if confidence is high enough
      let finalCategory = formData.category;
      let finalSeverity = formData.severity;
      
      try {
        const categorization = await apiService.categorizeRequest(
          formData.description,
          formData.category,
          formData.severity
        ) as CategorizationResult;
        
        if (categorization.confidence > 0.7 && categorization.suggestedType) {
          finalCategory = categorization.suggestedType;
          if (categorization.suggestedSeverity) {
            finalSeverity = categorization.suggestedSeverity;
          }
          console.log('AI categorization applied:', categorization);
        }
      } catch (error) {
        console.warn('AI categorization failed, using manual values:', error);
      }

      // Create need request - align payload with backend CreateNeedRequest DTO
      await apiService.createNeed({
        type: finalCategory,                 // backend expects 'type'
        severity: finalSeverity,             // integer 1-5
        notes: formData.description,         // optional notes/description
        lat: formData.location.lat,
        lng: formData.location.lng,
        mediaIds                              // optional list of UUIDs
      });

      onSuccess?.();
    } catch (error) {
      console.error('Failed to create need:', error);
      alert('Failed to create need request. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-900">Create Need Request</h2>
            {onCancel && (
              <button
                onClick={onCancel}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Category */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Category *
              </label>
              <select
                value={formData.category}
                onChange={(e) => setFormData(prev => ({ ...prev, category: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                required
              >
                <option value="">Select a category</option>
                {categories.map(cat => (
                  <option key={cat.value} value={cat.value}>{cat.label}</option>
                ))}
              </select>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Description *
              </label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                placeholder="Describe your needs in detail..."
                required
              />
            </div>

            {/* Severity */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Severity Level
              </label>
              <div className="grid grid-cols-5 gap-2">
                {severityLevels.map(level => (
                  <button
                    key={level.value}
                    type="button"
                    onClick={() => setFormData(prev => ({ ...prev, severity: level.value }))}
                    className={`p-2 rounded-md text-sm font-medium text-white ${
                      formData.severity === level.value ? level.color : 'bg-gray-300'
                    }`}
                  >
                    {level.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Location */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Location *
              </label>
              <div className="space-y-2">
                <button
                  type="button"
                  onClick={getCurrentLocation}
                  className="flex items-center px-3 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  <MapPin className="w-4 h-4 mr-2" />
                  Get Current Location
                </button>
                {locationError && (
                  <p className="text-sm text-red-600">{locationError}</p>
                )}
                {formData.location.lat !== 0 && formData.location.lng !== 0 && (
                  <div className="p-3 bg-green-50 border border-green-200 rounded-md">
                    <p className="text-sm text-green-800">
                      <strong>Location:</strong> {formData.location.address || `${formData.location.lat.toFixed(4)}, ${formData.location.lng.toFixed(4)}`}
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Media Upload */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Photos/Videos (Optional)
              </label>
              <div className="space-y-2">
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*,video/*"
                  multiple
                  onChange={handleFileSelect}
                  className="hidden"
                />
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="flex items-center px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  <Camera className="w-4 h-4 mr-2" />
                  Add Photos/Videos
                </button>
                {formData.media.length > 0 && (
                  <div className="grid grid-cols-2 gap-2">
                    {formData.media.map((file, index) => (
                      <div key={index} className="relative">
                        <div className="p-2 border border-gray-200 rounded-md">
                          <div className="flex items-center space-x-2">
                            <ImageIcon className="w-4 h-4 text-gray-400" />
                            <span className="text-sm text-gray-600 truncate">
                              {file.name}
                            </span>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeFile(index)}
                            className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center text-xs"
                          >
                            <X className="w-3 h-3" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex justify-end space-x-3">
              {onCancel && (
                <button
                  type="button"
                  onClick={onCancel}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
              )}
              <button
                type="submit"
                disabled={uploading}
                className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {uploading ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Creating...
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4 mr-2" />
                    Create Request
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NeedsForm;

