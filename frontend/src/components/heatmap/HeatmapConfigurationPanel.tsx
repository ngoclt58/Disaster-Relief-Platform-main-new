import React, { useState } from 'react';
import { HeatmapService, HeatmapLayer } from '../../services/heatmapService';

interface HeatmapConfigurationPanelProps {
  onLayerGenerated?: (layer: HeatmapLayer) => void;
}

export const HeatmapConfigurationPanel: React.FC<HeatmapConfigurationPanelProps> = ({
  onLayerGenerated
}) => {
  const [layerName, setLayerName] = useState('');
  const [layerDescription, setLayerDescription] = useState('');
  const [selectedHeatmapType, setSelectedHeatmapType] = useState('DISASTER_IMPACT');
  const [isPublic, setIsPublic] = useState(false);
  const [expiresAt, setExpiresAt] = useState('');
  const [generationParameters, setGenerationParameters] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const availableHeatmapTypes = HeatmapService.getAvailableHeatmapTypes();

  const handleGenerateLayer = async () => {
    if (!layerName.trim()) {
      setError('Layer name is required');
      return;
    }

    setIsGenerating(true);
    setError(null);

    try {
      const layerData = {
        name: layerName,
        description: layerDescription || undefined,
        heatmapType: selectedHeatmapType,
        isPublic,
        expiresAt: expiresAt || undefined,
        generationParameters: generationParameters || undefined
      };

      const layer = await HeatmapService.generateHeatmapLayer(layerData);
      onLayerGenerated?.(layer);
      setError(null);
    } catch (err) {
      setError('Failed to generate heatmap layer');
      console.error('Layer generation error:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  const getHeatmapTypeColor = (type: string) => {
    const heatmapType = availableHeatmapTypes.find(t => t.value === type);
    return heatmapType?.color || '#808080';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg max-w-2xl">
      <h2 className="text-xl font-bold mb-4">Heatmap Layer Configuration</h2>

      {/* Layer Information */}
      <div className="space-y-4 mb-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Layer Name *
          </label>
          <input
            type="text"
            value={layerName}
            onChange={(e) => setLayerName(e.target.value)}
            placeholder="Enter layer name"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description
          </label>
          <textarea
            value={layerDescription}
            onChange={(e) => setLayerDescription(e.target.value)}
            placeholder="Enter layer description"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={3}
          />
        </div>
      </div>

      {/* Heatmap Type Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Heatmap Type
        </label>
        <div className="grid grid-cols-2 gap-3">
          {availableHeatmapTypes.map(type => (
            <label
              key={type.value}
              className={`flex items-center p-3 border rounded-md cursor-pointer transition-colors ${
                selectedHeatmapType === type.value
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <input
                type="radio"
                name="heatmapType"
                value={type.value}
                checked={selectedHeatmapType === type.value}
                onChange={(e) => setSelectedHeatmapType(e.target.value)}
                className="mr-3"
              />
              <div className="flex items-center space-x-2">
                <span className="text-lg">{type.icon}</span>
                <div>
                  <div className="font-medium text-sm">{type.label}</div>
                  <div 
                    className="w-3 h-3 rounded-full mt-1"
                    style={{ backgroundColor: type.color }}
                  ></div>
                </div>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Layer Settings */}
      <div className="space-y-4 mb-6">
        <div className="flex items-center">
          <input
            type="checkbox"
            id="isPublic"
            checked={isPublic}
            onChange={(e) => setIsPublic(e.target.checked)}
            className="mr-2"
          />
          <label htmlFor="isPublic" className="text-sm font-medium text-gray-700">
            Make layer public
          </label>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Expiration Date (Optional)
          </label>
          <input
            type="datetime-local"
            value={expiresAt}
            onChange={(e) => setExpiresAt(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Generation Parameters (JSON)
          </label>
          <textarea
            value={generationParameters}
            onChange={(e) => setGenerationParameters(e.target.value)}
            placeholder='{"radius": 1000, "intensity": 0.8, "opacity": 0.6}'
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={3}
          />
        </div>
      </div>

      {/* Preview */}
      <div className="mb-6 p-4 bg-gray-50 rounded-md">
        <h3 className="font-semibold mb-2">Layer Preview</h3>
        <div className="space-y-2 text-sm">
          <div className="flex items-center space-x-2">
            <span className="text-lg">
              {availableHeatmapTypes.find(t => t.value === selectedHeatmapType)?.icon}
            </span>
            <span className="font-medium">{layerName || 'Untitled Layer'}</span>
          </div>
          {layerDescription && (
            <div className="text-gray-600">{layerDescription}</div>
          )}
          <div className="flex items-center space-x-4">
            <span className="text-gray-600">
              Type: {availableHeatmapTypes.find(t => t.value === selectedHeatmapType)?.label}
            </span>
            <span className="text-gray-600">
              Public: {isPublic ? 'Yes' : 'No'}
            </span>
          </div>
          {expiresAt && (
            <div className="text-gray-600">
              Expires: {new Date(expiresAt).toLocaleString()}
            </div>
          )}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-3">
        <button
          onClick={handleGenerateLayer}
          disabled={isGenerating || !layerName.trim()}
          className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isGenerating ? 'Generating...' : 'Generate Layer'}
        </button>
        
        <button
          onClick={() => {
            setLayerName('');
            setLayerDescription('');
            setSelectedHeatmapType('DISASTER_IMPACT');
            setIsPublic(false);
            setExpiresAt('');
            setGenerationParameters('');
            setError(null);
          }}
          className="px-6 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400"
        >
          Reset
        </button>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      {/* Instructions */}
      <div className="mt-6 text-sm text-gray-600">
        <p className="mb-2">
          <strong>Instructions:</strong>
        </p>
        <ul className="list-disc list-inside space-y-1">
          <li>Select a heatmap type to visualize different data categories</li>
          <li>Configure layer settings for public access and expiration</li>
          <li>Add generation parameters to customize the heatmap appearance</li>
          <li>Click "Generate Layer" to create the heatmap layer</li>
        </ul>
      </div>
    </div>
  );
};



