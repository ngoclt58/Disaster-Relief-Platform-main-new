package com.relief.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "security.rate-limiting")
@Data
public class RateLimitProperties {
    private boolean enabled = true;
    private int defaultLimit = 100;
    private int defaultWindowSeconds = 60;
    private Map<String, Policy> roles = new HashMap<>(); // role -> policy
    private List<IpPolicy> ipPolicies; // CIDR based
    private Map<String, Policy> endpoints = new HashMap<>(); // path prefix -> policy

    // Explicit getters for Lombok compatibility
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getDefaultLimit() { return defaultLimit; }
    public void setDefaultLimit(int defaultLimit) { this.defaultLimit = defaultLimit; }

    public int getDefaultWindowSeconds() { return defaultWindowSeconds; }
    public void setDefaultWindowSeconds(int defaultWindowSeconds) { this.defaultWindowSeconds = defaultWindowSeconds; }

    public Map<String, Policy> getRoles() { return roles; }
    public void setRoles(Map<String, Policy> roles) { this.roles = roles; }

    public List<IpPolicy> getIpPolicies() { return ipPolicies; }
    public void setIpPolicies(List<IpPolicy> ipPolicies) { this.ipPolicies = ipPolicies; }

    public Map<String, Policy> getEndpoints() { return endpoints; }
    public void setEndpoints(Map<String, Policy> endpoints) { this.endpoints = endpoints; }

    @Data
    public static class Policy {
        private Integer limit; // requests per window
        private Integer windowSeconds;

        // Explicit getters for Lombok compatibility
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }

        public Integer getWindowSeconds() { return windowSeconds; }
        public void setWindowSeconds(Integer windowSeconds) { this.windowSeconds = windowSeconds; }
    }

    @Data
    public static class IpPolicy {
        private String cidr;
        private Integer limit;
        private Integer windowSeconds;

        // Explicit getters for Lombok compatibility
        public String getCidr() { return cidr; }
        public void setCidr(String cidr) { this.cidr = cidr; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }

        public Integer getWindowSeconds() { return windowSeconds; }
        public void setWindowSeconds(Integer windowSeconds) { this.windowSeconds = windowSeconds; }
    }
}





