package com.relief.service;

import com.relief.security.RateLimitInterceptor.RateLimitConfig;
import com.relief.security.RateLimitInterceptor.RateLimitInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String RATE_LIMIT_COUNT_PREFIX = "rate_limit_count:";
    private static final String RATE_LIMIT_WINDOW_PREFIX = "rate_limit_window:";

    public boolean isAllowed(String clientId, String endpoint, RateLimitConfig config) {
        String key = RATE_LIMIT_KEY_PREFIX + clientId + ":" + endpoint;
        String countKey = RATE_LIMIT_COUNT_PREFIX + clientId + ":" + endpoint;
        String windowKey = RATE_LIMIT_WINDOW_PREFIX + clientId + ":" + endpoint;

        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (config.getWindowSizeInSeconds() * 1000);

        // Clean up old entries
        cleanupOldEntries(clientId, endpoint, windowStart);

        // Check if we're in a new window
        String windowStartStr = redisTemplate.opsForValue().get(windowKey);
        if (windowStartStr == null || Long.parseLong(windowStartStr) < windowStart) {
            // New window, reset counter
            redisTemplate.opsForValue().set(countKey, "1", Duration.ofSeconds(config.getWindowSizeInSeconds()));
            redisTemplate.opsForValue().set(windowKey, String.valueOf(currentTime), Duration.ofSeconds(config.getWindowSizeInSeconds()));
            return true;
        }

        // Check current count
        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (currentCount >= config.getMaxRequests()) {
            return false;
        }

        // Increment counter
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, Duration.ofSeconds(config.getWindowSizeInSeconds()));

        return true;
    }

    public RateLimitInfo getRateLimitInfo(String clientId, String endpoint, RateLimitConfig config) {
        String countKey = RATE_LIMIT_COUNT_PREFIX + clientId + ":" + endpoint;
        String windowKey = RATE_LIMIT_WINDOW_PREFIX + clientId + ":" + endpoint;

        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        int remaining = Math.max(0, config.getMaxRequests() - currentCount);

        String windowStartStr = redisTemplate.opsForValue().get(windowKey);
        long resetTime = windowStartStr != null ? 
            Long.parseLong(windowStartStr) + (config.getWindowSizeInSeconds() * 1000) :
            System.currentTimeMillis() + (config.getWindowSizeInSeconds() * 1000);

        return new RateLimitInfo(remaining, resetTime);
    }

    private void cleanupOldEntries(String clientId, String endpoint, long windowStart) {
        // This is a simplified cleanup - in production, you might want to use Redis TTL
        // or a more sophisticated cleanup mechanism
        String windowKey = RATE_LIMIT_WINDOW_PREFIX + clientId + ":" + endpoint;
        String windowStartStr = redisTemplate.opsForValue().get(windowKey);
        
        if (windowStartStr != null && Long.parseLong(windowStartStr) < windowStart) {
            // Window has expired, clean up
            String countKey = RATE_LIMIT_COUNT_PREFIX + clientId + ":" + endpoint;
            redisTemplate.delete(countKey);
            redisTemplate.delete(windowKey);
        }
    }

    // Method to check rate limit without incrementing (for header information)
    public boolean checkRateLimit(String clientId, String endpoint, RateLimitConfig config) {
        String countKey = RATE_LIMIT_COUNT_PREFIX + clientId + ":" + endpoint;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        return currentCount < config.getMaxRequests();
    }

    // Method to get current rate limit status
    public RateLimitStatus getRateLimitStatus(String clientId, String endpoint, RateLimitConfig config) {
        String countKey = RATE_LIMIT_COUNT_PREFIX + clientId + ":" + endpoint;
        String windowKey = RATE_LIMIT_WINDOW_PREFIX + clientId + ":" + endpoint;

        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        
        String windowStartStr = redisTemplate.opsForValue().get(windowKey);
        long windowStart = windowStartStr != null ? Long.parseLong(windowStartStr) : System.currentTimeMillis();
        long resetTime = windowStart + (config.getWindowSizeInSeconds() * 1000);

        return new RateLimitStatus(
            currentCount,
            config.getMaxRequests(),
            resetTime,
            currentCount >= config.getMaxRequests()
        );
    }

    public static class RateLimitStatus {
        private final int currentCount;
        private final int maxRequests;
        private final long resetTime;
        private final boolean isLimited;

        public RateLimitStatus(int currentCount, int maxRequests, long resetTime, boolean isLimited) {
            this.currentCount = currentCount;
            this.maxRequests = maxRequests;
            this.resetTime = resetTime;
            this.isLimited = isLimited;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public long getResetTime() {
            return resetTime;
        }

        public boolean isLimited() {
            return isLimited;
        }

        public int getRemainingRequests() {
            return Math.max(0, maxRequests - currentCount);
        }
    }
}



