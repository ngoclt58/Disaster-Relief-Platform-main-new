import React, { useEffect, useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { realtimeService } from './services/realtimeService';
import { registerServiceWorker, pwaInstaller, offlineDetector } from './utils/serviceWorker';
import Layout from './components/Layout';
import RouteGuard from './components/RouteGuard';
import OfflineIndicator from './components/OfflineIndicator';
import ConflictResolution from './components/ConflictResolution';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import MapPage from './pages/MapPage';
import TasksPage from './pages/TasksPage';
import InventoryPage from './pages/InventoryPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import { TerrainDashboard } from './pages/TerrainDashboard';
import { SatelliteDashboard } from './pages/SatelliteDashboard';
import { HeatmapDashboard } from './pages/HeatmapDashboard';
import { GeofencingDashboard } from './pages/GeofencingDashboard';
import { IndoorNavigationDashboard } from './pages/IndoorNavigationDashboard';
import { OfflineMapDashboard } from './pages/OfflineMapDashboard';
import { LocationAnalyticsDashboard } from './pages/LocationAnalyticsDashboard';
import CommunicationDashboard from './pages/CommunicationDashboard';
import FinancialDashboard from './pages/FinancialDashboard';
import TrainingDashboard from './pages/TrainingDashboard';
import IntegrationDashboard from './pages/IntegrationDashboard';
import AnalyticsDashboard from './pages/AnalyticsDashboard';
import RealtimeIntelligenceDashboard from './pages/RealtimeIntelligenceDashboard';
import SecurityDashboard from './pages/SecurityDashboard';
import AIDashboard from './pages/AIDashboard';
import OptimizationDashboard from './pages/OptimizationDashboard';
import OfflineBanner from './components/OfflineBanner';
import SyncIndicator from './components/SyncIndicator';

function App() {
  console.log('App component rendering...');
  const { isAuthenticated, user } = useAuthStore();
  console.log('Auth state:', { isAuthenticated, user: user?.email || 'no user' });
  const [showConflictResolution, setShowConflictResolution] = useState(false);
  const [showUpdatePrompt, setShowUpdatePrompt] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  // Initialize real-time service when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      realtimeService.connect();
    } else {
      realtimeService.disconnect();
    }

    return () => {
      realtimeService.disconnect();
    };
  }, [isAuthenticated]);

  // Initialize service worker and PWA features
  useEffect(() => {
    registerServiceWorker({
      onUpdate: (registration) => {
        setShowUpdatePrompt(true);
      },
      onSuccess: (registration) => {
        console.log('Service Worker registered successfully');
      },
      onOfflineReady: () => {
        console.log('App is ready for offline use');
      }
    });

    // Monitor online/offline status
    offlineDetector.addListener(setIsOnline);
  }, []);

  // Handle conflict resolution
  const handleConflictResolved = (conflictId: number) => {
    console.log('Conflict resolved:', conflictId);
  };

  const handleUpdateApp = () => {
    window.location.reload();
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50">
        <OfflineBanner />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <OfflineBanner />
      <SyncIndicator />
      
      {/* Offline Indicator */}
      <div className="fixed top-4 right-4 z-40">
        <OfflineIndicator />
      </div>
      
      <Layout>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={
            <RouteGuard requiredPermissions={['needs:read', 'task:read']}>
              <DashboardPage />
            </RouteGuard>
          } />
          <Route path="/map" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <MapPage />
            </RouteGuard>
          } />
          <Route path="/tasks" element={
            <RouteGuard requiredPermissions={['task:read']}>
              <TasksPage />
            </RouteGuard>
          } />
          <Route path="/inventory" element={
            <RouteGuard requiredPermissions={['inventory:read']}>
              <InventoryPage />
            </RouteGuard>
          } />
          <Route path="/profile" element={
            <RouteGuard requiredPermissions={['user:read']}>
              <ProfilePage />
            </RouteGuard>
          } />
          <Route path="/admin" element={
            <RouteGuard requiredRole="ADMIN" requiredPermissions={['system:monitor', 'user:read']}>
              <AdminPage />
            </RouteGuard>
          } />
          <Route path="/terrain" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <TerrainDashboard />
            </RouteGuard>
          } />
          <Route path="/satellite" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <SatelliteDashboard />
            </RouteGuard>
          } />
          <Route path="/heatmap" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <HeatmapDashboard />
            </RouteGuard>
          } />
          <Route path="/geofencing" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <GeofencingDashboard />
            </RouteGuard>
          } />
          <Route path="/indoor" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <IndoorNavigationDashboard />
            </RouteGuard>
          } />
          <Route path="/offline-maps" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <OfflineMapDashboard />
            </RouteGuard>
          } />
          <Route path="/location-analytics" element={
            <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
              <LocationAnalyticsDashboard />
            </RouteGuard>
          } />
        <Route path="/communication" element={
          <RouteGuard requiredPermissions={['needs:read', 'realtime:read']}>
            <CommunicationDashboard />
          </RouteGuard>
        } />
        <Route path="/financial" element={
          <RouteGuard requiredPermissions={['financial:read']}>
            <FinancialDashboard />
          </RouteGuard>
        } />
        <Route path="/training" element={
          <RouteGuard requiredPermissions={['training:read']}>
            <TrainingDashboard />
          </RouteGuard>
        } />
        <Route path="/integration" element={
          <RouteGuard requiredPermissions={['integration:read']}>
            <IntegrationDashboard />
          </RouteGuard>
        } />
        <Route path="/analytics" element={
          <RouteGuard requiredPermissions={['analytics:read']}>
            <AnalyticsDashboard />
          </RouteGuard>
        } />
        <Route path="/realtime" element={
          <RouteGuard requiredPermissions={['realtime:read']}>
            <RealtimeIntelligenceDashboard />
          </RouteGuard>
        } />
        <Route path="/security" element={
          <RouteGuard requiredPermissions={['security:read']}>
            <SecurityDashboard />
          </RouteGuard>
        } />
        <Route path="/ai" element={
          <RouteGuard requiredPermissions={['ai:read']}>
            <AIDashboard />
          </RouteGuard>
        } />
        <Route path="/optimization" element={
          <RouteGuard requiredPermissions={['optimization:read']}>
            <OptimizationDashboard />
          </RouteGuard>
        } />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Layout>

      {/* Conflict Resolution Modal */}
      {showConflictResolution && (
        <ConflictResolution
          onResolve={handleConflictResolved}
          onClose={() => setShowConflictResolution(false)}
        />
      )}

      {/* Update Prompt */}
      {showUpdatePrompt && (
        <div className="fixed bottom-4 left-4 right-4 bg-blue-600 text-white p-4 rounded-lg shadow-lg z-50">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-semibold">App Update Available</h3>
              <p className="text-sm opacity-90">A new version is available. Update now to get the latest features.</p>
            </div>
            <div className="flex space-x-2">
              <button
                onClick={() => setShowUpdatePrompt(false)}
                className="px-3 py-1 text-sm bg-blue-700 rounded hover:bg-blue-800"
              >
                Later
              </button>
              <button
                onClick={handleUpdateApp}
                className="px-3 py-1 text-sm bg-white text-blue-600 rounded hover:bg-gray-100"
              >
                Update
              </button>
            </div>
          </div>
        </div>
      )}

      {/* PWA Install Prompt */}
      {pwaInstaller.canInstall && (
        <div className="fixed bottom-4 right-4 bg-green-600 text-white p-4 rounded-lg shadow-lg z-50 max-w-sm">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-semibold">Install App</h3>
              <p className="text-sm opacity-90">Install this app for a better experience.</p>
            </div>
            <button
              onClick={() => pwaInstaller.install()}
              className="px-3 py-1 text-sm bg-white text-green-600 rounded hover:bg-gray-100"
            >
              Install
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
