import { apiService } from './api';

export interface KnowledgeArticle {
  id: string;
  title: string;
  content: string;
  category: string;
  tags: string[];
  authorId: string;
  language: string;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
  viewCount: number;
  rating: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'DELETED';
}

export interface KnowledgeCategory {
  id: string;
  name: string;
  description: string;
  parentCategoryId?: string;
  createdAt: string;
  isActive: boolean;
}

export interface KnowledgeTag {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  usageCount: number;
}

export interface KnowledgeAnalytics {
  articleId: string;
  viewCount: number;
  bookmarkCount: number;
  averageRating: number;
  ratingCount: number;
  searchRank: number;
  popularityScore: number;
}

export interface CreateArticleRequest {
  title: string;
  content: string;
  category: string;
  tags: string;
  language: string;
  isPublic: boolean;
}

export interface UpdateArticleRequest {
  title?: string;
  content?: string;
  category?: string;
  tags?: string;
  language?: string;
  isPublic?: boolean;
}

export interface RateArticleRequest {
  rating: number;
  comment?: string;
}

export interface CreateCategoryRequest {
  name: string;
  description: string;
  parentCategoryId?: string;
}

export interface CreateTagRequest {
  name: string;
  description: string;
}

class KnowledgeBaseService {
  private baseUrl = '/knowledge-base';

  async createArticle(request: CreateArticleRequest): Promise<KnowledgeArticle> {
    return apiService.post(`${this.baseUrl}/articles`, request);
  }

  async updateArticle(articleId: string, request: UpdateArticleRequest): Promise<KnowledgeArticle> {
    return apiService.put(`${this.baseUrl}/articles/${articleId}`, request);
  }

  async getArticle(articleId: string): Promise<KnowledgeArticle> {
    return apiService.get(`${this.baseUrl}/articles/${articleId}`);
  }

  async searchArticles(filters: {
    query?: string;
    category?: string;
    tags?: string[];
    language?: string;
    isPublic?: boolean;
    limit?: number;
  } = {}): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles`, filters);
  }

  async getArticlesByCategory(category: string, limit: number = 50): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles/category/${category}`, { limit });
  }

  async getPopularArticles(limit: number = 10): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles/popular`, { limit });
  }

  async getRecentArticles(limit: number = 10): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles/recent`, { limit });
  }

  async rateArticle(articleId: string, request: RateArticleRequest): Promise<KnowledgeArticle> {
    return apiService.post(`${this.baseUrl}/articles/${articleId}/rate`, request);
  }

  async bookmarkArticle(articleId: string): Promise<KnowledgeArticle> {
    return apiService.post(`${this.baseUrl}/articles/${articleId}/bookmark`);
  }

  async removeBookmark(articleId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/articles/${articleId}/bookmark`);
  }

  async getUserBookmarks(): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/bookmarks`);
  }

  async getRelatedArticles(articleId: string, limit: number = 5): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles/${articleId}/related`, { limit });
  }

  async createCategory(request: CreateCategoryRequest): Promise<KnowledgeCategory> {
    return apiService.post(`${this.baseUrl}/categories`, request);
  }

  async getCategories(): Promise<KnowledgeCategory[]> {
    return apiService.get(`${this.baseUrl}/categories`);
  }

  async createTag(request: CreateTagRequest): Promise<KnowledgeTag> {
    return apiService.post(`${this.baseUrl}/tags`, request);
  }

  async getPopularTags(limit: number = 20): Promise<KnowledgeTag[]> {
    return apiService.get(`${this.baseUrl}/tags/popular`, { limit });
  }

  async getArticleAnalytics(articleId: string): Promise<KnowledgeAnalytics> {
    return apiService.get(`${this.baseUrl}/articles/${articleId}/analytics`);
  }

  async getTrendingArticles(limit: number = 10): Promise<KnowledgeArticle[]> {
    return apiService.get(`${this.baseUrl}/articles/trending`, { limit });
  }

  async getSearchSuggestions(query: string): Promise<Record<string, any>> {
    return apiService.get(`${this.baseUrl}/search/suggestions`, { query });
  }

  async translateArticle(articleId: string, targetLanguage: string): Promise<KnowledgeArticle> {
    return apiService.post(`${this.baseUrl}/articles/${articleId}/translate`, { targetLanguage });
  }

  async deleteArticle(articleId: string): Promise<void> {
    return apiService.delete(`${this.baseUrl}/articles/${articleId}`);
  }
}

export const knowledgeBaseService = new KnowledgeBaseService();


