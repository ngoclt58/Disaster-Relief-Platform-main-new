import React, { useState, useEffect, useRef } from 'react';
import { documentCollaborationService, CollaborativeDocument, DocumentParticipant, DocumentChange } from '../../services/documentCollaborationService';

interface DocumentCollaborationProps {
  documentId?: string;
  onDocumentChange?: (document: CollaborativeDocument) => void;
  onClose?: () => void;
}

const DocumentCollaboration: React.FC<DocumentCollaborationProps> = ({ documentId, onDocumentChange, onClose }) => {
  const [document, setDocument] = useState<CollaborativeDocument | null>(null);
  const [participants, setParticipants] = useState<DocumentParticipant[]>([]);
  const [changes, setChanges] = useState<DocumentChange[]>([]);
  const [content, setContent] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const [showParticipants, setShowParticipants] = useState(false);
  const [showChanges, setShowChanges] = useState(false);

  const editorRef = useRef<HTMLTextAreaElement>(null);
  const saveTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (documentId) {
      loadDocument();
    }

    return () => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
      }
    };
  }, [documentId]);

  useEffect(() => {
    if (document) {
      setContent(document.content);
      onDocumentChange?.(document);
    }
  }, [document]);

  useEffect(() => {
    if (isConnected && documentId) {
      // Start auto-save
      documentCollaborationService.startAutoSave(documentId);
      
      // Add change handlers
      const handleChange = (change: DocumentChange) => {
        applyChange(change);
      };

      const handleParticipant = (participant: DocumentParticipant) => {
        setParticipants(prev => {
          const existing = prev.find(p => p.userId === participant.userId);
          if (existing) {
            return prev.map(p => p.userId === participant.userId ? participant : p);
          }
          return [...prev, participant];
        });
      };

      documentCollaborationService.addChangeHandler(documentId, handleChange);
      documentCollaborationService.addParticipantHandler(documentId, handleParticipant);

      return () => {
        documentCollaborationService.removeChangeHandler(documentId, handleChange);
        documentCollaborationService.removeParticipantHandler(documentId, handleParticipant);
        documentCollaborationService.stopAutoSave();
      };
    }
  }, [isConnected, documentId]);

  const loadDocument = async () => {
    if (!documentId) return;

    try {
      const doc = await documentCollaborationService.getDocument(documentId);
      setDocument(doc);
      setContent(doc.content);

      const participants = await documentCollaborationService.getDocumentParticipants(documentId);
      setParticipants(participants);

      const changes = await documentCollaborationService.getDocumentChanges(documentId);
      setChanges(changes);

      setIsConnected(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load document');
    }
  };

  const applyChange = (change: DocumentChange) => {
    setContent(prevContent => {
      let newContent = prevContent;
      
      switch (change.type) {
        case 'INSERT':
          newContent = prevContent.slice(0, change.position) + change.text + prevContent.slice(change.position);
          break;
        case 'DELETE':
          newContent = prevContent.slice(0, change.position) + prevContent.slice(change.position + change.length);
          break;
        case 'REPLACE':
          newContent = prevContent.slice(0, change.position) + change.text + prevContent.slice(change.position + change.length);
          break;
      }
      
      return newContent;
    });
  };

  const handleContentChange = (newContent: string) => {
    setContent(newContent);
    
    // Auto-save with debounce
    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current);
    }
    
    saveTimeoutRef.current = setTimeout(() => {
      saveDocument(newContent);
    }, 2000);
  };

  const saveDocument = async (newContent: string) => {
    if (!documentId || !isConnected) return;

    setIsSaving(true);
    
    try {
      // Create change
      const change = documentCollaborationService.createDocumentChange(
        documentId,
        'REPLACE',
        0,
        content.length,
        newContent
      );

      // Apply changes
      const result = await documentCollaborationService.applyChanges(documentId, [change]);
      
      if (result.success) {
        setDocument(result.document);
        setLastSaved(new Date());
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save document');
    } finally {
      setIsSaving(false);
    }
  };

  const getDocumentTypeIcon = (type: string) => {
    return documentCollaborationService.getDocumentTypeIcon(type);
  };

  const getParticipantRoleIcon = (role: string) => {
    return documentCollaborationService.getParticipantRoleIcon(role);
  };

  const getChangeTypeIcon = (type: string) => {
    return documentCollaborationService.getChangeTypeIcon(type);
  };

  const getParticipantLastActivity = (participant: DocumentParticipant) => {
    return documentCollaborationService.getParticipantLastActivity(participant);
  };

  const isParticipantActive = (participant: DocumentParticipant) => {
    return documentCollaborationService.isParticipantActive(participant);
  };

  const getActiveParticipantsCount = () => {
    return documentCollaborationService.getActiveParticipantsCount(documentId || '');
  };

  const getTotalChangesCount = () => {
    return documentCollaborationService.getTotalChangesCount(documentId || '');
  };

  const getDocumentCollaborationSummary = () => {
    return documentCollaborationService.getDocumentCollaborationSummary(documentId || '');
  };

  if (!document) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">Loading document...</span>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg border border-gray-200 h-full flex flex-col">
      {/* Header */}
      <div className="bg-gray-50 border-b border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <span className="text-2xl">{getDocumentTypeIcon(document.type)}</span>
            <div>
              <h3 className="text-lg font-semibold text-gray-900">{document.title}</h3>
              <p className="text-sm text-gray-600">Version {document.version} • {documentCollaborationService.getDocumentWordCount(content)} words</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <div className="flex items-center space-x-1 text-sm text-gray-600">
              <div className="w-2 h-2 bg-green-400 rounded-full"></div>
              <span>{getActiveParticipantsCount()} active</span>
            </div>
            {isSaving && (
              <div className="flex items-center space-x-1 text-sm text-blue-600">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                <span>Saving...</span>
              </div>
            )}
            {lastSaved && (
              <span className="text-sm text-gray-500">
                Saved {lastSaved.toLocaleTimeString()}
              </span>
            )}
            {onClose && (
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Toolbar */}
      <div className="bg-gray-50 border-b border-gray-200 p-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setShowParticipants(!showParticipants)}
              className={`px-3 py-1 text-sm rounded-lg transition-colors ${
                showParticipants ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              👥 Participants ({participants.length})
            </button>
            <button
              onClick={() => setShowChanges(!showChanges)}
              className={`px-3 py-1 text-sm rounded-lg transition-colors ${
                showChanges ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              📝 Changes ({getTotalChangesCount()})
            </button>
          </div>
          <div className="flex items-center space-x-2 text-sm text-gray-600">
            <span>Auto-save: ON</span>
            <span>•</span>
            <span>Real-time sync: ON</span>
          </div>
        </div>
      </div>

      <div className="flex-1 flex">
        {/* Main Editor */}
        <div className="flex-1 flex flex-col">
          <textarea
            ref={editorRef}
            value={content}
            onChange={(e) => handleContentChange(e.target.value)}
            className="flex-1 p-4 border-0 resize-none focus:outline-none focus:ring-0"
            placeholder="Start typing your document..."
            disabled={!isConnected}
          />
        </div>

        {/* Sidebar */}
        {(showParticipants || showChanges) && (
          <div className="w-80 bg-gray-50 border-l border-gray-200 flex flex-col">
            {showParticipants && (
              <div className="flex-1 p-4">
                <h4 className="font-semibold text-gray-900 mb-3">Participants</h4>
                <div className="space-y-2">
                  {participants.map((participant) => (
                    <div
                      key={participant.userId}
                      className="flex items-center space-x-3 p-2 bg-white rounded-lg"
                    >
                      <div className="flex-shrink-0">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                          isParticipantActive(participant) ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                        }`}>
                          {participant.userName.charAt(0).toUpperCase()}
                        </div>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {participant.userName}
                        </p>
                        <p className="text-xs text-gray-500">
                          {getParticipantRoleIcon(participant.role)} {participant.role} • {getParticipantLastActivity(participant)}
                        </p>
                      </div>
                      {isParticipantActive(participant) && (
                        <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {showChanges && (
              <div className="flex-1 p-4">
                <h4 className="font-semibold text-gray-900 mb-3">Recent Changes</h4>
                <div className="space-y-2">
                  {changes.slice(0, 10).map((change) => (
                    <div
                      key={change.id}
                      className="flex items-center space-x-3 p-2 bg-white rounded-lg"
                    >
                      <div className="flex-shrink-0">
                        <span className="text-lg">{getChangeTypeIcon(change.type)}</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm text-gray-900 truncate">
                          {change.text || `${change.type} at position ${change.position}`}
                        </p>
                        <p className="text-xs text-gray-500">
                          {new Date(change.appliedAt).toLocaleTimeString()}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border-t border-red-200 p-4">
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
      )}
    </div>
  );
};

export default DocumentCollaboration;


