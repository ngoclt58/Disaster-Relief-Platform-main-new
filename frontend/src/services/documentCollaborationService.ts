/**
 * Document Collaboration Service
 * Handles real-time document collaboration and editing
 */

import { apiService } from './api';

export interface CollaborativeDocument {
  id: string;
  title: string;
  content: string;
  type: 'EMERGENCY_PLAN' | 'COORDINATION_DOC' | 'TRAINING_MATERIAL' | 'REPORT' | 'MEETING_NOTES';
  creatorId: string;
  creatorName: string;
  createdAt: string;
  updatedAt: string;
  status: 'ACTIVE' | 'ARCHIVED' | 'DELETED';
  version: number;
  permissions: DocumentPermissions;
}

export interface DocumentParticipant {
  userId: string;
  userName: string;
  userEmail: string;
  role: 'OWNER' | 'COLLABORATOR' | 'VIEWER';
  joinedAt: string;
  lastActivity: string;
  changesCount: number;
}

export interface DocumentChange {
  id: string;
  documentId: string;
  type: 'INSERT' | 'DELETE' | 'REPLACE';
  position: number;
  length: number;
  text: string;
  appliedBy: string;
  appliedAt: string;
}

export interface DocumentJoinResult {
  document: CollaborativeDocument;
  participant: DocumentParticipant;
  webSocketUrl: string;
  collaborationConfig: CollaborationConfig;
}

export interface DocumentChangeResult {
  document: CollaborativeDocument;
  appliedChanges: DocumentChange[];
  version: number;
  success: boolean;
}

export interface DocumentPermissions {
  canEdit: boolean;
  canComment: boolean;
  canShare: boolean;
  canDelete: boolean;
  canManagePermissions: boolean;
}

export interface CollaborationConfig {
  documentId: string;
  version: number;
  maxParticipants: number;
  realTimeSync: boolean;
  conflictResolution: boolean;
  autoSave: boolean;
  autoSaveInterval: number;
}

class DocumentCollaborationService {
  private activeDocuments: Map<string, CollaborativeDocument> = new Map();
  private documentParticipants: Map<string, DocumentParticipant[]> = new Map();
  private documentChanges: Map<string, DocumentChange[]> = new Map();
  private changeHandlers: Map<string, ((change: DocumentChange) => void)[]> = new Map();
  private participantHandlers: Map<string, ((participant: DocumentParticipant) => void)[]> = new Map();
  private autoSaveInterval: number | null = null;

  /**
   * Create a new collaborative document
   */
  async createDocument(title: string, content: string, type: string): Promise<CollaborativeDocument> {
    const response = await apiService.post('/document-collaboration/create', {
      title,
      content,
      type
    });

    this.activeDocuments.set(response.id, response);
    return response;
  }

  /**
   * Join a collaborative document
   */
  async joinDocument(documentId: string): Promise<DocumentJoinResult> {
    const response = await apiService.post(`/document-collaboration/${documentId}/join`);
    
    this.activeDocuments.set(documentId, response.document);
    this.documentParticipants.set(documentId, [response.participant]);
    
    return response;
  }

  /**
   * Apply changes to document
   */
  async applyChanges(documentId: string, changes: DocumentChange[]): Promise<DocumentChangeResult> {
    const response = await apiService.post(`/document-collaboration/${documentId}/changes`, {
      changes
    });

    if (response.success) {
      this.activeDocuments.set(documentId, response.document);
      
      // Store changes
      const existingChanges = this.documentChanges.get(documentId) || [];
      this.documentChanges.set(documentId, [...existingChanges, ...response.appliedChanges]);
      
      // Notify change handlers
      this.notifyChangeHandlers(documentId, response.appliedChanges);
    }

    return response;
  }

  /**
   * Get document details
   */
  async getDocument(documentId: string): Promise<CollaborativeDocument> {
    const response = await apiService.get(`/document-collaboration/${documentId}`);
    this.activeDocuments.set(documentId, response);
    return response;
  }

  /**
   * Get document participants
   */
  async getDocumentParticipants(documentId: string): Promise<DocumentParticipant[]> {
    const response = await apiService.get(`/document-collaboration/${documentId}/participants`);
    this.documentParticipants.set(documentId, response);
    return response;
  }

  /**
   * Get document changes history
   */
  async getDocumentChanges(documentId: string, limit: number = 50): Promise<DocumentChange[]> {
    const response = await apiService.get(`/document-collaboration/${documentId}/changes?limit=${limit}`);
    this.documentChanges.set(documentId, response);
    return response;
  }

  /**
   * Update document permissions
   */
  async updatePermissions(documentId: string, permissions: DocumentPermissions): Promise<CollaborativeDocument> {
    const response = await apiService.put(`/document-collaboration/${documentId}/permissions`, permissions);
    this.activeDocuments.set(documentId, response);
    return response;
  }

  /**
   * Leave document
   */
  async leaveDocument(documentId: string): Promise<void> {
    await apiService.post(`/document-collaboration/${documentId}/leave`);
    
    this.activeDocuments.delete(documentId);
    this.documentParticipants.delete(documentId);
    this.documentChanges.delete(documentId);
    this.changeHandlers.delete(documentId);
    this.participantHandlers.delete(documentId);
  }

  /**
   * Get user's documents
   */
  async getUserDocuments(): Promise<CollaborativeDocument[]> {
    const response = await apiService.get<CollaborativeDocument[]>('/document-collaboration/my-documents');
    return response;
  }

  /**
   * Get collaborative documents (alias for getUserDocuments for compatibility)
   */
  async getCollaborativeDocuments(): Promise<CollaborativeDocument[]> {
    return this.getUserDocuments();
  }

  /**
   * Create document change
   */
  createDocumentChange(
    documentId: string,
    type: 'INSERT' | 'DELETE' | 'REPLACE',
    position: number,
    length: number,
    text: string
  ): DocumentChange {
    return {
      id: `change_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      documentId,
      type,
      position,
      length,
      text,
      appliedBy: 'current_user',
      appliedAt: new Date().toISOString()
    };
  }

  /**
   * Add change handler
   */
  addChangeHandler(documentId: string, handler: (change: DocumentChange) => void): void {
    if (!this.changeHandlers.has(documentId)) {
      this.changeHandlers.set(documentId, []);
    }
    this.changeHandlers.get(documentId)!.push(handler);
  }

  /**
   * Remove change handler
   */
  removeChangeHandler(documentId: string, handler: (change: DocumentChange) => void): void {
    const handlers = this.changeHandlers.get(documentId);
    if (handlers) {
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }

  /**
   * Add participant handler
   */
  addParticipantHandler(documentId: string, handler: (participant: DocumentParticipant) => void): void {
    if (!this.participantHandlers.has(documentId)) {
      this.participantHandlers.set(documentId, []);
    }
    this.participantHandlers.get(documentId)!.push(handler);
  }

  /**
   * Remove participant handler
   */
  removeParticipantHandler(documentId: string, handler: (participant: DocumentParticipant) => void): void {
    const handlers = this.participantHandlers.get(documentId);
    if (handlers) {
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }

  /**
   * Start auto-save
   */
  startAutoSave(documentId: string, interval: number = 30000): void {
    this.stopAutoSave();
    
    this.autoSaveInterval = window.setInterval(() => {
      this.autoSaveDocument(documentId);
    }, interval);
  }

  /**
   * Stop auto-save
   */
  stopAutoSave(): void {
    if (this.autoSaveInterval) {
      clearInterval(this.autoSaveInterval);
      this.autoSaveInterval = null;
    }
  }

  /**
   * Auto-save document
   */
  private async autoSaveDocument(documentId: string): Promise<void> {
    try {
      const document = this.activeDocuments.get(documentId);
      if (document) {
        // In real implementation, save document content
        console.log('Auto-saving document:', documentId);
      }
    } catch (error) {
      console.error('Error auto-saving document:', error);
    }
  }

  /**
   * Notify change handlers
   */
  private notifyChangeHandlers(documentId: string, changes: DocumentChange[]): void {
    const handlers = this.changeHandlers.get(documentId);
    if (handlers) {
      changes.forEach(change => {
        handlers.forEach(handler => {
          try {
            handler(change);
          } catch (error) {
            console.error('Error in change handler:', error);
          }
        });
      });
    }
  }

  /**
   * Notify participant handlers
   */
  private notifyParticipantHandlers(documentId: string, participant: DocumentParticipant): void {
    const handlers = this.participantHandlers.get(documentId);
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(participant);
        } catch (error) {
          console.error('Error in participant handler:', error);
        }
      });
    }
  }

  /**
   * Get active document
   */
  getActiveDocument(documentId: string): CollaborativeDocument | null {
    return this.activeDocuments.get(documentId) || null;
  }

  /**
   * Get cached document participants (synchronous, returns cached data)
   */
  getCachedDocumentParticipants(documentId: string): DocumentParticipant[] {
    return this.documentParticipants.get(documentId) || [];
  }

  /**
   * Get cached document changes (synchronous, returns cached data)
   */
  getCachedDocumentChanges(documentId: string): DocumentChange[] {
    return this.documentChanges.get(documentId) || [];
  }

  /**
   * Check if user can edit document
   */
  canEditDocument(documentId: string, userId: string): boolean {
    const document = this.activeDocuments.get(documentId);
    if (!document) return false;

    const participant = this.documentParticipants.get(documentId)?.find(p => p.userId === userId);
    if (!participant) return false;

    if (participant.role === 'OWNER') return true;
    if (participant.role === 'COLLABORATOR' && document.permissions.canEdit) return true;
    
    return false;
  }

  /**
   * Check if user can comment on document
   */
  canCommentOnDocument(documentId: string, userId: string): boolean {
    const document = this.activeDocuments.get(documentId);
    if (!document) return false;

    const participant = this.documentParticipants.get(documentId)?.find(p => p.userId === userId);
    if (!participant) return false;

    return document.permissions.canComment;
  }

  /**
   * Check if user can share document
   */
  canShareDocument(documentId: string, userId: string): boolean {
    const document = this.activeDocuments.get(documentId);
    if (!document) return false;

    const participant = this.documentParticipants.get(documentId)?.find(p => p.userId === userId);
    if (!participant) return false;

    return document.permissions.canShare;
  }

  /**
   * Get document type icon
   */
  getDocumentTypeIcon(type: string): string {
    switch (type) {
      case 'EMERGENCY_PLAN': return '🚨';
      case 'COORDINATION_DOC': return '📋';
      case 'TRAINING_MATERIAL': return '📚';
      case 'REPORT': return '📊';
      case 'MEETING_NOTES': return '📝';
      default: return '📄';
    }
  }

  /**
   * Get document status color
   */
  getDocumentStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return '#10b981'; // green
      case 'ARCHIVED': return '#6b7280'; // gray
      case 'DELETED': return '#ef4444'; // red
      default: return '#6b7280'; // gray
    }
  }

  /**
   * Get participant role icon
   */
  getParticipantRoleIcon(role: string): string {
    switch (role) {
      case 'OWNER': return '👑';
      case 'COLLABORATOR': return '✏️';
      case 'VIEWER': return '👁️';
      default: return '👤';
    }
  }

  /**
   * Get change type icon
   */
  getChangeTypeIcon(type: string): string {
    switch (type) {
      case 'INSERT': return '➕';
      case 'DELETE': return '➖';
      case 'REPLACE': return '🔄';
      default: return '📝';
    }
  }

  /**
   * Format document size
   */
  formatDocumentSize(content: string): string {
    const size = new Blob([content]).size;
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  /**
   * Get document word count
   */
  getDocumentWordCount(content: string): number {
    return content.trim().split(/\s+/).filter(word => word.length > 0).length;
  }

  /**
   * Get document character count
   */
  getDocumentCharacterCount(content: string): number {
    return content.length;
  }

  /**
   * Get document line count
   */
  getDocumentLineCount(content: string): number {
    return content.split('\n').length;
  }

  /**
   * Get document reading time
   */
  getDocumentReadingTime(content: string): string {
    const wordCount = this.getDocumentWordCount(content);
    const readingTime = Math.ceil(wordCount / 200); // 200 words per minute
    return `${readingTime} min read`;
  }

  /**
   * Get document last activity
   */
  getDocumentLastActivity(document: CollaborativeDocument): string {
    const updatedAt = new Date(document.updatedAt);
    const now = new Date();
    const diff = now.getTime() - updatedAt.getTime();
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  }

  /**
   * Get participant last activity
   */
  getParticipantLastActivity(participant: DocumentParticipant): string {
    const lastActivity = new Date(participant.lastActivity);
    const now = new Date();
    const diff = now.getTime() - lastActivity.getTime();
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    
    if (minutes < 1) return 'Active now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return 'Offline';
  }

  /**
   * Check if participant is active
   */
  isParticipantActive(participant: DocumentParticipant): boolean {
    const lastActivity = new Date(participant.lastActivity);
    const now = new Date();
    const diff = now.getTime() - lastActivity.getTime();
    
    return diff < 300000; // 5 minutes
  }

  /**
   * Get active participants count
   */
  getActiveParticipantsCount(documentId: string): number {
    const participants = this.documentParticipants.get(documentId) || [];
    return participants.filter(p => this.isParticipantActive(p)).length;
  }

  /**
   * Get total changes count
   */
  getTotalChangesCount(documentId: string): number {
    const changes = this.documentChanges.get(documentId) || [];
    return changes.length;
  }

  /**
   * Get changes count by user
   */
  getChangesCountByUser(documentId: string, userId: string): number {
    const changes = this.documentChanges.get(documentId) || [];
    return changes.filter(c => c.appliedBy === userId).length;
  }

  /**
   * Get document collaboration summary
   */
  getDocumentCollaborationSummary(documentId: string): {
    totalParticipants: number;
    activeParticipants: number;
    totalChanges: number;
    lastActivity: string;
  } {
    const participants = this.documentParticipants.get(documentId) || [];
    const changes = this.documentChanges.get(documentId) || [];
    const document = this.activeDocuments.get(documentId);
    
    return {
      totalParticipants: participants.length,
      activeParticipants: participants.filter(p => this.isParticipantActive(p)).length,
      totalChanges: changes.length,
      lastActivity: document ? this.getDocumentLastActivity(document) : 'Unknown'
    };
  }
}

export const documentCollaborationService = new DocumentCollaborationService();


