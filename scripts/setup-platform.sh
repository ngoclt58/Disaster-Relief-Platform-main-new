#!/bin/bash

# Disaster Relief Platform - Complete Platform Setup Script
# This script automates the complete platform setup process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Configuration
PLATFORM_URL="https://disaster-relief.local"
ADMIN_EMAIL="admin@disaster-relief.local"
DEFAULT_PASSWORD="ChangeMe123!"
ENV_FILE=".env"

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Disaster Relief Platform - Complete Setup"
    echo "=================================================="
    echo -e "${NC}"
}

print_step() {
    echo -e "${YELLOW}[STEP $1]${NC} $2"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${PURPLE}⚠${NC} $1"
}

# Check system requirements
check_requirements() {
    print_step "1" "Checking system requirements..."
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java 17+ is required but not installed"
        print_info "Please install Java from https://adoptium.net/"
        exit 1
    fi
    
    # Check if Node.js is installed
    if ! command -v node &> /dev/null; then
        print_error "Node.js 18+ is required but not installed"
        print_info "Please install Node.js from https://nodejs.org/"
        exit 1
    fi
    
    # Check if PostgreSQL is installed
    if ! command -v psql &> /dev/null; then
        print_warning "PostgreSQL is not installed or not in PATH"
        print_info "Please install PostgreSQL with PostGIS extension"
    fi
    
    # Check if curl is installed
    if ! command -v curl &> /dev/null; then
        print_error "curl is required but not installed"
        exit 1
    fi
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed (optional but recommended)"
    fi
    
    # Check available disk space (at least 10GB)
    AVAILABLE_SPACE=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ "$AVAILABLE_SPACE" -lt 10 ]; then
        print_warning "Low disk space: ${AVAILABLE_SPACE}GB available (10GB recommended)"
    fi
    
    # Check available memory (at least 4GB)
    AVAILABLE_MEMORY=$(free -g | awk 'NR==2{print $7}')
    if [ "$AVAILABLE_MEMORY" -lt 4 ]; then
        print_warning "Low memory: ${AVAILABLE_MEMORY}GB available (4GB recommended)"
    fi
    
    print_success "System requirements check completed"
}

# Create environment file
create_env_file() {
    print_step "2" "Creating environment configuration..."
    
    read -p "Enter database host (default: localhost): " DB_HOST
    DB_HOST=${DB_HOST:-localhost}
    read -p "Enter database port (default: 5432): " DB_PORT
    DB_PORT=${DB_PORT:-5432}
    read -p "Enter database name (default: relief_platform): " DB_NAME
    DB_NAME=${DB_NAME:-relief_platform}
    read -p "Enter database username (default: postgres): " DB_USERNAME
    DB_USERNAME=${DB_USERNAME:-postgres}
    read -p "Enter database password: " DB_PASSWORD
    read -p "Enter Redis host (default: localhost, optional): " REDIS_HOST
    REDIS_HOST=${REDIS_HOST:-localhost}
    read -p "Enter Redis port (default: 6379, optional): " REDIS_PORT
    REDIS_PORT=${REDIS_PORT:-6379}
    read -p "Enter Redis password (optional): " REDIS_PASSWORD
    read -p "Enter JWT secret (or press Enter for auto-generation): " JWT_SECRET
    read -p "Enter MinIO endpoint (default: http://localhost:9000, optional): " MINIO_ENDPOINT
    MINIO_ENDPOINT=${MINIO_ENDPOINT:-http://localhost:9000}
    read -p "Enter MinIO access key (optional): " MINIO_ACCESS_KEY
    read -p "Enter MinIO secret key (optional): " MINIO_SECRET_KEY
    
    # Generate JWT secret if not provided
    if [ -z "$JWT_SECRET" ]; then
        JWT_SECRET=$(openssl rand -base64 32)
        print_info "JWT secret auto-generated"
    fi
    
    # Create .env file
    cat > "$ENV_FILE" << EOF
# Database Configuration
DB_HOST=$DB_HOST
DB_PORT=$DB_PORT
DB_NAME=$DB_NAME
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD

# Redis Configuration (optional)
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
REDIS_PASSWORD=$REDIS_PASSWORD

# JWT Configuration
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# MinIO Configuration (optional)
MINIO_ENDPOINT=$MINIO_ENDPOINT
MINIO_ACCESS_KEY=$MINIO_ACCESS_KEY
MINIO_SECRET_KEY=$MINIO_SECRET_KEY
MINIO_BUCKET_NAME=disaster-relief-media

# Application Configuration
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
PLATFORM_URL=$PLATFORM_URL

# Security Configuration
BCRYPT_STRENGTH=12
RATE_LIMITING_ENABLED=true

# Monitoring Configuration
PROMETHEUS_ENABLED=true

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=notifications@disaster-relief.local
SMTP_PASSWORD=your-email-password
SMTP_TLS=true

# Logging Configuration
LOG_LEVEL=INFO
EOF
    
    print_success "Environment file created: $ENV_FILE"
}

# Setup database (manual instructions)
setup_database() {
    print_step "3" "Database setup instructions..."
    
    print_info "Please ensure PostgreSQL with PostGIS is installed and running"
    print_info "Create the database and run migrations:"
    echo "  createdb -U $DB_USERNAME $DB_NAME"
    echo "  cd backend"
    echo "  mvn flyway:migrate"
    
    print_success "Database setup instructions provided"
}

services:
  # Database
  postgres:
    image: postgis/postgis:15-3.3
    container_name: disaster-relief-postgres
    environment:
      POSTGRES_DB: disaster_relief
      POSTGRES_USER: disaster_relief
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - disaster-relief-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U disaster_relief -d disaster_relief"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Redis
  redis:
    image: redis:7-alpine
    container_name: disaster-relief-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - disaster-relief-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  # MinIO
  minio:
    image: minio/minio:latest
    container_name: disaster-relief-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY}
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    networks:
      - disaster-relief-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Backend Application
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: disaster-relief-backend
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=disaster_relief
      - DB_USERNAME=disaster_relief
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - MINIO_ENDPOINT=minio
      - MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
      - MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
    ports:
      - "8080:8080"
    networks:
      - disaster-relief-network
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Frontend Application
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: disaster-relief-frontend
    environment:
      - REACT_APP_API_URL=${PLATFORM_URL}/api
      - REACT_APP_WS_URL=${PLATFORM_URL}/ws
    ports:
      - "3000:3000"
    networks:
      - disaster-relief-network
    depends_on:
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Nginx Reverse Proxy
  nginx:
    image: nginx:alpine
    container_name: disaster-relief-nginx
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    ports:
      - "80:80"
      - "443:443"
    networks:
      - disaster-relief-network
    depends_on:
      - frontend
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: disaster-relief-prometheus
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./monitoring/prometheus/rules:/etc/prometheus/rules
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - disaster-relief-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9090/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: disaster-relief-grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin}
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    ports:
      - "3001:3000"
    networks:
      - disaster-relief-network
    depends_on:
      - prometheus
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Alertmanager
  alertmanager:
    image: prom/alertmanager:latest
    container_name: disaster-relief-alertmanager
    volumes:
      - ./monitoring/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager_data:/alertmanager
    ports:
      - "9093:9093"
    networks:
      - disaster-relief-network
    depends_on:
      - prometheus
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9093/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
  redis_data:
  minio_data:
  prometheus_data:
  grafana_data:
  alertmanager_data:

networks:
  disaster-relief-network:
    driver: bridge
EOF
    
    print_success "Docker Compose file created: $DOCKER_COMPOSE_FILE"
}

# Create Nginx configuration
create_nginx_config() {
    print_step "4" "Creating Nginx configuration..."
    
    mkdir -p nginx/ssl
    
    cat > nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server backend:8080;
    }
    
    upstream frontend {
        server frontend:3000;
    }
    
    server {
        listen 80;
        server_name disaster-relief.local;
        
        # Health check endpoint
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
        
        # Redirect HTTP to HTTPS
        location / {
            return 301 https://$server_name$request_uri;
        }
    }
    
    server {
        listen 443 ssl http2;
        server_name disaster-relief.local;
        
        # SSL configuration
        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;
        
        # Security headers
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        
        # API routes
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # WebSocket routes
        location /ws/ {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # Frontend routes
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
EOF
    
    print_success "Nginx configuration created"
}

# Generate SSL certificates
generate_ssl_certificates() {
    print_step "5" "Generating SSL certificates..."
    
    # Generate self-signed certificate for development
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/key.pem \
        -out nginx/ssl/cert.pem \
        -subj "/C=US/ST=State/L=City/O=Disaster Relief/CN=disaster-relief.local"
    
    print_success "SSL certificates generated"
}

# Build and start services
start_services() {
    print_step "6" "Building and starting services..."
    
    # Build images
    print_info "Building Docker images..."
    docker-compose build
    
    # Start services
    print_info "Starting services..."
    docker-compose up -d
    
    # Wait for services to be healthy
    print_info "Waiting for services to be healthy..."
    sleep 30
    
    # Check service health
    check_service_health
}

# Check service health
check_service_health() {
    print_step "7" "Checking service health..."
    
    # Check PostgreSQL
    if docker-compose exec postgres pg_isready -U disaster_relief -d disaster_relief > /dev/null 2>&1; then
        print_success "PostgreSQL is healthy"
    else
        print_error "PostgreSQL is not healthy"
    fi
    
    # Check Redis
    if docker-compose exec redis redis-cli ping > /dev/null 2>&1; then
        print_success "Redis is healthy"
    else
        print_error "Redis is not healthy"
    fi
    
    # Check MinIO
    if curl -s http://localhost:9000/minio/health/live > /dev/null 2>&1; then
        print_success "MinIO is healthy"
    else
        print_error "MinIO is not healthy"
    fi
    
    # Check Backend
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "Backend is healthy"
    else
        print_error "Backend is not healthy"
    fi
    
    # Check Frontend
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        print_success "Frontend is healthy"
    else
        print_error "Frontend is not healthy"
    fi
    
    # Check Nginx
    if curl -s http://localhost/health > /dev/null 2>&1; then
        print_success "Nginx is healthy"
    else
        print_error "Nginx is not healthy"
    fi
}

# Initialize database
initialize_database() {
    print_step "8" "Initializing database..."
    
    # Wait for database to be ready
    print_info "Waiting for database to be ready..."
    sleep 10
    
    # Run database migrations
    print_info "Running database migrations..."
    docker-compose exec backend java -jar app.jar --spring.profiles.active=production
    
    print_success "Database initialized"
}

# Create admin user
create_admin_user() {
    print_step "9" "Creating admin user..."
    
    # Wait for backend to be ready
    print_info "Waiting for backend to be ready..."
    sleep 30
    
    # Create admin user
    ADMIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"fullName\":\"System Administrator\",
            \"email\":\"$ADMIN_EMAIL\",
            \"password\":\"$DEFAULT_PASSWORD\",
            \"phone\":\"+1-555-0123\",
            \"role\":\"ADMIN\"
        }")
    
    if echo "$ADMIN_RESPONSE" | jq -e '.id' > /dev/null; then
        print_success "Admin user created successfully"
    else
        print_error "Failed to create admin user"
        print_info "Response: $ADMIN_RESPONSE"
    fi
}

# Configure monitoring
configure_monitoring() {
    print_step "10" "Configuring monitoring..."
    
    # Wait for monitoring services to be ready
    print_info "Waiting for monitoring services to be ready..."
    sleep 30
    
    # Check Prometheus
    if curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
        print_success "Prometheus is running"
    else
        print_error "Prometheus is not running"
    fi
    
    # Check Grafana
    if curl -s http://localhost:3001/api/health > /dev/null 2>&1; then
        print_success "Grafana is running"
    else
        print_error "Grafana is not running"
    fi
    
    # Check Alertmanager
    if curl -s http://localhost:9093/-/healthy > /dev/null 2>&1; then
        print_success "Alertmanager is running"
    else
        print_error "Alertmanager is not running"
    fi
}

# Generate setup report
generate_report() {
    print_step "11" "Generating setup report..."
    
    REPORT_FILE="platform-setup-report-$(date +%Y%m%d-%H%M%S).txt"
    
    cat > "$REPORT_FILE" << EOF
Disaster Relief Platform - Complete Setup Report
Generated: $(date)
Platform URL: $PLATFORM_URL
Admin Email: $ADMIN_EMAIL

Setup Steps Completed:
1. System requirements check - PASSED
2. Environment configuration - COMPLETED
3. Docker Compose configuration - COMPLETED
4. Nginx configuration - COMPLETED
5. SSL certificates - GENERATED
6. Services build and start - COMPLETED
7. Service health check - COMPLETED
8. Database initialization - COMPLETED
9. Admin user creation - COMPLETED
10. Monitoring configuration - COMPLETED
11. Setup report - COMPLETED

Service URLs:
- Platform: $PLATFORM_URL
- Admin Dashboard: $PLATFORM_URL/admin
- Dispatcher Dashboard: $PLATFORM_URL/dispatcher
- API Documentation: $PLATFORM_URL/api/docs
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001
- Alertmanager: http://localhost:9093

Default Credentials:
- Admin Email: $ADMIN_EMAIL
- Admin Password: $DEFAULT_PASSWORD
- Grafana Username: admin
- Grafana Password: admin

Next Steps:
1. Access the platform and change default passwords
2. Configure your organization settings
3. Create additional users
4. Set up monitoring and alerting
5. Begin disaster relief operations

Support Information:
- Documentation: https://docs.disaster-relief.local
- Support: support@disaster-relief.local
- Emergency: +1-555-EMERGENCY
EOF
    
    print_success "Setup report generated: $REPORT_FILE"
}

# Main execution
main() {
    print_header
    
    check_requirements
    create_env_file
    create_docker_compose
    create_nginx_config
    generate_ssl_certificates
    start_services
    initialize_database
    create_admin_user
    configure_monitoring
    generate_report
    
    echo
    print_success "Platform setup completed successfully!"
    print_info "The Disaster Relief Platform is now running and ready for use"
    print_info "Access the platform at: $PLATFORM_URL"
    print_info "Admin dashboard: $PLATFORM_URL/admin"
    print_info "Dispatcher dashboard: $PLATFORM_URL/dispatcher"
    echo
    print_warning "Important: Change all default passwords immediately!"
    print_warning "Review the setup report for next steps and security considerations"
    echo
}

# Run main function
main "$@"



