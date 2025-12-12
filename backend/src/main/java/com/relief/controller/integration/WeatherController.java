package com.relief.controller.integration;

import com.relief.service.integration.WeatherService;
import com.relief.service.integration.WeatherService.WeatherData;
import com.relief.service.integration.WeatherService.WeatherForecast;
import com.relief.service.integration.WeatherService.WeatherAlert;
import com.relief.service.integration.WeatherService.WeatherMapData;
import com.relief.service.integration.WeatherService.AirQualityData;
import com.relief.service.integration.WeatherService.WeatherStation;
import com.relief.service.integration.WeatherService.WeatherHistoricalData;
import com.relief.service.integration.WeatherService.WeatherDisasterRisk;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Weather service integration controller
 */
@RestController
@RequestMapping("/integration/weather")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weather Integration", description = "Real-time weather data integration")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    @Operation(summary = "Get current weather data")
    public ResponseEntity<WeatherData> getCurrentWeather(
            @RequestParam String location,
            @RequestParam(defaultValue = "metric") String units) {
        
        WeatherData data = weatherService.getCurrentWeather(location, units);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get weather forecast")
    public ResponseEntity<WeatherForecast> getWeatherForecast(
            @RequestParam String location,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "metric") String units) {
        
        WeatherForecast forecast = weatherService.getWeatherForecast(location, days, units);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get weather alerts")
    public ResponseEntity<List<WeatherAlert>> getWeatherAlerts(
            @RequestParam String location,
            @RequestParam(required = false) String severity) {
        
        List<WeatherAlert> alerts = weatherService.getWeatherAlerts(location, severity);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/map")
    @Operation(summary = "Get weather map data")
    public ResponseEntity<WeatherMapData> getWeatherMap(
            @RequestParam String mapType,
            @RequestParam String region) {
        
        WeatherMapData data = weatherService.getWeatherMap(mapType, region);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/air-quality")
    @Operation(summary = "Get air quality data")
    public ResponseEntity<AirQualityData> getAirQuality(
            @RequestParam String location) {
        
        AirQualityData data = weatherService.getAirQuality(location);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/stations")
    @Operation(summary = "Get weather stations")
    public ResponseEntity<List<WeatherStation>> getWeatherStations(
            @RequestParam String region) {
        
        List<WeatherStation> stations = weatherService.getWeatherStations(region);
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/historical")
    @Operation(summary = "Get historical weather data")
    public ResponseEntity<WeatherHistoricalData> getHistoricalWeather(
            @RequestParam String location,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        WeatherHistoricalData data = weatherService.getHistoricalWeather(location, start, end);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/disaster-risk")
    @Operation(summary = "Get disaster risk assessment")
    public ResponseEntity<WeatherDisasterRisk> getDisasterRisk(
            @RequestParam String location,
            @RequestParam String disasterType) {
        
        WeatherDisasterRisk risk = weatherService.getDisasterRisk(location, disasterType);
        return ResponseEntity.ok(risk);
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to weather alerts")
    public ResponseEntity<Void> subscribeToWeatherAlerts(
            @RequestParam String location,
            @RequestParam String alertType,
            @RequestParam String callbackUrl) {
        
        weatherService.subscribeToWeatherAlerts(location, alertType, callbackUrl);
        return ResponseEntity.ok().build();
    }
}


