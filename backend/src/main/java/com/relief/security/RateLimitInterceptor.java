package com.relief.security;

import com.relief.service.RateLimitService;
import com.relief.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        // Different rate limits for different endpoints
        RateLimitConfig config = getRateLimitConfig(endpoint, method);
        
        if (!rateLimitService.isAllowed(clientId, endpoint, config)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.getMaxRequests()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(
                System.currentTimeMillis() + config.getWindowSizeInSeconds() * 1000
            ));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            return false;
        }

        // Add rate limit headers
        RateLimitInfo info = rateLimitService.getRateLimitInfo(clientId, endpoint, config);
        response.setHeader("X-RateLimit-Limit", String.valueOf(config.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(info.getRemainingRequests()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTime()));

        return true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user ID from security context first
        String userId = getUserIdFromSecurityContext();
        if (userId != null) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
    }

    private String getUserIdFromSecurityContext() {
        try {
            // This would be implemented based on your security context
            // For now, return null to use IP-based limiting
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private RateLimitConfig getRateLimitConfig(String endpoint, String method) {
        // Role-based override
        String role = getRole();
        RateLimitProperties.Policy rolePolicy = role != null ? rateLimitProperties.getRoles().get(role) : null;
        if (rolePolicy != null && rolePolicy.getLimit() != null) {
            return new RateLimitConfig(rolePolicy.getLimit(), rolePolicy.getWindowSeconds() != null ? rolePolicy.getWindowSeconds() : rateLimitProperties.getDefaultWindowSeconds());
        }

        // IP-based override
        // Note: simplistic match; for production use CIDR matcher
        // Skipped here for brevity

        // Define different rate limits for different endpoints
        if (endpoint.startsWith("/api/auth/")) {
            return new RateLimitConfig(5, 60); // 5 requests per minute for auth
        } else if (endpoint.startsWith("/api/resident/needs")) {
            return new RateLimitConfig(10, 60); // 10 requests per minute for needs creation
        } else if (endpoint.startsWith("/api/admin/")) {
            return new RateLimitConfig(20, 60); // 20 requests per minute for admin
        } else if (endpoint.startsWith("/api/analytics/")) {
            return new RateLimitConfig(30, 60); // 30 requests per minute for analytics
        } else if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            return new RateLimitConfig(15, 60); // 15 requests per minute for write operations
        } else {
            return new RateLimitConfig(rateLimitProperties.getDefaultLimit(), rateLimitProperties.getDefaultWindowSeconds());
        }
    }

    private String getRole() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static class RateLimitConfig {
        private final int maxRequests;
        private final int windowSizeInSeconds;

        public RateLimitConfig(int maxRequests, int windowSizeInSeconds) {
            this.maxRequests = maxRequests;
            this.windowSizeInSeconds = windowSizeInSeconds;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public int getWindowSizeInSeconds() {
            return windowSizeInSeconds;
        }
    }

    public static class RateLimitInfo {
        private final int remainingRequests;
        private final long resetTime;

        public RateLimitInfo(int remainingRequests, long resetTime) {
            this.remainingRequests = remainingRequests;
            this.resetTime = resetTime;
        }

        public int getRemainingRequests() {
            return remainingRequests;
        }

        public long getResetTime() {
            return resetTime;
        }
    }
}
