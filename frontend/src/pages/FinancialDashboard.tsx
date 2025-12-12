import React, { useState, useEffect } from 'react';
import { 
  DollarSign, 
  TrendingUp, 
  PieChart, 
  BarChart3, 
  Download,
  Plus,
  Filter,
  Calendar,
  AlertTriangle,
  CheckCircle,
  XCircle
} from 'lucide-react';
import { budgetTrackingService, Budget, BudgetSummary } from '../services/budgetTrackingService';
import { donationManagementService, Donation, DonationSummary, DonationType } from '../services/donationManagementService';
import { costAnalysisService, CostAnalysis, CostCategory, CostAnalysisRequest } from '../services/costAnalysisService';
import { financialReportingService, FinancialReport, ReportType } from '../services/financialReportingService';

const FinancialDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'budgets' | 'donations' | 'analysis' | 'reports'>('overview');
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [donations, setDonations] = useState<Donation[]>([]);
  const [reports, setReports] = useState<FinancialReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showBudgetModal, setShowBudgetModal] = useState(false);
  const [showDonationModal, setShowDonationModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [financialKPIs, setFinancialKPIs] = useState<any>(null);
  const [donationSummary, setDonationSummary] = useState<DonationSummary | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [budgetsData, donationsData, reportsData, kpisData, summaryData] = await Promise.all([
        budgetTrackingService.getUserBudgets(),
        donationManagementService.getDonations({ limit: 10 }),
        financialReportingService.getReports({ limit: 5 }),
        financialReportingService.getFinancialKPIs().catch(() => null),
        donationManagementService.getDonationSummary().catch(() => null)
      ]);
      
      setBudgets(budgetsData);
      setDonations(donationsData);
      setReports(reportsData);
      setFinancialKPIs(kpisData);
      setDonationSummary(summaryData);
    } catch (err) {
      setError('Failed to load dashboard data');
      console.error('Error loading dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBudget = () => {
    setShowBudgetModal(true);
  };

  const handleRecordDonation = () => {
    setShowDonationModal(true);
  };

  const handleGenerateReport = () => {
    setShowReportModal(true);
  };

  const handleDownloadReport = async (report: FinancialReport) => {
    try {
      const blob = await financialReportingService.downloadReport(report.id, report.format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${report.title || 'report'}.${report.format.toLowerCase()}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      console.error('Failed to download report:', err);
      alert('Failed to download report. Please try again.');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
      case 'CONFIRMED':
      case 'COMPLETED':
        return 'text-green-600 bg-green-100';
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'CLOSED':
      case 'CANCELLED':
        return 'text-gray-600 bg-gray-100';
      case 'CRITICAL':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-blue-600 bg-blue-100';
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
          <XCircle className="h-5 w-5 text-red-400" />
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
          <h1 className="text-2xl font-bold text-gray-900">Financial Management</h1>
          <p className="text-gray-600">Monitor budgets, donations, and financial performance</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={handleCreateBudget}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-4 w-4 mr-2" />
            Create Budget
          </button>
          <button
            onClick={handleRecordDonation}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            <Plus className="h-4 w-4 mr-2" />
            Record Donation
          </button>
          <button
            onClick={handleGenerateReport}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <Download className="h-4 w-4 mr-2" />
            Generate Report
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'overview', name: 'Overview', icon: BarChart3 },
            { id: 'budgets', name: 'Budgets', icon: DollarSign },
            { id: 'donations', name: 'Donations', icon: TrendingUp },
            { id: 'analysis', name: 'Cost Analysis', icon: PieChart },
            { id: 'reports', name: 'Reports', icon: Download }
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
          <div className="space-y-6">
            {/* Financial Overview Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-5">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <DollarSign className="h-6 w-6 text-gray-400" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Total Budget</dt>
                        <dd className="text-lg font-medium text-gray-900">
                          {formatCurrency(budgets.reduce((sum, budget) => sum + budget.totalAmount, 0))}
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
                        <dt className="text-sm font-medium text-gray-500 truncate">Total Donations</dt>
                        <dd className="text-lg font-medium text-gray-900">
                          {formatCurrency(donationSummary?.totalAmount || donations.reduce((sum, donation) => sum + donation.amount, 0))}
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
                      <PieChart className="h-6 w-6 text-gray-400" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Active Budgets</dt>
                        <dd className="text-lg font-medium text-gray-900">
                          {budgets.filter(b => b.status === 'ACTIVE').length}
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
                      <BarChart3 className="h-6 w-6 text-gray-400" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Recent Reports</dt>
                        <dd className="text-lg font-medium text-gray-900">
                          {reports.length}
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Financial KPIs */}
            {financialKPIs && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Financial KPIs</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {financialKPIs.totalRevenue && (
                    <div className="bg-blue-50 p-4 rounded-lg">
                      <p className="text-sm text-gray-600">Total Revenue</p>
                      <p className="text-2xl font-bold text-blue-600">
                        {formatCurrency(financialKPIs.totalRevenue)}
                      </p>
                    </div>
                  )}
                  {financialKPIs.totalExpenses && (
                    <div className="bg-red-50 p-4 rounded-lg">
                      <p className="text-sm text-gray-600">Total Expenses</p>
                      <p className="text-2xl font-bold text-red-600">
                        {formatCurrency(financialKPIs.totalExpenses)}
                      </p>
                    </div>
                  )}
                  {financialKPIs.netIncome && (
                    <div className="bg-green-50 p-4 rounded-lg">
                      <p className="text-sm text-gray-600">Net Income</p>
                      <p className="text-2xl font-bold text-green-600">
                        {formatCurrency(financialKPIs.netIncome)}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Donation Summary */}
            {donationSummary && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Donation Summary</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <p className="text-sm text-gray-600">Total Donations</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {formatCurrency(donationSummary.totalAmount)}
                    </p>
                    <p className="text-sm text-gray-500 mt-1">
                      {donationSummary.donationCount} donations
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Average Donation</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {formatCurrency(donationSummary.averageDonation)}
                    </p>
                  </div>
                </div>
                {donationSummary.topDonors && donationSummary.topDonors.length > 0 && (
                  <div className="mt-6">
                    <h4 className="text-sm font-semibold text-gray-700 mb-2">Top Donors</h4>
                    <ul className="space-y-2">
                      {donationSummary.topDonors.slice(0, 5).map((donor, index) => (
                        <li key={index} className="flex justify-between text-sm">
                          <span className="text-gray-600">{donor.donorName}</span>
                          <span className="font-medium text-gray-900">{formatCurrency(donor.amount)}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}

            {/* Recent Activity */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h3>
              <div className="space-y-4">
                {donations.slice(0, 5).map((donation) => (
                  <div key={donation.id} className="flex items-center justify-between border-b border-gray-200 pb-3">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{donation.description}</p>
                      <p className="text-xs text-gray-500">{formatDate(donation.createdAt)}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-semibold text-green-600">
                        {formatCurrency(donation.amount)}
                      </p>
                      <span className={`text-xs px-2 py-1 rounded-full ${getStatusColor(donation.status)}`}>
                        {donation.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'budgets' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
                <div>
                  <h3 className="text-lg leading-6 font-medium text-gray-900">Budget Management</h3>
                  <p className="mt-1 max-w-2xl text-sm text-gray-500">Track and manage your budgets</p>
                </div>
                <button
                  onClick={handleCreateBudget}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Create Budget
                </button>
              </div>
              {budgets.length === 0 ? (
                <div className="px-4 py-12 text-center">
                  <DollarSign className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No budgets</h3>
                  <p className="mt-1 text-sm text-gray-500">Get started by creating a new budget.</p>
                  <div className="mt-6">
                    <button
                      onClick={handleCreateBudget}
                      className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Create Budget
                    </button>
                  </div>
                </div>
              ) : (
                <ul className="divide-y divide-gray-200">
                  {budgets.map((budget) => (
                    <li key={budget.id} className="px-4 py-4 sm:px-6 hover:bg-gray-50">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-blue-600 truncate">
                              {budget.name}
                            </p>
                            <div className="ml-2 flex-shrink-0 flex items-center space-x-2">
                              <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(budget.status)}`}>
                                {budget.status}
                              </span>
                            </div>
                          </div>
                          <div className="mt-2">
                            <div className="flex items-center text-sm text-gray-500">
                              <span className="truncate">{budget.description || 'No description'}</span>
                            </div>
                          </div>
                          <div className="mt-2">
                            <div className="flex items-center justify-between text-sm mb-1">
                              <span className="text-gray-500">Spent: {formatCurrency(budget.spentAmount)}</span>
                              <span className="text-gray-500">Remaining: {formatCurrency(budget.remainingAmount)}</span>
                              <span className="text-gray-500 font-semibold">Total: {formatCurrency(budget.totalAmount)}</span>
                            </div>
                            <div className="mt-1">
                              <div className="bg-gray-200 rounded-full h-2">
                                <div 
                                  className={`h-2 rounded-full ${
                                    (budget.spentAmount / budget.totalAmount) > 0.9 
                                      ? 'bg-red-600' 
                                      : (budget.spentAmount / budget.totalAmount) > 0.7 
                                      ? 'bg-yellow-600' 
                                      : 'bg-blue-600'
                                  }`}
                                  style={{ width: `${Math.min((budget.spentAmount / budget.totalAmount) * 100, 100)}%` }}
                                ></div>
                              </div>
                            </div>
                            <div className="mt-2 flex items-center text-xs text-gray-500">
                              <Calendar className="h-3 w-3 mr-1" />
                              <span>{formatDate(budget.startDate)} - {formatDate(budget.endDate)}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}

        {activeTab === 'donations' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
                <div>
                  <h3 className="text-lg leading-6 font-medium text-gray-900">Donations</h3>
                  <p className="mt-1 max-w-2xl text-sm text-gray-500">Track incoming donations and funding</p>
                </div>
                <button
                  onClick={handleRecordDonation}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Record Donation
                </button>
              </div>
              {donations.length === 0 ? (
                <div className="px-4 py-12 text-center">
                  <TrendingUp className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No donations</h3>
                  <p className="mt-1 text-sm text-gray-500">Get started by recording a donation.</p>
                  <div className="mt-6">
                    <button
                      onClick={handleRecordDonation}
                      className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Record Donation
                    </button>
                  </div>
                </div>
              ) : (
                <ul className="divide-y divide-gray-200">
                  {donations.map((donation) => (
                    <li key={donation.id} className="px-4 py-4 sm:px-6 hover:bg-gray-50">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-blue-600 truncate">
                              {donation.description}
                            </p>
                            <div className="ml-2 flex-shrink-0 flex items-center space-x-2">
                              <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(donation.status)}`}>
                                {donation.status}
                              </span>
                            </div>
                          </div>
                          <div className="mt-2">
                            <div className="flex items-center text-sm text-gray-500">
                              <span className="truncate">Type: {donation.type}</span>
                              <span className="ml-4 font-semibold text-gray-900">Amount: {formatCurrency(donation.amount)}</span>
                            </div>
                          </div>
                          <div className="mt-1">
                            <p className="text-xs text-gray-500">
                              Recorded on {formatDate(donation.createdAt)}
                              {donation.campaignId && ` • Campaign: ${donation.campaignId}`}
                            </p>
                          </div>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}

        {activeTab === 'analysis' && (
          <CostAnalysisTab />
        )}

        {activeTab === 'reports' && (
          <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-md">
              <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
                <div>
                  <h3 className="text-lg leading-6 font-medium text-gray-900">Financial Reports</h3>
                  <p className="mt-1 max-w-2xl text-sm text-gray-500">Generate and manage financial reports</p>
                </div>
                <button
                  onClick={handleGenerateReport}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Generate Report
                </button>
              </div>
              {reports.length === 0 ? (
                <div className="px-4 py-12 text-center">
                  <Download className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No reports</h3>
                  <p className="mt-1 text-sm text-gray-500">Get started by generating a financial report.</p>
                  <div className="mt-6">
                    <button
                      onClick={handleGenerateReport}
                      className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                    >
                      <Download className="h-4 w-4 mr-2" />
                      Generate Report
                    </button>
                  </div>
                </div>
              ) : (
                <ul className="divide-y divide-gray-200">
                  {reports.map((report) => (
                    <li key={report.id} className="px-4 py-4 sm:px-6 hover:bg-gray-50">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-blue-600 truncate">
                              {report.title || report.type}
                            </p>
                            <div className="ml-2 flex-shrink-0 flex items-center space-x-2">
                              <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(report.status)}`}>
                                {report.status}
                              </span>
                            </div>
                          </div>
                          <div className="mt-2">
                            <div className="flex items-center text-sm text-gray-500">
                              <span className="truncate">Type: {report.type}</span>
                              <span className="ml-4">Format: {report.format}</span>
                            </div>
                          </div>
                          <div className="mt-1">
                            <p className="text-xs text-gray-500">
                              Generated on {formatDate(report.createdAt)}
                              {report.completedAt && ` • Completed: ${formatDate(report.completedAt)}`}
                            </p>
                          </div>
                        </div>
                        <div className="ml-4 flex-shrink-0">
                          {report.status === 'COMPLETED' && report.fileUrl && (
                            <button 
                              onClick={() => handleDownloadReport(report)}
                              className="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                            >
                              <Download className="h-4 w-4 mr-1" />
                              Download
                            </button>
                          )}
                          {report.status === 'GENERATING' && (
                            <span className="text-sm text-gray-500">Generating...</span>
                          )}
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Budget Creation Modal */}
      {showBudgetModal && (
        <BudgetCreationModal
          onClose={() => setShowBudgetModal(false)}
          onCreate={async (budgetData) => {
            try {
              await budgetTrackingService.createBudget({
                name: budgetData.name,
                description: budgetData.description,
                totalAmount: budgetData.totalAmount,
                category: budgetData.category,
                startDate: budgetData.startDate,
                endDate: budgetData.endDate
              });
              await loadDashboardData();
              setShowBudgetModal(false);
            } catch (err) {
              console.error('Failed to create budget:', err);
              alert('Failed to create budget. Please try again.');
            }
          }}
        />
      )}

      {/* Donation Recording Modal */}
      {showDonationModal && (
        <DonationRecordingModal
          onClose={() => setShowDonationModal(false)}
          onRecord={async (donationData) => {
            try {
              await donationManagementService.recordDonation({
                donorName: donationData.donorName,
                donorEmail: donationData.donorEmail || undefined,
                donorPhone: donationData.donorPhone || undefined,
                amount: donationData.amount,
                currency: 'USD',
                paymentMethod: 'Other',
                campaignId: donationData.campaignId || undefined,
                notes: donationData.description
              });
              await loadDashboardData();
              setShowDonationModal(false);
            } catch (err) {
              console.error('Failed to record donation:', err);
              alert('Failed to record donation. Please try again.');
            }
          }}
        />
      )}

      {/* Report Generation Modal */}
      {showReportModal && (
        <ReportGenerationModal
          onClose={() => setShowReportModal(false)}
          onGenerate={async (reportData) => {
            try {
              await financialReportingService.generateReport({
                type: reportData.type,
                format: reportData.format,
                startDate: reportData.startDate,
                endDate: reportData.endDate,
                filters: reportData.filters || {}
              });
              await loadDashboardData();
              setShowReportModal(false);
            } catch (err) {
              console.error('Failed to generate report:', err);
              alert('Failed to generate report. Please try again.');
            }
          }}
        />
      )}
    </div>
  );
};

// Budget Creation Modal Component
const BudgetCreationModal: React.FC<{
  onClose: () => void;
  onCreate: (budgetData: {
    name: string;
    description: string;
    totalAmount: number;
    category: string;
    startDate: string;
    endDate: string;
  }) => Promise<void>;
}> = ({ onClose, onCreate }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    totalAmount: 0,
    category: '',
    startDate: '',
    endDate: ''
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.totalAmount || !formData.category || !formData.startDate || !formData.endDate) {
      alert('Please fill in all required fields');
      return;
    }

    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
      alert('End date must be after start date');
      return;
    }

    setLoading(true);
    try {
      await onCreate(formData);
    } catch (err) {
      console.error('Error creating budget:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Create Budget</h3>
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
              Budget Name *
            </label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Total Amount *
            </label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              value={formData.totalAmount || ''}
              onChange={(e) => setFormData({...formData, totalAmount: parseFloat(e.target.value) || 0})}
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
              placeholder="e.g., Emergency Relief, Operations"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date *
              </label>
              <input
                type="date"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date *
              </label>
              <input
                type="date"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>
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
              {loading ? 'Creating...' : 'Create Budget'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Donation Recording Modal Component
const DonationRecordingModal: React.FC<{
  onClose: () => void;
  onRecord: (donationData: {
    donorName: string;
    donorEmail?: string;
    donorPhone?: string;
    amount: number;
    type: string;
    description: string;
    campaignId?: string;
    referenceId?: string;
  }) => Promise<void>;
}> = ({ onClose, onRecord }) => {
  const [formData, setFormData] = useState({
    donorName: '',
    donorEmail: '',
    donorPhone: '',
    amount: 0,
    type: 'CASH' as 'CASH' | 'IN_KIND' | 'SERVICES' | 'EQUIPMENT' | 'FOOD' | 'MEDICAL' | 'OTHER',
    description: '',
    campaignId: '',
    referenceId: ''
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.donorName || !formData.amount || !formData.description) {
      alert('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await onRecord({
        donorName: formData.donorName,
        donorEmail: formData.donorEmail || undefined,
        donorPhone: formData.donorPhone || undefined,
        amount: formData.amount,
        type: formData.type,
        description: formData.description,
        campaignId: formData.campaignId || undefined,
        referenceId: formData.referenceId || undefined
      });
    } catch (err) {
      console.error('Error recording donation:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Record Donation</h3>
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
              Donor Name *
            </label>
            <input
              type="text"
              required
              value={formData.donorName}
              onChange={(e) => setFormData({...formData, donorName: e.target.value})}
              placeholder="Enter donor name"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Donor Email
            </label>
            <input
              type="email"
              value={formData.donorEmail}
              onChange={(e) => setFormData({...formData, donorEmail: e.target.value})}
              placeholder="Enter donor email (optional)"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Donor Phone
            </label>
            <input
              type="tel"
              value={formData.donorPhone}
              onChange={(e) => setFormData({...formData, donorPhone: e.target.value})}
              placeholder="Enter donor phone (optional)"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Amount *
            </label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              value={formData.amount || ''}
              onChange={(e) => setFormData({...formData, amount: parseFloat(e.target.value) || 0})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type *
            </label>
            <select
              required
              value={formData.type}
              onChange={(e) => setFormData({...formData, type: e.target.value as any})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="CASH">Cash</option>
              <option value="IN_KIND">In-Kind</option>
              <option value="SERVICES">Services</option>
              <option value="EQUIPMENT">Equipment</option>
              <option value="FOOD">Food</option>
              <option value="MEDICAL">Medical</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description *
            </label>
            <textarea
              required
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Campaign ID (optional)
            </label>
            <input
              type="text"
              value={formData.campaignId}
              onChange={(e) => setFormData({...formData, campaignId: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Reference ID (optional)
            </label>
            <input
              type="text"
              value={formData.referenceId}
              onChange={(e) => setFormData({...formData, referenceId: e.target.value})}
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
              className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
            >
              {loading ? 'Recording...' : 'Record Donation'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Report Generation Modal Component
const ReportGenerationModal: React.FC<{
  onClose: () => void;
  onGenerate: (reportData: {
    type: ReportType;
    format: 'PDF' | 'EXCEL' | 'CSV' | 'JSON';
    startDate: string;
    endDate: string;
    filters?: Record<string, any>;
  }) => Promise<void>;
}> = ({ onClose, onGenerate }) => {
  const [formData, setFormData] = useState({
    type: 'INCOME_STATEMENT' as ReportType,
    format: 'PDF' as 'PDF' | 'EXCEL' | 'CSV' | 'JSON',
    startDate: '',
    endDate: ''
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.startDate || !formData.endDate) {
      alert('Please select start and end dates');
      return;
    }

    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
      alert('End date must be after start date');
      return;
    }

    setLoading(true);
    try {
      await onGenerate(formData);
    } catch (err) {
      console.error('Error generating report:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Generate Financial Report</h3>
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
              Report Type *
            </label>
            <select
              required
              value={formData.type}
              onChange={(e) => setFormData({...formData, type: e.target.value as ReportType})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="INCOME_STATEMENT">Income Statement</option>
              <option value="BALANCE_SHEET">Balance Sheet</option>
              <option value="CASH_FLOW">Cash Flow</option>
              <option value="BUDGET_VS_ACTUAL">Budget vs Actual</option>
              <option value="DONATION_SUMMARY">Donation Summary</option>
              <option value="EXPENSE_ANALYSIS">Expense Analysis</option>
              <option value="COST_BREAKDOWN">Cost Breakdown</option>
              <option value="FINANCIAL_DASHBOARD">Financial Dashboard</option>
              <option value="CUSTOM">Custom</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Format *
            </label>
            <select
              required
              value={formData.format}
              onChange={(e) => setFormData({...formData, format: e.target.value as any})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="PDF">PDF</option>
              <option value="EXCEL">Excel</option>
              <option value="CSV">CSV</option>
              <option value="JSON">JSON</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date *
              </label>
              <input
                type="date"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date *
              </label>
              <input
                type="date"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>
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
              {loading ? 'Generating...' : 'Generate Report'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Cost Analysis Tab Component
const CostAnalysisTab: React.FC = () => {
  const [analysis, setAnalysis] = useState<CostAnalysis | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showAnalysisModal, setShowAnalysisModal] = useState(false);
  const [categories, setCategories] = useState<CostCategory[]>([]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const cats = await costAnalysisService.getCostCategories();
      setCategories(cats);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleRunAnalysis = () => {
    setShowAnalysisModal(true);
  };

  const handlePerformAnalysis = async (request: CostAnalysisRequest) => {
    try {
      setLoading(true);
      setError(null);
      const result = await costAnalysisService.performCostAnalysis(request);
      setAnalysis(result);
      setShowAnalysisModal(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to perform cost analysis');
      console.error('Cost analysis error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Cost Analysis</h3>
        <p className="mt-1 text-sm text-gray-500">Analyze costs and identify optimization opportunities</p>
        <div className="mt-4">
          <button 
            onClick={handleRunAnalysis}
            disabled={loading}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50"
          >
            <PieChart className="h-4 w-4 mr-2" />
            {loading ? 'Running Analysis...' : 'Run Cost Analysis'}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <XCircle className="h-5 w-5 text-red-400" />
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error</h3>
              <div className="mt-2 text-sm text-red-700">{error}</div>
            </div>
          </div>
        </div>
      )}

      {analysis && (
        <div className="space-y-6">
          {/* Analysis Summary */}
          <div className="bg-white shadow rounded-lg p-6">
            <h4 className="text-lg font-semibold text-gray-900 mb-4">Analysis Summary</h4>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="bg-blue-50 p-4 rounded-lg">
                <p className="text-sm text-gray-600">Total Cost</p>
                <p className="text-2xl font-bold text-blue-600">
                  {formatCurrency(analysis.totalCost)}
                </p>
              </div>
              <div className="bg-green-50 p-4 rounded-lg">
                <p className="text-sm text-gray-600">Average Cost</p>
                <p className="text-2xl font-bold text-green-600">
                  {formatCurrency(analysis.averageCost)}
                </p>
              </div>
              <div className="bg-yellow-50 p-4 rounded-lg">
                <p className="text-sm text-gray-600">Cost Per Unit</p>
                <p className="text-2xl font-bold text-yellow-600">
                  {formatCurrency(analysis.costPerUnit)}
                </p>
              </div>
              <div className="bg-purple-50 p-4 rounded-lg">
                <p className="text-sm text-gray-600">Unit Count</p>
                <p className="text-2xl font-bold text-purple-600">
                  {analysis.unitCount}
                </p>
              </div>
            </div>
          </div>

          {/* Insights */}
          {analysis.insights && analysis.insights.length > 0 && (
            <div className="bg-white shadow rounded-lg p-6">
              <h4 className="text-lg font-semibold text-gray-900 mb-4">Key Insights</h4>
              <ul className="space-y-2">
                {analysis.insights.map((insight, index) => (
                  <li key={index} className="flex items-start">
                    <CheckCircle className="h-5 w-5 text-green-500 mr-2 mt-0.5 flex-shrink-0" />
                    <span className="text-gray-700">{insight}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Recommendations */}
          {analysis.recommendations && analysis.recommendations.length > 0 && (
            <div className="bg-white shadow rounded-lg p-6">
              <h4 className="text-lg font-semibold text-gray-900 mb-4">Recommendations</h4>
              <ul className="space-y-2">
                {analysis.recommendations.map((rec, index) => (
                  <li key={index} className="flex items-start">
                    <AlertTriangle className="h-5 w-5 text-yellow-500 mr-2 mt-0.5 flex-shrink-0" />
                    <span className="text-gray-700">{rec}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Cost Breakdown */}
          {analysis.costBreakdown && (
            <div className="bg-white shadow rounded-lg p-6">
              <h4 className="text-lg font-semibold text-gray-900 mb-4">Cost Breakdown</h4>
              <div className="space-y-3">
                {analysis.costBreakdown.subcategories?.map((sub, index) => (
                  <div key={index} className="border-b border-gray-200 pb-3">
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-medium text-gray-900">{sub.name}</span>
                      <span className="text-gray-700">{formatCurrency(sub.cost)} ({sub.percentage}%)</span>
                    </div>
                    <div className="bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-blue-600 h-2 rounded-full" 
                        style={{ width: `${sub.percentage}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Cost Drivers */}
          {analysis.drivers && analysis.drivers.length > 0 && (
            <div className="bg-white shadow rounded-lg p-6">
              <h4 className="text-lg font-semibold text-gray-900 mb-4">Cost Drivers</h4>
              <div className="space-y-3">
                {analysis.drivers.map((driver, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <h5 className="font-medium text-gray-900">{driver.name}</h5>
                      <span className="text-sm text-gray-600">{driver.percentage}% impact</span>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{driver.description}</p>
                    {driver.recommendations && driver.recommendations.length > 0 && (
                      <ul className="list-disc list-inside text-sm text-gray-600">
                        {driver.recommendations.map((rec, recIndex) => (
                          <li key={recIndex}>{rec}</li>
                        ))}
                      </ul>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Cost Analysis Modal */}
      {showAnalysisModal && (
        <CostAnalysisModal
          categories={categories}
          onClose={() => setShowAnalysisModal(false)}
          onAnalyze={handlePerformAnalysis}
        />
      )}
    </div>
  );
};

// Cost Analysis Modal Component
const CostAnalysisModal: React.FC<{
  categories: CostCategory[];
  onClose: () => void;
  onAnalyze: (request: CostAnalysisRequest) => Promise<void>;
}> = ({ categories, onClose, onAnalyze }) => {
  const [formData, setFormData] = useState<CostAnalysisRequest>({
    category: '',
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    filters: {}
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.category || !formData.startDate || !formData.endDate) {
      alert('Please fill in all required fields');
      return;
    }

    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
      alert('End date must be after start date');
      return;
    }

    setLoading(true);
    try {
      await onAnalyze({
        ...formData,
        startDate: new Date(formData.startDate).toISOString(),
        endDate: new Date(formData.endDate).toISOString()
      });
    } catch (err) {
      console.error('Error performing analysis:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">Run Cost Analysis</h3>
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
              Category *
            </label>
            <select
              required
              value={formData.category}
              onChange={(e) => setFormData({...formData, category: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            >
              <option value="">Select a category</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.name}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date *
              </label>
              <input
                type="date"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date *
              </label>
              <input
                type="date"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
              />
            </div>
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
              {loading ? 'Analyzing...' : 'Run Analysis'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default FinancialDashboard;


