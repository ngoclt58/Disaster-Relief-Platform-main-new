import React, { useState, useEffect } from 'react';
import VideoConferencing from '../components/communication/VideoConferencing';
import ChatBot from '../components/communication/ChatBot';
import TranslationWidget from '../components/communication/TranslationWidget';
import DocumentCollaboration from '../components/communication/DocumentCollaboration';
import { videoConferencingService, VideoConference } from '../services/videoConferencingService';
import { chatBotService, ChatSession } from '../services/chatBotService';
import { translationService, LanguageSupport } from '../services/translationService';
import { documentCollaborationService, CollaborativeDocument } from '../services/documentCollaborationService';

const CommunicationDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'video' | 'chat' | 'translation' | 'documents'>('video');
  const [conferences, setConferences] = useState<VideoConference[]>([]);
  const [chatSessions, setChatSessions] = useState<ChatSession[]>([]);
  const [documents, setDocuments] = useState<CollaborativeDocument[]>([]);
  const [supportedLanguages, setSupportedLanguages] = useState<LanguageSupport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      
      const [conferencesData, chatSessionsData, documentsData, languagesData] = await Promise.all([
        videoConferencingService.getUserConferences().catch(() => []),
        chatBotService.getUserSessions().catch(() => []),
        documentCollaborationService.getCollaborativeDocuments().catch(() => []),
        translationService.getSupportedLanguages().catch(() => [])
      ]);

      // Ensure all data is arrays
      setConferences(Array.isArray(conferencesData) ? conferencesData : []);
      setChatSessions(Array.isArray(chatSessionsData) ? chatSessionsData : []);
      setDocuments(Array.isArray(documentsData) ? documentsData : []);
      setSupportedLanguages(Array.isArray(languagesData) ? languagesData : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
      // Set empty arrays on error
      setConferences([]);
      setChatSessions([]);
      setDocuments([]);
      setSupportedLanguages([]);
    } finally {
      setLoading(false);
    }
  };

  const handleEmergencyDetected = () => {
    // Handle emergency detection
    console.log('Emergency detected!');
  };

  const handleDocumentChange = (document: CollaborativeDocument) => {
    setDocuments(prev => 
      prev.map(doc => doc.id === document.id ? document : doc)
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">Loading communication dashboard...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error</h3>
            <p className="mt-1 text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Communication & Collaboration</h1>
          <p className="mt-2 text-gray-600">Video calls, AI chat, translation, and document collaboration tools</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Active Conferences</p>
                <p className="text-2xl font-semibold text-gray-900">{conferences.filter(c => c.status === 'ACTIVE').length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Chat Sessions</p>
                <p className="text-2xl font-semibold text-gray-900">{chatSessions.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v8a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm2 1v6h10V5H5z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Supported Languages</p>
                <p className="text-2xl font-semibold text-gray-900">{supportedLanguages.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Collaborative Documents</p>
                <p className="text-2xl font-semibold text-gray-900">{documents.length}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8 px-6">
              {[
                { id: 'video', name: 'Video Conferencing', icon: '📹' },
                { id: 'chat', name: 'AI Chat Bot', icon: '🤖' },
                { id: 'translation', name: 'Translation', icon: '🌐' },
                { id: 'documents', name: 'Document Collaboration', icon: '📄' }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`py-4 px-1 border-b-2 font-medium text-sm ${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <span className="mr-2">{tab.icon}</span>
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {activeTab === 'video' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold text-gray-900">Video Conferences</h3>
                  <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    Create Conference
                  </button>
                </div>
                
                {conferences.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {conferences.map((conference) => (
                      <div key={conference.id} className="bg-gray-50 rounded-lg p-4">
                        <h4 className="font-semibold text-gray-900">{conference.title}</h4>
                        <p className="text-sm text-gray-600 mt-1">{conference.description}</p>
                        <div className="mt-3 flex items-center justify-between">
                          <span className={`px-2 py-1 text-xs rounded-full ${
                            conference.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                            conference.status === 'SCHEDULED' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-gray-100 text-gray-800'
                          }`}>
                            {conference.status}
                          </span>
                          <button className="text-blue-600 hover:text-blue-800 text-sm">
                            Join
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <p className="text-gray-500">No video conferences found</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'chat' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold text-gray-900">AI Chat Bot</h3>
                  <button className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors">
                    Start New Chat
                  </button>
                </div>
                
                {chatSessions.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {chatSessions.map((session) => (
                      <div key={session.id} className="bg-gray-50 rounded-lg p-4">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-sm font-medium text-gray-900">Session {session.id.slice(0, 8)}</span>
                          <span className={`px-2 py-1 text-xs rounded-full ${
                            session.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                          }`}>
                            {session.status}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">
                          {session.messages.length} messages
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          Last activity: {new Date(session.lastActivity).toLocaleString()}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <p className="text-gray-500">No chat sessions found</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'translation' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold text-gray-900">Translation Services</h3>
                  <button className="bg-yellow-600 text-white px-4 py-2 rounded-lg hover:bg-yellow-700 transition-colors">
                    Translate Text
                  </button>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {supportedLanguages.slice(0, 6).map((language) => (
                    <div key={language.code} className="bg-gray-50 rounded-lg p-4">
                      <div className="flex items-center space-x-3">
                        <span className="text-2xl">{translationService.getLanguageFlag(language.code)}</span>
                        <div>
                          <h4 className="font-semibold text-gray-900">{language.name}</h4>
                          <p className="text-sm text-gray-600">{language.nativeName}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'documents' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold text-gray-900">Collaborative Documents</h3>
                  <button className="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors">
                    Create Document
                  </button>
                </div>
                
                {documents.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {documents.map((document) => (
                      <div key={document.id} className="bg-gray-50 rounded-lg p-4">
                        <div className="flex items-center space-x-3 mb-2">
                          <span className="text-2xl">{documentCollaborationService.getDocumentTypeIcon(document.type)}</span>
                          <div className="flex-1">
                            <h4 className="font-semibold text-gray-900">{document.title}</h4>
                            <p className="text-sm text-gray-600">Version {document.version}</p>
                          </div>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className={`px-2 py-1 text-xs rounded-full ${
                            document.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                          }`}>
                            {document.status}
                          </span>
                          <button className="text-purple-600 hover:text-purple-800 text-sm">
                            Open
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <p className="text-gray-500">No collaborative documents found</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Floating Chat Bot */}
        <ChatBot onEmergencyDetected={handleEmergencyDetected} />
      </div>
    </div>
  );
};

export default CommunicationDashboard;


