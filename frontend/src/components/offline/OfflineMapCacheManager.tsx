import React, { useState, useEffect } from 'react';
import { OfflineMapService, OfflineMapCache, OfflineMapCacheRequest } from '../../services/offlineMapService';
import { 
  Download, 
  Pause, 
  Play, 
  Trash2, 
  MapPin, 
  HardDrive, 
  Clock, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  RefreshCw,
  Plus,
  Filter,
  Search
} from 'lucide-react';

interface OfflineMapCacheManagerProps {
  className?: string;
}

export const OfflineMapCacheManager: React.FC<OfflineMapCacheManagerProps> = ({ className = '' }) => {
  const [caches, setCaches] = useState<OfflineMapCache[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCache, setSelectedCache] = useState<OfflineMapCache | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [statistics, setStatistics] = useState<any>(null);

  useEffect(() => {
    loadCaches();
    loadStatistics();
  }, []);

  const loadCaches = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await OfflineMapService.getAllOfflineMapCaches();
      setCaches(data);
    } catch (err) {
      console.error('Error loading caches:', err);
      setError('Failed to load offline map caches');
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const stats = await OfflineMapService.getGlobalStatistics();
      setStatistics(stats);
    } catch (err) {
      console.error('Error loading statistics:', err);
    }
  };

  const handleCreateCache = async (request: OfflineMapCacheRequest) => {
    try {
      const newCache = await OfflineMapService.createOfflineMapCache(request);
      setCaches(prev => [newCache, ...prev]);
      setShowCreateForm(false);
    } catch (err) {
      console.error('Error creating cache:', err);
      setError('Failed to create offline map cache');
    }
  };

  const handleStartDownload = async (cacheId: number) => {
    try {
      await OfflineMapService.startCacheDownload(cacheId);
      await loadCaches();
    } catch (err) {
      console.error('Error starting download:', err);
      setError('Failed to start download');
    }
  };

  const handlePauseDownload = async (cacheId: number) => {
    try {
      await OfflineMapService.pauseCacheDownload(cacheId);
      await loadCaches();
    } catch (err) {
      console.error('Error pausing download:', err);
      setError('Failed to pause download');
    }
  };

  const handleResumeDownload = async (cacheId: number) => {
    try {
      await OfflineMapService.resumeCacheDownload(cacheId);
      await loadCaches();
    } catch (err) {
      console.error('Error resuming download:', err);
      setError('Failed to resume download');
    }
  };

  const handleDeleteCache = async (cacheId: number) => {
    if (!window.confirm('Are you sure you want to delete this cache? This action cannot be undone.')) {
      return;
    }

    try {
      await OfflineMapService.deleteOfflineMapCache(cacheId);
      setCaches(prev => prev.filter(cache => cache.id !== cacheId));
    } catch (err) {
      console.error('Error deleting cache:', err);
      setError('Failed to delete cache');
    }
  };

  const handleCleanupExpired = async () => {
    try {
      await OfflineMapService.cleanupExpiredCaches();
      await loadCaches();
    } catch (err) {
      console.error('Error cleaning up caches:', err);
      setError('Failed to cleanup expired caches');
    }
  };

  const filteredCaches = caches.filter(cache => {
    const matchesStatus = filterStatus === 'ALL' || cache.status === filterStatus;
    const matchesSearch = searchQuery === '' || 
      cache.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cache.regionName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesStatus && matchesSearch;
  });

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-4 h-4 text-green-600" />;
      case 'DOWNLOADING':
        return <RefreshCw className="w-4 h-4 text-blue-600 animate-spin" />;
      case 'FAILED':
        return <XCircle className="w-4 h-4 text-red-600" />;
      case 'PENDING':
        return <Clock className="w-4 h-4 text-yellow-600" />;
      case 'PAUSED':
        return <Pause className="w-4 h-4 text-orange-600" />;
      default:
        return <AlertTriangle className="w-4 h-4 text-gray-600" />;
    }
  };

  const getActionButton = (cache: OfflineMapCache) => {
    switch (cache.status) {
      case 'PENDING':
        return (
          <button
            onClick={() => handleStartDownload(cache.id)}
            className="flex items-center space-x-1 px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            <Download className="w-4 h-4" />
            <span>Start</span>
          </button>
        );
      case 'DOWNLOADING':
        return (
          <button
            onClick={() => handlePauseDownload(cache.id)}
            className="flex items-center space-x-1 px-3 py-1 bg-orange-600 text-white rounded-md hover:bg-orange-700"
          >
            <Pause className="w-4 h-4" />
            <span>Pause</span>
          </button>
        );
      case 'PAUSED':
        return (
          <button
            onClick={() => handleResumeDownload(cache.id)}
            className="flex items-center space-x-1 px-3 py-1 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            <Play className="w-4 h-4" />
            <span>Resume</span>
          </button>
        );
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className={`flex items-center justify-center h-64 ${className}`}>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`flex items-center justify-center h-64 ${className}`}>
        <div className="text-red-600 text-center">
          <AlertTriangle className="w-8 h-8 mx-auto mb-2" />
          <p>{error}</p>
          <button
            onClick={loadCaches}
            className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-white rounded-lg shadow-lg ${className}`}>
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">Offline Map Caches</h2>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setShowCreateForm(true)}
              className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              <Plus className="w-4 h-4" />
              <span>Create Cache</span>
            </button>
            <button
              onClick={handleCleanupExpired}
              className="flex items-center space-x-2 px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
            >
              <Trash2 className="w-4 h-4" />
              <span>Cleanup</span>
            </button>
          </div>
        </div>

        {/* Statistics */}
        {statistics && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
            <div className="bg-blue-50 p-3 rounded-md">
              <div className="text-sm text-blue-600 font-medium">Total Caches</div>
              <div className="text-2xl font-bold text-blue-900">{statistics.totalCaches}</div>
            </div>
            <div className="bg-green-50 p-3 rounded-md">
              <div className="text-sm text-green-600 font-medium">Completed</div>
              <div className="text-2xl font-bold text-green-900">{statistics.completedCaches}</div>
            </div>
            <div className="bg-blue-50 p-3 rounded-md">
              <div className="text-sm text-blue-600 font-medium">Downloading</div>
              <div className="text-2xl font-bold text-blue-900">{statistics.downloadingCaches}</div>
            </div>
            <div className="bg-gray-50 p-3 rounded-md">
              <div className="text-sm text-gray-600 font-medium">Total Size</div>
              <div className="text-2xl font-bold text-gray-900">
                {OfflineMapService.formatFileSize(statistics.totalSizeBytes)}
              </div>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="flex items-center space-x-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search caches..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="DOWNLOADING">Downloading</option>
            <option value="COMPLETED">Completed</option>
            <option value="PAUSED">Paused</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>
      </div>

      {/* Cache List */}
      <div className="p-6">
        <div className="space-y-4">
          {filteredCaches.map(cache => (
            <div
              key={cache.id}
              className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    {getStatusIcon(cache.status)}
                    <span className="text-lg font-medium">{cache.name}</span>
                    <span className={`px-2 py-1 rounded-full text-xs ${OfflineMapService.getStatusColor(cache.status)}`}>
                      {cache.status}
                    </span>
                    <span className={`px-2 py-1 rounded-full text-xs ${OfflineMapService.getPriorityColor(cache.priority)}`}>
                      {cache.priority}
                    </span>
                  </div>
                </div>
                
                <div className="flex items-center space-x-4">
                  <div className="text-right">
                    <div className="text-sm text-gray-600">
                      {cache.downloadedTiles} / {cache.totalTiles} tiles
                    </div>
                    <div className="text-sm text-gray-600">
                      {OfflineMapService.formatFileSize(cache.cacheSizeBytes)}
                    </div>
                  </div>
                  
                  {getActionButton(cache)}
                  
                  <button
                    onClick={() => handleDeleteCache(cache.id)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded-md"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
              
              <div className="mt-2">
                <div className="text-sm text-gray-600 mb-2">
                  {cache.regionName} • {OfflineMapService.getMapTypeIcon(cache.mapType)} {cache.mapType}
                </div>
                
                {cache.status === 'DOWNLOADING' && (
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${cache.downloadProgress * 100}%` }}
                    ></div>
                  </div>
                )}
                
                <div className="flex items-center justify-between mt-2 text-xs text-gray-500">
                  <span>Created: {new Date(cache.createdAt).toLocaleDateString()}</span>
                  <span>Progress: {OfflineMapService.formatDownloadProgress(cache.downloadProgress)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        {filteredCaches.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <MapPin className="w-12 h-12 mx-auto mb-4 text-gray-400" />
            <p>No offline map caches found</p>
            <p className="text-sm">Create your first cache to get started</p>
          </div>
        )}
      </div>

      {/* Create Cache Form Modal */}
      {showCreateForm && (
        <CreateCacheForm
          onClose={() => setShowCreateForm(false)}
          onSubmit={handleCreateCache}
        />
      )}
    </div>
  );
};

// Create Cache Form Component
interface CreateCacheFormProps {
  onClose: () => void;
  onSubmit: (request: OfflineMapCacheRequest) => void;
}

const CreateCacheForm: React.FC<CreateCacheFormProps> = ({ onClose, onSubmit }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    regionId: '',
    regionName: '',
    mapType: 'SATELLITE',
    tileSource: '',
    tileFormat: 'png',
    priority: 'MEDIUM',
    isCompressed: false,
    autoStart: true,
    zoomLevels: [10, 11, 12, 13, 14, 15],
    boundsCoordinates: [
      { longitude: -122.4194, latitude: 37.7749 },
      { longitude: -122.4094, latitude: 37.7749 },
      { longitude: -122.4094, latitude: 37.7849 },
      { longitude: -122.4194, latitude: 37.7849 },
      { longitude: -122.4194, latitude: 37.7749 }
    ]
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      ...formData,
      createdBy: 'user' // This should come from auth context
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <h3 className="text-lg font-semibold mb-4">Create Offline Map Cache</h3>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Region ID</label>
              <input
                type="text"
                value={formData.regionId}
                onChange={(e) => setFormData({ ...formData, regionId: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows={3}
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Map Type</label>
              <select
                value={formData.mapType}
                onChange={(e) => setFormData({ ...formData, mapType: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="SATELLITE">Satellite</option>
                <option value="STREET_MAP">Street Map</option>
                <option value="TERRAIN">Terrain</option>
                <option value="HYBRID">Hybrid</option>
                <option value="TOPOGRAPHIC">Topographic</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
              <select
                value={formData.priority}
                onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
                <option value="BACKGROUND">Background</option>
              </select>
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tile Source URL</label>
            <input
              type="url"
              value={formData.tileSource}
              onChange={(e) => setFormData({ ...formData, tileSource: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
              required
            />
          </div>
          
          <div className="flex items-center space-x-4">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.isCompressed}
                onChange={(e) => setFormData({ ...formData, isCompressed: e.target.checked })}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="ml-2 text-sm text-gray-700">Compress tiles</span>
            </label>
            
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.autoStart}
                onChange={(e) => setFormData({ ...formData, autoStart: e.target.checked })}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="ml-2 text-sm text-gray-700">Start download immediately</span>
            </label>
          </div>
          
          <div className="flex justify-end space-x-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Create Cache
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};



