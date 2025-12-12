import React, { useState } from 'react';
import { GeofencingService, Geofence, GeofenceRequest } from '../../services/geofencingService';

interface GeofenceManagementProps {
  onGeofenceCreated?: (geofence: Geofence) => void;
  onGeofenceUpdated?: (geofence: Geofence) => void;
  onGeofenceDeleted?: (geofenceId: number) => void;
}

export const GeofenceManagement: React.FC<GeofenceManagementProps> = ({
  onGeofenceCreated,
  onGeofenceUpdated,
  onGeofenceDeleted
}) => {
  const [isCreating, setIsCreating] = useState(false);
  const [editingGeofence, setEditingGeofence] = useState<Geofence | null>(null);
  const [formData, setFormData] = useState<Partial<GeofenceRequest>>({
    name: '',
    description: '',
    geofenceType: 'DISASTER_ZONE',
    priority: 'MEDIUM',
    isActive: true,
    checkIntervalSeconds: 300,
    alertThreshold: 1,
    cooldownPeriodSeconds: 3600,
    createdBy: 'current_user' // This would be from auth context
  });
  const [boundaryCoordinates, setBoundaryCoordinates] = useState<Array<{ longitude: number; latitude: number }>>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const availableGeofenceTypes = GeofencingService.getAvailableGeofenceTypes();
  const availablePriorities = GeofencingService.getAvailablePriorities();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.name?.trim()) {
      setError('Geofence name is required');
      return;
    }

    if (boundaryCoordinates.length < 3) {
      setError('At least 3 boundary coordinates are required');
      return;
    }

    // Validate JSON fields
    let parsedNotificationChannels: string | undefined;
    if (formData.notificationChannels?.trim()) {
      try {
        parsedNotificationChannels = JSON.parse(formData.notificationChannels);
      } catch (err) {
        setError('Invalid JSON in Notification Channels field');
        return;
      }
    }

    let parsedAutoActions: string | undefined;
    if (formData.autoActions?.trim()) {
      try {
        parsedAutoActions = JSON.parse(formData.autoActions);
      } catch (err) {
        setError('Invalid JSON in Auto Actions field');
        return;
      }
    }

    let parsedMetadata: string | undefined;
    if (formData.metadata?.trim()) {
      try {
        parsedMetadata = JSON.parse(formData.metadata);
      } catch (err) {
        setError('Invalid JSON in Metadata field. Please check your JSON syntax.');
        return;
      }
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const geofenceData: GeofenceRequest = {
        name: formData.name!,
        description: formData.description,
        boundaryCoordinates,
        geofenceType: formData.geofenceType!,
        priority: formData.priority!,
        isActive: formData.isActive!,
        bufferDistanceMeters: formData.bufferDistanceMeters,
        checkIntervalSeconds: formData.checkIntervalSeconds,
        alertThreshold: formData.alertThreshold,
        cooldownPeriodSeconds: formData.cooldownPeriodSeconds,
        notificationChannels: parsedNotificationChannels ? JSON.stringify(parsedNotificationChannels) : undefined,
        autoActions: parsedAutoActions ? JSON.stringify(parsedAutoActions) : undefined,
        metadata: parsedMetadata ? JSON.stringify(parsedMetadata) : undefined,
        createdBy: formData.createdBy!
      };

      if (editingGeofence) {
        const updatedGeofence = await GeofencingService.updateGeofence(editingGeofence.id, geofenceData);
        onGeofenceUpdated?.(updatedGeofence);
        setEditingGeofence(null);
      } else {
        const newGeofence = await GeofencingService.createGeofence(geofenceData);
        onGeofenceCreated?.(newGeofence);
      }

      resetForm();
    } catch (err) {
      setError('Failed to save geofence');
      console.error('Geofence save error:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (geofenceId: number) => {
    if (!window.confirm('Are you sure you want to delete this geofence?')) {
      return;
    }

    try {
      await GeofencingService.deleteGeofence(geofenceId);
      onGeofenceDeleted?.(geofenceId);
    } catch (err) {
      setError('Failed to delete geofence');
      console.error('Geofence delete error:', err);
    }
  };

  const handleEdit = (geofence: Geofence) => {
    setEditingGeofence(geofence);
    setFormData({
      name: geofence.name,
      description: geofence.description,
      geofenceType: geofence.geofenceType,
      priority: geofence.priority,
      isActive: geofence.isActive,
      bufferDistanceMeters: geofence.bufferDistanceMeters,
      checkIntervalSeconds: geofence.checkIntervalSeconds,
      alertThreshold: geofence.alertThreshold,
      cooldownPeriodSeconds: geofence.cooldownPeriodSeconds,
      notificationChannels: geofence.notificationChannels,
      autoActions: geofence.autoActions,
      metadata: geofence.metadata,
      createdBy: geofence.createdBy
    });
    // This would populate boundary coordinates from the geofence
    setBoundaryCoordinates([]);
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      geofenceType: 'DISASTER_ZONE',
      priority: 'MEDIUM',
      isActive: true,
      checkIntervalSeconds: 300,
      alertThreshold: 1,
      cooldownPeriodSeconds: 3600,
      createdBy: 'current_user'
    });
    setBoundaryCoordinates([]);
    setEditingGeofence(null);
    setError(null);
  };

  const addBoundaryCoordinate = () => {
    setBoundaryCoordinates([...boundaryCoordinates, { longitude: 0, latitude: 0 }]);
  };

  const removeBoundaryCoordinate = (index: number) => {
    setBoundaryCoordinates(boundaryCoordinates.filter((_, i) => i !== index));
  };

  const updateBoundaryCoordinate = (index: number, field: 'longitude' | 'latitude', value: number) => {
    const updated = [...boundaryCoordinates];
    updated[index] = { ...updated[index], [field]: value };
    setBoundaryCoordinates(updated);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg max-w-4xl">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold">
          {editingGeofence ? 'Edit Geofence' : 'Create Geofence'}
        </h2>
        <button
          onClick={resetForm}
          className="px-4 py-2 text-gray-600 hover:text-gray-800"
        >
          Cancel
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Name *
            </label>
            <input
              type="text"
              value={formData.name || ''}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter geofence name"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type *
            </label>
            <select
              value={formData.geofenceType || ''}
              onChange={(e) => setFormData({ ...formData, geofenceType: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {availableGeofenceTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.icon} {type.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Priority *
            </label>
            <select
              value={formData.priority || ''}
              onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {availablePriorities.map(priority => (
                <option key={priority.value} value={priority.value}>
                  {priority.label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center">
            <input
              type="checkbox"
              id="isActive"
              checked={formData.isActive || false}
              onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
              className="mr-2"
            />
            <label htmlFor="isActive" className="text-sm font-medium text-gray-700">
              Active
            </label>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description
          </label>
          <textarea
            value={formData.description || ''}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={3}
            placeholder="Enter geofence description"
          />
        </div>

        {/* Boundary Coordinates */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Boundary Coordinates *
          </label>
          <div className="space-y-2">
            {boundaryCoordinates.map((coord, index) => (
              <div key={index} className="flex items-center space-x-2">
                <input
                  type="number"
                  step="any"
                  placeholder="Longitude"
                  value={coord.longitude}
                  onChange={(e) => updateBoundaryCoordinate(index, 'longitude', parseFloat(e.target.value) || 0)}
                  className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="number"
                  step="any"
                  placeholder="Latitude"
                  value={coord.latitude}
                  onChange={(e) => updateBoundaryCoordinate(index, 'latitude', parseFloat(e.target.value) || 0)}
                  className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  type="button"
                  onClick={() => removeBoundaryCoordinate(index)}
                  className="px-3 py-2 text-red-600 hover:text-red-800"
                >
                  Remove
                </button>
              </div>
            ))}
            <button
              type="button"
              onClick={addBoundaryCoordinate}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Add Coordinate
            </button>
          </div>
        </div>

        {/* Monitoring Settings */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Check Interval (seconds)
            </label>
            <input
              type="number"
              value={formData.checkIntervalSeconds || 300}
              onChange={(e) => setFormData({ ...formData, checkIntervalSeconds: parseInt(e.target.value) || 300 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="60"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Alert Threshold
            </label>
            <input
              type="number"
              value={formData.alertThreshold || 1}
              onChange={(e) => setFormData({ ...formData, alertThreshold: parseInt(e.target.value) || 1 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="1"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Cooldown Period (seconds)
            </label>
            <input
              type="number"
              value={formData.cooldownPeriodSeconds || 3600}
              onChange={(e) => setFormData({ ...formData, cooldownPeriodSeconds: parseInt(e.target.value) || 3600 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="60"
            />
          </div>
        </div>

        {/* Advanced Settings */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Buffer Distance (meters)
            </label>
            <input
              type="number"
              value={formData.bufferDistanceMeters || ''}
              onChange={(e) => setFormData({ ...formData, bufferDistanceMeters: parseFloat(e.target.value) || undefined })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="0"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notification Channels (JSON)
            </label>
            <input
              type="text"
              value={formData.notificationChannels || ''}
              onChange={(e) => setFormData({ ...formData, notificationChannels: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder='["email", "sms", "push"]'
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Auto Actions (JSON)
          </label>
          <input
            type="text"
            value={formData.autoActions || ''}
            onChange={(e) => setFormData({ ...formData, autoActions: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder='["notify_team", "update_status", "send_alert"]'
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Metadata (JSON)
          </label>
          <textarea
            value={formData.metadata || ''}
            onChange={(e) => setFormData({ ...formData, metadata: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={3}
            placeholder='{"custom_field": "value", "tags": ["tag1", "tag2"]}'
          />
        </div>

        {/* Error Display */}
        {error && (
          <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-3">
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? 'Saving...' : (editingGeofence ? 'Update Geofence' : 'Create Geofence')}
          </button>
          
          {editingGeofence && (
            <button
              type="button"
              onClick={() => handleDelete(editingGeofence.id)}
              className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
            >
              Delete Geofence
            </button>
          )}
        </div>
      </form>
    </div>
  );
};



