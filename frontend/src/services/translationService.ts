/**
 * Translation Service
 * Handles real-time translation and multi-language support
 */

import { apiService } from './api';

export interface TranslationResult {
  originalText: string;
  sourceLanguage: string;
  targetLanguage: string;
  translatedText: string;
  confidence: number;
  method: string;
}

export interface TranslationRequest {
  text: string;
  sourceLanguage: string;
  targetLanguage: string;
}

export interface LanguageDetectionResult {
  language: string;
  confidence: number;
  method: string;
}

export interface LanguageSupport {
  code: string;
  name: string;
  nativeName: string;
  rtl: boolean;
  supported: boolean;
}

export interface TranslationStatistics {
  totalTranslations: number;
  supportedLanguages: number;
  cacheHitRate: number;
  mostTranslatedLanguage: string;
  lastUpdated: string;
}

class TranslationService {
  private supportedLanguages: LanguageSupport[] = [];
  private translationCache: Map<string, TranslationResult> = new Map();
  private currentLanguage: string = 'en';

  /**
   * Translate text from source to target language
   */
  async translateText(text: string, sourceLanguage: string, targetLanguage: string): Promise<TranslationResult> {
    // Check cache first
    const cacheKey = `${sourceLanguage}:${targetLanguage}:${text}`;
    const cached = this.translationCache.get(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await apiService.post('/translation/translate', {
      text,
      sourceLanguage,
      targetLanguage
    });

    // Cache the result
    this.translationCache.set(cacheKey, response);
    return response;
  }

  /**
   * Batch translate multiple texts
   */
  async translateBatch(requests: TranslationRequest[]): Promise<TranslationResult[]> {
    const response = await apiService.post('/translation/translate-batch', requests);
    return response;
  }

  /**
   * Detect language of input text
   */
  async detectLanguage(text: string): Promise<LanguageDetectionResult> {
    const response = await apiService.post('/translation/detect-language', { text });
    return response;
  }

  /**
   * Translate text with auto-detection
   */
  async translateWithAutoDetection(text: string, targetLanguage: string): Promise<TranslationResult> {
    const response = await apiService.post('/translation/translate-auto', {
      text,
      targetLanguage
    });
    return response;
  }

  /**
   * Get supported languages
   */
  async getSupportedLanguages(): Promise<LanguageSupport[]> {
    if (this.supportedLanguages.length === 0) {
      const response = await apiService.get('/translation/languages');
      this.supportedLanguages = response;
    }
    return this.supportedLanguages;
  }

  /**
   * Get language support details
   */
  async getLanguageSupport(languageCode: string): Promise<LanguageSupport> {
    const response = await apiService.get(`/translation/languages/${languageCode}`);
    return response;
  }

  /**
   * Get translation statistics
   */
  async getTranslationStatistics(): Promise<TranslationStatistics> {
    const response = await apiService.get('/translation/statistics');
    return response;
  }

  /**
   * Clear translation cache
   */
  async clearCache(): Promise<void> {
    await apiService.delete('/translation/cache');
    this.translationCache.clear();
  }

  /**
   * Set current language
   */
  setCurrentLanguage(languageCode: string): void {
    this.currentLanguage = languageCode;
  }

  /**
   * Get current language
   */
  getCurrentLanguage(): string {
    return this.currentLanguage;
  }

  /**
   * Translate text to current language
   */
  async translateToCurrentLanguage(text: string, sourceLanguage?: string): Promise<TranslationResult> {
    if (sourceLanguage) {
      return this.translateText(text, sourceLanguage, this.currentLanguage);
    } else {
      return this.translateWithAutoDetection(text, this.currentLanguage);
    }
  }

  /**
   * Translate text from current language
   */
  async translateFromCurrentLanguage(text: string, targetLanguage: string): Promise<TranslationResult> {
    return this.translateText(text, this.currentLanguage, targetLanguage);
  }

  /**
   * Get language name by code
   */
  getLanguageName(languageCode: string): string {
    const language = this.supportedLanguages.find(lang => lang.code === languageCode);
    return language ? language.name : languageCode;
  }

  /**
   * Get native language name by code
   */
  getNativeLanguageName(languageCode: string): string {
    const language = this.supportedLanguages.find(lang => lang.code === languageCode);
    return language ? language.nativeName : languageCode;
  }

  /**
   * Check if language is RTL
   */
  isRTL(languageCode: string): boolean {
    const language = this.supportedLanguages.find(lang => lang.code === languageCode);
    return language ? language.rtl : false;
  }

  /**
   * Get language flag emoji
   */
  getLanguageFlag(languageCode: string): string {
    const flagMap: { [key: string]: string } = {
      'en': '🇺🇸',
      'es': '🇪🇸',
      'fr': '🇫🇷',
      'de': '🇩🇪',
      'zh': '🇨🇳',
      'ar': '🇸🇦',
      'ru': '🇷🇺',
      'ja': '🇯🇵',
      'ko': '🇰🇷',
      'pt': '🇵🇹',
      'it': '🇮🇹',
      'nl': '🇳🇱',
      'sv': '🇸🇪',
      'no': '🇳🇴',
      'da': '🇩🇰',
      'fi': '🇫🇮',
      'pl': '🇵🇱',
      'tr': '🇹🇷',
      'hi': '🇮🇳',
      'th': '🇹🇭',
      'vi': '🇻🇳',
      'id': '🇮🇩',
      'ms': '🇲🇾',
      'tl': '🇵🇭'
    };
    
    return flagMap[languageCode] || '🌐';
  }

  /**
   * Format confidence as percentage
   */
  formatConfidence(confidence: number): string {
    return `${Math.round(confidence * 100)}%`;
  }

  /**
   * Check if translation is reliable
   */
  isReliableTranslation(confidence: number): boolean {
    return confidence >= 0.8;
  }

  /**
   * Get translation quality indicator
   */
  getTranslationQuality(confidence: number): 'high' | 'medium' | 'low' {
    if (confidence >= 0.9) return 'high';
    if (confidence >= 0.7) return 'medium';
    return 'low';
  }

  /**
   * Get translation quality color
   */
  getTranslationQualityColor(confidence: number): string {
    const quality = this.getTranslationQuality(confidence);
    switch (quality) {
      case 'high': return '#10b981'; // green
      case 'medium': return '#f59e0b'; // yellow
      case 'low': return '#ef4444'; // red
      default: return '#6b7280'; // gray
    }
  }

  /**
   * Get translation quality icon
   */
  getTranslationQualityIcon(confidence: number): string {
    const quality = this.getTranslationQuality(confidence);
    switch (quality) {
      case 'high': return '✅';
      case 'medium': return '⚠️';
      case 'low': return '❌';
      default: return '❓';
    }
  }

  /**
   * Get cache size
   */
  getCacheSize(): number {
    return this.translationCache.size;
  }

  /**
   * Clear local cache
   */
  clearLocalCache(): void {
    this.translationCache.clear();
  }

  /**
   * Get cached translation
   */
  getCachedTranslation(text: string, sourceLanguage: string, targetLanguage: string): TranslationResult | null {
    const cacheKey = `${sourceLanguage}:${targetLanguage}:${text}`;
    return this.translationCache.get(cacheKey) || null;
  }

  /**
   * Check if translation is cached
   */
  isTranslationCached(text: string, sourceLanguage: string, targetLanguage: string): boolean {
    const cacheKey = `${sourceLanguage}:${targetLanguage}:${text}`;
    return this.translationCache.has(cacheKey);
  }

  /**
   * Get translation method icon
   */
  getTranslationMethodIcon(method: string): string {
    switch (method.toLowerCase()) {
      case 'cached translation': return '💾';
      case 'simplified translation': return '🔧';
      case 'same language': return '🔄';
      case 'fallback (no translation available)': return '⚠️';
      default: return '🤖';
    }
  }

  /**
   * Format translation result for display
   */
  formatTranslationResult(result: TranslationResult): string {
    const confidence = this.formatConfidence(result.confidence);
    const quality = this.getTranslationQuality(result.confidence);
    const icon = this.getTranslationQualityIcon(result.confidence);
    
    return `${icon} ${result.translatedText} (${confidence} ${quality})`;
  }

  /**
   * Get language selection options
   */
  getLanguageOptions(): Array<{ code: string; name: string; nativeName: string; flag: string }> {
    return this.supportedLanguages.map(lang => ({
      code: lang.code,
      name: lang.name,
      nativeName: lang.nativeName,
      flag: this.getLanguageFlag(lang.code)
    }));
  }

  /**
   * Get popular languages
   */
  getPopularLanguages(): string[] {
    return ['en', 'es', 'fr', 'de', 'zh', 'ar', 'ru', 'ja', 'ko', 'pt'];
  }

  /**
   * Check if language is popular
   */
  isPopularLanguage(languageCode: string): boolean {
    return this.getPopularLanguages().includes(languageCode);
  }

  /**
   * Get language by code
   */
  getLanguageByCode(languageCode: string): LanguageSupport | null {
    return this.supportedLanguages.find(lang => lang.code === languageCode) || null;
  }

  /**
   * Search languages by name
   */
  searchLanguages(query: string): LanguageSupport[] {
    const lowerQuery = query.toLowerCase();
    return this.supportedLanguages.filter(lang => 
      lang.name.toLowerCase().includes(lowerQuery) ||
      lang.nativeName.toLowerCase().includes(lowerQuery) ||
      lang.code.toLowerCase().includes(lowerQuery)
    );
  }
}

export const translationService = new TranslationService();


