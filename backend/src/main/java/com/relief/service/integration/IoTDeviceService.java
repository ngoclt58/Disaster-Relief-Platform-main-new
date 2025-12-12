package com.relief.service.integration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * IoT device integration service for sensors, drones, and other IoT devices
 */
@Service
@RequiredArgsConstructor
public class IoTDeviceService {

    private static final Logger log = LoggerFactory.getLogger(IoTDeviceService.class);

    private final RestTemplate restTemplate;
    private final IntegrationConfigService integrationConfigService;

    public List<IoTDevice> getDevices(String deviceType, String status) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices?type=%s&status=%s", apiUrl, deviceType, status);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<IoTDevice[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, IoTDevice[].class
            );
            
            log.info("Retrieved {} IoT devices of type: {}", response.getBody().length, deviceType);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving IoT devices", e);
            return Collections.emptyList();
        }
    }

    public IoTDeviceData getDeviceData(String deviceId, String dataType, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices/%s/data?type=%s&start=%s&end=%s", 
                apiUrl, deviceId, dataType, startTime.toString(), endTime.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<IoTDeviceData> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, IoTDeviceData.class
            );
            
            log.info("Retrieved device data for device: {}", deviceId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving device data", e);
            return new IoTDeviceData();
        }
    }

    public boolean controlDevice(String deviceId, String action, Map<String, Object> parameters) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices/%s/control", apiUrl, deviceId);
            
            Map<String, Object> request = new HashMap<>();
            request.put("action", action);
            request.put("parameters", parameters);
            request.put("timestamp", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
            );
            
            log.info("Sent control command to device: {} - action: {}", deviceId, action);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error controlling device", e);
            return false;
        }
    }

    public List<DroneMission> getDroneMissions(String status) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/drones/missions?status=%s", apiUrl, status);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<DroneMission[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, DroneMission[].class
            );
            
            log.info("Retrieved {} drone missions with status: {}", response.getBody().length, status);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving drone missions", e);
            return Collections.emptyList();
        }
    }

    public DroneMission createDroneMission(String missionType, String area, Map<String, Object> parameters) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = apiUrl + "/drones/missions";
            
            Map<String, Object> request = new HashMap<>();
            request.put("missionType", missionType);
            request.put("area", area);
            request.put("parameters", parameters);
            request.put("createdAt", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<DroneMission> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, DroneMission.class
            );
            
            log.info("Created drone mission: {}", response.getBody().getId());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating drone mission", e);
            return new DroneMission();
        }
    }

    public List<SensorReading> getSensorReadings(String sensorId, String sensorType, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/sensors/%s/readings?type=%s&start=%s&end=%s", 
                apiUrl, sensorId, sensorType, startTime.toString(), endTime.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SensorReading[]> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, SensorReading[].class
            );
            
            log.info("Retrieved {} sensor readings for sensor: {}", response.getBody().length, sensorId);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error retrieving sensor readings", e);
            return Collections.emptyList();
        }
    }

    public IoTAlert getDeviceAlerts(String deviceId, String alertType) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices/%s/alerts?type=%s", apiUrl, deviceId, alertType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<IoTAlert> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, IoTAlert.class
            );
            
            log.info("Retrieved alerts for device: {}", deviceId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving device alerts", e);
            return new IoTAlert();
        }
    }

    public IoTDeviceStatus getDeviceStatus(String deviceId) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices/%s/status", apiUrl, deviceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<IoTDeviceStatus> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, IoTDeviceStatus.class
            );
            
            log.info("Retrieved status for device: {}", deviceId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving device status", e);
            return new IoTDeviceStatus();
        }
    }

    public IoTDeviceAnalytics getDeviceAnalytics(String deviceId, String timeRange) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = String.format("%s/devices/%s/analytics?timeRange=%s", apiUrl, deviceId, timeRange);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<IoTDeviceAnalytics> response = restTemplate.exchange(
                endpoint, HttpMethod.GET, entity, IoTDeviceAnalytics.class
            );
            
            log.info("Retrieved analytics for device: {}", deviceId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving device analytics", e);
            return new IoTDeviceAnalytics();
        }
    }

    public void subscribeToDeviceEvents(String deviceId, String eventType, String callbackUrl) {
        try {
            String apiUrl = integrationConfigService.getIoTApiUrl();
            String endpoint = apiUrl + "/subscriptions";
            
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("deviceId", deviceId);
            subscription.put("eventType", eventType);
            subscription.put("callbackUrl", callbackUrl);
            subscription.put("createdAt", LocalDateTime.now());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + integrationConfigService.getIoTApiKey());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(subscription, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
            );
            
            log.info("Subscribed to device events for device: {}", deviceId);
        } catch (Exception e) {
            log.error("Error subscribing to device events", e);
        }
    }

    // Data classes
    public static class IoTDevice {
        private String id;
        private String name;
        private String type;
        private String status;
        private String location;
        private double latitude;
        private double longitude;
        private Map<String, Object> capabilities;
        private LocalDateTime lastSeen;
        private String firmwareVersion;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public Map<String, Object> getCapabilities() { return capabilities; }
        public void setCapabilities(Map<String, Object> capabilities) { this.capabilities = capabilities; }

        public LocalDateTime getLastSeen() { return lastSeen; }
        public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

        public String getFirmwareVersion() { return firmwareVersion; }
        public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    }

    public static class IoTDeviceData {
        private String deviceId;
        private String dataType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<DataPoint> dataPoints;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public List<DataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<DataPoint> dataPoints) { this.dataPoints = dataPoints; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class DataPoint {
        private LocalDateTime timestamp;
        private double value;
        private String unit;
        private Map<String, Object> additionalData;

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
    }

    public static class DroneMission {
        private String id;
        private String missionType;
        private String area;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Map<String, Object> parameters;
        private List<String> waypoints;
        private String pilotId;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getMissionType() { return missionType; }
        public void setMissionType(String missionType) { this.missionType = missionType; }

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public List<String> getWaypoints() { return waypoints; }
        public void setWaypoints(List<String> waypoints) { this.waypoints = waypoints; }

        public String getPilotId() { return pilotId; }
        public void setPilotId(String pilotId) { this.pilotId = pilotId; }
    }

    public static class SensorReading {
        private String id;
        private String sensorId;
        private String sensorType;
        private double value;
        private String unit;
        private LocalDateTime timestamp;
        private String location;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSensorId() { return sensorId; }
        public void setSensorId(String sensorId) { this.sensorId = sensorId; }

        public String getSensorType() { return sensorType; }
        public void setSensorType(String sensorType) { this.sensorType = sensorType; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class IoTAlert {
        private String id;
        private String deviceId;
        private String alertType;
        private String severity;
        private String message;
        private LocalDateTime createdAt;
        private String location;
        private Map<String, Object> data;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    public static class IoTDeviceStatus {
        private String deviceId;
        private String status;
        private double batteryLevel;
        private String signalStrength;
        private LocalDateTime lastUpdate;
        private Map<String, Object> diagnostics;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public double getBatteryLevel() { return batteryLevel; }
        public void setBatteryLevel(double batteryLevel) { this.batteryLevel = batteryLevel; }

        public String getSignalStrength() { return signalStrength; }
        public void setSignalStrength(String signalStrength) { this.signalStrength = signalStrength; }

        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

        public Map<String, Object> getDiagnostics() { return diagnostics; }
        public void setDiagnostics(Map<String, Object> diagnostics) { this.diagnostics = diagnostics; }
    }

    public static class IoTDeviceAnalytics {
        private String deviceId;
        private String timeRange;
        private Map<String, Object> performanceMetrics;
        private List<String> trends;
        private Map<String, Object> usageStatistics;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

        public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }

        public List<String> getTrends() { return trends; }
        public void setTrends(List<String> trends) { this.trends = trends; }

        public Map<String, Object> getUsageStatistics() { return usageStatistics; }
        public void setUsageStatistics(Map<String, Object> usageStatistics) { this.usageStatistics = usageStatistics; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }
}


