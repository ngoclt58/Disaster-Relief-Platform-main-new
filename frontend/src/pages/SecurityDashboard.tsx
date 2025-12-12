import React, { useEffect, useState } from 'react';
import { ShieldIcon, AlertTriangle, LockIcon, KeyIcon, BarChart3Icon } from 'lucide-react';
import { securityService, ThreatAlert, Metric } from '../services/securityService';

const SecurityDashboard: React.FC = () => {
  const [alerts, setAlerts] = useState<ThreatAlert[]>([]);
  const [metrics, setMetrics] = useState<Metric[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    try {
      const [a, m] = await Promise.all([
        securityService.getThreatAlerts(),
        securityService.getRecentMetrics(20)
      ]);
      setAlerts(a);
      setMetrics(m);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <div className="min-h-full flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-full">
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900 flex items-center">
            <ShieldIcon className="h-7 w-7 mr-2 text-blue-600" /> Advanced Security
          </h1>
          <p className="mt-2 text-sm text-gray-600">Zero-Trust, Threat Detection, DLP, Encryption, and Analytics</p>
        </div>
      </header>

      <main>
        <div className="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white overflow-hidden shadow rounded-lg p-5">
              <div className="flex items-center">
                <AlertTriangle className="h-6 w-6 text-red-600" />
                <div className="ml-5">
                  <p className="text-sm font-medium text-gray-500">Active Alerts</p>
                  <p className="text-2xl font-semibold text-gray-900">{alerts.length}</p>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg p-5">
              <div className="flex items-center">
                <BarChart3Icon className="h-6 w-6 text-green-600" />
                <div className="ml-5">
                  <p className="text-sm font-medium text-gray-500">Recent Metrics</p>
                  <p className="text-2xl font-semibold text-gray-900">{metrics.length}</p>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg p-5">
              <div className="flex items-center">
                <LockIcon className="h-6 w-6 text-purple-600" />
                <div className="ml-5">
                  <p className="text-sm font-medium text-gray-500">Zero-Trust</p>
                  <p className="text-2xl font-semibold text-gray-900">Enabled</p>
                </div>
              </div>
            </div>

            <div className="bg-white overflow-hidden shadow rounded-lg p-5">
              <div className="flex items-center">
                <KeyIcon className="h-6 w-6 text-yellow-600" />
                <div className="ml-5">
                  <p className="text-sm font-medium text-gray-500">Encryption</p>
                  <p className="text-2xl font-semibold text-gray-900">AES-GCM</p>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4">Threat Alerts</h2>
              <div className="space-y-3">
                {alerts.map(a => (
                  <div key={a.id} className={`border-l-4 p-3 rounded ${a.severity === 'CRITICAL' ? 'border-red-600 bg-red-50' : a.severity === 'HIGH' ? 'border-orange-500 bg-orange-50' : 'border-yellow-500 bg-yellow-50'}`}>
                    <div className="flex justify-between">
                      <div>
                        <p className="font-medium">{a.title}</p>
                        <p className="text-sm text-gray-700">{a.message}</p>
                      </div>
                      {!a.acknowledged && (
                        <button className="text-blue-600 text-sm" onClick={() => securityService.acknowledgeAlert(a.id, 'me').then(load)}>Acknowledge</button>
                      )}
                    </div>
                    <p className="text-xs text-gray-500 mt-1">{new Date(a.createdAt).toLocaleString()}</p>
                  </div>
                ))}
                {alerts.length === 0 && <p className="text-sm text-gray-500">No active alerts.</p>}
              </div>
            </div>

            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4">Recent Metrics</h2>
              <ul className="divide-y divide-gray-200">
                {metrics.map(m => (
                  <li key={m.id} className="py-3 text-sm flex justify-between">
                    <span className="font-medium">{m.name}</span>
                    <span className="text-gray-600">{m.value.toFixed(2)}</span>
                  </li>
                ))}
                {metrics.length === 0 && <p className="text-sm text-gray-500">No metrics recorded.</p>}
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default SecurityDashboard;




