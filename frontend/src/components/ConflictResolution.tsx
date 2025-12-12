import React, { useState, useEffect } from 'react';
import { AlertTriangle, CheckCircle, X, Merge, User, Server, Clock } from 'lucide-react';
import { offlineApiService } from '../services/offlineApiService';

interface Conflict {
  id?: number;
  entityId: string;
  entityType: string;
  localData: any;
  serverData: any;
  resolution: 'local' | 'server' | 'merge' | 'manual';
  resolvedAt?: number;
}

interface ConflictResolutionProps {
  onResolve?: (conflictId: number) => void;
  onClose?: () => void;
}

const ConflictResolution: React.FC<ConflictResolutionProps> = ({ onResolve, onClose }) => {
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [selectedConflict, setSelectedConflict] = useState<Conflict | null>(null);
  const [mergedData, setMergedData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadConflicts();
  }, []);

  const loadConflicts = async () => {
    try {
      const pendingConflicts = await offlineApiService.getPendingConflicts();
      setConflicts(pendingConflicts);
    } catch (error) {
      console.error('Failed to load conflicts:', error);
    }
  };

  const handleResolve = async (conflictId: number | undefined, resolution: 'local' | 'server' | 'merge') => {
    if (conflictId === undefined) {
      console.error('Cannot resolve conflict: conflictId is undefined');
      return;
    }
    setLoading(true);
    try {
      await offlineApiService.resolveConflict(conflictId, resolution, mergedData);
      await loadConflicts();
      onResolve?.(conflictId);
      
      if (conflicts.length === 1) {
        onClose?.();
      }
    } catch (error) {
      console.error('Failed to resolve conflict:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMerge = (conflict: Conflict) => {
    setSelectedConflict(conflict);
    setMergedData({
      ...conflict.serverData,
      ...conflict.localData,
      _merged: true,
      _mergedAt: Date.now()
    });
  };

  const updateMergedData = (field: string, value: any) => {
    setMergedData((prev: any) => ({
      ...prev,
      [field]: value
    }));
  };

  const getEntityDisplayName = (entityType: string) => {
    switch (entityType) {
      case 'needs': return 'Need Request';
      case 'tasks': return 'Task';
      case 'inventory': return 'Inventory Item';
      case 'users': return 'User';
      case 'deliveries': return 'Delivery';
      default: return entityType;
    }
  };

  const formatData = (data: any) => {
    if (typeof data === 'object' && data !== null) {
      return Object.entries(data)
        .filter(([key]) => !key.startsWith('_'))
        .map(([key, value]) => (
          <div key={key} className="text-sm">
            <span className="font-medium">{key}:</span> {String(value)}
          </div>
        ));
    }
    return <div className="text-sm">{String(data)}</div>;
  };

  if (conflicts.length === 0) {
    return null;
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <AlertTriangle className="w-6 h-6 text-orange-600" />
              <h2 className="text-xl font-semibold text-gray-900">
                Data Conflicts Detected
              </h2>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          <div className="mb-4 p-4 bg-orange-50 border border-orange-200 rounded-lg">
            <p className="text-sm text-orange-800">
              The following data conflicts were detected when syncing your offline changes. 
              Please choose how to resolve each conflict.
            </p>
          </div>

          {conflicts.map((conflict, index) => (
            <div key={conflict.id ?? `conflict-${index}`} className="mb-6 border border-gray-200 rounded-lg p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-900">
                  {getEntityDisplayName(conflict.entityType)} - {conflict.entityId}
                </h3>
                <span className="text-sm text-gray-500">
                  <Clock className="w-4 h-4 inline mr-1" />
                  {new Date(conflict.localData._timestamp || Date.now()).toLocaleString()}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                {/* Local Data */}
                <div className="border border-blue-200 rounded-lg p-4">
                  <div className="flex items-center space-x-2 mb-3">
                    <User className="w-4 h-4 text-blue-600" />
                    <h4 className="font-medium text-blue-900">Your Changes (Local)</h4>
                  </div>
                  <div className="space-y-2">
                    {formatData(conflict.localData)}
                  </div>
                </div>

                {/* Server Data */}
                <div className="border border-green-200 rounded-lg p-4">
                  <div className="flex items-center space-x-2 mb-3">
                    <Server className="w-4 h-4 text-green-600" />
                    <h4 className="font-medium text-green-900">Server Version</h4>
                  </div>
                  <div className="space-y-2">
                    {formatData(conflict.serverData)}
                  </div>
                </div>
              </div>

              {/* Resolution Options */}
              <div className="flex items-center space-x-4">
                <button
                  onClick={() => handleResolve(conflict.id, 'local')}
                  disabled={loading}
                  className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                >
                  <User className="w-4 h-4 mr-2" />
                  Keep My Changes
                </button>

                <button
                  onClick={() => handleResolve(conflict.id, 'server')}
                  disabled={loading}
                  className="flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                >
                  <Server className="w-4 h-4 mr-2" />
                  Use Server Version
                </button>

                <button
                  onClick={() => handleMerge(conflict)}
                  disabled={loading}
                  className="flex items-center px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 disabled:opacity-50"
                >
                  <Merge className="w-4 h-4 mr-2" />
                  Merge Changes
                </button>
              </div>
            </div>
          ))}

          {/* Merge Modal */}
          {selectedConflict && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-60 p-4">
              <div className="bg-white rounded-lg w-full max-w-2xl max-h-[80vh] overflow-y-auto">
                <div className="p-6">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Merge Changes
                    </h3>
                    <button
                      onClick={() => setSelectedConflict(null)}
                      className="text-gray-400 hover:text-gray-600"
                    >
                      <X className="w-6 h-6" />
                    </button>
                  </div>

                  <div className="space-y-4">
                    {Object.entries(selectedConflict.localData)
                      .filter(([key]) => !key.startsWith('_'))
                      .map(([key, value]) => (
                        <div key={key} className="border border-gray-200 rounded-lg p-4">
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            {key}
                          </label>
                          <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                            <div className="text-sm text-blue-600">
                              <div className="font-medium">Your version:</div>
                              <div>{String(value)}</div>
                            </div>
                            <div className="text-sm text-green-600">
                              <div className="font-medium">Server version:</div>
                              <div>{String(selectedConflict.serverData[key] || '')}</div>
                            </div>
                            <div>
                              <input
                                type="text"
                                value={mergedData?.[key] || ''}
                                onChange={(e) => updateMergedData(key, e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                placeholder="Merged value"
                              />
                            </div>
                          </div>
                        </div>
                      ))}
                  </div>

                  <div className="flex justify-end space-x-3 mt-6">
                    <button
                      onClick={() => setSelectedConflict(null)}
                      className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={() => handleResolve(selectedConflict.id, 'merge')}
                      disabled={loading}
                      className="flex items-center px-4 py-2 text-sm font-medium text-white bg-purple-600 border border-transparent rounded-md hover:bg-purple-700 disabled:opacity-50"
                    >
                      <Merge className="w-4 h-4 mr-2" />
                      Apply Merge
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ConflictResolution;



