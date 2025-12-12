package com.relief.domain.indoor;

/**
 * Methods for indoor positioning in GPS-denied environments
 */
public enum PositioningMethod {
    WIFI_FINGERPRINTING,   // WiFi signal fingerprinting
    BLUETOOTH_BEACONS,     // Bluetooth Low Energy beacons
    UWB,                   // Ultra-Wideband positioning
    INFRARED,              // Infrared positioning
    MAGNETIC_FIELD,        // Magnetic field fingerprinting
    VISUAL_LANDMARKS,      // Visual landmark recognition
    PEDESTRIAN_DEAD_RECKONING, // Pedestrian dead reckoning
    INERTIAL_NAVIGATION,   // Inertial navigation system
    CELLULAR,              // Cellular network positioning
    MANUAL_INPUT,          // Manual position input
    QR_CODE,               // QR code scanning
    NFC,                   // Near Field Communication
    CUSTOM                 // Custom positioning method
}



