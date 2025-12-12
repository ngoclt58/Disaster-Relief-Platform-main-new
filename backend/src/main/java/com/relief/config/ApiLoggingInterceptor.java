package com.relief.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(ApiLoggingInterceptor.class);

    private static final String START_TIME_ATTRIBUTE = "API_START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Get authenticated user if available
        String username = getCurrentUsername();

        // Log request parameters
        Map<String, String> params = getRequestParameters(request);

        logger.info("=== API Request ===");
        logger.info("Method: {}", method);
        logger.info("URI: {}", fullUri);
        logger.info("Client IP: {}", clientIp);
        logger.info("User: {}", username != null ? username : "Anonymous");
        logger.info("User-Agent: {}", userAgent);
        
        if (!params.isEmpty()) {
            logger.info("Request Parameters: {}", params);
        }

        // Log request headers (optional, can be enabled for debugging)
        if (logger.isDebugEnabled()) {
            Map<String, String> headers = getRequestHeaders(request);
            logger.debug("Request Headers: {}", headers);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, @Nullable Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String username = getCurrentUsername();

        logger.info("=== API Response ===");
        logger.info("Method: {}", method);
        logger.info("URI: {}", uri);
        logger.info("Status: {} {}", status, getStatusMessage(status));
        logger.info("User: {}", username != null ? username : "Anonymous");
        logger.info("Duration: {} ms", duration);

        if (ex != null) {
            logger.error("Exception occurred while processing request: {}", uri, ex);
        }

        logger.info("=== End API Call ===\n");
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

    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore if security context is not available
        }
        return null;
    }

    private Map<String, String> getRequestParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            // Mask sensitive parameters
            if (isSensitiveParameter(paramName)) {
                params.put(paramName, "***");
            } else {
                params.put(paramName, paramValue);
            }
        }
        return params;
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // Mask sensitive headers
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "***");
            } else {
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    private boolean isSensitiveParameter(String paramName) {
        String lowerParam = paramName.toLowerCase();
        return lowerParam.contains("password") 
            || lowerParam.contains("token") 
            || lowerParam.contains("secret")
            || lowerParam.contains("key")
            || lowerParam.contains("authorization");
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization")
            || lowerHeader.contains("cookie")
            || lowerHeader.contains("x-api-key");
    }

    private String getStatusMessage(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> "Unknown";
        };
    }
}

