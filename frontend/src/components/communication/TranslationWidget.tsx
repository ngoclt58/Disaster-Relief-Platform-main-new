import React, { useState, useEffect } from 'react';
import { translationService, TranslationResult, LanguageSupport } from '../../services/translationService';

interface TranslationWidgetProps {
  text: string;
  onTranslation?: (result: TranslationResult) => void;
  onClose?: () => void;
}

const TranslationWidget: React.FC<TranslationWidgetProps> = ({ text, onTranslation, onClose }) => {
  const [supportedLanguages, setSupportedLanguages] = useState<LanguageSupport[]>([]);
  const [sourceLanguage, setSourceLanguage] = useState<string>('auto');
  const [targetLanguage, setTargetLanguage] = useState<string>('en');
  const [translationResult, setTranslationResult] = useState<TranslationResult | null>(null);
  const [isTranslating, setIsTranslating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showAdvanced, setShowAdvanced] = useState(false);

  useEffect(() => {
    loadSupportedLanguages();
    if (text) {
      translateText();
    }
  }, [text, sourceLanguage, targetLanguage]);

  const loadSupportedLanguages = async () => {
    try {
      const languages = await translationService.getSupportedLanguages();
      setSupportedLanguages(languages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load languages');
    }
  };

  const translateText = async () => {
    if (!text.trim()) return;

    setIsTranslating(true);
    setError(null);

    try {
      let result: TranslationResult;

      if (sourceLanguage === 'auto') {
        result = await translationService.translateWithAutoDetection(text, targetLanguage);
      } else {
        result = await translationService.translateText(text, sourceLanguage, targetLanguage);
      }

      setTranslationResult(result);
      onTranslation?.(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Translation failed');
    } finally {
      setIsTranslating(false);
    }
  };

  const swapLanguages = () => {
    if (translationResult) {
      setSourceLanguage(translationResult.targetLanguage);
      setTargetLanguage(translationResult.sourceLanguage);
    }
  };

  const copyTranslation = () => {
    if (translationResult) {
      navigator.clipboard.writeText(translationResult.translatedText);
    }
  };

  const getLanguageName = (code: string) => {
    if (code === 'auto') return 'Auto-detect';
    const language = supportedLanguages.find(lang => lang.code === code);
    return language ? language.name : code;
  };

  const getLanguageFlag = (code: string) => {
    if (code === 'auto') return '🔍';
    return translationService.getLanguageFlag(code);
  };

  const getTranslationQualityColor = (confidence: number) => {
    return translationService.getTranslationQualityColor(confidence);
  };

  const getTranslationQualityIcon = (confidence: number) => {
    return translationService.getTranslationQualityIcon(confidence);
  };

  const formatConfidence = (confidence: number) => {
    return translationService.formatConfidence(confidence);
  };

  const isReliableTranslation = (confidence: number) => {
    return translationService.isReliableTranslation(confidence);
  };

  return (
    <div className="bg-white rounded-lg shadow-lg border border-gray-200 p-4 max-w-2xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Translation</h3>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setShowAdvanced(!showAdvanced)}
            className="text-sm text-gray-600 hover:text-gray-800 transition-colors"
          >
            {showAdvanced ? 'Simple' : 'Advanced'}
          </button>
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

      {/* Language Selection */}
      <div className="flex items-center space-x-4 mb-4">
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700 mb-1">From</label>
          <select
            value={sourceLanguage}
            onChange={(e) => setSourceLanguage(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="auto">🔍 Auto-detect</option>
            {supportedLanguages.map((lang) => (
              <option key={lang.code} value={lang.code}>
                {getLanguageFlag(lang.code)} {lang.name}
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={swapLanguages}
          className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
          title="Swap languages"
        >
          <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M8 5a1 1 0 011 1v1h1a1 1 0 110 2H9v1a1 1 0 11-2 0V8H7a1 1 0 110-2h1V6a1 1 0 011-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clipRule="evenodd" />
          </svg>
        </button>

        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700 mb-1">To</label>
          <select
            value={targetLanguage}
            onChange={(e) => setTargetLanguage(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {supportedLanguages.map((lang) => (
              <option key={lang.code} value={lang.code}>
                {getLanguageFlag(lang.code)} {lang.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Original Text */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">Original Text</label>
        <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
          <p className="text-sm text-gray-900">{text}</p>
        </div>
      </div>

      {/* Translation Result */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">Translation</label>
        <div className="relative">
          <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
            {isTranslating ? (
              <div className="flex items-center space-x-2">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                <span className="text-sm text-gray-600">Translating...</span>
              </div>
            ) : translationResult ? (
              <div>
                <p className="text-sm text-gray-900 mb-2">{translationResult.translatedText}</p>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <span className={`text-xs px-2 py-1 rounded-full ${isReliableTranslation(translationResult.confidence) ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                      {getTranslationQualityIcon(translationResult.confidence)} {formatConfidence(translationResult.confidence)}
                    </span>
                    <span className="text-xs text-gray-500">{translationResult.method}</span>
                  </div>
                  <button
                    onClick={copyTranslation}
                    className="text-xs text-blue-600 hover:text-blue-800 transition-colors"
                  >
                    Copy
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-500">No translation available</p>
            )}
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      {/* Advanced Options */}
      {showAdvanced && translationResult && (
        <div className="border-t border-gray-200 pt-4">
          <h4 className="text-sm font-medium text-gray-700 mb-2">Translation Details</h4>
          <div className="space-y-2 text-xs text-gray-600">
            <div className="flex justify-between">
              <span>Source Language:</span>
              <span>{getLanguageName(translationResult.sourceLanguage)}</span>
            </div>
            <div className="flex justify-between">
              <span>Target Language:</span>
              <span>{getLanguageName(translationResult.targetLanguage)}</span>
            </div>
            <div className="flex justify-between">
              <span>Confidence:</span>
              <span className="flex items-center space-x-1">
                <span>{formatConfidence(translationResult.confidence)}</span>
                <span style={{ color: getTranslationQualityColor(translationResult.confidence) }}>
                  {getTranslationQualityIcon(translationResult.confidence)}
                </span>
              </span>
            </div>
            <div className="flex justify-between">
              <span>Method:</span>
              <span>{translationResult.method}</span>
            </div>
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-end space-x-2">
        <button
          onClick={translateText}
          disabled={isTranslating || !text.trim()}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isTranslating ? 'Translating...' : 'Translate Again'}
        </button>
        {translationResult && (
          <button
            onClick={copyTranslation}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
          >
            Copy Translation
          </button>
        )}
      </div>
    </div>
  );
};

export default TranslationWidget;


