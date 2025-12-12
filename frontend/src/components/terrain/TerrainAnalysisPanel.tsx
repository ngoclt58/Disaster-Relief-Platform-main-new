import React, { useState } from 'react';
import { TerrainService, TerrainAnalysis, ElevationStatistics } from '../../services/terrainService';

interface TerrainAnalysisPanelProps {
  onAnalysisComplete?: (analysis: TerrainAnalysis) => void;
  onStatisticsUpdate?: (stats: ElevationStatistics) => void;
}

export const TerrainAnalysisPanel: React.FC<TerrainAnalysisPanelProps> = ({
  onAnalysisComplete,
  onStatisticsUpdate
}) => {
  const [coordinates, setCoordinates] = useState<Array<{ longitude: number; latitude: number }>>([]);
  const [analysisType, setAnalysisType] = useState('GENERAL');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysis, setAnalysis] = useState<TerrainAnalysis | null>(null);
  const [statistics, setStatistics] = useState<ElevationStatistics | null>(null);
  const [error, setError] = useState<string | null>(null);

  const analysisTypes = [
    { value: 'GENERAL', label: 'General Analysis' },
    { value: 'ROUTING', label: 'Routing Analysis' },
    { value: 'ACCESSIBILITY', label: 'Accessibility Analysis' },
    { value: 'FLOOD_RISK', label: 'Flood Risk Analysis' },
    { value: 'EMERGENCY_RESPONSE', label: 'Emergency Response' },
    { value: 'INFRASTRUCTURE', label: 'Infrastructure Planning' }
  ];

  const addCoordinate = () => {
    setCoordinates([...coordinates, { longitude: 0, latitude: 0 }]);
  };

  const updateCoordinate = (index: number, field: 'longitude' | 'latitude', value: number) => {
    const newCoordinates = [...coordinates];
    newCoordinates[index] = { ...newCoordinates[index], [field]: value };
    setCoordinates(newCoordinates);
  };

  const removeCoordinate = (index: number) => {
    setCoordinates(coordinates.filter((_, i) => i !== index));
  };

  const performAnalysis = async () => {
    if (coordinates.length < 3) {
      setError('At least 3 coordinates are required to define an area');
      return;
    }

    setIsAnalyzing(true);
    setError(null);

    try {
      const result = await TerrainService.performTerrainAnalysis(coordinates, analysisType);
      if (result) {
        setAnalysis(result);
        onAnalysisComplete?.(result);
      } else {
        setError('Failed to perform terrain analysis');
      }
    } catch (err) {
      setError('Error performing terrain analysis');
      console.error('Analysis error:', err);
    } finally {
      setIsAnalyzing(false);
    }
  };

  const loadStatistics = async () => {
    if (coordinates.length < 2) {
      setError('At least 2 coordinates are required for statistics');
      return;
    }

    try {
      const lons = coordinates.map(c => c.longitude);
      const lats = coordinates.map(c => c.latitude);
      const minLon = Math.min(...lons);
      const maxLon = Math.max(...lons);
      const minLat = Math.min(...lats);
      const maxLat = Math.max(...lats);

      const stats = await TerrainService.getElevationStatistics(minLon, minLat, maxLon, maxLat);
      if (stats) {
        setStatistics(stats);
        onStatisticsUpdate?.(stats);
      }
    } catch (err) {
      setError('Error loading elevation statistics');
      console.error('Statistics error:', err);
    }
  };

  const getAccessibilityColor = (score: number) => {
    if (score >= 0.8) return 'text-green-600';
    if (score >= 0.6) return 'text-yellow-600';
    if (score >= 0.4) return 'text-orange-600';
    return 'text-red-600';
  };

  const getFloodRiskColor = (score: number) => {
    if (score >= 0.8) return 'text-red-600';
    if (score >= 0.6) return 'text-orange-600';
    if (score >= 0.4) return 'text-yellow-600';
    return 'text-green-600';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg max-w-2xl">
      <h2 className="text-xl font-bold mb-4">Terrain Analysis</h2>

      {/* Coordinate Input */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Area Coordinates (Longitude, Latitude)
        </label>
        <div className="space-y-2">
          {coordinates.map((coord, index) => (
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

      {/* Analysis Type */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Analysis Type
        </label>
        <select
          value={analysisType}
          onChange={(e) => setAnalysisType(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {analysisTypes.map(type => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-2 mb-4">
        <button
          onClick={performAnalysis}
          disabled={isAnalyzing || coordinates.length < 3}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isAnalyzing ? 'Analyzing...' : 'Analyze Terrain'}
        </button>
        <button
          onClick={loadStatistics}
          disabled={coordinates.length < 2}
          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Load Statistics
        </button>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Analysis Results */}
      {analysis && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md">
          <h3 className="font-semibold mb-2">Analysis Results</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="font-medium">Elevation Range:</span>
              <div className="text-gray-600">
                {analysis.minElevation?.toFixed(1)}m - {analysis.maxElevation?.toFixed(1)}m
              </div>
            </div>
            <div>
              <span className="font-medium">Average Elevation:</span>
              <div className="text-gray-600">{analysis.avgElevation?.toFixed(1)}m</div>
            </div>
            <div>
              <span className="font-medium">Slope Range:</span>
              <div className="text-gray-600">
                0° - {analysis.slopeMaximum?.toFixed(1)}°
              </div>
            </div>
            <div>
              <span className="font-medium">Average Slope:</span>
              <div className="text-gray-600">{analysis.slopeAverage?.toFixed(1)}°</div>
            </div>
            <div>
              <span className="font-medium">Accessibility Score:</span>
              <div className={`font-medium ${getAccessibilityColor(analysis.accessibilityScore || 0)}`}>
                {((analysis.accessibilityScore || 0) * 100).toFixed(1)}%
              </div>
            </div>
            <div>
              <span className="font-medium">Flood Risk Score:</span>
              <div className={`font-medium ${getFloodRiskColor(analysis.floodRiskScore || 0)}`}>
                {((analysis.floodRiskScore || 0) * 100).toFixed(1)}%
              </div>
            </div>
            <div>
              <span className="font-medium">Roughness Index:</span>
              <div className="text-gray-600">{analysis.roughnessIndex?.toFixed(1)}</div>
            </div>
            <div>
              <span className="font-medium">Aspect:</span>
              <div className="text-gray-600">{analysis.aspectAverage?.toFixed(1)}°</div>
            </div>
          </div>
        </div>
      )}

      {/* Statistics Results */}
      {statistics && (
        <div className="mb-4 p-4 bg-blue-50 rounded-md">
          <h3 className="font-semibold mb-2">Elevation Statistics</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="font-medium">Min Elevation:</span>
              <div className="text-gray-600">{statistics.minElevation?.toFixed(1)}m</div>
            </div>
            <div>
              <span className="font-medium">Max Elevation:</span>
              <div className="text-gray-600">{statistics.maxElevation?.toFixed(1)}m</div>
            </div>
            <div>
              <span className="font-medium">Average Elevation:</span>
              <div className="text-gray-600">{statistics.avgElevation?.toFixed(1)}m</div>
            </div>
            <div>
              <span className="font-medium">Standard Deviation:</span>
              <div className="text-gray-600">{statistics.elevationStddev?.toFixed(1)}m</div>
            </div>
            <div>
              <span className="font-medium">Data Points:</span>
              <div className="text-gray-600">{statistics.pointCount}</div>
            </div>
          </div>
        </div>
      )}

      {/* Instructions */}
      <div className="text-sm text-gray-600">
        <p className="mb-2">
          <strong>Instructions:</strong>
        </p>
        <ul className="list-disc list-inside space-y-1">
          <li>Add at least 3 coordinates to define an analysis area</li>
          <li>Choose the appropriate analysis type for your needs</li>
          <li>Click "Analyze Terrain" to perform the analysis</li>
          <li>Use "Load Statistics" to get elevation data summary</li>
        </ul>
      </div>
    </div>
  );
};



