import { apiService } from './api';

export interface SocialMediaPost {
  id: string;
  platform: string;
  author: string;
  content: string;
  publishedAt: string;
  likes: number;
  shares: number;
  comments: number;
  hashtags: string[];
  location?: string;
  sentiment: string;
  metadata: Record<string, any>;
}

export interface SocialMediaAnalytics {
  platform: string;
  timeRange: string;
  totalPosts: number;
  totalEngagement: number;
  averageSentiment: number;
  topHashtags: string[];
  topInfluencers: string[];
  demographics: Record<string, any>;
  generatedAt: string;
}

export interface SocialMediaInfluencer {
  id: string;
  username: string;
  platform: string;
  followers: number;
  engagement: number;
  category: string;
  influenceScore: number;
  location?: string;
}

export interface SocialMediaSentiment {
  text: string;
  sentiment: string;
  confidence: number;
  emotions: Record<string, number>;
  language: string;
}

export interface SocialMediaHashtag {
  hashtag: string;
  platform: string;
  usageCount: number;
  trendScore: number;
  category: string;
  lastUsed: string;
}

export interface SocialMediaCrisisDetection {
  location: string;
  timeRange: string;
  crisisDetected: boolean;
  crisisScore: number;
  indicators: string[];
  severity: string;
  detectedAt: string;
}

export interface SocialMediaAlert {
  id: string;
  platform: string;
  alertType: string;
  severity: string;
  message: string;
  createdAt: string;
  location?: string;
  metadata: Record<string, any>;
}

export interface SocialMediaReport {
  id: string;
  platform: string;
  timeRange: string;
  reportType: string;
  data: Record<string, any>;
  generatedAt: string;
  fileUrl?: string;
}

class SocialMediaService {
  private baseUrl = '/integration/social-media';

  async searchPosts(query: string, platform?: string, limit: number = 50): Promise<SocialMediaPost[]> {
    return apiService.get(`${this.baseUrl}/posts/search`, { query, platform, limit });
  }

  async getTrendingPosts(platform: string, category?: string): Promise<SocialMediaPost[]> {
    return apiService.get(`${this.baseUrl}/posts/trending`, { platform, category });
  }

  async getAnalytics(platform: string, timeRange: string): Promise<SocialMediaAnalytics> {
    return apiService.get(`${this.baseUrl}/analytics`, { platform, timeRange });
  }

  async getInfluencers(platform: string, category?: string): Promise<SocialMediaInfluencer[]> {
    return apiService.get(`${this.baseUrl}/influencers`, { platform, category });
  }

  async analyzeSentiment(text: string, language: string = 'en'): Promise<SocialMediaSentiment> {
    return apiService.post(`${this.baseUrl}/sentiment`, { text, language });
  }

  async getTrendingHashtags(platform: string, category?: string): Promise<SocialMediaHashtag[]> {
    return apiService.get(`${this.baseUrl}/hashtags/trending`, { platform, category });
  }

  async detectCrisis(location: string, timeRange: string): Promise<SocialMediaCrisisDetection> {
    return apiService.get(`${this.baseUrl}/crisis/detect`, { location, timeRange });
  }

  async getAlerts(platform: string, severity?: string): Promise<SocialMediaAlert[]> {
    return apiService.get(`${this.baseUrl}/alerts`, { platform, severity });
  }

  async subscribeToKeywords(keywords: string[], callbackUrl: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/subscriptions`, { keywords, callbackUrl });
  }

  async generateReport(platform: string, timeRange: string, reportType: string): Promise<SocialMediaReport> {
    return apiService.get(`${this.baseUrl}/reports`, { platform, timeRange, reportType });
  }
}

export const socialMediaService = new SocialMediaService();


