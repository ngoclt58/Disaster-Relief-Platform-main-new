package com.relief.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Integration configuration service for managing external API configurations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationConfigService {

    @Value("${integration.government.api.url:https://api.government.gov/disaster}")
    private String governmentApiUrl;

    @Value("${integration.government.api.key:}")
    private String governmentApiKey;

    @Value("${integration.weather.api.url:https://api.weather.com/v1}")
    private String weatherApiUrl;

    @Value("${integration.weather.api.key:}")
    private String weatherApiKey;

    @Value("${integration.social-media.api.url:https://api.socialmedia.com/v1}")
    private String socialMediaApiUrl;

    @Value("${integration.social-media.api.key:}")
    private String socialMediaApiKey;

    @Value("${integration.iot.api.url:https://api.iot.com/v1}")
    private String iotApiUrl;

    @Value("${integration.iot.api.key:}")
    private String iotApiKey;

    @Value("${integration.logistics.api.url:https://api.logistics.com/v1}")
    private String logisticsApiUrl;

    @Value("${integration.logistics.api.key:}")
    private String logisticsApiKey;

    public String getGovernmentApiUrl() {
        return governmentApiUrl;
    }

    public String getGovernmentApiKey() {
        return governmentApiKey;
    }

    public String getWeatherApiUrl() {
        return weatherApiUrl;
    }

    public String getWeatherApiKey() {
        return weatherApiKey;
    }

    public String getSocialMediaApiUrl() {
        return socialMediaApiUrl;
    }

    public String getSocialMediaApiKey() {
        return socialMediaApiKey;
    }

    public String getIoTApiUrl() {
        return iotApiUrl;
    }

    public String getIoTApiKey() {
        return iotApiKey;
    }

    public String getLogisticsApiUrl() {
        return logisticsApiUrl;
    }

    public String getLogisticsApiKey() {
        return logisticsApiKey;
    }
}


