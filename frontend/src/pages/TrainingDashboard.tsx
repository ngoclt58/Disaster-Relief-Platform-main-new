import React, { useState, useEffect } from 'react';
import { 
  GraduationCap, 
  BookOpen, 
  Award, 
  BarChart3, 
  Play,
  Search,
  Plus,
  Filter,
  Calendar,
  Users,
  Clock,
  Star,
  TrendingUp,
  Target,
  CheckCircle,
  AlertTriangle,
  XCircle
} from 'lucide-react';
import { disasterSimulationService, SimulationScenario, SimulationSession } from '../services/disasterSimulationService';
import { knowledgeBaseService, KnowledgeArticle } from '../services/knowledgeBaseService';
import { certificationTrackingService, UserCertification, Certification } from '../services/certificationTrackingService';
import { performanceAssessmentService, Assessment, AssessmentSession } from '../services/performanceAssessmentService';

const TrainingDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'simulations' | 'knowledge' | 'certifications' | 'assessments'>('overview');
  const [scenarios, setScenarios] = useState<SimulationScenario[]>([]);
  const [sessions, setSessions] = useState<SimulationSession[]>([]);
  const [articles, setArticles] = useState<KnowledgeArticle[]>([]);
  const [certifications, setCertifications] = useState<UserCertification[]>([]);
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showSimulationModal, setShowSimulationModal] = useState(false);
  const [showArticleModal, setShowArticleModal] = useState(false);
  const [showAssessmentModal, setShowAssessmentModal] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [scenariosData, sessionsData, articlesData, certificationsData, assessmentsData] = await Promise.all([
        disasterSimulationService.getScenarios({ isActive: true }),
        disasterSimulationService.getSessions({}),
        knowledgeBaseService.getRecentArticles(5),
        certificationTrackingService.getUserCertifications('current-user'),
        performanceAssessmentService.getAssessments({ isActive: true })
      ]);
      
      setScenarios(scenariosData);
      setSessions(sessionsData);
      setArticles(articlesData);
      setCertifications(certificationsData);
      setAssessments(assessmentsData);
    } catch (err) {
      setError('Failed to load dashboard data');
      console.error('Error loading dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartSimulation = () => {
    setShowSimulationModal(true);
  };

  const handleCreateArticle = () => {
    setShowArticleModal(true);
  };

  const handleStartAssessment = () => {
    setShowAssessmentModal(true);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
      case 'COMPLETED':
      case 'PUBLISHED':
        return 'text-green-600 bg-green-100';
      case 'IN_PROGRESS':
      case 'SCHEDULED':
        return 'text-blue-600 bg-blue-100';
      case 'EXPIRED':
      case 'CANCELLED':
        return 'text-red-600 bg-red-100';
      case 'DRAFT':
        return 'text-yellow-600 bg-yellow-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty.toLowerCase()) {
      case 'easy':
        return 'text-green-600 bg-green-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'hard':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
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
          <AlertTriangle className="h-5 w-5 text-red-400" />
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
          <h1 className="text-2xl font-bold text-gray-900">Training & Simulation</h1>
          <p className="text-gray-600">Enhance response capabilities through training and assessment</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={handleStartSimulation}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            <Play className="h-4 w-4 mr-2" />
            Start Simulation
          </button>
          <button
            onClick={handleCreateArticle}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            <Plus className="h-4 w-4 mr-2" />
            Create Article
          </button>
          <button
            onClick={handleStartAssessment}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <Target className="h-4 w-4 mr-2" />
            Start Assessment
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'overview', name: 'Overview', icon: BarChart3 },
            { id: 'simulations', name: 'Simulations', icon: Play },
            { id: 'knowledge', name: 'Knowledge Base', icon: BookOpen },
            { id: 'certifications', name: 'Certifications', icon: Award },
            { id: 'assessments', name: 'Assessments', icon: Target }
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
            {/* Training Overview Cards */}
            <div className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Play className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Active Simulations</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {sessions.filter(s => s.status === 'ACTIVE').length}
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
                    <BookOpen className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Knowledge Articles</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {articles.length}
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
                    <Award className="h-6 w-6 text-gray-400" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">Certifications</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {certifications.filter(c => c.status === 'COMPLETED').length}
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
                      <dt className="text-sm font-medium text-gray-500 truncate">Assessments</dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {assessments.length}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'simulations' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Disaster Simulations</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Virtual training scenarios for responders</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {scenarios.map((scenario) => (
                  <li key={scenario.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {scenario.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex space-x-2">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(scenario.status)}`}>
                              {scenario.status}
                            </span>
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getDifficultyColor(scenario.difficulty)}`}>
                              {scenario.difficulty}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">{scenario.description}</span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">Type: {scenario.disasterType}</span>
                            <span className="ml-4">Location: {scenario.location}</span>
                          </div>
                        </div>
                      </div>
                      <div className="ml-4 flex-shrink-0">
                        <button className="text-blue-600 hover:text-blue-900 text-sm font-medium">
                          Start
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'knowledge' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Knowledge Base</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Searchable repository of best practices and procedures</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {articles.map((article) => (
                  <li key={article.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {article.title}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(article.status)}`}>
                              {article.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">Category: {article.category}</span>
                            <span className="ml-4">Language: {article.language}</span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">Tags: {article.tags.join(', ')}</span>
                            <span className="ml-4">Views: {article.viewCount}</span>
                            <span className="ml-4">Rating: {article.rating}/5</span>
                          </div>
                        </div>
                      </div>
                      <div className="ml-4 flex-shrink-0">
                        <button className="text-blue-600 hover:text-blue-900 text-sm font-medium">
                          Read
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'certifications' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Certifications</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Track training and certification requirements</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {certifications.map((cert) => (
                  <li key={cert.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            Certification #{cert.id}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(cert.status)}`}>
                              {cert.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">Assigned: {formatDate(cert.assignedDate)}</span>
                            <span className="ml-4">Expires: {formatDate(cert.expiryDate)}</span>
                          </div>
                        </div>
                        {cert.completionDate && (
                          <div className="mt-1">
                            <p className="text-xs text-gray-500">
                              Completed: {formatDate(cert.completionDate)}
                            </p>
                          </div>
                        )}
                      </div>
                      <div className="ml-4 flex-shrink-0">
                        <button className="text-blue-600 hover:text-blue-900 text-sm font-medium">
                          View Details
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'assessments' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Performance Assessments</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">Evaluate and improve response capabilities</p>
              </div>
              <ul className="divide-y divide-gray-200">
                {assessments.map((assessment) => (
                  <li key={assessment.id} className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {assessment.name}
                          </p>
                          <div className="ml-2 flex-shrink-0 flex">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(assessment.isActive ? 'ACTIVE' : 'INACTIVE')}`}>
                              {assessment.isActive ? 'ACTIVE' : 'INACTIVE'}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">{assessment.description}</span>
                          </div>
                        </div>
                        <div className="mt-2">
                          <div className="flex items-center text-sm text-gray-500">
                            <span className="truncate">Type: {assessment.assessmentType}</span>
                            <span className="ml-4">Category: {assessment.category}</span>
                          </div>
                        </div>
                      </div>
                      <div className="ml-4 flex-shrink-0">
                        <button className="text-blue-600 hover:text-blue-900 text-sm font-medium">
                          Start
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

      {/* Simulation Start Modal */}
      {showSimulationModal && (
        <SimulationStartModal
          scenarios={scenarios}
          onClose={() => setShowSimulationModal(false)}
          onStart={async (scenarioId, sessionName, participantIds) => {
            try {
              await disasterSimulationService.startSession({
                scenarioId,
                sessionName,
                participantIds
              });
              await loadDashboardData();
              setShowSimulationModal(false);
            } catch (err) {
              console.error('Failed to start simulation:', err);
              alert('Failed to start simulation. Please try again.');
            }
          }}
        />
      )}

      {/* Article Creation Modal */}
      {showArticleModal && (
        <ArticleCreationModal
          onClose={() => setShowArticleModal(false)}
          onCreate={async (articleData) => {
            try {
              await knowledgeBaseService.createArticle({
                title: articleData.title,
                content: articleData.content,
                category: articleData.category,
                tags: articleData.tags,
                language: articleData.language,
                isPublic: articleData.isPublic
              });
              await loadDashboardData();
              setShowArticleModal(false);
            } catch (err) {
              console.error('Failed to create article:', err);
              alert('Failed to create article. Please try again.');
            }
          }}
        />
      )}

      {/* Assessment Start Modal */}
      {showAssessmentModal && (
        <AssessmentStartModal
          assessments={assessments}
          onClose={() => setShowAssessmentModal(false)}
          onStart={async (assessmentId, sessionName, context) => {
            try {
              await performanceAssessmentService.startAssessment({
                assessmentId,
                sessionName,
                context
              });
              await loadDashboardData();
              setShowAssessmentModal(false);
            } catch (err) {
              console.error('Failed to start assessment:', err);
              alert('Failed to start assessment. Please try again.');
            }
          }}
        />
      )}
    </div>
  );
};

// Simulation Start Modal Component
const SimulationStartModal: React.FC<{
  scenarios: SimulationScenario[];
  onClose: () => void;
  onStart: (scenarioId: string, sessionName: string, participantIds: string[]) => Promise<void>;
}> = ({ scenarios, onClose, onStart }) => {
  const [formData, setFormData] = useState({
    scenarioId: '',
    sessionName: '',
    participantIds: [] as string[]
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.scenarioId || !formData.sessionName) {
      alert('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await onStart(formData.scenarioId, formData.sessionName, formData.participantIds);
    } catch (err) {
      console.error('Error starting simulation:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Start Simulation</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircle className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Scenario *
            </label>
            <select
              required
              value={formData.scenarioId}
              onChange={(e) => setFormData({...formData, scenarioId: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="">Select a scenario</option>
              {scenarios.filter(s => s.status === 'PUBLISHED').map((scenario) => (
                <option key={scenario.id} value={scenario.id}>
                  {scenario.name} - {scenario.disasterType}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Session Name *
            </label>
            <input
              type="text"
              required
              value={formData.sessionName}
              onChange={(e) => setFormData({...formData, sessionName: e.target.value})}
              placeholder="e.g., Training Session 2024"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Participant IDs (optional, comma-separated)
            </label>
            <input
              type="text"
              value={formData.participantIds.join(', ')}
              onChange={(e) => setFormData({
                ...formData,
                participantIds: e.target.value.split(',').map(id => id.trim()).filter(id => id)
              })}
              placeholder="user1, user2, user3"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? 'Starting...' : 'Start Simulation'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Article Creation Modal Component
const ArticleCreationModal: React.FC<{
  onClose: () => void;
  onCreate: (articleData: {
    title: string;
    content: string;
    category: string;
    tags: string;
    language: string;
    isPublic: boolean;
  }) => Promise<void>;
}> = ({ onClose, onCreate }) => {
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    category: '',
    tags: '',
    language: 'en',
    isPublic: true
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title || !formData.content || !formData.category) {
      alert('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await onCreate(formData);
    } catch (err) {
      console.error('Error creating article:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Create Knowledge Article</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircle className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Title *
            </label>
            <input
              type="text"
              required
              value={formData.title}
              onChange={(e) => setFormData({...formData, title: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category *
            </label>
            <input
              type="text"
              required
              value={formData.category}
              onChange={(e) => setFormData({...formData, category: e.target.value})}
              placeholder="e.g., Emergency Response, Best Practices"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Tags (comma-separated)
            </label>
            <input
              type="text"
              value={formData.tags}
              onChange={(e) => setFormData({...formData, tags: e.target.value})}
              placeholder="e.g., emergency, flood, safety"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Language *
            </label>
            <select
              required
              value={formData.language}
              onChange={(e) => setFormData({...formData, language: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="en">English</option>
              <option value="fr">French</option>
              <option value="es">Spanish</option>
              <option value="de">German</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Content *
            </label>
            <textarea
              required
              value={formData.content}
              onChange={(e) => setFormData({...formData, content: e.target.value})}
              rows={10}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              placeholder="Enter the article content..."
            />
          </div>

          <div className="flex items-center">
            <input
              type="checkbox"
              id="isPublic"
              checked={formData.isPublic}
              onChange={(e) => setFormData({...formData, isPublic: e.target.checked})}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="isPublic" className="ml-2 block text-sm text-gray-700">
              Make this article public
            </label>
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Create Article'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Assessment Start Modal Component
const AssessmentStartModal: React.FC<{
  assessments: Assessment[];
  onClose: () => void;
  onStart: (assessmentId: string, sessionName: string, context: Record<string, any>) => Promise<void>;
}> = ({ assessments, onClose, onStart }) => {
  const [formData, setFormData] = useState({
    assessmentId: '',
    sessionName: '',
    context: ''
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.assessmentId || !formData.sessionName) {
      alert('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      let contextObj = {};
      if (formData.context) {
        try {
          contextObj = JSON.parse(formData.context);
        } catch {
          // If not valid JSON, treat as plain text
          contextObj = { notes: formData.context };
        }
      }
      await onStart(formData.assessmentId, formData.sessionName, contextObj);
    } catch (err) {
      console.error('Error starting assessment:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Start Assessment</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircle className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Assessment *
            </label>
            <select
              required
              value={formData.assessmentId}
              onChange={(e) => setFormData({...formData, assessmentId: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="">Select an assessment</option>
              {assessments.filter(a => a.isActive).map((assessment) => (
                <option key={assessment.id} value={assessment.id}>
                  {assessment.name} - {assessment.assessmentType}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Session Name *
            </label>
            <input
              type="text"
              required
              value={formData.sessionName}
              onChange={(e) => setFormData({...formData, sessionName: e.target.value})}
              placeholder="e.g., Q1 2024 Assessment"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Context (JSON or notes, optional)
            </label>
            <textarea
              value={formData.context}
              onChange={(e) => setFormData({...formData, context: e.target.value})}
              rows={3}
              placeholder='{"training": "emergency_response"} or plain text notes'
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? 'Starting...' : 'Start Assessment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TrainingDashboard;


