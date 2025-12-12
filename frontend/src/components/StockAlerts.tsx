import React, { useState, useEffect } from 'react';
import { 
  AlertTriangle, 
  Package, 
  TrendingDown, 
  RefreshCw,
  Bell,
  BellOff
} from 'lucide-react';
import { apiService } from '../services/api';

interface StockAlert {
  id: string;
  item: {
    id: string;
    name: string;
    code: string;
    unit: string;
  };
  hub: {
    id: string;
    name: string;
    address: string;
  };
  qtyAvailable: number;
  qtyReserved: number;
  threshold: number;
  severity: 'low' | 'critical' | 'out';
}

const StockAlerts: React.FC = () => {
  const [alerts, setAlerts] = useState<StockAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [dismissedAlerts, setDismissedAlerts] = useState<Set<string>>(new Set());

  useEffect(() => {
    fetchStockAlerts();
    
    // Check for notifications permission
    if ('Notification' in window) {
      Notification.requestPermission();
    }
  }, []);

  const fetchStockAlerts = async () => {
    try {
      setLoading(true);
      // Use apiService to ensure authentication headers are included
      const stockData = await apiService.getInventoryStock();
      
      // Check if response is valid array
      if (stockData && Array.isArray(stockData)) {
        const alerts = generateAlerts(stockData);
        setAlerts(alerts);
        
        // Show browser notification for critical alerts
        if (notificationsEnabled && 'Notification' in window && Notification.permission === 'granted') {
          const criticalAlerts = alerts.filter(a => a.severity === 'critical' || a.severity === 'out');
          if (criticalAlerts.length > 0) {
            new Notification('Critical Stock Alert', {
              body: `${criticalAlerts.length} items are critically low or out of stock`,
              icon: '/favicon.ico'
            });
          }
        }
      } else {
        // If not array, might be error response - set empty alerts
        console.warn('Invalid stock data format:', stockData);
        setAlerts([]);
      }
    } catch (error: any) {
      // Handle connection errors
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED') || error.message?.includes('Cannot connect')) {
        console.warn('Backend server not available, stock alerts will not be loaded');
        setAlerts([]);
        return;
      }
      // Handle JSON parsing errors specifically
      if (error instanceof SyntaxError || error.message?.includes('JSON') || error.message?.includes('HTML')) {
        console.warn('Server returned HTML instead of JSON - backend may not be running:', error.message);
        setAlerts([]);
      } else {
        console.error('Failed to fetch stock alerts:', error);
        setAlerts([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const generateAlerts = (stockData: any[]): StockAlert[] => {
    const alerts: StockAlert[] = [];
    
    stockData.forEach(stock => {
      const { qtyAvailable, qtyReserved } = stock;
      const totalStock = qtyAvailable + qtyReserved;
      
      let severity: 'low' | 'critical' | 'out' = 'low';
      let threshold = 25;
      
      if (qtyAvailable === 0) {
        severity = 'out';
        threshold = 0;
      } else if (qtyAvailable < 5) {
        severity = 'critical';
        threshold = 5;
      } else if (qtyAvailable < 25) {
        severity = 'low';
        threshold = 25;
      }
      
      if (severity !== 'low' || qtyAvailable < 25) {
        alerts.push({
          id: `${stock.hub.id}-${stock.item.id}`,
          item: stock.item,
          hub: stock.hub,
          qtyAvailable,
          qtyReserved,
          threshold,
          severity
        });
      }
    });
    
    return alerts.sort((a, b) => {
      const severityOrder = { 'out': 0, 'critical': 1, 'low': 2 };
      return severityOrder[a.severity] - severityOrder[b.severity];
    });
  };

  const dismissAlert = (alertId: string) => {
    setDismissedAlerts(prev => new Set(Array.from(prev).concat(alertId)));
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'out': return 'bg-red-100 text-red-800 border-red-200';
      case 'critical': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'low': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'out': return <AlertTriangle className="w-5 h-5 text-red-600" />;
      case 'critical': return <TrendingDown className="w-5 h-5 text-orange-600" />;
      case 'low': return <Package className="w-5 h-5 text-yellow-600" />;
      default: return <Package className="w-5 h-5 text-gray-600" />;
    }
  };

  const activeAlerts = alerts.filter(alert => !dismissedAlerts.has(alert.id));

  if (loading) {
    return (
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-center">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (activeAlerts.length === 0) {
    return (
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-center text-center">
          <div>
            <Package className="mx-auto h-12 w-12 text-green-500" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">All Stock Levels Good</h3>
            <p className="mt-1 text-sm text-gray-500">No low stock alerts at this time.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <AlertTriangle className="w-6 h-6 text-red-600" />
            <div>
              <h3 className="text-lg font-semibold text-gray-900">Stock Alerts</h3>
              <p className="text-sm text-gray-500">
                {activeAlerts.length} item{activeAlerts.length !== 1 ? 's' : ''} need{activeAlerts.length !== 1 ? '' : 's'} attention
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setNotificationsEnabled(!notificationsEnabled)}
              className={`p-2 rounded-md ${
                notificationsEnabled 
                  ? 'bg-blue-100 text-blue-600' 
                  : 'bg-gray-100 text-gray-600'
              }`}
              title={notificationsEnabled ? 'Disable notifications' : 'Enable notifications'}
            >
              {notificationsEnabled ? <Bell className="w-5 h-5" /> : <BellOff className="w-5 h-5" />}
            </button>
            <button
              onClick={fetchStockAlerts}
              className="p-2 text-gray-600 hover:text-gray-800"
              title="Refresh alerts"
            >
              <RefreshCw className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      {/* Alerts List */}
      <div className="space-y-3">
        {activeAlerts.map(alert => (
          <div
            key={alert.id}
            className={`bg-white border-l-4 rounded-lg p-4 shadow-sm ${getSeverityColor(alert.severity)}`}
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start space-x-3">
                {getSeverityIcon(alert.severity)}
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <h4 className="text-sm font-medium">{alert.item.name}</h4>
                    <span className="text-xs text-gray-500">({alert.item.code})</span>
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                      alert.severity === 'out' ? 'bg-red-200 text-red-800' :
                      alert.severity === 'critical' ? 'bg-orange-200 text-orange-800' :
                      'bg-yellow-200 text-yellow-800'
                    }`}>
                      {alert.severity.toUpperCase()}
                    </span>
                  </div>
                  <p className="text-sm text-gray-600 mt-1">
                    <strong>Hub:</strong> {alert.hub.name}
                  </p>
                  <p className="text-sm text-gray-600">
                    <strong>Available:</strong> {alert.qtyAvailable} {alert.item.unit} | 
                    <strong> Reserved:</strong> {alert.qtyReserved} {alert.item.unit}
                  </p>
                  {alert.severity === 'out' && (
                    <p className="text-sm text-red-700 font-medium mt-1">
                      ⚠️ OUT OF STOCK - Immediate restocking required
                    </p>
                  )}
                  {alert.severity === 'critical' && (
                    <p className="text-sm text-orange-700 font-medium mt-1">
                      ⚠️ Critical stock level - Restock within 24 hours
                    </p>
                  )}
                </div>
              </div>
              <button
                onClick={() => dismissAlert(alert.id)}
                className="text-gray-400 hover:text-gray-600 ml-4"
                title="Dismiss alert"
              >
                ×
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Summary */}
      <div className="bg-gray-50 rounded-lg p-4">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-2xl font-bold text-red-600">
              {activeAlerts.filter(a => a.severity === 'out').length}
            </div>
            <div className="text-sm text-gray-600">Out of Stock</div>
          </div>
          <div>
            <div className="text-2xl font-bold text-orange-600">
              {activeAlerts.filter(a => a.severity === 'critical').length}
            </div>
            <div className="text-sm text-gray-600">Critical</div>
          </div>
          <div>
            <div className="text-2xl font-bold text-yellow-600">
              {activeAlerts.filter(a => a.severity === 'low').length}
            </div>
            <div className="text-sm text-gray-600">Low Stock</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StockAlerts;



