# Security Hardening Guide

## 🔒 Overview

This guide provides comprehensive security hardening measures for the Disaster Relief Platform, covering infrastructure, application, and operational security. Follow these guidelines to ensure maximum security for sensitive disaster response data.

## 🏗️ Infrastructure Hardening

### Server Hardening

#### Operating System Security

```bash
# Ubuntu/Debian Hardening
sudo apt update && sudo apt upgrade -y

# Install security tools
sudo apt install -y ufw fail2ban aide rkhunter chkrootkit

# Configure firewall
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# Configure fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# Configure AIDE for file integrity monitoring
sudo aideinit
sudo mv /var/lib/aide/aide.db.new /var/lib/aide/aide.db
sudo crontab -e
# Add: 0 2 * * * /usr/bin/aide --check
```

#### Docker Security

```yaml
# docker-compose.security.yml
version: '3.8'

services:
  backend:
    image: disaster-relief-backend:latest
    container_name: disaster-relief-backend
    security_opt:
      - no-new-privileges:true
      - seccomp:unconfined
    cap_drop:
      - ALL
    cap_add:
      - NET_BIND_SERVICE
    read_only: true
    tmpfs:
      - /tmp:noexec,nosuid,size=100m
    user: "1000:1000"
    environment:
      - SPRING_PROFILES_ACTIVE=production
    networks:
      - internal
    restart: unless-stopped

  frontend:
    image: disaster-relief-frontend:latest
    container_name: disaster-relief-frontend
    security_opt:
      - no-new-privileges:true
    cap_drop:
      - ALL
    read_only: true
    tmpfs:
      - /tmp:noexec,nosuid,size=50m
    user: "1000:1000"
    networks:
      - internal
    restart: unless-stopped

networks:
  internal:
    driver: bridge
    internal: true
```

#### Network Security

```yaml
# Network segmentation
Production Network:
  - DMZ: Load balancer, reverse proxy
  - Application: Backend, frontend containers
  - Database: Database servers
  - Management: Monitoring, logging

Security Zones:
  - Public: Internet-facing services
  - DMZ: Semi-trusted services
  - Internal: Trusted services
  - Management: Administrative services
```

### Database Hardening

#### PostgreSQL Security

```sql
-- Create secure database user
CREATE USER disaster_relief_app WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE disaster_relief TO disaster_relief_app;
GRANT USAGE ON SCHEMA public TO disaster_relief_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO disaster_relief_app;

-- Enable SSL
ALTER SYSTEM SET ssl = on;
ALTER SYSTEM SET ssl_cert_file = '/etc/ssl/certs/server.crt';
ALTER SYSTEM SET ssl_key_file = '/etc/ssl/private/server.key';

-- Configure authentication
ALTER SYSTEM SET password_encryption = 'scram-sha-256';
ALTER SYSTEM SET log_connections = on;
ALTER SYSTEM SET log_disconnections = on;
ALTER SYSTEM SET log_statement = 'all';

-- Enable row-level security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY user_access_policy ON users FOR ALL TO disaster_relief_app 
  USING (id = current_user_id());

-- Configure connection limits
ALTER USER disaster_relief_app CONNECTION LIMIT 10;
```

#### Redis Security

```conf
# redis.conf
# Disable dangerous commands
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command KEYS ""
rename-command CONFIG ""
rename-command SHUTDOWN ""
rename-command DEBUG ""

# Enable authentication
requirepass secure_redis_password

# Bind to specific interface
bind 127.0.0.1

# Disable protected mode
protected-mode yes

# Enable SSL
tls-port 6380
tls-cert-file /etc/ssl/certs/redis.crt
tls-key-file /etc/ssl/private/redis.key
tls-ca-cert-file /etc/ssl/certs/ca.crt
```

### Container Security

#### Dockerfile Security

```dockerfile
# Multi-stage build for security
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production image
FROM openjdk:17-jre-slim
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy application
COPY --from=builder /app/target/*.jar app.jar

# Set security options
USER appuser
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Container Scanning

```bash
# Scan container images for vulnerabilities
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image disaster-relief-backend:latest

# Scan with specific severity levels
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image --severity HIGH,CRITICAL disaster-relief-backend:latest

# Generate security report
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image --format json --output security-report.json \
  disaster-relief-backend:latest
```

## 🔐 Application Security

### Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            .build();
    }
}
```

### Input Validation

```java
@RestController
@Validated
public class NeedsController {

    @PostMapping("/needs")
    public ResponseEntity<NeedsRequest> createNeed(
            @Valid @RequestBody CreateNeedRequest request,
            Authentication authentication) {
        
        // Additional validation
        if (request.getSeverity() < 1 || request.getSeverity() > 5) {
            throw new ValidationException("Severity must be between 1 and 5");
        }
        
        // Sanitize input
        String sanitizedDescription = sanitizeInput(request.getDescription());
        request.setDescription(sanitizedDescription);
        
        return ResponseEntity.ok(needsService.createNeed(request));
    }
    
    private String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("script", "")
                   .replaceAll("javascript:", "")
                   .trim();
    }
}
```

### SQL Injection Prevention

```java
@Repository
public class NeedsRequestRepository extends JpaRepository<NeedsRequest, UUID> {
    
    // Use parameterized queries
    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.severity >= :minSeverity")
    List<NeedsRequest> findByMinSeverity(@Param("minSeverity") Integer minSeverity);
    
    // Use native queries with parameters
    @Query(value = "SELECT * FROM needs_requests WHERE ST_DWithin(geom_point, ST_GeomFromText(:point, 4326), :radius)", 
           nativeQuery = true)
    List<NeedsRequest> findNearbyRequests(@Param("point") String point, @Param("radius") double radius);
    
    // Avoid string concatenation in queries
    // BAD: @Query("SELECT nr FROM NeedsRequest nr WHERE nr.category = '" + category + "'")
    // GOOD: Use @Param annotation
}
```

### XSS Prevention

```java
@Component
public class XssProtectionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        XssRequestWrapper wrappedRequest = new XssRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XssRequestWrapper extends HttpServletRequestWrapper {
    
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitizeXss(value);
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                values[i] = sanitizeXss(values[i]);
            }
        }
        return values;
    }
    
    private String sanitizeXss(String value) {
        if (value == null) return null;
        
        return value.replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("&", "&amp;");
    }
}
```

## 🚦 Rate Limiting Implementation

### Redis-based Rate Limiting

```java
@Service
public class RateLimitService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String clientId, String endpoint, RateLimitConfig config) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        String countKey = "rate_limit_count:" + clientId + ":" + endpoint;
        String windowKey = "rate_limit_window:" + clientId + ":" + endpoint;
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (config.getWindowSizeInSeconds() * 1000);
        
        // Check if we're in a new window
        String windowStartStr = redisTemplate.opsForValue().get(windowKey);
        if (windowStartStr == null || Long.parseLong(windowStartStr) < windowStart) {
            // New window, reset counter
            redisTemplate.opsForValue().set(countKey, "1", 
                Duration.ofSeconds(config.getWindowSizeInSeconds()));
            redisTemplate.opsForValue().set(windowKey, String.valueOf(currentTime), 
                Duration.ofSeconds(config.getWindowSizeInSeconds()));
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
}
```

### Rate Limiting Configuration (role/IP aware)

```yaml
# application.yml
security:
  rate-limiting:
    enabled: true
    redis-url: ${REDIS_URL}
    default-limit: 100
    default-window-seconds: 60
    roles:
      ADMIN:
        limit: 300
        window-seconds: 60
      DISPATCHER:
        limit: 200
        window-seconds: 60
      HELPER:
        limit: 150
        window-seconds: 60
      RESIDENT:
        limit: 100
        window-seconds: 60
    ip-policies:
      - cidr: 10.0.0.0/8
        limit: 500
        window-seconds: 60
```

### Malware Scanning on Uploads

- Integrate ClamAV or ICAP gateway; scan bytes before processing.
- Fail closed on scanner unavailability for sensitive contexts.
- Quarantine suspicious files and alert security.

### Configurable Privacy Redaction Levels

```yaml
redaction:
  levels:
    standard:
      remove-metadata: true
      remove-gps: true
    enhanced:
      remove-metadata: true
      remove-gps: true
      blur-faces: true
      redact-text-patterns: ["phone", "email"]
    maximum:
      remove-metadata: true
      remove-gps: true
      blur-faces: true
      strip-colorspace: true
      downscale: 1080
```

### Secret Rotation Runbook

1) JWT keys: dual-publish new public key, rotate signer, retire old after TTL.
2) DB creds: provision new user, update External Secrets, roll pods, revoke old.
3) MinIO keys: create new pair, update secrets, verify, revoke old.
4) Redis ACL: add new user/pass, switch app, remove old.
5) Validate via health checks, audit logs; keep rollback plan ready.

## 🖼️ Media Security and Redaction

### Media Upload Security

```java
@RestController
@RequestMapping("/api/media")
public class MediaController {
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String context,
            Authentication authentication) {
        
        // Validate file type
        if (!isAllowedFileType(file.getOriginalFilename())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File type not allowed"));
        }
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File too large"));
        }
        
        // Scan for malware
        if (malwareScanService.scanFile(file)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File contains malware"));
        }
        
        // Process with redaction
        MediaRedactionService.RedactionResult result = 
            mediaRedactionService.processMedia(file, authentication.getName(), context);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "uploadUrl", result.getUploadUrl(),
            "redacted", result.wasRedacted()
        ));
    }
    
    private boolean isAllowedFileType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
```

### Media Redaction Implementation

```java
@Service
public class MediaRedactionService {
    
    public RedactionResult processMedia(MultipartFile file, String userId, String context) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            
            if (!requiresRedaction(fileExtension)) {
                return new RedactionResult(false, file.getBytes(), originalFilename);
            }
            
            // Log redaction attempt
            auditService.logAdminAction("MEDIA_REDACTION_STARTED", 
                "Media redaction started for file: " + originalFilename);
            
            byte[] redactedContent = performRedaction(file.getBytes(), fileExtension);
            String redactedFilename = generateRedactedFilename(originalFilename);
            
            // Log successful redaction
            auditService.logAdminAction("MEDIA_REDACTION_COMPLETED", 
                "Media redaction completed for file: " + originalFilename);
            
            return new RedactionResult(true, redactedContent, redactedFilename);
            
        } catch (Exception e) {
            auditService.logAdminAction("MEDIA_REDACTION_FAILED", 
                "Media redaction failed: " + e.getMessage());
            throw new RuntimeException("Failed to process media", e);
        }
    }
    
    private byte[] performRedaction(byte[] content, String fileExtension) {
        // Implement redaction based on file type
        if (isImageFile(fileExtension)) {
            return redactImage(content);
        } else if (isVideoFile(fileExtension)) {
            return redactVideo(content);
        } else if (isDocumentFile(fileExtension)) {
            return redactDocument(content);
        }
        return content;
    }
}
```

## 🔍 Security Monitoring

### Security Event Logging

```java
@Component
public class SecurityEventLogger {
    
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = getClientIpAddress();
        
        auditService.logSecurityEvent("AUTHENTICATION_FAILURE", 
            "Failed login attempt for user: " + username + " from IP: " + ipAddress);
        
        // Check for brute force attempts
        if (isBruteForceAttempt(username, ipAddress)) {
            auditService.logSecurityEvent("BRUTE_FORCE_ATTEMPT", 
                "Brute force attack detected for user: " + username);
            // Implement account lockout or IP blocking
        }
    }
    
    @EventListener
    public void handleAuthorizationFailure(AuthorizationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String resource = event.getSource().toString();
        
        auditService.logSecurityEvent("AUTHORIZATION_FAILURE", 
            "Unauthorized access attempt by user: " + username + " to resource: " + resource);
    }
}
```

### Security Metrics

```java
@Component
public class SecurityMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter loginAttempts;
    private final Counter loginFailures;
    private final Counter rateLimitViolations;
    private final Counter mediaRedactions;
    
    public SecurityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.loginAttempts = Counter.builder("security_login_attempts_total")
            .description("Total login attempts")
            .register(meterRegistry);
        this.loginFailures = Counter.builder("security_login_failures_total")
            .description("Total login failures")
            .register(meterRegistry);
        this.rateLimitViolations = Counter.builder("security_rate_limit_violations_total")
            .description("Total rate limit violations")
            .register(meterRegistry);
        this.mediaRedactions = Counter.builder("security_media_redactions_total")
            .description("Total media redactions")
            .register(meterRegistry);
    }
    
    public void recordLoginAttempt() {
        loginAttempts.increment();
    }
    
    public void recordLoginFailure() {
        loginFailures.increment();
    }
    
    public void recordRateLimitViolation() {
        rateLimitViolations.increment();
    }
    
    public void recordMediaRedaction() {
        mediaRedactions.increment();
    }
}
```

## 🚨 Incident Response

### Security Incident Response Plan

```yaml
Incident Classification:
  Critical:
    - Data breach
    - System compromise
    - Unauthorized access
    - Malware infection
    Response Time: 15 minutes
  
  High:
    - Suspicious activity
    - Policy violation
    - Security vulnerability
    - Service disruption
    Response Time: 1 hour
  
  Medium:
    - Minor security issue
    - Configuration error
    - Performance degradation
    - User complaint
    Response Time: 4 hours
  
  Low:
    - Information request
    - General inquiry
    - Routine maintenance
    - Documentation update
    Response Time: 24 hours

Response Team:
  - Incident Commander
  - Security Analyst
  - Technical Lead
  - Communications Lead
  - Legal Counsel
  - Management Representative
```

### Incident Response Procedures

```bash
#!/bin/bash
# incident-response.sh

# Incident response automation script

INCIDENT_ID=$(date +%Y%m%d%H%M%S)
LOG_FILE="/var/log/security/incident-$INCIDENT_ID.log"

# Log incident start
echo "$(date): Incident $INCIDENT_ID started" >> $LOG_FILE

# Collect system information
echo "$(date): Collecting system information" >> $LOG_FILE
ps aux >> $LOG_FILE
netstat -tulpn >> $LOG_FILE
ss -tulpn >> $LOG_FILE

# Check for suspicious processes
echo "$(date): Checking for suspicious processes" >> $LOG_FILE
ps aux | grep -E "(nc|netcat|ncat|socat|tcpdump|wireshark)" >> $LOG_FILE

# Check network connections
echo "$(date): Checking network connections" >> $LOG_FILE
netstat -an | grep ESTABLISHED >> $LOG_FILE

# Check file integrity
echo "$(date): Checking file integrity" >> $LOG_FILE
aide --check >> $LOG_FILE

# Check logs for anomalies
echo "$(date): Checking logs for anomalies" >> $LOG_FILE
grep -i "error\|fail\|denied\|unauthorized" /var/log/auth.log >> $LOG_FILE
grep -i "error\|fail\|denied\|unauthorized" /var/log/syslog >> $LOG_FILE

# Create incident report
echo "$(date): Creating incident report" >> $LOG_FILE
cat > "/var/log/security/incident-$INCIDENT_ID-report.txt" << EOF
Incident ID: $INCIDENT_ID
Start Time: $(date)
Status: Active
Description: Security incident detected
Actions Taken: System information collected, logs analyzed
Next Steps: Manual investigation required
EOF

echo "$(date): Incident $INCIDENT_ID response completed" >> $LOG_FILE
```

## 🔧 Security Tools and Scripts

### Security Scanning Script

```bash
#!/bin/bash
# security-scan.sh

# Comprehensive security scanning script

echo "Starting security scan..."

# Update package lists
apt update

# Check for security updates
echo "Checking for security updates..."
apt list --upgradable | grep -i security

# Check for vulnerable packages
echo "Checking for vulnerable packages..."
apt list --installed | grep -E "(openssl|libssl|libcrypto)"

# Check file permissions
echo "Checking file permissions..."
find /etc -type f -perm /o+w 2>/dev/null
find /var -type f -perm /o+w 2>/dev/null

# Check for SUID/SGID files
echo "Checking for SUID/SGID files..."
find / -type f \( -perm -4000 -o -perm -2000 \) 2>/dev/null

# Check for world-writable files
echo "Checking for world-writable files..."
find / -type f -perm -002 2>/dev/null

# Check for empty password fields
echo "Checking for empty password fields..."
awk -F: '($2 == "") {print $1}' /etc/shadow

# Check for accounts with UID 0
echo "Checking for accounts with UID 0..."
awk -F: '($3 == 0) {print $1}' /etc/passwd

# Check for duplicate UIDs
echo "Checking for duplicate UIDs..."
awk -F: '{print $3}' /etc/passwd | sort -n | uniq -d

# Check for duplicate GIDs
echo "Checking for duplicate GIDs..."
awk -F: '{print $3}' /etc/group | sort -n | uniq -d

# Check for suspicious network connections
echo "Checking for suspicious network connections..."
netstat -tulpn | grep -E "(LISTEN|ESTABLISHED)"

# Check for open ports
echo "Checking for open ports..."
nmap -sT -O localhost

# Check for running services
echo "Checking for running services..."
systemctl list-units --type=service --state=running

# Check for cron jobs
echo "Checking for cron jobs..."
crontab -l 2>/dev/null
ls -la /etc/cron.* 2>/dev/null

# Check for scheduled tasks
echo "Checking for scheduled tasks..."
atq 2>/dev/null

# Check for recent logins
echo "Checking for recent logins..."
last -n 20

# Check for failed login attempts
echo "Checking for failed login attempts..."
grep "Failed password" /var/log/auth.log | tail -20

# Check for root login attempts
echo "Checking for root login attempts..."
grep "root" /var/log/auth.log | tail -20

# Check for sudo usage
echo "Checking for sudo usage..."
grep "sudo" /var/log/auth.log | tail -20

# Check for file system integrity
echo "Checking file system integrity..."
aide --check 2>/dev/null

# Check for malware
echo "Checking for malware..."
rkhunter --check 2>/dev/null
chkrootkit 2>/dev/null

# Check for suspicious processes
echo "Checking for suspicious processes..."
ps aux | grep -E "(nc|netcat|ncat|socat|tcpdump|wireshark|nmap|masscan|zmap)"

# Check for suspicious files
echo "Checking for suspicious files..."
find /tmp -type f -name ".*" 2>/dev/null
find /var/tmp -type f -name ".*" 2>/dev/null

# Check for hidden files
echo "Checking for hidden files..."
find /home -name ".*" -type f 2>/dev/null

# Check for large files
echo "Checking for large files..."
find / -type f -size +100M 2>/dev/null

# Check for recently modified files
echo "Checking for recently modified files..."
find / -type f -mtime -1 2>/dev/null

# Check for files with unusual permissions
echo "Checking for files with unusual permissions..."
find / -type f -perm 777 2>/dev/null
find / -type f -perm 666 2>/dev/null

# Check for files owned by root but writable by others
echo "Checking for files owned by root but writable by others..."
find / -type f -user root -perm -002 2>/dev/null

# Check for files with no owner
echo "Checking for files with no owner..."
find / -type f -nouser 2>/dev/null

# Check for files with no group
echo "Checking for files with no group..."
find / -type f -nogroup 2>/dev/null

# Check for files with unusual timestamps
echo "Checking for files with unusual timestamps..."
find / -type f -newermt "2020-01-01" -not -newermt "2024-01-01" 2>/dev/null

# Check for files with unusual sizes
echo "Checking for files with unusual sizes..."
find / -type f -size 0 2>/dev/null

# Check for files with unusual names
echo "Checking for files with unusual names..."
find / -type f -name "*.exe" 2>/dev/null
find / -type f -name "*.bat" 2>/dev/null
find / -type f -name "*.cmd" 2>/dev/null

# Check for files with unusual extensions
echo "Checking for files with unusual extensions..."
find / -type f -name "*.php" 2>/dev/null
find / -type f -name "*.asp" 2>/dev/null
find / -type f -name "*.jsp" 2>/dev/null

# Check for files with unusual content
echo "Checking for files with unusual content..."
find / -type f -exec grep -l "eval(" {} \; 2>/dev/null
find / -type f -exec grep -l "base64_decode" {} \; 2>/dev/null
find / -type f -exec grep -l "system(" {} \; 2>/dev/null

# Check for files with unusual permissions
echo "Checking for files with unusual permissions..."
find / -type f -perm 000 2>/dev/null
find / -type f -perm 001 2>/dev/null
find / -type f -perm 002 2>/dev/null

# Check for files with unusual ownership
echo "Checking for files with unusual ownership..."
find / -type f -user nobody 2>/dev/null
find / -type f -user daemon 2>/dev/null
find / -type f -user bin 2>/dev/null

# Check for files with unusual groups
echo "Checking for files with unusual groups..."
find / -type f -group nobody 2>/dev/null
find / -type f -group daemon 2>/dev/null
find / -type f -group bin 2>/dev/null

# Check for files with unusual timestamps
echo "Checking for files with unusual timestamps..."
find / -type f -newermt "1970-01-01" -not -newermt "1980-01-01" 2>/dev/null

# Check for files with unusual sizes
echo "Checking for files with unusual sizes..."
find / -type f -size +1G 2>/dev/null

# Check for files with unusual names
echo "Checking for files with unusual names..."
find / -type f -name ".*" 2>/dev/null
find / -type f -name "*..*" 2>/dev/null
find / -type f -name "*...*" 2>/dev/null

# Check for files with unusual extensions
echo "Checking for files with unusual extensions..."
find / -type f -name "*.sh" 2>/dev/null
find / -type f -name "*.py" 2>/dev/null
find / -type f -name "*.pl" 2>/dev/null

# Check for files with unusual content
echo "Checking for files with unusual content..."
find / -type f -exec grep -l "password" {} \; 2>/dev/null
find / -type f -exec grep -l "secret" {} \; 2>/dev/null
find / -type f -exec grep -l "key" {} \; 2>/dev/null

echo "Security scan completed."
```

### Security Monitoring Script

```bash
#!/bin/bash
# security-monitor.sh

# Real-time security monitoring script

while true; do
    # Check for new processes
    ps aux | grep -E "(nc|netcat|ncat|socat|tcpdump|wireshark|nmap|masscan|zmap)" | while read line; do
        echo "$(date): Suspicious process detected: $line" >> /var/log/security/monitor.log
        # Send alert
        echo "Suspicious process detected: $line" | mail -s "Security Alert" admin@disaster-relief.local
    done
    
    # Check for new network connections
    netstat -tulpn | grep ESTABLISHED | while read line; do
        echo "$(date): New network connection: $line" >> /var/log/security/monitor.log
    done
    
    # Check for failed login attempts
    grep "Failed password" /var/log/auth.log | tail -1 | while read line; do
        echo "$(date): Failed login attempt: $line" >> /var/log/security/monitor.log
        # Send alert if multiple failures
        if [ $(grep "Failed password" /var/log/auth.log | wc -l) -gt 10 ]; then
            echo "Multiple failed login attempts detected" | mail -s "Security Alert" admin@disaster-relief.local
        fi
    done
    
    # Check for file system changes
    aide --check 2>/dev/null | grep -E "(Added|Changed|Removed)" | while read line; do
        echo "$(date): File system change: $line" >> /var/log/security/monitor.log
        # Send alert
        echo "File system change detected: $line" | mail -s "Security Alert" admin@disaster-relief.local
    done
    
    # Check for suspicious files
    find /tmp -type f -name ".*" 2>/dev/null | while read file; do
        echo "$(date): Suspicious file in /tmp: $file" >> /var/log/security/monitor.log
        # Send alert
        echo "Suspicious file detected: $file" | mail -s "Security Alert" admin@disaster-relief.local
    done
    
    # Sleep for 60 seconds
    sleep 60
done
```

## 📊 Security Compliance

### Security Checklist

```yaml
Infrastructure Security:
  - [ ] Operating system hardened
  - [ ] Firewall configured
  - [ ] Intrusion detection enabled
  - [ ] File integrity monitoring
  - [ ] Log monitoring enabled
  - [ ] Backup encryption enabled
  - [ ] Network segmentation implemented
  - [ ] Access controls configured

Application Security:
  - [ ] Input validation implemented
  - [ ] Output encoding enabled
  - [ ] SQL injection prevention
  - [ ] XSS protection enabled
  - [ ] CSRF protection enabled
  - [ ] Rate limiting implemented
  - [ ] Authentication secured
  - [ ] Authorization configured

Data Security:
  - [ ] Encryption at rest enabled
  - [ ] Encryption in transit enabled
  - [ ] Key management implemented
  - [ ] Data classification applied
  - [ ] Access logging enabled
  - [ ] Data retention configured
  - [ ] Secure deletion implemented
  - [ ] Privacy protection enabled

Monitoring Security:
  - [ ] Security monitoring enabled
  - [ ] Incident response plan
  - [ ] Security metrics collected
  - [ ] Alerting configured
  - [ ] Log analysis automated
  - [ ] Threat detection enabled
  - [ ] Vulnerability scanning
  - [ ] Penetration testing

Compliance Security:
  - [ ] Privacy policy implemented
  - [ ] Data protection measures
  - [ ] Regulatory compliance
  - [ ] Security documentation
  - [ ] Training program
  - [ ] Audit procedures
  - [ ] Incident response
  - [ ] Risk management
```

### Security Testing

```bash
#!/bin/bash
# security-test.sh

# Comprehensive security testing script

echo "Starting security testing..."

# Test authentication
echo "Testing authentication..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' \
  -w "HTTP Status: %{http_code}\n"

# Test authorization
echo "Testing authorization..."
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer invalid_token" \
  -w "HTTP Status: %{http_code}\n"

# Test rate limiting
echo "Testing rate limiting..."
for i in {1..10}; do
  curl -X GET http://localhost:8080/api/needs \
    -w "HTTP Status: %{http_code}\n"
done

# Test input validation
echo "Testing input validation..."
curl -X POST http://localhost:8080/api/needs \
  -H "Content-Type: application/json" \
  -d '{"description":"<script>alert(\"XSS\")</script>","severity":10}' \
  -w "HTTP Status: %{http_code}\n"

# Test SQL injection
echo "Testing SQL injection..."
curl -X GET "http://localhost:8080/api/needs?search=' OR 1=1--" \
  -w "HTTP Status: %{http_code}\n"

# Test file upload
echo "Testing file upload..."
curl -X POST http://localhost:8080/api/media/upload \
  -F "file=@/etc/passwd" \
  -w "HTTP Status: %{http_code}\n"

# Test CSRF protection
echo "Testing CSRF protection..."
curl -X POST http://localhost:8080/api/needs \
  -H "Content-Type: application/json" \
  -d '{"description":"test","severity":1}' \
  -w "HTTP Status: %{http_code}\n"

# Test security headers
echo "Testing security headers..."
curl -I http://localhost:8080/api/health \
  -w "HTTP Status: %{http_code}\n"

# Test SSL/TLS
echo "Testing SSL/TLS..."
openssl s_client -connect localhost:443 -servername localhost

# Test port scanning
echo "Testing port scanning..."
nmap -sS -O localhost

# Test vulnerability scanning
echo "Testing vulnerability scanning..."
nmap --script vuln localhost

echo "Security testing completed."
```

---

**Last Updated**: 2024-01-15  
**Version**: 1.0  
**Next Review**: 2024-04-15
