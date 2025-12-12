import React, { useState, useEffect } from 'react';
import { 
  Brain, 
  AlertTriangle, 
  TrendingUp, 
  Target,
  Activity,
  BarChart3,
  LineChart,
  PieChart,
  Bell,
  RefreshCw
} from 'lucide-react';
import { disasterPredictionService, DisasterPrediction } from '../services/disasterPredictionService';
import { resourceForecastingService, DemandForecast } from '../services/resourceForecastingService';
import { riskScoringService, RiskScore } from '../services/riskScoringService';
import { earlyWarningService, EarlyWarning } from '../services/earlyWarningService';

const AIDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'predictions' | 'forecasting' | 'risk' | 'warnings'>('overview');
  const [loading, setLoading] = useState(true);
  const [predictions, setPredictions] = useState<DisasterPrediction[]>([]);
  const [forecasts, setForecasts] = useState<DemandForecast[]>([]);
  const [riskScores, setRiskScores] = useState<RiskScore[]>([]);
  const [warnings, setWarnings] = useState<EarlyWarning[]>([]);

  useEffect(() => {
    loadAIData();
  }, []);

  const loadAIData = async () => {
    try {
      setLoading(true);
      
      const [models, resourceModels, scores, activeWarnings] = await Promise.all([
        disasterPredictionService.getModels().catch(() => []),
        resourceForecastingService.getModels().catch(() => []),
        riskScoringService.getRiskScores(
          new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          new Date().toISOString()
        ).catch(() => []),
        earlyWarningService.getActiveWarnings().catch(() => [])
      ]);

      // Ensure all data is arrays
      const modelsArray = Array.isArray(models) ? models : [];
      const resourceModelsArray = Array.isArray(resourceModels) ? resourceModels : [];
      const scoresArray = Array.isArray(scores) ? scores : [];
      const warningsArray = Array.isArray(activeWarnings) ? activeWarnings : [];

      // Load predictions for first model
      if (modelsArray.length > 0) {
        try {
          const predictionsData = await disasterPredictionService.getPredictions(
            modelsArray[0].id,
            new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
            new Date().toISOString()
          );
          setPredictions(Array.isArray(predictionsData) ? predictionsData : []);
        } catch (err) {
          console.error('Failed to load predictions:', err);
          setPredictions([]);
        }
      } else {
        setPredictions([]);
      }

      // Load forecasts for first model
      if (resourceModelsArray.length > 0) {
        try {
          const forecastsData = await resourceForecastingService.getForecasts(
            resourceModelsArray[0].id,
            new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
            new Date().toISOString()
          );
          setForecasts(Array.isArray(forecastsData) ? forecastsData : []);
        } catch (err) {
          console.error('Failed to load forecasts:', err);
          setForecasts([]);
        }
      } else {
        setForecasts([]);
      }

      setRiskScores(scoresArray);
      setWarnings(warningsArray);
      
    } catch (error) {
      console.error('Error loading AI data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-full flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-full">
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">
            AI & Machine Learning
          </h1>
          <p className="mt-2 text-sm text-gray-600">
            Predictive analytics, risk assessment, and early warning systems
          </p>
        </div>
      </header>

      <main>
        <div className="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'overview', name: 'Overview', icon: BarChart3 },
                { id: 'predictions', name: 'Disaster Predictions', icon: Brain },
                { id: 'forecasting', name: 'Resource Forecasting', icon: TrendingUp },
                { id: 'risk', name: 'Risk Scoring', icon: Target },
                { id: 'warnings', name: 'Early Warnings', icon: Bell }
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

          <div className="mt-6">
            {activeTab === 'overview' && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <Brain className="h-6 w-6 text-blue-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Active Predictions</p>
                        <p className="text-2xl font-semibold text-gray-900">{predictions.length}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <TrendingUp className="h-6 w-6 text-green-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Resource Forecasts</p>
                        <p className="text-2xl font-semibold text-gray-900">{forecasts.length}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <Target className="h-6 w-6 text-yellow-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Risk Assessments</p>
                        <p className="text-2xl font-semibold text-gray-900">{riskScores.length}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <Bell className="h-6 w-6 text-red-600" />
                      <div className="ml-5">
                        <p className="text-sm font-medium text-gray-500">Active Warnings</p>
                        <p className="text-2xl font-semibold text-gray-900">{warnings.length}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'predictions' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Disaster Predictions</h2>
                <div className="space-y-4">
                  {predictions.map(prediction => (
                    <div key={prediction.id} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">{prediction.disasterType}</h3>
                        <span className={`px-2 py-1 rounded text-xs ${
                          prediction.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                          prediction.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                          'bg-yellow-100 text-yellow-800'
                        }`}>
                          {prediction.severity}
                        </span>
                      </div>
                      <div className="mt-2 flex gap-4 text-sm">
                        <span>Likelihood: {(prediction.likelihood * 100).toFixed(1)}%</span>
                        <span>Confidence: {(prediction.confidence * 100).toFixed(1)}%</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'forecasting' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Resource Demand Forecasting</h2>
                <div className="space-y-4">
                  {forecasts.map(forecast => (
                    <div key={forecast.id} className="border-b pb-4">
                      <div className="flex justify-between">
                        <h3 className="font-medium">{forecast.resourceType}</h3>
                        <span className="text-lg font-bold">{forecast.projectedDemand.toFixed(0)} units</span>
                      </div>
                      <div className="mt-2 text-sm text-gray-600">
                        Confidence: {(forecast.confidence * 100).toFixed(1)}%
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'risk' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Risk Scoring</h2>
                <div className="space-y-4">
                  {riskScores.map(score => (
                    <div key={score.id} className="border-b pb-4">
                      <div className="flex justify-between items-center">
                        <h3 className="font-medium">Risk Assessment</h3>
                        <div className="flex items-center gap-2">
                          <div className="w-32 bg-gray-200 rounded-full h-2">
                            <div 
                              className={`h-2 rounded-full ${
                                score.overallRisk >= 80 ? 'bg-red-600' :
                                score.overallRisk >= 60 ? 'bg-orange-600' :
                                score.overallRisk >= 40 ? 'bg-yellow-600' :
                                'bg-green-600'
                              }`}
                              style={{ width: `${score.overallRisk}%` }}
                            ></div>
                          </div>
                          <span className="font-bold">{score.overallRisk.toFixed(1)}</span>
                        </div>
                      </div>
                      <div className="mt-2 text-sm text-gray-600">
                        Level: {score.riskLevel}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'warnings' && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Early Warnings</h2>
                <div className="space-y-4">
                  {warnings.map(warning => (
                    <div 
                      key={warning.id} 
                      className={`border-l-4 rounded p-4 ${
                        warning.severity === 'CRITICAL' ? 'border-red-500 bg-red-50' :
                        warning.severity === 'HIGH' ? 'border-orange-500 bg-orange-50' :
                        'border-yellow-500 bg-yellow-50'
                      }`}
                    >
                      <div className="flex justify-between">
                        <h3 className="font-medium">{warning.title}</h3>
                        {!warning.isAcknowledged && (
                          <button className="text-sm text-blue-600">Acknowledge</button>
                        )}
                      </div>
                      <p className="mt-1 text-sm">{warning.message}</p>
                      <p className="mt-2 text-xs text-gray-500">{new Date(warning.triggeredAt).toLocaleString()}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default AIDashboard;

