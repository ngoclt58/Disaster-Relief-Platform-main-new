import { useAuthStore } from '../store/authStore';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Type definitions for API responses
export interface LoginResponse {
  user: any;
  accessToken: string;
}

export interface Profile {
  fullName?: string;
  email?: string;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: string;
  preferredLanguage?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  householdSize?: number;
  specialNeeds?: string;
  latitude?: number;
  longitude?: number;
  consentToContact?: boolean;
  consentToShare?: boolean;
}

export interface DedupeLink {
  id: string;
  requestId: string;
  entityId: string;
  score?: number;
  reason?: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

class ApiService {
  // Expose baseUrl and getHeaders for services that need them
  get baseUrl(): string {
    return API_BASE_URL;
  }

  getHeaders(): HeadersInit {
    const { token } = useAuthStore.getState();
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  }


  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorText = '';
      let errorMessage = '';
      try {
        errorText = await response.text();
        // Check if response is HTML (error page)
        if (errorText.trim().startsWith('<!DOCTYPE') || errorText.trim().startsWith('<html')) {
          throw new Error(`API Error: ${response.status} - Server returned HTML instead of JSON. Backend may not be running or endpoint not found.`);
        }
        // Try to parse as JSON to extract message
        try {
          const errorJson = JSON.parse(errorText);
          errorMessage = errorJson.message || errorJson.error || errorText;
        } catch {
          // Not JSON, use raw text
          errorMessage = errorText;
        }
      } catch (e) {
        if (e instanceof Error) {
          throw e;
        }
        errorMessage = `Failed to read error response: ${e}`;
      }
      const error = new Error(errorMessage);
      (error as any).status = response.status;
      (error as any).originalError = errorText;
      throw error;
    }
    
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        return await response.json();
      } catch (e) {
        const text = await response.text();
        if (text.trim().startsWith('<!DOCTYPE') || text.trim().startsWith('<html')) {
          throw new Error('Server returned HTML instead of JSON. Backend may not be running or endpoint not found.');
        }
        throw new Error(`Failed to parse JSON response: ${e}`);
      }
    }
    
    // If not JSON, return as text
    return response.text() as unknown as T;
  }

  // Auth APIs
  async login(emailOrPhone: string, password: string): Promise<LoginResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ emailOrPhone, password })
      });
      return await this.handleResponse<LoginResponse>(response);
    } catch (error: any) {
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      }
      throw error;
    }
  }

  async register(userData: any) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    return this.handleResponse(response);
  }

  async refreshToken() {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Resident APIs
  async updateProfile(profileData: any) {
    const response = await fetch(`${API_BASE_URL}/resident/profile`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(profileData)
    });
    return this.handleResponse(response);
  }

  async createNeed(needData: any) {
    const response = await fetch(`${API_BASE_URL}/resident/needs`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(needData)
    });
    return this.handleResponse(response);
  }

  async getMyNeeds() {
    const response = await fetch(`${API_BASE_URL}/resident/needs`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Media APIs
  async getPresignedUploadUrl(objectName: string, contentType: string): Promise<{ url: string; uploadUrl?: string; [key: string]: any }> {
    const response = await fetch(`${API_BASE_URL}/media/presign?objectName=${encodeURIComponent(objectName)}&contentType=${encodeURIComponent(contentType)}`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    const data = await this.handleResponse<{ uploadUrl: string; url?: string; [key: string]: any }>(response);
    // Backend returns 'uploadUrl', but frontend expects 'url', so map it
    // data.uploadUrl is guaranteed to be a string from backend response, so use it directly
    return {
      ...data,
      url: data.uploadUrl
    };
  }

  async uploadToMinIO(url: string, file: File) {
    const response = await fetch(url, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type
      }
    });
    if (!response.ok) {
      throw new Error('Upload failed');
    }
    return response;
  }

  // Requests APIs
  async getRequests(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value));
      }
    });
    
    try {
      const response = await fetch(`${API_BASE_URL}/requests?${params}`, {
        headers: this.getHeaders()
      });
      const data = await this.handleResponse(response);
      // cache in idb
      try {
        const { putAll } = await import('./idb');
        if (Array.isArray((data as any).content)) {
          await putAll('requests' as any, (data as any).content);
        }
      } catch {}
      return data;
    } catch (e) {
      // offline fallback
      const { getAll } = await import('./idb');
      const cached = await getAll<any>('requests' as any);
      return { content: cached } as any;
    }
  }

  // Task APIs
  async getMyTasks(page = 0, size = 20): Promise<PagedResponse<any>> {
    const response = await fetch(`${API_BASE_URL}/tasks/mine?page=${page}&size=${size}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<PagedResponse<any>>(response);
  }

  async createTask(requestId: string, assigneeId?: string, plannedKitCode?: string) {
    const response = await fetch(`${API_BASE_URL}/tasks`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ requestId, assigneeId, plannedKitCode })
    });
    return this.handleResponse(response);
  }

  async claimTask(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}:claim`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async assignTask(taskId: string, assigneeId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}:assign`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ assigneeId })
    });
    return this.handleResponse(response);
  }

  async updateTaskStatus(taskId: string, status: string, eta?: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
      method: 'PATCH',
      headers: this.getHeaders(),
      body: JSON.stringify({ status, eta })
    });
    return this.handleResponse(response);
  }

  // Delivery APIs
  async createDelivery(deliveryData: any) {
    const response = await fetch(`${API_BASE_URL}/deliveries`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(deliveryData)
    });
    return this.handleResponse(response);
  }

  async getDeliveryByTask(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/deliveries/task/${taskId}`, {
      headers: this.getHeaders()
    });
    if (response.status === 404) {
      return null;
    }
    return this.handleResponse(response);
  }

  // Inventory APIs
  async getInventoryHubs() {
    const response = await fetch(`${API_BASE_URL}/inventory/hubs`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getInventoryItems() {
    const response = await fetch(`${API_BASE_URL}/inventory/items`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getInventoryStock(hubId?: string, itemId?: string) {
    const params = new URLSearchParams();
    if (hubId) params.append('hub', hubId);
    if (itemId) params.append('item', itemId);
    
    const response = await fetch(`${API_BASE_URL}/inventory/stock?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateStock(hubId: string, itemId: string, qtyAvailable?: number, qtyReserved?: number, reason?: string) {
    const response = await fetch(`${API_BASE_URL}/inventory/stock`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ hubId, itemId, qtyAvailable, qtyReserved, reason })
    });
    return this.handleResponse(response);
  }

  async reserveStock(hubId: string, itemId: string, quantity: number) {
    const response = await fetch(`${API_BASE_URL}/inventory/reserve`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ hubId, itemId, quantity })
    });
    return this.handleResponse(response);
  }

  async releaseStock(hubId: string, itemId: string, quantity: number) {
    const response = await fetch(`${API_BASE_URL}/inventory/release`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ hubId, itemId, quantity })
    });
    return this.handleResponse(response);
  }

  async getStockMovements(hubId?: string, itemId?: string) {
    const params = new URLSearchParams();
    if (hubId) params.append('hub', hubId);
    if (itemId) params.append('item', itemId);
    
    const response = await fetch(`${API_BASE_URL}/inventory/movements?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Dedupe APIs
  async createDedupeGroup(payload: { entityType: string; note?: string; links: { entityId: string; score?: number; reason?: string }[] }) {
    const response = await fetch(`${API_BASE_URL}/dedupe/groups`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(payload)
    });
    return this.handleResponse(response);
  }

  async getDedupeGroupLinks(groupId: string): Promise<DedupeLink[]> {
    const response = await fetch(`${API_BASE_URL}/dedupe/groups/${groupId}/links`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<DedupeLink[]>(response);
  }

  // Admin / analytics APIs
  async getAdminStats(): Promise<any> {
    const response = await fetch(`${API_BASE_URL}/admin/stats`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<any>(response);
  }

  async getAnalyticsOverview(startDate: string, endDate: string): Promise<any>;
  async getAnalyticsOverview(startDate?: string, endDate?: string): Promise<any>;
  async getAnalyticsOverview(startDate?: string, endDate?: string): Promise<any> {
    const params = new URLSearchParams();
    if (startDate) params.append('start', startDate);
    if (endDate) params.append('end', endDate);
    const response = await fetch(`${API_BASE_URL}/analytics/overview?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<any>(response);
  }

  async getNeedsTrends(startDate: string, endDate: string, granularity: string): Promise<any>;
  async getNeedsTrends(startDate?: string, endDate?: string, granularity?: string): Promise<any>;
  async getNeedsTrends(startDate?: string, endDate?: string, granularity = 'day'): Promise<any> {
    const params = new URLSearchParams();
    if (startDate) params.append('start', startDate);
    if (endDate) params.append('end', endDate);
    params.append('granularity', granularity);
    const response = await fetch(`${API_BASE_URL}/analytics/needs/trends?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<any>(response);
  }

  // -------- Generic helpers used by other services (video conferencing, trend analysis, etc.) --------

  async get<T = any>(path: string, queryParams?: Record<string, any>): Promise<T> {
    try {
      let url = `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
      if (queryParams) {
        const params = new URLSearchParams();
        Object.entries(queryParams).forEach(([key, value]) => {
          if (value !== undefined && value !== null && value !== '') {
            params.append(key, String(value));
          }
        });
        const queryString = params.toString();
        if (queryString) {
          url += (url.includes('?') ? '&' : '?') + queryString;
        }
      }
      const response = await fetch(url, {
        method: 'GET',
        headers: this.getHeaders()
      });
      return await this.handleResponse<T>(response);
    } catch (error: any) {
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      }
      throw error;
    }
  }

  async post<T = any>(path: string, body?: any, options?: { params?: Record<string, any>; headers?: HeadersInit }): Promise<T> {
    try {
      let url = `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
      if (options?.params) {
        const params = new URLSearchParams();
        Object.entries(options.params).forEach(([key, value]) => {
          if (value !== undefined && value !== null && value !== '') {
            params.append(key, String(value));
          }
        });
        const queryString = params.toString();
        if (queryString) {
          url += (url.includes('?') ? '&' : '?') + queryString;
        }
      }
      const headers = { ...this.getHeaders(), ...options?.headers };
      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined
      });
      return await this.handleResponse<T>(response);
    } catch (error: any) {
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      }
      throw error;
    }
  }

  async put<T = any>(path: string, body?: any): Promise<T> {
    try {
      const response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
        method: 'PUT',
        headers: this.getHeaders(),
        body: body !== undefined ? JSON.stringify(body) : undefined
      });
      return await this.handleResponse<T>(response);
    } catch (error: any) {
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      }
      throw error;
    }
  }

  async delete<T = any>(path: string): Promise<T> {
    try {
      const response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
        method: 'DELETE',
        headers: this.getHeaders()
      });
      // Many delete endpoints return no content; handle that gracefully
      if (response.status === 204 || response.status === 202 || (response.status === 200 && !response.headers.get('content-type'))) {
        return undefined as T;
      }
      return await this.handleResponse<T>(response);
    } catch (error: any) {
      if (error.message?.includes('Failed to fetch') || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      }
      throw error;
    }
  }

  async patch<T = any>(path: string, body?: any): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
      method: 'PATCH',
      headers: this.getHeaders(),
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
    return this.handleResponse<T>(response);
  }

  async mergeDedupeGroup(groupId: string, canonicalId: string) {
    const response = await fetch(`${API_BASE_URL}/dedupe/groups/${groupId}/merge`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ canonicalId })
    });
    return this.handleResponse(response);
  }

  async dismissDedupeGroup(groupId: string, reason?: string) {
    const response = await fetch(`${API_BASE_URL}/dedupe/groups/${groupId}/dismiss`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ reason })
    });
    return this.handleResponse(response);
  }

  // AI Categorization APIs
  async categorizeRequest(description: string, currentType?: string, currentSeverity?: number) {
    const response = await fetch(`${API_BASE_URL}/ai/categorization/analyze`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ description, currentType, currentSeverity })
    });
    return this.handleResponse(response);
  }

  async batchCategorize(requests: Array<{ id: string; description: string; currentType?: string; currentSeverity?: number }>) {
    const response = await fetch(`${API_BASE_URL}/ai/categorization/batch-analyze`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ requests })
    });
    return this.handleResponse(response);
  }

  // Workflow Orchestration APIs
  async executeWorkflow(requestId: string, workflowType: string) {
    const response = await fetch(`${API_BASE_URL}/workflows/execute`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ requestId, workflowType })
    });
    return this.handleResponse(response);
  }

  async getWorkflowTemplates() {
    const response = await fetch(`${API_BASE_URL}/workflows/templates`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWorkflowTemplate(templateName: string) {
    const response = await fetch(`${API_BASE_URL}/workflows/templates/${templateName}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWorkflowExecutionStatus(executionId: string) {
    const response = await fetch(`${API_BASE_URL}/workflows/executions/${executionId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Smart Notifications APIs
  async getUserNotificationPreferences() {
    const response = await fetch(`${API_BASE_URL}/notifications/preferences`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateUserNotificationPreferences(preferences: any) {
    const response = await fetch(`${API_BASE_URL}/notifications/preferences`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(preferences)
    });
    return this.handleResponse(response);
  }

  async sendSmartNotification(requestId: string, eventType: string, message: string) {
    const response = await fetch(`${API_BASE_URL}/notifications/send`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ requestId, eventType, message })
    });
    return this.handleResponse(response);
  }

  async getNotificationHistory(hours: number = 24) {
    const response = await fetch(`${API_BASE_URL}/notifications/history?hours=${hours}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Auto-Escalation APIs
  async getEscalationRules() {
    const response = await fetch(`${API_BASE_URL}/escalations/rules`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getEscalationRule(ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/escalations/rules/${ruleId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async triggerEscalationCheck() {
    const response = await fetch(`${API_BASE_URL}/escalations/trigger`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getEscalationStatus() {
    const response = await fetch(`${API_BASE_URL}/escalations/status`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createEscalationRule(rule: any) {
    const response = await fetch(`${API_BASE_URL}/escalations/rules`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(rule)
    });
    return this.handleResponse(response);
  }

  async updateEscalationRule(ruleId: string, rule: any) {
    const response = await fetch(`${API_BASE_URL}/escalations/rules/${ruleId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(rule)
    });
    return this.handleResponse(response);
  }

  async deleteEscalationRule(ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/escalations/rules/${ruleId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Advanced Task Management APIs
  async createDynamicTasks(requestId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/create-dynamic?requestId=${requestId}`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Communication & Collaboration APIs
  // Video Conferencing
  async createVideoConference(title: string, description: string, type: string) {
    const response = await fetch(`${API_BASE_URL}/video-conference/create`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ title, description, type })
    });
    return this.handleResponse(response);
  }

  async joinVideoConference(conferenceId: string) {
    const response = await fetch(`${API_BASE_URL}/video-conference/${conferenceId}/join`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async leaveVideoConference(conferenceId: string) {
    const response = await fetch(`${API_BASE_URL}/video-conference/${conferenceId}/leave`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getVideoConferences() {
    const response = await fetch(`${API_BASE_URL}/video-conference/my-conferences`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getVideoConferenceParticipants(conferenceId: string) {
    const response = await fetch(`${API_BASE_URL}/video-conference/${conferenceId}/participants`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateVideoConferenceSettings(conferenceId: string, settings: any) {
    const response = await fetch(`${API_BASE_URL}/video-conference/${conferenceId}/settings`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(settings)
    });
    return this.handleResponse(response);
  }

  async endVideoConference(conferenceId: string) {
    const response = await fetch(`${API_BASE_URL}/video-conference/${conferenceId}/end`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Chat Bot
  async sendChatBotMessage(sessionId: string, message: string) {
    const response = await fetch(`${API_BASE_URL}/chatbot/message`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ sessionId, message })
    });
    return this.handleResponse(response);
  }

  async getChatBotSessions() {
    const response = await fetch(`${API_BASE_URL}/chatbot/sessions`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getChatBotSession(sessionId: string) {
    const response = await fetch(`${API_BASE_URL}/chatbot/sessions/${sessionId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async endChatBotSession(sessionId: string) {
    const response = await fetch(`${API_BASE_URL}/chatbot/sessions/${sessionId}/end`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Translation
  async translateText(text: string, sourceLanguage: string, targetLanguage: string) {
    const response = await fetch(`${API_BASE_URL}/translation/translate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ text, sourceLanguage, targetLanguage })
    });
    return this.handleResponse(response);
  }

  async translateBatch(requests: any[]) {
    const response = await fetch(`${API_BASE_URL}/translation/translate-batch`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(requests)
    });
    return this.handleResponse(response);
  }

  async detectLanguage(text: string) {
    const response = await fetch(`${API_BASE_URL}/translation/detect-language`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ text })
    });
    return this.handleResponse(response);
  }

  async translateWithAutoDetection(text: string, targetLanguage: string) {
    const response = await fetch(`${API_BASE_URL}/translation/translate-auto`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ text, targetLanguage })
    });
    return this.handleResponse(response);
  }

  async getSupportedLanguages() {
    const response = await fetch(`${API_BASE_URL}/translation/languages`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getLanguageSupport(languageCode: string) {
    const response = await fetch(`${API_BASE_URL}/translation/languages/${languageCode}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTranslationStatistics() {
    const response = await fetch(`${API_BASE_URL}/translation/statistics`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async clearTranslationCache() {
    const response = await fetch(`${API_BASE_URL}/translation/cache`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Document Collaboration
  async createCollaborativeDocument(title: string, content: string, type: string) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/create`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ title, content, type })
    });
    return this.handleResponse(response);
  }

  async joinCollaborativeDocument(documentId: string) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/join`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async applyDocumentChanges(documentId: string, changes: any[]) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/changes`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ changes })
    });
    return this.handleResponse(response);
  }

  async getCollaborativeDocument(documentId: string) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getDocumentParticipants(documentId: string) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/participants`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getDocumentChanges(documentId: string, limit: number = 50) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/changes?limit=${limit}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateDocumentPermissions(documentId: string, permissions: any) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/permissions`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(permissions)
    });
    return this.handleResponse(response);
  }

  async leaveCollaborativeDocument(documentId: string) {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/${documentId}/leave`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCollaborativeDocuments() {
    const response = await fetch(`${API_BASE_URL}/document-collaboration/my-documents`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createTasksFromPatterns(requestId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/create-from-patterns?requestId=${requestId}`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async findSkillMatch(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/match-skills?taskId=${taskId}`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async autoAssignTasks() {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/auto-assign`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTaskDependencies(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/dependencies/${taskId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCriticalPath(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/dependencies/${taskId}/critical-path`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getReadyToStartTasks() {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/dependencies/ready-to-start`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async validateDependencies(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/dependencies/${taskId}/validate`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTaskWorkflow(taskId: string) {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/workflow/${taskId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTaskPerformanceAnalytics(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/analytics?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getPerformanceDashboard() {
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/analytics/dashboard`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getHelperPerformance(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/analytics/helpers?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTaskTypePerformance(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/analytics/task-types?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getPerformanceTrends(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/tasks/advanced/analytics/trends?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Admin APIs (getAdminStats removed - using overloaded version above)

  async getAllUsers(search?: string, role?: string, page = 0, size = 20) {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (role) params.append('role', role);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await fetch(`${API_BASE_URL}/admin/users?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getUserById(id: string) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createUser(userData: any) {
    const response = await fetch(`${API_BASE_URL}/admin/users`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(userData)
    });
    return this.handleResponse(response);
  }

  async updateUser(id: string, userData: any) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(userData)
    });
    return this.handleResponse(response);
  }

  async deleteUser(id: string) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateUserRole(id: string, role: string) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/roles`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ role })
    });
    return this.handleResponse(response);
  }

  async activateUser(id: string) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/activate`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async deactivateUser(id: string) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/deactivate`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getAuditLogs(action?: string, userId?: string, page = 0, size = 20) {
    const params = new URLSearchParams();
    if (action) params.append('action', action);
    if (userId) params.append('userId', userId);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await fetch(`${API_BASE_URL}/admin/audit-logs?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getSystemHealth() {
    const response = await fetch(`${API_BASE_URL}/admin/system-health`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async toggleMaintenanceMode(enabled: boolean) {
    const response = await fetch(`${API_BASE_URL}/admin/system/maintenance`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ enabled })
    });
    return this.handleResponse(response);
  }

  // Analytics APIs (getAnalyticsOverview and getNeedsTrends removed - using overloaded versions above)

  async getTaskPerformance(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/tasks/performance?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getUserActivity(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/users/activity?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getInventoryStatus() {
    const response = await fetch(`${API_BASE_URL}/analytics/inventory/status`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getResponseTimes(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/response-times?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getGeographicDistribution(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/geographic/distribution?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getSeverityBreakdown(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/severity/breakdown?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async exportAnalytics(format: string, startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    params.append('format', format);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/analytics/export?${params}`, {
      headers: this.getHeaders()
    });
    return response.arrayBuffer();
  }

  // Resident Profile APIs
  async getMyProfile(): Promise<Profile> {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<Profile>(response);
  }

  async updateMyProfile(payload: {
    fullName?: string;
    phone?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    dateOfBirth?: string; // ISO date
    gender?: string;
    preferredLanguage?: string;
    emergencyContactName?: string;
    emergencyContactPhone?: string;
    householdSize?: number;
    specialNeeds?: string;
    latitude?: number;
    longitude?: number;
    consentToContact?: boolean;
    consentToShare?: boolean;
  }): Promise<Profile> {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(payload)
    });
    return this.handleResponse<Profile>(response);
  }

  // Financial Management APIs
  // Budget Tracking
  async createBudget(name: string, description: string, totalAmount: number, category: string, startDate: string, endDate: string) {
    const response = await fetch(`${API_BASE_URL}/budget-tracking/create`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, totalAmount, category, startDate, endDate })
    });
    return this.handleResponse(response);
  }

  async recordBudgetTransaction(budgetId: string, description: string, amount: number, type: string, category: string, referenceId?: string) {
    const response = await fetch(`${API_BASE_URL}/budget-tracking/${budgetId}/transactions`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ description, amount, type, category, referenceId })
    });
    return this.handleResponse(response);
  }

  async getBudget(budgetId: string) {
    const response = await fetch(`${API_BASE_URL}/budget-tracking/${budgetId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getUserBudgets() {
    const response = await fetch(`${API_BASE_URL}/budget-tracking/my-budgets`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getBudgetSummary(budgetId: string) {
    const response = await fetch(`${API_BASE_URL}/budget-tracking/${budgetId}/summary`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Donation Management
  async recordDonation(donorId: string, amount: number, type: string, description: string, campaignId?: string, referenceId?: string) {
    const response = await fetch(`${API_BASE_URL}/donation-management/donations`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ donorId, amount, type, description, campaignId, referenceId })
    });
    return this.handleResponse(response);
  }

  async registerDonor(name: string, email: string, phone?: string, address?: string, organization?: string, type: string = 'INDIVIDUAL') {
    const response = await fetch(`${API_BASE_URL}/donation-management/donors`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, email, phone, address, organization, type })
    });
    return this.handleResponse(response);
  }

  async getDonations(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/donation-management/donations?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getDonationSummary(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/donation-management/summary?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Cost Analysis
  async performCostAnalysis(category: string, startDate: string, endDate: string, filters?: any) {
    const response = await fetch(`${API_BASE_URL}/cost-analysis/analyze`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ category, startDate, endDate, filters })
    });
    return this.handleResponse(response);
  }

  async getCostBreakdown(category: string, startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const response = await fetch(`${API_BASE_URL}/cost-analysis/breakdown/${category}?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCostCategories() {
    const response = await fetch(`${API_BASE_URL}/cost-analysis/categories`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Financial Reporting
  async generateFinancialReport(type: string, format: string, startDate: string, endDate: string, filters?: any) {
    const response = await fetch(`${API_BASE_URL}/financial-reporting/reports`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ type, format, startDate, endDate, filters })
    });
    return this.handleResponse(response);
  }

  async getFinancialReports(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/financial-reporting/reports?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getFinancialDashboard(startDate?: string, endDate?: string) {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const response = await fetch(`${API_BASE_URL}/financial-reporting/dashboard?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Training & Simulation APIs
  // Disaster Simulation
  async createSimulationScenario(name: string, description: string, disasterType: string, difficulty: string, location: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/disaster-simulation/scenarios`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, disasterType, difficulty, location, parameters })
    });
    return this.handleResponse(response);
  }

  async startSimulationSession(scenarioId: string, sessionName: string, participantIds: string[]) {
    const response = await fetch(`${API_BASE_URL}/disaster-simulation/sessions`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ scenarioId, sessionName, participantIds })
    });
    return this.handleResponse(response);
  }

  async getSimulationScenarios(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/disaster-simulation/scenarios?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Knowledge Base
  async createKnowledgeArticle(title: string, content: string, category: string, tags: string, language: string, isPublic: boolean) {
    const response = await fetch(`${API_BASE_URL}/knowledge-base/articles`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ title, content, category, tags, language, isPublic })
    });
    return this.handleResponse(response);
  }

  async searchKnowledgeArticles(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          value.forEach(v => params.append(key, v.toString()));
        } else {
          params.append(key, value.toString());
        }
      }
    });
    const response = await fetch(`${API_BASE_URL}/knowledge-base/articles?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getKnowledgeCategories() {
    const response = await fetch(`${API_BASE_URL}/knowledge-base/categories`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Certification Tracking
  async createCertification(name: string, description: string, category: string, issuingOrganization: string, validityMonths: number, requiredSkills: string[], requirements: any) {
    const response = await fetch(`${API_BASE_URL}/certification-tracking/certifications`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, category, issuingOrganization, validityMonths, requiredSkills, requirements })
    });
    return this.handleResponse(response);
  }

  async assignCertification(userId: string, certificationId: string, assignedDate: string, expiryDate: string) {
    const response = await fetch(`${API_BASE_URL}/certification-tracking/user-certifications`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ userId, certificationId, assignedDate, expiryDate })
    });
    return this.handleResponse(response);
  }

  async getUserCertifications(userId: string, status?: string) {
    const params = new URLSearchParams();
    params.append('userId', userId);
    if (status) params.append('status', status);
    const response = await fetch(`${API_BASE_URL}/certification-tracking/user-certifications?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Performance Assessment
  async createAssessment(name: string, description: string, assessmentType: string, category: string, criteria: any) {
    const response = await fetch(`${API_BASE_URL}/performance-assessment/assessments`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, assessmentType, category, criteria })
    });
    return this.handleResponse(response);
  }

  async startAssessment(assessmentId: string, sessionName: string, context: any) {
    const response = await fetch(`${API_BASE_URL}/performance-assessment/sessions`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ assessmentId, sessionName, context })
    });
    return this.handleResponse(response);
  }

  async getAssessments(filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/performance-assessment/assessments?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Integration & Interoperability APIs
  // Government API Integration
  async getGovernmentDisasterData(region: string, disasterType: string) {
    const response = await fetch(`${API_BASE_URL}/integration/government/disasters?region=${region}&disasterType=${disasterType}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getGovernmentAlerts(region: string) {
    const response = await fetch(`${API_BASE_URL}/integration/government/alerts?region=${region}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getGovernmentResources(region: string, resourceType: string) {
    const response = await fetch(`${API_BASE_URL}/integration/government/resources?region=${region}&resourceType=${resourceType}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Weather Services Integration
  async getCurrentWeather(location: string, units: string = 'metric') {
    const response = await fetch(`${API_BASE_URL}/integration/weather/current?location=${location}&units=${units}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWeatherForecast(location: string, days: number = 7, units: string = 'metric') {
    const response = await fetch(`${API_BASE_URL}/integration/weather/forecast?location=${location}&days=${days}&units=${units}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWeatherAlerts(location: string, severity?: string) {
    const params = new URLSearchParams();
    params.append('location', location);
    if (severity) params.append('severity', severity);
    const response = await fetch(`${API_BASE_URL}/integration/weather/alerts?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Social Media Monitoring
  async searchSocialMediaPosts(query: string, platform?: string, limit: number = 50) {
    const params = new URLSearchParams();
    params.append('query', query);
    if (platform) params.append('platform', platform);
    params.append('limit', limit.toString());
    const response = await fetch(`${API_BASE_URL}/integration/social-media/posts/search?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getSocialMediaAnalytics(platform: string, timeRange: string) {
    const response = await fetch(`${API_BASE_URL}/integration/social-media/analytics?platform=${platform}&timeRange=${timeRange}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // IoT Device Integration
  async getIoTDevices(deviceType?: string, status?: string) {
    const params = new URLSearchParams();
    if (deviceType) params.append('deviceType', deviceType);
    if (status) params.append('status', status);
    const response = await fetch(`${API_BASE_URL}/integration/iot/devices?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getIoTDeviceData(deviceId: string, dataType: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/integration/iot/devices/${deviceId}/data?dataType=${dataType}&startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async controlIoTDevice(deviceId: string, action: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/integration/iot/devices/${deviceId}/control`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ action, parameters })
    });
    return this.handleResponse(response);
  }

  // Logistics Integration
  async getLogisticsQuote(origin: string, destination: string, items: any[], serviceType: string) {
    const response = await fetch(`${API_BASE_URL}/integration/logistics/quotes`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ origin, destination, items, serviceType })
    });
    return this.handleResponse(response);
  }

  async createLogisticsShipment(quoteId: string, recipientName: string, recipientAddress: string, recipientPhone: string, specialInstructions: any) {
    const response = await fetch(`${API_BASE_URL}/integration/logistics/shipments`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ quoteId, recipientName, recipientAddress, recipientPhone, specialInstructions })
    });
    return this.handleResponse(response);
  }

  async getLogisticsTracking(shipmentId: string) {
    const response = await fetch(`${API_BASE_URL}/integration/logistics/shipments/${shipmentId}/tracking`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getLogisticsProviders(serviceType: string, region: string) {
    const response = await fetch(`${API_BASE_URL}/integration/logistics/providers?serviceType=${serviceType}&region=${region}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Advanced Analytics & Intelligence APIs
  // Custom Dashboards
  async createCustomDashboard(name: string, description: string, userId: string, userRole: string, isPublic: boolean, layout: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, userId, userRole, isPublic, layout })
    });
    return this.handleResponse(response);
  }

  async getCustomDashboards(userId: string, userRole: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards?userId=${userId}&userRole=${userRole}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCustomDashboard(dashboardId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async addDashboardWidget(dashboardId: string, widgetType: string, title: string, configuration: any, position: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}/widgets`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ widgetType, title, configuration, position })
    });
    return this.handleResponse(response);
  }

  async getDashboardWidgets(dashboardId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}/widgets`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWidgetData(widgetId: string, filters: any = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/widgets/${widgetId}/data?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Advanced Reporting
  async createAdvancedReport(name: string, description: string, reportType: string, userId: string, dataSources: any[], configuration: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, reportType, userId, dataSources, configuration })
    });
    return this.handleResponse(response);
  }

  async getAdvancedReports(userId: string, reportType?: string) {
    const params = new URLSearchParams();
    params.append('userId', userId);
    if (reportType) params.append('reportType', reportType);
    const response = await fetch(`${API_BASE_URL}/analytics/reports?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async executeReport(reportId: string, parameters: any, userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}/execute`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ parameters, userId })
    });
    return this.handleResponse(response);
  }

  async getReportResult(executionId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/executions/${executionId}/result`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Data Mining
  async createMiningJob(name: string, description: string, algorithm: string, dataSources: string[], parameters: any, userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, algorithm, dataSources, parameters, userId })
    });
    return this.handleResponse(response);
  }

  async getMiningJobs(userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs?userId=${userId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async executeMiningJob(jobId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs/${jobId}/execute`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getMiningResult(jobId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs/${jobId}/result`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async discoverPatterns(dataSource: string, patternType: string, filters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/patterns/discover`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, patternType, filters })
    });
    return this.handleResponse(response);
  }

  async generateInsights(dataSource: string, insightType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/insights/generate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, insightType, parameters })
    });
    return this.handleResponse(response);
  }

  // ROI Analysis
  async createROIAnalysis(name: string, description: string, analysisType: string, projectId: string, parameters: any, userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analyses`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, analysisType, projectId, parameters, userId })
    });
    return this.handleResponse(response);
  }

  async getROIAnalyses(userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analyses?userId=${userId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async executeROIAnalysis(analysisId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analyses/${analysisId}/execute`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async calculateROI(projectId: string, startDate: string, endDate: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/metrics/calculate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, startDate, endDate, parameters })
    });
    return this.handleResponse(response);
  }

  async performCostBenefitAnalysis(projectId: string, costs: any[], benefits: any[]) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/cost-benefit`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, costs, benefits })
    });
    return this.handleResponse(response);
  }

  async measureEffectiveness(projectId: string, metricType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/effectiveness/measure`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, metricType, parameters })
    });
    return this.handleResponse(response);
  }

  // Additional missing API methods for complete alignment
  async updateCustomDashboard(dashboardId: string, name: string, description: string, layout: any, isPublic: boolean) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, layout, isPublic })
    });
    return this.handleResponse(response);
  }

  async getPublicDashboards() {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/public`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateDashboardWidget(widgetId: string, title: string, configuration: any, position: any, isVisible: boolean) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/widgets/${widgetId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ title, configuration, position, isVisible })
    });
    return this.handleResponse(response);
  }

  async getWidget(widgetId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/widgets/${widgetId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async removeWidget(widgetId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/widgets/${widgetId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createDashboardTemplate(name: string, description: string, userRole: string, template: any, createdBy: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/templates`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, userRole, template, createdBy })
    });
    return this.handleResponse(response);
  }

  async getDashboardTemplates(userRole: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/templates?userRole=${userRole}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async cloneDashboard(dashboardId: string, newName: string, userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}/clone`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ newName, userId })
    });
    return this.handleResponse(response);
  }

  async shareDashboard(dashboardId: string, userId: string, permission: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}/share`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ userId, permission })
    });
    return this.handleResponse(response);
  }

  async getDashboardAnalytics(dashboardId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/dashboards/${dashboardId}/analytics`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateAdvancedReport(reportId: string, name: string, description: string, dataSources: any[], configuration: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, dataSources, configuration })
    });
    return this.handleResponse(response);
  }

  async getAdvancedReport(reportId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getPublicReports(reportType?: string) {
    const params = new URLSearchParams();
    if (reportType) params.append('reportType', reportType);
    const response = await fetch(`${API_BASE_URL}/analytics/reports/public?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getReportExecutions(reportId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}/executions`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getExecution(executionId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/executions/${executionId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async scheduleReport(reportId: string, scheduleType: string, cronExpression: string, userId: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}/schedule`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ scheduleType, cronExpression, userId, parameters })
    });
    return this.handleResponse(response);
  }

  async cancelSchedule(scheduleId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/schedules/${scheduleId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createReportTemplate(name: string, description: string, reportType: string, userRole: string, template: any, createdBy: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/templates`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, reportType, userRole, template, createdBy })
    });
    return this.handleResponse(response);
  }

  async getReportTemplates(reportType?: string, userRole?: string) {
    const params = new URLSearchParams();
    if (reportType) params.append('reportType', reportType);
    if (userRole) params.append('userRole', userRole);
    const response = await fetch(`${API_BASE_URL}/analytics/reports/templates?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getReportAnalytics(reportId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}/analytics`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async shareReport(reportId: string, userId: string, permission: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}/share`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ userId, permission })
    });
    return this.handleResponse(response);
  }

  async exportReport(executionId: string, format: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/executions/${executionId}/export`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ format })
    });
    return this.handleResponse(response);
  }

  async deleteReport(reportId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/reports/${reportId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getMiningJob(jobId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs/${jobId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createPredictiveModel(name: string, modelType: string, targetVariable: string, features: string[], parameters: any, userId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/models`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, modelType, targetVariable, features, parameters, userId })
    });
    return this.handleResponse(response);
  }

  async makePrediction(modelId: string, inputData: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/models/${modelId}/predict`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ inputData })
    });
    return this.handleResponse(response);
  }

  async detectAnomalies(dataSource: string, detectionType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/anomalies/detect`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, detectionType, parameters })
    });
    return this.handleResponse(response);
  }

  async analyzeTrends(dataSource: string, trendType: string, startDate: string, endDate: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/trends/analyze`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, trendType, startDate, endDate })
    });
    return this.handleResponse(response);
  }

  async findDataMiningCorrelations(dataSource: string, variables: string[]): Promise<any> {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/correlations/find`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, variables })
    });
    return this.handleResponse<any>(response);
  }

  async performClustering(dataSource: string, algorithm: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/clustering/perform`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ dataSource, algorithm, parameters })
    });
    return this.handleResponse(response);
  }

  async getMiningAnalytics(dataSource: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/analytics?dataSource=${dataSource}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async deleteMiningJob(jobId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/data-mining/jobs/${jobId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getROIAnalysis(analysisId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analyses/${analysisId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async assessImpact(projectId: string, impactType: string, criteria: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/impact/assess`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, impactType, criteria })
    });
    return this.handleResponse(response);
  }

  async benchmarkPerformance(projectId: string, benchmarkType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/benchmark`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, benchmarkType, parameters })
    });
    return this.handleResponse(response);
  }

  async analyzeValueForMoney(projectId: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/value-for-money`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, parameters })
    });
    return this.handleResponse(response);
  }

  async compareROI(projectIds: string[], comparisonType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/compare`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectIds, comparisonType, parameters })
    });
    return this.handleResponse(response);
  }

  async analyzeROITrends(projectId: string, startDate: string, endDate: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/trends/analyze`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ projectId, startDate, endDate })
    });
    return this.handleResponse(response);
  }

  async getROIAnalytics(projectId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analytics?projectId=${projectId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async deleteROIAnalysis(analysisId: string) {
    const response = await fetch(`${API_BASE_URL}/analytics/roi/analyses/${analysisId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Real-time Intelligence APIs
  // Stream Processing
  async createStreamProcessor(name: string, description: string, dataSource: string, configuration: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, dataSource, configuration })
    });
    return this.handleResponse(response);
  }

  async startStreamProcessor(processorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/start`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async stopStreamProcessor(processorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/stop`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getStreamProcessor(processorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getStreamProcessors() {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async addStreamRule(processorId: string, rule: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/rules`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(rule)
    });
    return this.handleResponse(response);
  }

  async removeStreamRule(processorId: string, ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/rules/${ruleId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async processStreamData(processorId: string, data: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/process`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data)
    });
    return this.handleResponse(response);
  }

  async getStreamProcessorMetrics(processorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}/metrics`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async deleteStreamProcessor(processorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/stream-processing/processors/${processorId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Event Correlation
  async createCorrelationRule(name: string, description: string, pattern: string, conditions: any, action: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/rules`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, pattern, conditions, action })
    });
    return this.handleResponse(response);
  }

  async processEvent(event: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/events`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(event)
    });
    return this.handleResponse(response);
  }

  async correlateEvents(ruleId: string, eventIds: string[]) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/correlate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ ruleId, eventIds })
    });
    return this.handleResponse(response);
  }

  async findEventCorrelations(source?: string, startTime?: string, endTime?: string): Promise<any> {
    const params = new URLSearchParams();
    if (source) params.append('source', source);
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/correlations?${params}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse<any>(response);
  }

  async detectEventPattern(source: string, eventType: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/patterns/detect`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ source, eventType, startTime, endTime })
    });
    return this.handleResponse(response);
  }

  async getCorrelationAnalytics(source: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/analytics?source=${source}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCorrelationRule(ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/rules/${ruleId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getCorrelationRules() {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/rules`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateCorrelationRule(ruleId: string, name: string, description: string, pattern: string, conditions: any, action: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/rules/${ruleId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, pattern, conditions, action })
    });
    return this.handleResponse(response);
  }

  async deleteCorrelationRule(ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/event-correlation/rules/${ruleId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Trend Analysis
  async createTrendAnalyzer(name: string, description: string, dataSource: string, metric: string, configuration: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, dataSource, metric, configuration })
    });
    return this.handleResponse(response);
  }

  async addTrendDataPoint(analyzerId: string, dataPoint: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}/data`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(dataPoint)
    });
    return this.handleResponse(response);
  }

  async analyzeTrend(analyzerId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}/analyze`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTrends(analyzerId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}/trends?startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createTrendAlert(analyzerId: string, name: string, condition: string, threshold: string, action: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/alerts`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ analyzerId, name, condition, threshold, action })
    });
    return this.handleResponse(response);
  }

  async checkTrendAlerts(analyzerId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}/alerts`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTrendAnalytics(analyzerId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}/analytics`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTrendAnalyzer(analyzerId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getTrendAnalyzers() {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateTrendAnalyzer(analyzerId: string, name: string, description: string, configuration: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, configuration })
    });
    return this.handleResponse(response);
  }

  async deleteTrendAnalyzer(analyzerId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/trend-analysis/analyzers/${analyzerId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Anomaly Detection
  async createAnomalyDetector(name: string, description: string, dataSource: string, detectionType: string, configuration: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detectors`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, dataSource, detectionType, configuration })
    });
    return this.handleResponse(response);
  }

  async trainAnomalyModel(detectorId: string, trainingData: any[], parameters: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/models`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ detectorId, trainingData, parameters })
    });
    return this.handleResponse(response);
  }

  async detectAnomaly(detectorId: string, dataPoint: any) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detect`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ detectorId, dataPoint })
    });
    return this.handleResponse(response);
  }

  async getAnomalies(detectorId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/anomalies?detectorId=${detectorId}&startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getAnomalySummary(detectorId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/anomalies/summary?detectorId=${detectorId}&startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async resolveAnomaly(anomalyId: string, resolution: string, resolvedBy: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/anomalies/${anomalyId}/resolve`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ resolution, resolvedBy })
    });
    return this.handleResponse(response);
  }

  async detectAnomalyPattern(detectorId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/patterns/detect`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ detectorId, startTime, endTime })
    });
    return this.handleResponse(response);
  }

  async getDetectionAnalytics(detectorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/analytics?detectorId=${detectorId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getAnomalyDetector(detectorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detectors/${detectorId}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getAnomalyDetectors() {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detectors`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateAnomalyDetector(detectorId: string, name: string, description: string, configuration: any, sensitivity: number) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detectors/${detectorId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, configuration, sensitivity })
    });
    return this.handleResponse(response);
  }

  async deleteAnomalyDetector(detectorId: string) {
    const response = await fetch(`${API_BASE_URL}/realtime/anomaly-detection/detectors/${detectorId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Advanced Mobile Features APIs
  // AR Integration
  async createARMarker(marker: any) {
    const response = await fetch(`${API_BASE_URL}/ar/markers`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(marker)
    });
    return this.handleResponse(response);
  }

  async getARMarkersNearby(latitude: number, longitude: number, radius: number) {
    const response = await fetch(`${API_BASE_URL}/ar/markers/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async createDamageAssessment(assessment: any) {
    const response = await fetch(`${API_BASE_URL}/ar/damage-assessments`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(assessment)
    });
    return this.handleResponse(response);
  }

  // Push Notifications
  async subscribePushNotifications(subscription: any) {
    const response = await fetch(`${API_BASE_URL}/push/subscribe`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(subscription)
    });
    return this.handleResponse(response);
  }

  async unsubscribePushNotifications() {
    const response = await fetch(`${API_BASE_URL}/push/unsubscribe`, {
      method: 'POST',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // AI & Machine Learning APIs
  // Disaster Prediction
  async createDisasterPredictionModel(name: string, description: string, disasterType: string, features: any, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, disasterType, features, parameters })
    });
    return this.handleResponse(response);
  }

  async trainDisasterModel(modelId: string, trainingData: any[]) {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models/${modelId}/train`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(trainingData)
    });
    return this.handleResponse(response);
  }

  async predictDisaster(modelId: string, input: any) {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models/${modelId}/predict`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(input)
    });
    return this.handleResponse(response);
  }

  async getDisasterPredictions(modelId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models/${modelId}/predictions?startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async evaluateDisasterModel(modelId: string, testData: any[]) {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models/${modelId}/evaluate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(testData)
    });
    return this.handleResponse(response);
  }

  async getDisasterModels() {
    const response = await fetch(`${API_BASE_URL}/ai/disaster-prediction/models`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Resource Demand Forecasting
  async createResourceForecastModel(name: string, description: string, resourceType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/ai/resource-forecasting/models`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, resourceType, parameters })
    });
    return this.handleResponse(response);
  }

  async trainResourceModel(modelId: string, trainingData: any[]) {
    const response = await fetch(`${API_BASE_URL}/ai/resource-forecasting/models/${modelId}/train`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(trainingData)
    });
    return this.handleResponse(response);
  }

  async forecastResourceDemand(modelId: string, input: any) {
    const response = await fetch(`${API_BASE_URL}/ai/resource-forecasting/models/${modelId}/forecast`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(input)
    });
    return this.handleResponse(response);
  }

  async getResourceForecasts(modelId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/ai/resource-forecasting/models/${modelId}/forecasts?startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getResourceModels() {
    const response = await fetch(`${API_BASE_URL}/ai/resource-forecasting/models`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Risk Scoring
  async calculateRiskScore(input: any) {
    const response = await fetch(`${API_BASE_URL}/ai/risk-scoring/calculate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(input)
    });
    return this.handleResponse(response);
  }

  async getRiskScores(startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/ai/risk-scoring/scores?startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async compareRiskScores(scoreIds: string[]) {
    const response = await fetch(`${API_BASE_URL}/ai/risk-scoring/compare`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(scoreIds)
    });
    return this.handleResponse(response);
  }

  // Early Warning Systems
  async createWarningRule(name: string, description: string, triggerCondition: string, thresholds: any, action: string) {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/rules`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, triggerCondition, thresholds, action })
    });
    return this.handleResponse(response);
  }

  async getActiveWarnings() {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/active`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async getWarnings(ruleId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/warnings?ruleId=${ruleId}&startTime=${startTime}&endTime=${endTime}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async acknowledgeWarning(warningId: string, acknowledgedBy: string) {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/warnings/${warningId}/acknowledge`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ acknowledgedBy })
    });
    return this.handleResponse(response);
  }

  async getWarningRules() {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/rules`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async updateWarningRule(ruleId: string, name: string, description: string, triggerCondition: string, thresholds: any, action: string, severity: string) {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/rules/${ruleId}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify({ name, description, triggerCondition, thresholds, action, severity })
    });
    return this.handleResponse(response);
  }

  async deleteWarningRule(ruleId: string) {
    const response = await fetch(`${API_BASE_URL}/ai/early-warnings/rules/${ruleId}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Intelligent Resource Optimization APIs
  // Dynamic Routing
  async createOptimizedRoute(originLat: string, originLon: string, destinations: string[], constraints?: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/routing/routes?originLat=${originLat}&originLon=${originLon}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(destinations)
    });
    return this.handleResponse(response);
  }

  async reoptimizeRoute(routeId: string, newConditions: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/routing/routes/${routeId}/reoptimize`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(newConditions)
    });
    return this.handleResponse(response);
  }

  async getOptimizationRoutes() {
    const response = await fetch(`${API_BASE_URL}/optimization/routing/routes`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Smart Inventory Management
  async analyzeInventory(itemId: string, currentStock: number, minThreshold: number, historicalData?: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/inventory/analyze?itemId=${itemId}&currentStock=${currentStock}&minThreshold=${minThreshold}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(historicalData || {})
    });
    return this.handleResponse(response);
  }

  async optimizeStock(currentStock: any, itemData?: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/inventory/optimize`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ currentStock, itemData: itemData || {} })
    });
    return this.handleResponse(response);
  }

  async createReorderRule(itemId: string, ruleType: string, parameters: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/inventory/rules?itemId=${itemId}&ruleType=${ruleType}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(parameters)
    });
    return this.handleResponse(response);
  }

  async getReorderRules() {
    const response = await fetch(`${API_BASE_URL}/optimization/inventory/rules`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Resource Allocation
  async allocateResources(needs: any[], availableResources: any) {
    const response = await fetch(`${API_BASE_URL}/optimization/allocation/allocate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ needs, availableResources })
    });
    return this.handleResponse(response);
  }

  async getResourceAllocations() {
    const response = await fetch(`${API_BASE_URL}/optimization/allocation/allocations`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Load Balancing
  async balanceWorkload(availableWorkers: any[], pendingTasks: any[]) {
    const response = await fetch(`${API_BASE_URL}/optimization/load-balancing/balance`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify({ availableWorkers, pendingTasks })
    });
    return this.handleResponse(response);
  }

  async getWorkloadAssignments() {
    const response = await fetch(`${API_BASE_URL}/optimization/load-balancing/assignments`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  // Shelter APIs
  async getShelters(region?: string, latitude?: number, longitude?: number) {
    const params = new URLSearchParams();
    if (region) params.append('region', region);
    if (latitude) params.append('latitude', latitude.toString());
    if (longitude) params.append('longitude', longitude.toString());
    
    const response = await fetch(`${API_BASE_URL}/integration/government/shelters${params.toString() ? '?' + params.toString() : ''}`, {
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async findNearbyShelters(latitude: number, longitude: number, radiusKm: number = 50) {
    // Use needs requests with type "Shelter" as a fallback, or government API
    try {
      const shelters = await this.getShelters(undefined, latitude, longitude);
      return shelters;
    } catch (error) {
      // Fallback: search for needs requests with type "Shelter"
      const requests = await this.getRequests({ 
        type: 'Shelter',
        page: 0,
        size: 100
      });
      return requests.content || [];
    }
  }
}

export const apiService = new ApiService();
