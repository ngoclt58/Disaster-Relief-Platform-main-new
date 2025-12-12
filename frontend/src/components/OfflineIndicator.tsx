import React, { useState, useEffect } from 'react';
import { Wifi, WifiOff, RefreshCw, AlertTriangle, Clock } from 'lucide-react';
import { offlineManager } from '../services/offlineManager';

interface OfflineIndicatorProps {
  className?: string;
}

const OfflineIndicator: React.FC<OfflineIndicatorProps> = ({ className = '' }) => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [isSyncInProgress, setIsSyncInProgress] = useState(false);
  const [queuedActions, setQueuedActions] = useState(0);
  const [lastSyncTime, setLastSyncTime] = useState<Date | null>(null);
  const [syncError, setSyncError] = useState<string | null>(null);

  useEffect(() => {
    const updateOnlineStatus = () => {
      setIsOnline(navigator.onLine);
    };

    const updateSyncStatus = () => {
      setIsSyncInProgress(offlineManager.isSyncInProgress);
    };

    const handleSyncComplete = () => {
      setIsSyncInProgress(false);
      setLastSyncTime(new Date());
      setSyncError(null);
      updateQueuedActions();
    };

    const handleSyncFailed = (event: CustomEvent) => {
      setIsSyncInProgress(false);
      setSyncError(event.detail?.message || 'Sync failed');
    };

    // Initial load
    updateOnlineStatus();
    updateSyncStatus();
    updateQueuedActions();

    // Event listeners
    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    window.addEventListener('offlineSyncComplete', handleSyncComplete);
    window.addEventListener('offlineSyncFailed', handleSyncFailed as EventListener);

    // Periodic updates
    const interval = setInterval(() => {
      updateSyncStatus();
      updateQueuedActions();
    }, 5000);

    return () => {
      window.removeEventListener('online', updateOnlineStatus);
      window.removeEventListener('offline', updateOnlineStatus);
      window.removeEventListener('offlineSyncComplete', handleSyncComplete);
      window.removeEventListener('offlineSyncFailed', handleSyncFailed as EventListener);
      clearInterval(interval);
    };
  }, []);

  const updateQueuedActions = async () => {
    try {
      const actions = await offlineManager.getQueuedActions();
      setQueuedActions(actions.length);
    } catch (error) {
      console.error('Failed to get queued actions:', error);
    }
  };

  const handleRetrySync = async () => {
    try {
      setSyncError(null);
      await offlineManager.triggerSync();
    } catch (error) {
      setSyncError('Failed to start sync');
    }
  };

  const getStatusColor = () => {
    if (syncError) return 'text-red-600';
    if (isSyncInProgress) return 'text-blue-600';
    if (!isOnline) return 'text-orange-600';
    return 'text-green-600';
  };

  const getStatusIcon = () => {
    if (syncError) return <AlertTriangle className="w-4 h-4" />;
    if (isSyncInProgress) return <RefreshCw className="w-4 h-4 animate-spin" />;
    if (!isOnline) return <WifiOff className="w-4 h-4" />;
    return <Wifi className="w-4 h-4" />;
  };

  const getStatusText = () => {
    if (syncError) return 'Sync Error';
    if (isSyncInProgress) return 'Syncing...';
    if (!isOnline) return 'Offline';
    return 'Online';
  };

  const getStatusDescription = () => {
    if (syncError) return syncError;
    if (isSyncInProgress) return 'Synchronizing your changes...';
    if (!isOnline) return `${queuedActions} actions queued for sync`;
    if (lastSyncTime) return `Last synced ${lastSyncTime.toLocaleTimeString()}`;
    return 'All changes synced';
  };

  return (
    <div className={`flex items-center space-x-2 ${className}`}>
      <div className={`flex items-center space-x-1 ${getStatusColor()}`}>
        {getStatusIcon()}
        <span className="text-sm font-medium">{getStatusText()}</span>
      </div>
      
      <div className="text-xs text-gray-500">
        {getStatusDescription()}
      </div>

      {queuedActions > 0 && !isSyncInProgress && (
        <div className="flex items-center space-x-1">
          <Clock className="w-3 h-3 text-orange-500" />
          <span className="text-xs text-orange-600">{queuedActions} pending</span>
        </div>
      )}

      {syncError && (
        <button
          onClick={handleRetrySync}
          className="text-xs text-blue-600 hover:text-blue-800 underline"
        >
          Retry
        </button>
      )}

      {!isOnline && (
        <div className="flex items-center space-x-1 text-xs text-orange-600">
          <AlertTriangle className="w-3 h-3" />
          <span>Working offline</span>
        </div>
      )}
    </div>
  );
};

export default OfflineIndicator;



