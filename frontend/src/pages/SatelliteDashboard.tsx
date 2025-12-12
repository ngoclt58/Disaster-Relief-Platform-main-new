import React, { useState } from 'react';
import { SatelliteImageryViewer } from '../components/satellite/SatelliteImageryViewer';
import { DamageAssessmentPanel } from '../components/satellite/DamageAssessmentPanel';
import { SatelliteService, SatelliteImage, DamageAssessment, SatelliteImageStatistics, DamageAssessmentStatistics } from '../services/satelliteService';

export const SatelliteDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'viewer' | 'assessment' | 'statistics'>('viewer');
  const [selectedImage, setSelectedImage] = useState<SatelliteImage | null>(null);
  const [selectedAssessment, setSelectedAssessment] = useState<DamageAssessment | null>(null);
  const [imageStatistics, setImageStatistics] = useState<SatelliteImageStatistics | null>(null);
  const [damageStatistics, setDamageStatistics] = useState<DamageAssessmentStatistics | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>([-74.0, 40.7]); // Default to NYC area
  const [mapZoom, setMapZoom] = useState(10);

  const handleImageSelect = (image: SatelliteImage) => {
    setSelectedImage(image);
    setActiveTab('assessment');
  };

  const handleDamageSelect = (assessment: DamageAssessment) => {
    setSelectedAssessment(assessment);
  };

  const handleAssessmentComplete = (assessment: DamageAssessment) => {
    setSelectedAssessment(assessment);
    setActiveTab('viewer');
  };

  const loadStatistics = async () => {
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30); // Last 30 days
    const endDate = new Date();

    try {
      const [imageStats, damageStats] = await Promise.all([
        SatelliteService.getImageStatistics(startDate.toISOString(), endDate.toISOString()),
        SatelliteService.getDamageStatistics(startDate.toISOString(), endDate.toISOString())
      ]);

      setImageStatistics(imageStats);
      setDamageStatistics(damageStats);
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  };

  const tabs = [
    { id: 'viewer', label: 'Satellite Viewer', icon: '🛰️' },
    { id: 'assessment', label: 'Damage Assessment', icon: '📊' },
    { id: 'statistics', label: 'Statistics', icon: '📈' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Satellite Imagery & Damage Assessment</h1>
              <p className="text-gray-600">Real-time satellite data analysis for disaster relief operations</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-500">
                {selectedImage && (
                  <span className="inline-block px-2 py-1 bg-blue-100 text-blue-800 rounded">
                    Image: {selectedImage.provider}
                  </span>
                )}
                {selectedAssessment && (
                  <span className="inline-block px-2 py-1 bg-red-100 text-red-800 rounded ml-2">
                    Damage: {selectedAssessment.damageType}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex space-x-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id as any);
                  if (tab.id === 'statistics') {
                    loadStatistics();
                  }
                }}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'viewer' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Panel - Controls */}
            <div className="lg:col-span-1">
              <div className="space-y-4">
                <div className="bg-white p-4 rounded-lg shadow">
                  <h3 className="font-semibold mb-3">Map Controls</h3>
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Center Coordinates
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        <input
                          type="number"
                          step="any"
                          placeholder="Longitude"
                          value={mapCenter[0]}
                          onChange={(e) => setMapCenter([parseFloat(e.target.value) || 0, mapCenter[1]])}
                          className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Latitude"
                          value={mapCenter[1]}
                          onChange={(e) => setMapCenter([mapCenter[0], parseFloat(e.target.value) || 0])}
                          className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Zoom Level
                      </label>
                      <input
                        type="range"
                        min="1"
                        max="18"
                        value={mapZoom}
                        onChange={(e) => setMapZoom(parseInt(e.target.value))}
                        className="w-full"
                      />
                      <div className="text-xs text-gray-500 text-center">{mapZoom}</div>
                    </div>
                  </div>
                </div>

                {/* Selected Image Info */}
                {selectedImage && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Selected Image</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Provider:</span>
                        <span className="font-medium">{selectedImage.provider}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Satellite:</span>
                        <span>{selectedImage.satelliteName}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Captured:</span>
                        <span>{new Date(selectedImage.capturedAt).toLocaleDateString()}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Resolution:</span>
                        <span>{SatelliteService.formatResolution(selectedImage.resolutionMeters)}</span>
                      </div>
                      {selectedImage.cloudCoverPercentage && (
                        <div className="flex justify-between">
                          <span>Cloud Cover:</span>
                          <span>{selectedImage.cloudCoverPercentage.toFixed(1)}%</span>
                        </div>
                      )}
                      {selectedImage.qualityScore && (
                        <div className="flex justify-between">
                          <span>Quality:</span>
                          <span className={`font-medium ${
                            selectedImage.qualityScore >= 0.8 ? 'text-green-600' :
                            selectedImage.qualityScore >= 0.6 ? 'text-yellow-600' : 'text-red-600'
                          }`}>
                            {(selectedImage.qualityScore * 100).toFixed(0)}%
                          </span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span>Status:</span>
                        <span className={`font-medium ${
                          selectedImage.processingStatus === 'COMPLETED' ? 'text-green-600' :
                          selectedImage.processingStatus === 'PROCESSING' ? 'text-yellow-600' : 'text-gray-600'
                        }`}>
                          {selectedImage.processingStatus}
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Selected Assessment Info */}
                {selectedAssessment && (
                  <div className="bg-white p-4 rounded-lg shadow">
                    <h3 className="font-semibold mb-3">Selected Assessment</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Type:</span>
                        <span className="font-medium">
                          {SatelliteService.getDamageTypeIcon(selectedAssessment.damageType)} {selectedAssessment.damageType}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Severity:</span>
                        <span className={`font-medium ${
                          selectedAssessment.severity === 'CATASTROPHIC' ? 'text-red-600' :
                          selectedAssessment.severity === 'SEVERE' ? 'text-orange-600' :
                          selectedAssessment.severity === 'MODERATE' ? 'text-yellow-600' : 'text-green-600'
                        }`}>
                          {selectedAssessment.severity}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Confidence:</span>
                        <span className={`font-medium ${
                          selectedAssessment.confidenceScore >= 0.8 ? 'text-green-600' :
                          selectedAssessment.confidenceScore >= 0.6 ? 'text-yellow-600' : 'text-red-600'
                        }`}>
                          {(selectedAssessment.confidenceScore * 100).toFixed(0)}%
                        </span>
                      </div>
                      {selectedAssessment.damagePercentage && (
                        <div className="flex justify-between">
                          <span>Damage %:</span>
                          <span>{selectedAssessment.damagePercentage.toFixed(1)}%</span>
                        </div>
                      )}
                      {selectedAssessment.affectedAreaSqm && (
                        <div className="flex justify-between">
                          <span>Area:</span>
                          <span>{SatelliteService.formatArea(selectedAssessment.affectedAreaSqm)}</span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span>Assessed:</span>
                        <span>{new Date(selectedAssessment.assessedAt).toLocaleDateString()}</span>
                      </div>
                      {selectedAssessment.assessedBy && (
                        <div className="flex justify-between">
                          <span>By:</span>
                          <span>{selectedAssessment.assessedBy}</span>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right Panel - Map */}
            <div className="lg:col-span-2">
              <div className="bg-white rounded-lg shadow h-96 lg:h-[600px]">
                <SatelliteImageryViewer
                  center={mapCenter}
                  zoom={mapZoom}
                  showImages={true}
                  showDamage={true}
                  selectedImage={selectedImage || undefined}
                  onImageSelect={handleImageSelect}
                  onDamageSelect={handleDamageSelect}
                />
              </div>
            </div>
          </div>
        )}

        {activeTab === 'assessment' && (
          <div className="max-w-4xl mx-auto">
            <DamageAssessmentPanel
              onAssessmentComplete={handleAssessmentComplete}
              selectedImage={selectedImage || undefined}
            />
          </div>
        )}

        {activeTab === 'statistics' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Image Statistics */}
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4">Satellite Image Statistics</h3>
              {imageStatistics ? (
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span>Total Images:</span>
                    <span className="font-medium">{imageStatistics.totalImages}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Average Resolution:</span>
                    <span>{SatelliteService.formatResolution(imageStatistics.avgResolution)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Average Cloud Cover:</span>
                    <span>{imageStatistics.avgCloudCover.toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Average Quality:</span>
                    <span className={`font-medium ${
                      imageStatistics.avgQuality >= 0.8 ? 'text-green-600' :
                      imageStatistics.avgQuality >= 0.6 ? 'text-yellow-600' : 'text-red-600'
                    }`}>
                      {(imageStatistics.avgQuality * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Earliest Capture:</span>
                    <span>{new Date(imageStatistics.earliestCapture).toLocaleDateString()}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Latest Capture:</span>
                    <span>{new Date(imageStatistics.latestCapture).toLocaleDateString()}</span>
                  </div>
                </div>
              ) : (
                <div className="text-gray-500">Loading statistics...</div>
              )}
            </div>

            {/* Damage Statistics */}
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4">Damage Assessment Statistics</h3>
              {damageStatistics ? (
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span>Total Assessments:</span>
                    <span className="font-medium">{damageStatistics.totalAssessments}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Average Confidence:</span>
                    <span className={`font-medium ${
                      damageStatistics.avgConfidence >= 0.8 ? 'text-green-600' :
                      damageStatistics.avgConfidence >= 0.6 ? 'text-yellow-600' : 'text-red-600'
                    }`}>
                      {(damageStatistics.avgConfidence * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Average Damage %:</span>
                    <span>{damageStatistics.avgDamagePercentage.toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Total Affected Area:</span>
                    <span>{SatelliteService.formatArea(damageStatistics.totalAffectedArea)}</span>
                  </div>
                </div>
              ) : (
                <div className="text-gray-500">Loading statistics...</div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="bg-white border-t mt-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="text-sm text-gray-500">
            <p>
              <strong>Satellite Imagery & Damage Assessment</strong> - Real-time satellite data analysis 
              for disaster relief operations with automated damage detection and assessment capabilities.
            </p>
            <p className="mt-2">
              Features include multi-provider satellite imagery support, automated damage detection algorithms, 
              change detection analysis, and comprehensive damage assessment tools for emergency response teams.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};



