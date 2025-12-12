import React, { useState, useEffect } from 'react';
import { RefreshCw, CheckCircle } from 'lucide-react';

const SyncIndicator: React.FC = () => {
  const [isSyncing, setIsSyncing] = useState(false);
  const [lastSync, setLastSync] = useState<Date | null>(null);

  useEffect(() => {
    // Simulate sync status - in real app, this would come from a sync service
    const interval = setInterval(() => {
      setIsSyncing(true);
      setTimeout(() => {
        setIsSyncing(false);
        setLastSync(new Date());
      }, 2000);
    }, 30000); // Sync every 30 seconds

    return () => clearInterval(interval);
  }, []);

  if (!isSyncing && !lastSync) return null;

  return (
    <div className="sync-indicator">
      {isSyncing ? (
        <RefreshCw className="w-5 h-5 animate-spin" />
      ) : (
        <CheckCircle className="w-5 h-5 text-green-500" />
      )}
    </div>
  );
};

export default SyncIndicator;



