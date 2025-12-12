import React, { useState, useEffect } from 'react';
import { 
  Package, 
  Plus, 
  Edit, 
  Trash2, 
  Search, 
  Filter,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  CheckCircle,
  BarChart3,
  Download,
  Upload
} from 'lucide-react';
import { apiService } from '../services/api';

interface InventoryHub {
  id: string;
  name: string;
  address: string;
  capacity: number;
}

interface ItemCatalog {
  id: string;
  code: string;
  name: string;
  unit: string;
}

interface InventoryStock {
  id: string;
  hub: InventoryHub;
  item?: ItemCatalog;
  itemId?: string;
  qtyAvailable: number;
  qtyReserved: number;
  updatedAt: string;
}

interface StockMovement {
  id: string;
  type: 'in' | 'out' | 'reserve' | 'release';
  quantity: number;
  reason: string;
  timestamp: string;
  user: string;
}

const InventoryManager: React.FC = () => {
  const [hubs, setHubs] = useState<InventoryHub[]>([]);
  const [items, setItems] = useState<ItemCatalog[]>([]);
  const [stock, setStock] = useState<InventoryStock[]>([]);
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedHub, setSelectedHub] = useState<string>('');
  const [selectedItem, setSelectedItem] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState('');
  const [showAddStock, setShowAddStock] = useState(false);
  const [showMovementForm, setShowMovementForm] = useState(false);
  const [showAnalytics, setShowAnalytics] = useState(false);
  const [alerts, setAlerts] = useState<any | null>(null);
  const [editing, setEditing] = useState<{ hubId: string; itemId: string; qtyAvailable: number; qtyReserved: number } | null>(null);
  const [newStock, setNewStock] = useState<{ hubId: string; itemId: string; qtyAvailable: number; reason: string }>({
    hubId: '',
    itemId: '',
    qtyAvailable: 0,
    reason: ''
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      console.log('🔄 Starting to fetch inventory data...');
      
      const [hubsData, itemsData, stockData, invStatus] = await Promise.all([
        apiService.getInventoryHubs().catch(err => {
          console.error('❌ Error fetching hubs:', err);
          return [];
        }),
        apiService.getInventoryItems().catch(err => {
          console.error('❌ Error fetching items:', err);
          return [];
        }),
        apiService.getInventoryStock().catch(err => {
          console.error('❌ Error fetching stock:', err);
          console.error('Error details:', err.message, err);
          return [];
        }),
        apiService.getInventoryStatus().catch(err => {
          console.error('❌ Error fetching status:', err);
          return null;
        })
      ]);

      console.log('✅ Fetch completed:', {
        hubs: Array.isArray(hubsData) ? hubsData.length : typeof hubsData,
        items: Array.isArray(itemsData) ? itemsData.length : typeof itemsData,
        stock: Array.isArray(stockData) ? stockData.length : typeof stockData
      });

      setHubs(hubsData as InventoryHub[]);
      setItems(itemsData as ItemCatalog[]);
      
      // Debug: Log stock data to see what we're getting
      console.log('=== STOCK DATA DEBUG ===');
      console.log('Stock data received:', stockData);
      console.log('Stock data type:', typeof stockData);
      console.log('Stock data length:', Array.isArray(stockData) ? stockData.length : 'Not an array');
      if (Array.isArray(stockData) && stockData.length > 0) {
        console.log('First stock item (full):', JSON.stringify(stockData[0], null, 2));
        console.log('First stock item hub:', stockData[0]?.hub);
        console.log('First stock item hub type:', typeof stockData[0]?.hub);
        console.log('First stock item item:', stockData[0]?.item);
        console.log('First stock item item type:', typeof stockData[0]?.item);
        console.log('All stock items:', stockData.map(s => ({
          id: s.id,
          hasHub: !!s.hub,
          hasItem: !!s.item,
          hubId: s.hub?.id,
          hubName: s.hub?.name,
          itemId: s.item?.id,
          itemName: s.item?.name,
          qtyAvailable: s.qtyAvailable
        })));
      } else {
        console.warn('Stock data is empty or not an array!');
      }
      console.log('=== END STOCK DATA DEBUG ===');
      
      setStock(stockData as InventoryStock[]);
      setAlerts(invStatus as any);
      
      // Fetch real movements data
      try {
        const movementsData = await apiService.getStockMovements() as any[];
        const formattedMovements = Array.isArray(movementsData) 
          ? movementsData.map((m: any) => ({
              id: m.id,
              type: m.movementType === 'in' ? 'in' : m.movementType === 'out' ? 'out' : m.movementType,
              quantity: m.quantity,
              reason: m.reason || 'Stock movement',
              timestamp: m.createdAt,
              user: m.user ? m.user.fullName || m.user.email : 'System'
            }))
          : [];
        setMovements(formattedMovements);
      } catch (error) {
        console.error('Failed to fetch movements:', error);
        setMovements([]);
      }
    } catch (error) {
      console.error('Failed to fetch inventory data:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredStock = stock.filter(s => {
    // Allow items without linked ItemCatalog; show them as 'N/A' in UI instead of hiding
    if (!s.hub) {
      console.warn('=== STOCK FILTERED OUT - Missing Hub ===');
      console.warn('Stock item:', s);
      return false;
    }
    
    const matchesHub = !selectedHub || (s.hub && s.hub.id === selectedHub);
    const matchesItem = !selectedItem || (s.item && s.item.id === selectedItem);
    const matchesSearch = !searchTerm || 
      s.item?.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.item?.code?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.hub.name?.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesHub && matchesItem && matchesSearch;
  });
  
  // Debug: Log filter results
  console.log('=== FILTER RESULTS ===');
  console.log('Total stock:', stock.length);
  console.log('Filtered stock:', filteredStock.length);
  console.log('Selected hub:', selectedHub);
  console.log('Selected item:', selectedItem);
  console.log('Search term:', searchTerm);
  console.log('=== END FILTER RESULTS ===');

  const getStockAnalytics = () => {
    const totalStock = stock.reduce((sum, s) => sum + s.qtyAvailable, 0);
    const totalReserved = stock.reduce((sum, s) => sum + s.qtyReserved, 0);
    const lowStockItems = stock.filter(s => s.qtyAvailable < 10).length;
    const outOfStockItems = stock.filter(s => s.qtyAvailable === 0).length;
    
    return {
      totalStock,
      totalReserved,
      lowStockItems,
      outOfStockItems,
      utilizationRate: totalStock > 0 ? (totalReserved / totalStock) * 100 : 0
    };
  };

  const handleStockUpdate = async (hubId: string, itemId: string, qtyAvailable: number, qtyReserved: number, reason?: string) => {
    try {
      await apiService.updateStock(hubId, itemId, qtyAvailable, qtyReserved);
      await fetchData(); // Refresh data
    } catch (error) {
      console.error('Failed to update stock:', error);
    }
  };
  
  const handleAddStock = async () => {
    if (!newStock.hubId || !newStock.itemId || newStock.qtyAvailable <= 0) {
      alert('Please fill in all required fields');
      return;
    }
    try {
      // Get current stock to calculate new quantity
      // Add null safety checks for hub and item
      const currentStock = stock.find(s => 
        s.hub && s.item && 
        s.hub.id === newStock.hubId && 
        s.item.id === newStock.itemId
      );
      const currentQty = currentStock ? currentStock.qtyAvailable : 0;
      const newQty = currentQty + newStock.qtyAvailable;
      
      await apiService.updateStock(newStock.hubId, newStock.itemId, newQty, currentStock?.qtyReserved || 0, newStock.reason);
      await fetchData(); // Refresh data
      setShowAddStock(false);
      setNewStock({ hubId: '', itemId: '', qtyAvailable: 0, reason: '' });
    } catch (error: any) {
      console.error('Failed to add stock:', error);
      const errorMessage = error?.message || error?.error || 'Failed to add stock. Please check that the hub and item exist.';
      alert(errorMessage);
    }
  };

  const startEdit = (s: InventoryStock) => {
    const hubId = s.hub?.id;
    const itemId = s.item?.id || s.itemId;
    if (!hubId || !itemId) {
      console.error('Cannot edit stock: hub or item id is missing', s);
      return;
    }
    setEditing({ hubId, itemId, qtyAvailable: s.qtyAvailable, qtyReserved: s.qtyReserved });
  };

  const saveEdit = async () => {
    if (!editing) return;
    await handleStockUpdate(editing.hubId, editing.itemId, editing.qtyAvailable, editing.qtyReserved);
    setEditing(null);
  };

  const handleReserveStock = async (hubId: string, itemId: string, quantity: number) => {
    try {
      await apiService.reserveStock(hubId, itemId, quantity);
      await fetchData(); // Refresh data
    } catch (error) {
      console.error('Failed to reserve stock:', error);
    }
  };

  const handleReleaseStock = async (hubId: string, itemId: string, quantity: number) => {
    try {
      await apiService.releaseStock(hubId, itemId, quantity);
      await fetchData(); // Refresh data
    } catch (error) {
      console.error('Failed to release stock:', error);
    }
  };

  const exportStockReport = () => {
    const csvContent = [
      ['Hub', 'Item', 'Code', 'Available', 'Reserved', 'Total', 'Last Updated'],
      ...filteredStock.map(s => [
        s.hub?.name || 'N/A',
        s.item?.name || 'N/A',
        s.item?.code || 'N/A',
        s.qtyAvailable.toString(),
        s.qtyReserved.toString(),
        (s.qtyAvailable + s.qtyReserved).toString(),
        new Date(s.updatedAt).toLocaleDateString()
      ])
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `stock-report-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const analytics = getStockAnalytics();

  // Debug panel - always visible to help diagnose
  const debugInfo = {
    stockCount: stock.length,
    filteredCount: filteredStock.length,
    hubsCount: hubs.length,
    itemsCount: items.length,
    selectedHub,
    selectedItem,
    searchTerm,
    hasStockWithHub: stock.filter(s => s.hub).length,
    hasStockWithItem: stock.filter(s => s.item).length,
    hasStockWithBoth: stock.filter(s => s.hub && s.item).length
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Debug Panel - Remove this after fixing */}
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <h3 className="text-sm font-semibold text-yellow-900 mb-2">🔍 Debug Information</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs">
          <div><strong>Stock Count:</strong> {debugInfo.stockCount}</div>
          <div><strong>Filtered:</strong> {debugInfo.filteredCount}</div>
          <div><strong>Hubs:</strong> {debugInfo.hubsCount}</div>
          <div><strong>Items:</strong> {debugInfo.itemsCount}</div>
          <div><strong>Selected Hub:</strong> {debugInfo.selectedHub || 'None'}</div>
          <div><strong>Selected Item:</strong> {debugInfo.selectedItem || 'None'}</div>
          <div><strong>Stock w/ Hub:</strong> {debugInfo.hasStockWithHub}</div>
          <div><strong>Stock w/ Item:</strong> {debugInfo.hasStockWithItem}</div>
          <div><strong>Stock w/ Both:</strong> {debugInfo.hasStockWithBoth}</div>
        </div>
        <button 
          onClick={() => {
            console.log('=== MANUAL DEBUG TRIGGER ===');
            console.log('Stock array:', stock);
            console.log('Filtered stock:', filteredStock);
            console.log('Hubs:', hubs);
            console.log('Items:', items);
            console.log('Debug info:', debugInfo);
            console.log('=== END MANUAL DEBUG ===');
          }}
          className="mt-2 px-3 py-1 bg-yellow-600 text-white text-xs rounded hover:bg-yellow-700"
        >
          Log to Console
        </button>
      </div>

      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">Inventory Management</h2>
            <p className="text-gray-600 mt-1">Advanced stock management and analytics</p>
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setShowAnalytics(!showAnalytics)}
              className="flex items-center px-3 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              <BarChart3 className="w-4 h-4 mr-2" />
              Analytics
            </button>
            <button
              onClick={exportStockReport}
              className="flex items-center px-3 py-2 text-sm bg-green-600 text-white rounded-md hover:bg-green-700"
            >
              <Download className="w-4 h-4 mr-2" />
              Export
            </button>
            <button
              onClick={() => setShowAddStock(true)}
              className="flex items-center px-3 py-2 text-sm bg-purple-600 text-white rounded-md hover:bg-purple-700"
            >
              <Plus className="w-4 h-4 mr-2" />
              Add Stock
            </button>
          </div>
        </div>
      </div>

      {/* Analytics Panel */}
      {showAnalytics && (
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Stock Analytics</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <div className="flex items-center">
                <Package className="w-8 h-8 text-blue-600" />
                <div className="ml-3">
                  <p className="text-sm font-medium text-blue-900">Total Stock</p>
                  <p className="text-2xl font-bold text-blue-600">{analytics.totalStock}</p>
                </div>
              </div>
            </div>
            <div className="bg-orange-50 p-4 rounded-lg">
              <div className="flex items-center">
                <TrendingUp className="w-8 h-8 text-orange-600" />
                <div className="ml-3">
                  <p className="text-sm font-medium text-orange-900">Reserved</p>
                  <p className="text-2xl font-bold text-orange-600">{analytics.totalReserved}</p>
                </div>
              </div>
            </div>
            <div className="bg-yellow-50 p-4 rounded-lg">
              <div className="flex items-center">
                <AlertTriangle className="w-8 h-8 text-yellow-600" />
                <div className="ml-3">
                  <p className="text-sm font-medium text-yellow-900">Low Stock</p>
                  <p className="text-2xl font-bold text-yellow-600">{analytics.lowStockItems}</p>
                </div>
              </div>
            </div>
            <div className="bg-red-50 p-4 rounded-lg">
              <div className="flex items-center">
                <AlertTriangle className="w-8 h-8 text-red-600" />
                <div className="ml-3">
                  <p className="text-sm font-medium text-red-900">Out of Stock</p>
                  <p className="text-2xl font-bold text-red-600">{analytics.outOfStockItems}</p>
                </div>
              </div>
            </div>
          </div>
          {alerts && (
            <div className="mt-6">
              <h4 className="text-md font-semibold mb-2">Backend Alerts</h4>
              <div className="text-sm text-gray-700">Low stock items: {alerts.totalLowStockCount}</div>
            </div>
          )}
        </div>
      )}

      {/* Filters */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Hub</label>
            <select
              value={selectedHub}
              onChange={(e) => {
                const newValue = e.target.value;
                console.log('Hub filter changed:', newValue);
                setSelectedHub(newValue);
              }}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">All Hubs</option>
              {hubs.map(hub => (
                <option key={hub.id} value={hub.id}>{hub.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Item</label>
            <select
              value={selectedItem}
              onChange={(e) => setSelectedItem(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">All Items</option>
              {items.map(item => (
                <option key={item.id} value={item.id}>{item.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Search</label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search items..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          <div className="flex items-end">
            <button
              onClick={() => {
                setSelectedHub('');
                setSelectedItem('');
                setSearchTerm('');
              }}
              className="w-full px-3 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* Stock Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">Stock Levels</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Hub
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Item
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Available
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Reserved
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredStock.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                    {stock.length === 0 
                      ? 'No stock data available. Add stock using the "+ Add Stock" button.'
                      : 'No stock matches the current filters. Try clearing filters or adjusting your search.'}
                    {stock.length > 0 && (
                      <div className="mt-2 text-xs text-gray-400">
                        Total stock entries: {stock.length} | Filtered: {filteredStock.length}
                      </div>
                    )}
                  </td>
                </tr>
              ) : (
                filteredStock.map(item => (
                  <tr key={item.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{item.hub?.name || 'N/A'}</div>
                      <div className="text-sm text-gray-500">{item.hub?.address || ''}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{item.item?.name || 'N/A'}</div>
                      <div className="text-sm text-gray-500">{item.item?.code || ''} • {item.item?.unit || ''}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {item.qtyAvailable}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {item.qtyReserved}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        item.qtyAvailable === 0
                          ? 'bg-red-100 text-red-800'
                          : item.qtyAvailable < 10
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-green-100 text-green-800'
                      }`}>
                        {item.qtyAvailable === 0 ? 'Out of Stock' : 
                         item.qtyAvailable < 10 ? 'Low Stock' : 'In Stock'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                      <button
                      onClick={() => {
                        const hubId = item.hub?.id;
                        const itemId = item.item?.id || item.itemId;
                        if (hubId && itemId) {
                          handleReserveStock(hubId, itemId, 1);
                        }
                      }}
                        className="text-blue-600 hover:text-blue-900"
                      disabled={!item.hub?.id || !(item.item?.id || item.itemId)}
                      >
                        Reserve
                      </button>
                      <button
                      onClick={() => {
                        const hubId = item.hub?.id;
                        const itemId = item.item?.id || item.itemId;
                        if (hubId && itemId) {
                          handleReleaseStock(hubId, itemId, 1);
                        }
                      }}
                        className="text-green-600 hover:text-green-900"
                      disabled={!item.hub?.id || !(item.item?.id || item.itemId)}
                      >
                        Release
                      </button>
                      <button 
                        className="text-gray-600 hover:text-gray-900" 
                        onClick={() => startEdit(item)}
                      disabled={!item.hub?.id || !(item.item?.id || item.itemId)}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Recent Movements */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">Recent Stock Movements</h3>
        </div>
        <div className="p-6">
          <div className="space-y-3">
            {movements.map(movement => (
              <div key={movement.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  {movement.type === 'in' ? (
                    <TrendingUp className="w-5 h-5 text-green-600" />
                  ) : (
                    <TrendingDown className="w-5 h-5 text-red-600" />
                  )}
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {movement.type === 'in' ? 'Stock In' : 'Stock Out'} - {movement.quantity} units
                    </p>
                    <p className="text-sm text-gray-500">{movement.reason}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500">{movement.user}</p>
                  <p className="text-xs text-gray-400">
                    {new Date(movement.timestamp).toLocaleString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Add Stock Modal */}
      {showAddStock && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded shadow-lg w-full max-w-md p-6">
            <h3 className="text-lg font-semibold mb-4">Add Stock</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Hub</label>
                <select
                  value={newStock.hubId}
                  onChange={(e) => setNewStock({ ...newStock, hubId: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                >
                  <option value="">Select Hub</option>
                  {hubs.map(hub => (
                    <option key={hub.id} value={hub.id}>{hub.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Item</label>
                <select
                  value={newStock.itemId}
                  onChange={(e) => setNewStock({ ...newStock, itemId: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                >
                  <option value="">Select Item</option>
                  {items.map(item => (
                    <option key={item.id} value={item.id}>{item.name} ({item.code})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Quantity to Add</label>
                <input
                  type="number"
                  min="1"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  value={newStock.qtyAvailable}
                  onChange={(e) => setNewStock({ ...newStock, qtyAvailable: Number(e.target.value) })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Reason (optional)</label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  placeholder="e.g., Restock from supplier"
                  value={newStock.reason}
                  onChange={(e) => setNewStock({ ...newStock, reason: e.target.value })}
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50" onClick={() => {
                setShowAddStock(false);
                setNewStock({ hubId: '', itemId: '', qtyAvailable: 0, reason: '' });
              }}>Cancel</button>
              <button className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700" onClick={handleAddStock}>Add Stock</button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Stock Modal */}
      {editing && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded shadow-lg w-full max-w-md p-6">
            <h3 className="text-lg font-semibold mb-4">Edit Stock</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium">Available Qty</label>
                <input type="number" className="mt-1 w-full border rounded p-2" value={editing.qtyAvailable}
                  onChange={(e) => setEditing({ ...editing, qtyAvailable: Number(e.target.value) })} />
              </div>
              <div>
                <label className="block text-sm font-medium">Reserved Qty</label>
                <input type="number" className="mt-1 w-full border rounded p-2" value={editing.qtyReserved}
                  onChange={(e) => setEditing({ ...editing, qtyReserved: Number(e.target.value) })} />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button className="px-4 py-2" onClick={() => setEditing(null)}>Cancel</button>
              <button className="px-4 py-2 bg-blue-600 text-white rounded" onClick={saveEdit}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default InventoryManager;
