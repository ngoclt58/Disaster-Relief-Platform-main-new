import React, { useState } from 'react';
import { SatelliteService, DamageAssessment, SatelliteImage } from '../../services/satelliteService';

interface DamageAssessmentPanelProps {
  onAssessmentComplete?: (assessment: DamageAssessment) => void;
  selectedImage?: SatelliteImage;
}

export const DamageAssessmentPanel: React.FC<DamageAssessmentPanelProps> = ({
  onAssessmentComplete,
  selectedImage
}) => {
  const [damageCoordinates, setDamageCoordinates] = useState<Array<{ longitude: number; latitude: number }>>([]);
  const [damageType, setDamageType] = useState('GENERAL');
  const [analysisAlgorithm, setAnalysisAlgorithm] = useState('MANUAL');
  const [analysisParameters, setAnalysisParameters] = useState('');
  const [assessedBy, setAssessedBy] = useState('');
  const [notes, setNotes] = useState('');
  const [isAssessing, setIsAssessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const damageTypes = [
    { value: 'GENERAL', label: 'General Damage', icon: '⚠️' },
    { value: 'BUILDING_COLLAPSE', label: 'Building Collapse', icon: '🏢' },
    { value: 'FLOODING', label: 'Flooding', icon: '🌊' },
    { value: 'FIRE', label: 'Fire Damage', icon: '🔥' },
    { value: 'LANDSLIDE', label: 'Landslide', icon: '⛰️' },
    { value: 'DEBRIS', label: 'Debris', icon: '🗑️' },
    { value: 'INFRASTRUCTURE', label: 'Infrastructure', icon: '🛣️' },
    { value: 'VEGETATION', label: 'Vegetation', icon: '🌳' },
    { value: 'EROSION', label: 'Erosion', icon: '🏔️' },
    { value: 'CONTAMINATION', label: 'Contamination', icon: '☢️' }
  ];

  const analysisAlgorithms = [
    { value: 'MANUAL', label: 'Manual Assessment' },
    { value: 'NDVI_CHANGE_DETECTION', label: 'NDVI Change Detection' },
    { value: 'WATER_INDEX_ANALYSIS', label: 'Water Index Analysis' },
    { value: 'THERMAL_ANOMALY_DETECTION', label: 'Thermal Anomaly Detection' },
    { value: 'MACHINE_LEARNING', label: 'Machine Learning' },
    { value: 'DEEP_LEARNING', label: 'Deep Learning' }
  ];

  const addCoordinate = () => {
    setDamageCoordinates([...damageCoordinates, { longitude: 0, latitude: 0 }]);
  };

  const updateCoordinate = (index: number, field: 'longitude' | 'latitude', value: number) => {
    const newCoordinates = [...damageCoordinates];
    newCoordinates[index] = { ...newCoordinates[index], [field]: value };
    setDamageCoordinates(newCoordinates);
  };

  const removeCoordinate = (index: number) => {
    setDamageCoordinates(damageCoordinates.filter((_, i) => i !== index));
  };

  const performAssessment = async () => {
    if (!selectedImage) {
      setError('Please select a satellite image first');
      return;
    }

    if (damageCoordinates.length < 3) {
      setError('At least 3 coordinates are required to define a damage area');
      return;
    }

    setIsAssessing(true);
    setError(null);

    try {
      const assessment = await SatelliteService.performDamageAssessment({
        satelliteImageId: selectedImage.id,
        damageCoordinates,
        analysisAlgorithm,
        analysisParameters,
        assessedBy: assessedBy || 'Current User',
        notes
      });

      onAssessmentComplete?.(assessment);
      setError(null);
    } catch (err) {
      setError('Failed to perform damage assessment');
      console.error('Assessment error:', err);
    } finally {
      setIsAssessing(false);
    }
  };

  const performAutomatedDetection = async () => {
    if (!selectedImage) {
      setError('Please select a satellite image first');
      return;
    }

    setIsAssessing(true);
    setError(null);

    try {
      const assessments = await SatelliteService.performAutomatedDamageDetection(
        selectedImage.id,
        analysisAlgorithm
      );

      if (assessments.length > 0) {
        onAssessmentComplete?.(assessments[0]);
        setError(null);
      } else {
        setError('No damage detected by automated analysis');
      }
    } catch (err) {
      setError('Failed to perform automated damage detection');
      console.error('Automated detection error:', err);
    } finally {
      setIsAssessing(false);
    }
  };

  const getDamageTypeIcon = (type: string) => {
    const damageType = damageTypes.find(dt => dt.value === type);
    return damageType?.icon || '⚠️';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg max-w-2xl">
      <h2 className="text-xl font-bold mb-4">Damage Assessment</h2>

      {/* Selected Image Info */}
      {selectedImage && (
        <div className="mb-4 p-3 bg-blue-50 rounded-md">
          <h3 className="font-semibold mb-2">Selected Satellite Image</h3>
          <div className="grid grid-cols-2 gap-2 text-sm">
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
          </div>
        </div>
      )}

      {/* Damage Area Coordinates */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Damage Area Coordinates (Longitude, Latitude)
        </label>
        <div className="space-y-2">
          {damageCoordinates.map((coord, index) => (
            <div key={index} className="flex items-center space-x-2">
              <span className="text-sm text-gray-500 w-8">{index + 1}.</span>
              <input
                type="number"
                step="any"
                placeholder="Longitude"
                value={coord.longitude}
                onChange={(e) => updateCoordinate(index, 'longitude', parseFloat(e.target.value) || 0)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <input
                type="number"
                step="any"
                placeholder="Latitude"
                value={coord.latitude}
                onChange={(e) => updateCoordinate(index, 'latitude', parseFloat(e.target.value) || 0)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={() => removeCoordinate(index)}
                className="px-2 py-1 text-red-600 hover:bg-red-100 rounded"
              >
                ×
              </button>
            </div>
          ))}
        </div>
        <button
          onClick={addCoordinate}
          className="mt-2 px-4 py-2 text-blue-600 hover:bg-blue-100 rounded-md"
        >
          + Add Coordinate
        </button>
      </div>

      {/* Damage Type */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Damage Type
        </label>
        <div className="grid grid-cols-2 gap-2">
          {damageTypes.map(type => (
            <label
              key={type.value}
              className={`flex items-center p-2 border rounded-md cursor-pointer transition-colors ${
                damageType === type.value
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <input
                type="radio"
                name="damageType"
                value={type.value}
                checked={damageType === type.value}
                onChange={(e) => setDamageType(e.target.value)}
                className="mr-2"
              />
              <span className="mr-2">{type.icon}</span>
              <span className="text-sm">{type.label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Analysis Algorithm */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Analysis Algorithm
        </label>
        <select
          value={analysisAlgorithm}
          onChange={(e) => setAnalysisAlgorithm(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {analysisAlgorithms.map(algorithm => (
            <option key={algorithm.value} value={algorithm.value}>
              {algorithm.label}
            </option>
          ))}
        </select>
      </div>

      {/* Analysis Parameters */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Analysis Parameters (JSON)
        </label>
        <textarea
          value={analysisParameters}
          onChange={(e) => setAnalysisParameters(e.target.value)}
          placeholder='{"threshold": 0.5, "sensitivity": "high"}'
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          rows={3}
        />
      </div>

      {/* Assessed By */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Assessed By
        </label>
        <input
          type="text"
          value={assessedBy}
          onChange={(e) => setAssessedBy(e.target.value)}
          placeholder="Your name or system identifier"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Notes */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Notes
        </label>
        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="Additional notes about the damage assessment..."
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          rows={3}
        />
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-2 mb-4">
        <button
          onClick={performAssessment}
          disabled={isAssessing || !selectedImage || damageCoordinates.length < 3}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isAssessing ? 'Assessing...' : 'Manual Assessment'}
        </button>
        <button
          onClick={performAutomatedDetection}
          disabled={isAssessing || !selectedImage}
          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isAssessing ? 'Detecting...' : 'Automated Detection'}
        </button>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Instructions */}
      <div className="text-sm text-gray-600">
        <p className="mb-2">
          <strong>Instructions:</strong>
        </p>
        <ul className="list-disc list-inside space-y-1">
          <li>Select a satellite image from the map</li>
          <li>Add coordinates to define the damage area</li>
          <li>Choose the appropriate damage type and analysis algorithm</li>
          <li>Click "Manual Assessment" for human assessment or "Automated Detection" for AI analysis</li>
        </ul>
      </div>
    </div>
  );
};



