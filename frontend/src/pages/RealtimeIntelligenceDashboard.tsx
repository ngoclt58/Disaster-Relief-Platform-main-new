import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  TrendingUp, 
  AlertTriangle, 
  Zap,
  Play,
  Pause,
  Settings,
  RefreshCw,
  Plus,
  Eye,
  Edit,
  Trash2,
  Filter,
  Calendar,
  ChevronDown,
  ChevronRight,
  BarChart3,
  LineChart,
  PieChart,
  AlertCircle,
  CheckCircle,
  Clock,
  Target
} from 'lucide-react';
import { streamProcessingService, StreamProcessor, StreamMetrics } from '../services/streamProcessingService';
import { eventCorrelationService, CorrelationResult, EventPattern } from '../services/eventCorrelationService';
import { trendAnalysisService, TrendResult, TrendAnalyzer } from '../services/trendAnalysisService';
import { anomalyDetectionService, Anomaly, AnomalyDetector } from '../services/anomalyDetectionService';

const RealtimeIntelligenceDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'streams' | 'correlations' | 'trends' | 'anomalies'>('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Data states
  const [processors, setProcessors] = useState<StreamProcessor[]>([]);
  const [correlations, setCorrelations] = useState<CorrelationResult[]>([]);
  const [trends, setTrends] = useState<TrendResult[]>([]);
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [patterns, setPatterns] = useState<EventPattern[]>([]);

  useEffect(() => {
    loadRealtimeData();
  }, []);

  const loadRealtimeData = async () => {
    try {
      setLoading(true);
      
      // Load data from all realtime services with error handling
      const [
        processorsData,
        correlationsData,
        trendsData,
        anomaliesData,
        patternsData
      ] = await Promise.all([
        streamProcessingService.getProcessors().catch(() => []),
        eventCorrelationService.findCorrelations().catch(() => []),
        trendAnalysisService.getAnalyzers().then(analyzers => 
          Promise.all(analyzers.map(analyzer => 
            trendAnalysisService.getTrends(analyzer.id, 
              new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
              new Date().toISOString()
            ).catch(() => [])
          ))
        ).then(results => results.flat()).catch(() => []),
        anomalyDetectionService.getDetectors().then(detectors =>
          Promise.all(detectors.map(detector =>
            anomalyDetectionService.getAnomalies(detector.id,
              new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
              new Date().toISOString()
            ).catch(() => [])
          ))
        ).then(results => results.flat()).catch(() => []),
        eventCorrelationService.detectPattern('relief_operations', 'emergency', 
          new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          new Date().toISOString()
        ).then(pattern => pattern ? [pattern] : []).catch(() => [])
      ]);
      
      // Ensure all data is arrays
      setProcessors(Array.isArray(processorsData) ? processorsData : []);
      setCorrelations(Array.isArray(correlationsData) ? correlationsData : []);
      setTrends(Array.isArray(trendsData) ? trendsData : []);
      setAnomalies(Array.isArray(anomaliesData) ? anomaliesData : []);
      setPatterns(Array.isArray(patternsData) ? patternsData : []);
      
    } catch (err) {
      setError('Failed to load realtime intelligence data');
      console.error('Error loading realtime data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    loadRealtimeData();
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'text-green-600 bg-green-100';
      case 'INACTIVE': return 'text-gray-600 bg-gray-100';
      case 'ERROR': return 'text-red-600 bg-red-100';
      case 'MAINTENANCE': return 'text-yellow-600 bg-yellow-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH': return 'text-red-600 bg-red-100';
      case 'MEDIUM': return 'text-yellow-600 bg-yellow-100';
      case 'LOW': return 'text-blue-600 bg-blue-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="min-h-full flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-full flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="mx-auto h-12 w-12 text-red-500" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">Error</h3>
          <p className="mt-1 text-sm text-gray-500">{error}</p>
          <button
            onClick={handleRefresh}
            className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-full">
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-gray-900">Real-time Intelligence</h1>
              <p className="mt-2 text-sm text-gray-600">
                Stream processing, event correlation, trend analysis, and anomaly detection
              </p>
            </div>
            <button
              onClick={handleRefresh}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </button>
          </div>
        </div>
      </header>

      <main>
        <div className="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
          {/* Tab Navigation */}
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'overview', name: 'Overview', icon: BarChart3 },
                { id: 'streams', name: 'Stream Processing', icon: Activity },
                { id: 'correlations', name: 'Event Correlation', icon: Zap },
                { id: 'trends', name: 'Trend Analysis', icon: TrendingUp },
                { id: 'anomalies', name: 'Anomaly Detection', icon: AlertTriangle }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
                >
                  <tab.icon className="h-4 w-4 mr-2" />
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          <div className="mt-6">
            {activeTab === 'overview' && (
              <div className="space-y-6">
                {/* Key Metrics */}
                <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="p-5">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <Activity className="h-6 w-6 text-blue-600" />
                        </div>
                        <div className="ml-5 w-0 flex-1">
                          <dl>
                            <dt className="text-sm font-medium text-gray-500 truncate">
                              Active Processors
                            </dt>
                            <dd className="text-lg font-medium text-gray-900">
                              {processors.filter(p => p.isRunning).length}
                            </dd>
                          </dl>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="p-5">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <Zap className="h-6 w-6 text-yellow-600" />
                        </div>
                        <div className="ml-5 w-0 flex-1">
                          <dl>
                            <dt className="text-sm font-medium text-gray-500 truncate">
                              Correlations
                            </dt>
                            <dd className="text-lg font-medium text-gray-900">
                              {correlations.length}
                            </dd>
                          </dl>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="p-5">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <TrendingUp className="h-6 w-6 text-green-600" />
                        </div>
                        <div className="ml-5 w-0 flex-1">
                          <dl>
                            <dt className="text-sm font-medium text-gray-500 truncate">
                              Active Trends
                            </dt>
                            <dd className="text-lg font-medium text-gray-900">
                              {trends.filter(t => t.isSignificant).length}
                            </dd>
                          </dl>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="p-5">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <AlertTriangle className="h-6 w-6 text-red-600" />
                        </div>
                        <div className="ml-5 w-0 flex-1">
                          <dl>
                            <dt className="text-sm font-medium text-gray-500 truncate">
                              Anomalies
                            </dt>
                            <dd className="text-lg font-medium text-gray-900">
                              {anomalies.filter(a => !a.isResolved).length}
                            </dd>
                          </dl>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Recent Activity */}
                <div className="bg-white shadow rounded-lg">
                  <div className="px-4 py-5 sm:p-6">
                    <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
                      Recent Activity
                    </h3>
                    <div className="space-y-4">
                      {correlations.slice(0, 5).map((correlation) => (
                        <div key={correlation.id} className="flex items-center space-x-3">
                          <div className="flex-shrink-0">
                            <Zap className="h-5 w-5 text-yellow-500" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm text-gray-900">
                              Event correlation detected with score {correlation.correlationScore.toFixed(2)}
                            </p>
                            <p className="text-xs text-gray-500">
                              {formatDate(correlation.correlatedAt)}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'streams' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Stream Processors
                  </h3>
                  <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700">
                    <Plus className="h-4 w-4 mr-2" />
                    New Processor
                  </button>
                </div>

                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {processors.map((processor) => (
                    <div key={processor.id} className="bg-white shadow rounded-lg">
                      <div className="p-6">
                        <div className="flex items-center justify-between">
                          <h4 className="text-lg font-medium text-gray-900">
                            {processor.name}
                          </h4>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(processor.status)}`}>
                            {processor.status}
                          </span>
                        </div>
                        <p className="mt-2 text-sm text-gray-600">
                          {processor.description}
                        </p>
                        <div className="mt-4 flex items-center justify-between">
                          <span className="text-sm text-gray-500">
                            Source: {processor.dataSource}
                          </span>
                          <div className="flex space-x-2">
                            {processor.isRunning ? (
                              <button className="text-red-600 hover:text-red-800">
                                <Pause className="h-4 w-4" />
                              </button>
                            ) : (
                              <button className="text-green-600 hover:text-green-800">
                                <Play className="h-4 w-4" />
                              </button>
                            )}
                            <button className="text-gray-600 hover:text-gray-800">
                              <Settings className="h-4 w-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'correlations' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Event Correlations
                  </h3>
                  <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700">
                    <Plus className="h-4 w-4 mr-2" />
                    New Rule
                  </button>
                </div>

                <div className="bg-white shadow overflow-hidden sm:rounded-md">
                  <ul className="divide-y divide-gray-200">
                    {correlations.map((correlation) => (
                      <li key={correlation.id} className="px-6 py-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center">
                            <div className="flex-shrink-0">
                              <Zap className="h-5 w-5 text-yellow-500" />
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                Correlation Score: {correlation.correlationScore.toFixed(2)}
                              </div>
                              <div className="text-sm text-gray-500">
                                {correlation.events.length} events correlated
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              correlation.isSignificant ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                            }`}>
                              {correlation.isSignificant ? 'Significant' : 'Normal'}
                            </span>
                            <span className="text-sm text-gray-500">
                              {formatDate(correlation.correlatedAt)}
                            </span>
                          </div>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}

            {activeTab === 'trends' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Trend Analysis
                  </h3>
                  <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700">
                    <Plus className="h-4 w-4 mr-2" />
                    New Analyzer
                  </button>
                </div>

                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {trends.map((trend) => (
                    <div key={trend.id} className="bg-white shadow rounded-lg">
                      <div className="p-6">
                        <div className="flex items-center justify-between">
                          <h4 className="text-lg font-medium text-gray-900">
                            {trend.trendDirection}
                          </h4>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            trend.isSignificant ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                          }`}>
                            {trend.isSignificant ? 'Significant' : 'Normal'}
                          </span>
                        </div>
                        <div className="mt-4">
                          <div className="flex justify-between text-sm text-gray-600">
                            <span>Strength: {(trend.trendStrength * 100).toFixed(1)}%</span>
                            <span>Confidence: {(trend.confidence * 100).toFixed(1)}%</span>
                          </div>
                          <div className="mt-2">
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className="bg-blue-600 h-2 rounded-full" 
                                style={{ width: `${trend.trendStrength * 100}%` }}
                              ></div>
                            </div>
                          </div>
                        </div>
                        <div className="mt-4 text-sm text-gray-500">
                          {formatDate(trend.analyzedAt)}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'anomalies' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Anomaly Detection
                  </h3>
                  <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700">
                    <Plus className="h-4 w-4 mr-2" />
                    New Detector
                  </button>
                </div>

                <div className="bg-white shadow overflow-hidden sm:rounded-md">
                  <ul className="divide-y divide-gray-200">
                    {anomalies.map((anomaly) => (
                      <li key={anomaly.id} className="px-6 py-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center">
                            <div className="flex-shrink-0">
                              <AlertTriangle className="h-5 w-5 text-red-500" />
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                {anomaly.description}
                              </div>
                              <div className="text-sm text-gray-500">
                                Score: {anomaly.anomalyScore.toFixed(2)}
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(anomaly.severity)}`}>
                              {anomaly.severity}
                            </span>
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              anomaly.isResolved ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                            }`}>
                              {anomaly.isResolved ? 'Resolved' : 'Open'}
                            </span>
                            <span className="text-sm text-gray-500">
                              {formatDate(anomaly.detectedAt)}
                            </span>
                          </div>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default RealtimeIntelligenceDashboard;


