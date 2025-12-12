import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  PieChart, 
  TrendingUp, 
  Target, 
  Search, 
  Lightbulb,
  Activity,
  DollarSign,
  Users,
  Clock,
  Settings,
  RefreshCw,
  Plus,
  Download,
  Share2,
  Eye,
  Edit,
  Trash2,
  Filter,
  Calendar,
  ChevronDown,
  ChevronRight
} from 'lucide-react';
import { customDashboardService, Dashboard, DashboardWidget } from '../services/customDashboardService';
import { advancedReportingService, Report, ReportExecution } from '../services/advancedReportingService';
import { dataMiningService, MiningJob, DataPattern, DataInsight } from '../services/dataMiningService';
import { roiAnalysisService, ROIAnalysis, ROIMetrics } from '../services/roiAnalysisService';

const AnalyticsDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'dashboards' | 'reports' | 'mining' | 'roi'>('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Data states
  const [dashboards, setDashboards] = useState<Dashboard[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [miningJobs, setMiningJobs] = useState<MiningJob[]>([]);
  const [roiAnalyses, setRoiAnalyses] = useState<ROIAnalysis[]>([]);
  const [patterns, setPatterns] = useState<DataPattern[]>([]);
  const [insights, setInsights] = useState<DataInsight[]>([]);

  const USER_ID = 'user-123';
  const USER_ROLE = 'ADMIN';

  useEffect(() => {
    loadAnalyticsData();
  }, []);

  const loadAnalyticsData = async () => {
    try {
      setLoading(true);
      
      // Load data from all analytics services with error handling
      const [
        dashboardsData,
        reportsData,
        miningJobsData,
        roiAnalysesData,
        patternsData,
        insightsData
      ] = await Promise.all([
        customDashboardService.getUserDashboards('user-123', 'ADMIN').catch(() => []),
        advancedReportingService.getUserReports('user-123').catch(() => []),
        dataMiningService.getUserJobs('user-123').catch(() => []),
        roiAnalysisService.getUserAnalyses('user-123').catch(() => []),
        dataMiningService.discoverPatterns('relief_operations', 'trend', {}).catch(() => []),
        dataMiningService.generateInsights('relief_operations', 'optimization', {}).catch(() => [])
      ]);
      
      // Ensure all data is arrays
      setDashboards(Array.isArray(dashboardsData) ? dashboardsData : []);
      setReports(Array.isArray(reportsData) ? reportsData : []);
      setMiningJobs(Array.isArray(miningJobsData) ? miningJobsData : []);
      setRoiAnalyses(Array.isArray(roiAnalysesData) ? roiAnalysesData : []);
      setPatterns(Array.isArray(patternsData) ? patternsData : []);
      setInsights(Array.isArray(insightsData) ? insightsData : []);
      
    } catch (err) {
      setError('Failed to load analytics data');
      console.error('Error loading analytics data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    loadAnalyticsData();
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed':
      case 'active':
      case 'published':
        return 'text-green-600 bg-green-100';
      case 'running':
      case 'pending':
        return 'text-yellow-600 bg-yellow-100';
      case 'failed':
      case 'error':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getImpactColor = (impact: string) => {
    switch (impact.toLowerCase()) {
      case 'high':
      case 'critical':
        return 'text-red-600 bg-red-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'low':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const handleNewDashboard = async () => {
    try {
      const name = window.prompt('Dashboard name', 'Operations Overview (Custom)');
      if (!name) return;
      const description = window.prompt('Description', 'Quick dashboard created from Analytics page') || '';
      const created = await customDashboardService.createDashboard(
        name,
        description,
        USER_ID,
        USER_ROLE,
        true,
        { widgets: [] }
      );
      setDashboards((prev) => [...prev, created]);
      setActiveTab('dashboards');
    } catch (err) {
      console.error('Failed to create dashboard', err);
      window.alert('Failed to create dashboard. Please check console for details.');
    }
  };

  const handleCreateReport = async () => {
    try {
      const name = window.prompt('Report name', 'Operational Summary Report');
      if (!name) return;
      const description = window.prompt('Description', 'Quick analytics report for relief operations') || '';
      const created = await advancedReportingService.createReport(
        name,
        description,
        'ANALYTICS',
        USER_ID,
        [],
        {}
      );
      setReports((prev) => [...prev, created]);
      setActiveTab('reports');
    } catch (err) {
      console.error('Failed to create report', err);
      window.alert('Failed to create report. Please check console for details.');
    }
  };

  const handleNewMiningJob = async () => {
    try {
      const name = window.prompt('Mining job name', 'Needs Trends - Ad hoc');
      if (!name) return;
      const description = window.prompt('Description', 'Analyze trends for recent needs requests') || '';
      const created = await dataMiningService.createMiningJob(
        name,
        description,
        'TIME_SERIES',
        ['needs_requests'],
        { window_days: 7 },
        USER_ID
      );
      setMiningJobs((prev) => [...prev, created]);
      setActiveTab('mining');
    } catch (err) {
      console.error('Failed to create mining job', err);
      window.alert('Failed to create mining job. Please check console for details.');
    }
  };

  const handleNewRoiAnalysis = async () => {
    try {
      const name = window.prompt('ROI analysis name', 'Logistics Optimization ROI');
      if (!name) return;
      const description = window.prompt('Description', 'Analyze ROI for logistics optimisation project') || '';
      const created = await roiAnalysisService.createAnalysis(
        name,
        description,
        'COMPREHENSIVE',
        'project-logistics-001',
        { region: 'Global' },
        USER_ID
      );
      setRoiAnalyses((prev) => [...prev, created]);
      setActiveTab('roi');
    } catch (err) {
      console.error('Failed to create ROI analysis', err);
      window.alert('Failed to create ROI analysis. Please check console for details.');
    }
  };

  const handleNewAnalysis = async () => {
    // From the Overview header, create a generic data mining job as a quick analysis
    await handleNewMiningJob();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <Activity className="h-5 w-5 text-red-400" />
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error</h3>
            <div className="mt-2 text-sm text-red-700">{error}</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Advanced Analytics & Intelligence</h1>
          <p className="text-gray-600">Business intelligence, reporting, data mining, and ROI analysis</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={handleRefresh}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </button>
          <button
            onClick={handleNewAnalysis}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-4 w-4 mr-2" />
            New Analysis
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'overview', name: 'Overview', icon: BarChart3 },
            { id: 'dashboards', name: 'Custom Dashboards', icon: PieChart },
            { id: 'reports', name: 'Advanced Reporting', icon: TrendingUp },
            { id: 'mining', name: 'Data Mining', icon: Search },
            { id: 'roi', name: 'ROI Analysis', icon: Target }
          ].map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
              >
                <Icon className="h-4 w-4 mr-2" />
                {tab.name}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="mt-6">
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Analytics Overview Cards */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <PieChart className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Custom Dashboards</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {dashboards.length} Active
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
                    <TrendingUp className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Advanced Reports</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {reports.length} Generated
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
                    <Search className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Data Mining Jobs</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {miningJobs.length} Running
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
                    <Target className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">ROI Analyses</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {roiAnalyses.length} Completed
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent Insights */}
            <div className="col-span-full bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Recent Data Insights</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">AI-generated insights from data mining</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {insights.slice(0, 3).map((insight) => (
                  <li key={insight.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {insight.title}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getImpactColor(insight.impact)}`}>
                              {insight.impact}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <p className="text-sm text-gray-500">{insight.description}</p>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Confidence: {(insight.confidence * 100).toFixed(1)}%</span>
                            <span className="ml-4">Type: {insight.insightType}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'dashboards' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Custom Dashboards</h3>
              <button
                onClick={handleNewDashboard}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                Create Dashboard
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {dashboards.map((dashboard) => (
                <div key={dashboard.id} className="bg-white shadow overflow-hidden sm:rounded-md">
                  <div className="px-4 py-5 sm:px-6">
                    <div className="flex items-center justify-between">
                      <h4 className="text-lg font-medium text-gray-900">{dashboard.name}</h4>
                      <div className="flex space-x-2">
                        <button className="text-gray-400 hover:text-gray-600">
                          <Eye className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Edit className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Share2 className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-red-600">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                    <p className="mt-1 text-sm text-gray-500">{dashboard.description}</p>
                    <div className="mt-2 flex items-center text-sm text-gray-500">
                      <span>Role: {dashboard.userRole}</span>
                      <span className="ml-4">Created: {formatDate(dashboard.createdAt)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'reports' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Advanced Reports</h3>
              <button
                onClick={handleCreateReport}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                Create Report
              </button>
            </div>
            
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <ul className="divide-y divide-gray-200">
                {reports.map((report) => (
                  <li key={report.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {report.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(report.status)}`}>
                              {report.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <p className="text-sm text-gray-500">{report.description}</p>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Type: {report.reportType}</span>
                            <span className="ml-4">Data Sources: {report.dataSources.length}</span>
                            <span className="ml-4">Updated: {formatDate(report.updatedAt)}</span>
                          </div>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <button className="text-gray-400 hover:text-gray-600">
                          <Download className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Share2 className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Edit className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'mining' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Data Mining</h3>
              <button
                onClick={handleNewMiningJob}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                New Mining Job
              </button>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Mining Jobs */}
              <div className="bg-white shadow overflow-hidden sm:rounded-md">
                <div className="px-4 py-5 sm:px-6">
                  <h4 className="text-lg leading-6 font-medium text-gray-900">Mining Jobs</h4>
                </div>
                <ul className="divide-y divide-gray-200">
                  {miningJobs.map((job) => (
                    <li key={job.id} className="px-4 py-4 sm:px-6">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900 truncate">{job.name}</p>
                          <p className="text-sm text-gray-500">{job.description}</p>
                          <div className="mt-1 flex items-center text-sm text-gray-500">
                            <span>Algorithm: {job.algorithm}</span>
                            <span className="ml-4">Status: {job.status}</span>
                          </div>
                        </div>
                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(job.status)}`}>
                          {job.status}
                        </span>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Discovered Patterns */}
              <div className="bg-white shadow overflow-hidden sm:rounded-md">
                <div className="px-4 py-5 sm:px-6">
                  <h4 className="text-lg leading-6 font-medium text-gray-900">Discovered Patterns</h4>
                </div>
                <ul className="divide-y divide-gray-200">
                  {patterns.map((pattern) => (
                    <li key={pattern.id} className="px-4 py-4 sm:px-6">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900 truncate">{pattern.patternType}</p>
                          <p className="text-sm text-gray-500">{pattern.description}</p>
                          <div className="mt-1 flex items-center text-sm text-gray-500">
                            <span>Confidence: {(pattern.confidence * 100).toFixed(1)}%</span>
                            <span className="ml-4">Frequency: {(pattern.frequency * 100).toFixed(1)}%</span>
                          </div>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'roi' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h3 className="text-lg leading-6 font-medium text-gray-900">ROI Analysis</h3>
              <button
                onClick={handleNewRoiAnalysis}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                New ROI Analysis
              </button>
            </div>
            
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <ul className="divide-y divide-gray-200">
                {roiAnalyses.map((analysis) => (
                  <li key={analysis.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {analysis.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(analysis.status)}`}>
                              {analysis.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <p className="text-sm text-gray-500">{analysis.description}</p>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span>Type: {analysis.analysisType}</span>
                            <span className="ml-4">Project: {analysis.projectId}</span>
                            <span className="ml-4">Created: {formatDate(analysis.createdAt)}</span>
                          </div>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <button className="text-gray-400 hover:text-gray-600">
                          <Eye className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Download className="h-4 w-4" />
                        </button>
                        <button className="text-gray-400 hover:text-gray-600">
                          <Edit className="h-4 w-4" />
                        </button>
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
  );
};

export default AnalyticsDashboard;


