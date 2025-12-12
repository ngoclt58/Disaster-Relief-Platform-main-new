package com.relief.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Weather service integration for real-time weather data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final IntegrationConfigService integrationConfigService;

    public WeatherData getCurrentWeather(String location, String units) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/current?location=%s&units=%s", apiUrl, location, units);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherData.class
            );
            
            log.info("Retrieved current weather data for location: {}", location);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving current weather data", e);
            return new WeatherData();
        }
    }

    public WeatherForecast getWeatherForecast(String location, int days, String units) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/forecast?location=%s&days=%d&units=%s", apiUrl, location, days, units);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherForecast> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherForecast.class
            );
            
            log.info("Retrieved weather forecast for location: {} ({} days)", location, days);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving weather forecast", e);
            return new WeatherForecast();
        }
    }

    public List<WeatherAlert> getWeatherAlerts(String location, String severity) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/alerts?location=%s&severity=%s", apiUrl, location, severity);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherAlert[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherAlert[].class
            );
            
            log.info("Retrieved {} weather alerts for location: {}", response.getBody().length, location);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving weather alerts", e);
            return Collections.emptyList();
        }
    }

    public WeatherMapData getWeatherMap(String mapType, String region) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/map?type=%s&region=%s", apiUrl, mapType, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherMapData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherMapData.class
            );
            
            log.info("Retrieved weather map data for region: {}", region);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving weather map data", e);
            return new WeatherMapData();
        }
    }

    public AirQualityData getAirQuality(String location) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/air-quality?location=%s", apiUrl, location);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<AirQualityData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, AirQualityData.class
            );
            
            log.info("Retrieved air quality data for location: {}", location);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving air quality data", e);
            return new AirQualityData();
        }
    }

    public List<WeatherStation> getWeatherStations(String region) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/stations?region=%s", apiUrl, region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherStation[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherStation[].class
            );
            
            log.info("Retrieved {} weather stations for region: {}", response.getBody().length, region);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving weather stations", e);
            return Collections.emptyList();
        }
    }

    public WeatherHistoricalData getHistoricalWeather(String location, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/historical?location=%s&start=%s&end=%s", 
                apiUrl, location, startDate.toString(), endDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherHistoricalData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherHistoricalData.class
            );
            
            log.info("Retrieved historical weather data for location: {}", location);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving historical weather data", e);
            return new WeatherHistoricalData();
        }
    }

    public WeatherDisasterRisk getDisasterRisk(String location, String disasterType) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = String.format("%s/disaster-risk?location=%s&type=%s", apiUrl, location, disasterType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<WeatherDisasterRisk> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, WeatherDisasterRisk.class
            );
            
            log.info("Retrieved disaster risk assessment for location: {}", location);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving disaster risk assessment", e);
            return new WeatherDisasterRisk();
        }
    }

    public void subscribeToWeatherAlerts(String location, String alertType, String callbackUrl) {
        try {
            String apiUrl = integrationConfigService.getWeatherApiUrl();
            String endpoint = apiUrl + "/subscriptions";
            
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("location", location);
            subscription.put("alertType", alertType);
            subscription.put("callbackUrl", callbackUrl);
            subscription.put("createdAt", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getWeatherApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(subscription, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
            );
            
            log.info("Subscribed to weather alerts for location: {}", location);
        } catch (Exception e) {
            log.error("Error subscribing to weather alerts", e);
        }
    }

    // Data classes
    public static class WeatherData {
        private String location;
        private double temperature;
        private double humidity;
        private double pressure;
        private double windSpeed;
        private double windDirection;
        private String condition;
        private String description;
        private LocalDateTime timestamp;
        private Map<String, Object> additionalData;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public double getHumidity() { return humidity; }
        public void setHumidity(double humidity) { this.humidity = humidity; }

        public double getPressure() { return pressure; }
        public void setPressure(double pressure) { this.pressure = pressure; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

        public double getWindDirection() { return windDirection; }
        public void setWindDirection(double windDirection) { this.windDirection = windDirection; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
    }

    public static class WeatherForecast {
        private String location;
        private List<WeatherDay> dailyForecast;
        private List<WeatherHour> hourlyForecast;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public List<WeatherDay> getDailyForecast() { return dailyForecast; }
        public void setDailyForecast(List<WeatherDay> dailyForecast) { this.dailyForecast = dailyForecast; }

        public List<WeatherHour> getHourlyForecast() { return hourlyForecast; }
        public void setHourlyForecast(List<WeatherHour> hourlyForecast) { this.hourlyForecast = hourlyForecast; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class WeatherDay {
        private LocalDateTime date;
        private double maxTemperature;
        private double minTemperature;
        private String condition;
        private double precipitation;
        private double windSpeed;
        private String description;

        // Getters and setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public double getMaxTemperature() { return maxTemperature; }
        public void setMaxTemperature(double maxTemperature) { this.maxTemperature = maxTemperature; }

        public double getMinTemperature() { return minTemperature; }
        public void setMinTemperature(double minTemperature) { this.minTemperature = minTemperature; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class WeatherHour {
        private LocalDateTime time;
        private double temperature;
        private String condition;
        private double precipitation;
        private double windSpeed;

        // Getters and setters
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    }

    public static class WeatherAlert {
        private String id;
        private String location;
        private String alertType;
        private String severity;
        private String title;
        private String description;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
        private List<String> affectedAreas;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getIssuedAt() { return issuedAt; }
        public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public List<String> getAffectedAreas() { return affectedAreas; }
        public void setAffectedAreas(List<String> affectedAreas) { this.affectedAreas = affectedAreas; }
    }

    public static class WeatherMapData {
        private String mapType;
        private String region;
        private String imageUrl;
        private LocalDateTime generatedAt;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getMapType() { return mapType; }
        public void setMapType(String mapType) { this.mapType = mapType; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class AirQualityData {
        private String location;
        private int aqi;
        private String qualityLevel;
        private Map<String, Double> pollutants;
        private LocalDateTime timestamp;
        private String healthRecommendation;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public int getAqi() { return aqi; }
        public void setAqi(int aqi) { this.aqi = aqi; }

        public String getQualityLevel() { return qualityLevel; }
        public void setQualityLevel(String qualityLevel) { this.qualityLevel = qualityLevel; }

        public Map<String, Double> getPollutants() { return pollutants; }
        public void setPollutants(Map<String, Double> pollutants) { this.pollutants = pollutants; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getHealthRecommendation() { return healthRecommendation; }
        public void setHealthRecommendation(String healthRecommendation) { this.healthRecommendation = healthRecommendation; }
    }

    public static class WeatherStation {
        private String id;
        private String name;
        private String location;
        private double latitude;
        private double longitude;
        private String status;
        private List<String> capabilities;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public List<String> getCapabilities() { return capabilities; }
        public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    }

    public static class WeatherHistoricalData {
        private String location;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<WeatherData> historicalData;
        private Map<String, Object> statistics;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public List<WeatherData> getHistoricalData() { return historicalData; }
        public void setHistoricalData(List<WeatherData> historicalData) { this.historicalData = historicalData; }

        public Map<String, Object> getStatistics() { return statistics; }
        public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }
    }

    public static class WeatherDisasterRisk {
        private String location;
        private String disasterType;
        private String riskLevel;
        private double riskScore;
        private List<String> riskFactors;
        private String recommendation;
        private LocalDateTime assessedAt;

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public LocalDateTime getAssessedAt() { return assessedAt; }
        public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
    }
}


