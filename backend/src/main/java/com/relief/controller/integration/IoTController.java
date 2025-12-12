package com.relief.controller.integration;

import com.relief.service.integration.IoTDeviceService;
import com.relief.service.integration.IoTDeviceService.IoTDevice;
import com.relief.service.integration.IoTDeviceService.IoTDeviceData;
import com.relief.service.integration.IoTDeviceService.DroneMission;
import com.relief.service.integration.IoTDeviceService.SensorReading;
import com.relief.service.integration.IoTDeviceService.IoTAlert;
import com.relief.service.integration.IoTDeviceService.IoTDeviceStatus;
import com.relief.service.integration.IoTDeviceService.IoTDeviceAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT device integration controller
 */
@RestController
@RequestMapping("/integration/iot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "IoT Device Integration", description = "Connect with sensors, drones, and other IoT devices")
public class IoTController {

    private final IoTDeviceService ioTDeviceService;

    @GetMapping("/devices")
    @Operation(summary = "Get IoT devices")
    public ResponseEntity<List<IoTDevice>> getDevices(
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String status) {
        
        List<IoTDevice> devices = ioTDeviceService.getDevices(deviceType, status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/devices/{deviceId}/data")
    @Operation(summary = "Get device data")
    public ResponseEntity<IoTDeviceData> getDeviceData(
            @PathVariable String deviceId,
            @RequestParam String dataType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        IoTDeviceData data = ioTDeviceService.getDeviceData(deviceId, dataType, start, end);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/devices/{deviceId}/control")
    @Operation(summary = "Control IoT device")
    public ResponseEntity<Boolean> controlDevice(
            @PathVariable String deviceId,
            @RequestParam String action,
            @RequestBody Map<String, Object> parameters) {
        
        boolean success = ioTDeviceService.controlDevice(deviceId, action, parameters);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/drones/missions")
    @Operation(summary = "Get drone missions")
    public ResponseEntity<List<DroneMission>> getDroneMissions(
            @RequestParam(required = false) String status) {
        
        List<DroneMission> missions = ioTDeviceService.getDroneMissions(status);
        return ResponseEntity.ok(missions);
    }

    @PostMapping("/drones/missions")
    @Operation(summary = "Create drone mission")
    public ResponseEntity<DroneMission> createDroneMission(
            @RequestParam String missionType,
            @RequestParam String area,
            @RequestBody Map<String, Object> parameters) {
        
        DroneMission mission = ioTDeviceService.createDroneMission(missionType, area, parameters);
        return ResponseEntity.ok(mission);
    }

    @GetMapping("/sensors/{sensorId}/readings")
    @Operation(summary = "Get sensor readings")
    public ResponseEntity<List<SensorReading>> getSensorReadings(
            @PathVariable String sensorId,
            @RequestParam String sensorType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<SensorReading> readings = ioTDeviceService.getSensorReadings(sensorId, sensorType, start, end);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/devices/{deviceId}/alerts")
    @Operation(summary = "Get device alerts")
    public ResponseEntity<IoTAlert> getDeviceAlerts(
            @PathVariable String deviceId,
            @RequestParam(required = false) String alertType) {
        
        IoTAlert alerts = ioTDeviceService.getDeviceAlerts(deviceId, alertType);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/devices/{deviceId}/status")
    @Operation(summary = "Get device status")
    public ResponseEntity<IoTDeviceStatus> getDeviceStatus(@PathVariable String deviceId) {
        IoTDeviceStatus status = ioTDeviceService.getDeviceStatus(deviceId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/devices/{deviceId}/analytics")
    @Operation(summary = "Get device analytics")
    public ResponseEntity<IoTDeviceAnalytics> getDeviceAnalytics(
            @PathVariable String deviceId,
            @RequestParam String timeRange) {
        
        IoTDeviceAnalytics analytics = ioTDeviceService.getDeviceAnalytics(deviceId, timeRange);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to device events")
    public ResponseEntity<Void> subscribeToDeviceEvents(
            @RequestParam String deviceId,
            @RequestParam String eventType,
            @RequestParam String callbackUrl) {
        
        ioTDeviceService.subscribeToDeviceEvents(deviceId, eventType, callbackUrl);
        return ResponseEntity.ok().build();
    }
}


