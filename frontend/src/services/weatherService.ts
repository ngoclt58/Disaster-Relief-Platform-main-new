import { apiService } from './api';

export interface WeatherData {
  location: string;
  temperature: number;
  humidity: number;
  pressure: number;
  windSpeed: number;
  windDirection: number;
  condition: string;
  description: string;
  timestamp: string;
  additionalData: Record<string, any>;
}

export interface WeatherForecast {
  location: string;
  dailyForecast: WeatherDay[];
  hourlyForecast: WeatherHour[];
  generatedAt: string;
}

export interface WeatherDay {
  date: string;
  maxTemperature: number;
  minTemperature: number;
  condition: string;
  precipitation: number;
  windSpeed: number;
  description: string;
}

export interface WeatherHour {
  time: string;
  temperature: number;
  condition: string;
  precipitation: number;
  windSpeed: number;
}

export interface WeatherAlert {
  id: string;
  location: string;
  alertType: string;
  severity: string;
  title: string;
  description: string;
  issuedAt: string;
  expiresAt?: string;
  affectedAreas: string[];
}

export interface WeatherMapData {
  mapType: string;
  region: string;
  imageUrl: string;
  generatedAt: string;
  metadata: Record<string, any>;
}

export interface AirQualityData {
  location: string;
  aqi: number;
  qualityLevel: string;
  pollutants: Record<string, number>;
  timestamp: string;
  healthRecommendation: string;
}

export interface WeatherStation {
  id: string;
  name: string;
  location: string;
  latitude: number;
  longitude: number;
  status: string;
  capabilities: string[];
}

export interface WeatherHistoricalData {
  location: string;
  startDate: string;
  endDate: string;
  historicalData: WeatherData[];
  statistics: Record<string, any>;
}

export interface WeatherDisasterRisk {
  location: string;
  disasterType: string;
  riskLevel: string;
  riskScore: number;
  riskFactors: string[];
  recommendation: string;
  assessedAt: string;
}

class WeatherService {
  private baseUrl = '/integration/weather';

  async getCurrentWeather(location: string, units: string = 'metric'): Promise<WeatherData> {
    return apiService.get(`${this.baseUrl}/current`, { location, units });
  }

  async getWeatherForecast(location: string, days: number = 7, units: string = 'metric'): Promise<WeatherForecast> {
    return apiService.get(`${this.baseUrl}/forecast`, { location, days, units });
  }

  async getWeatherAlerts(location: string, severity?: string): Promise<WeatherAlert[]> {
    return apiService.get(`${this.baseUrl}/alerts`, { location, severity });
  }

  async getWeatherMap(mapType: string, region: string): Promise<WeatherMapData> {
    return apiService.get(`${this.baseUrl}/map`, { mapType, region });
  }

  async getAirQuality(location: string): Promise<AirQualityData> {
    return apiService.get(`${this.baseUrl}/air-quality`, { location });
  }

  async getWeatherStations(region: string): Promise<WeatherStation[]> {
    return apiService.get(`${this.baseUrl}/stations`, { region });
  }

  async getHistoricalWeather(location: string, startDate: string, endDate: string): Promise<WeatherHistoricalData> {
    return apiService.get(`${this.baseUrl}/historical`, { location, startDate, endDate });
  }

  async getDisasterRisk(location: string, disasterType: string): Promise<WeatherDisasterRisk> {
    return apiService.get(`${this.baseUrl}/disaster-risk`, { location, disasterType });
  }

  async subscribeToWeatherAlerts(location: string, alertType: string, callbackUrl: string): Promise<void> {
    return apiService.post(`${this.baseUrl}/subscriptions`, { location, alertType, callbackUrl });
  }
}

export const weatherService = new WeatherService();


