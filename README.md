# Disaster Relief Platform

A modern, reliable platform to coordinate relief during floods/hurricanes/"inondation" events. Built with Java Spring Boot (backend), React (frontend), PostgreSQL + PostGIS (geospatial), and Flyway (DB versioning).

## Features

- **Resident Portal**: Report status with geolocation and photos, find nearby shelters
- **Helper/Volunteer App**: Accept tasks, update delivery status, work offline
- **Dispatcher Dashboard**: Triage requests, manage tasks, avoid duplicate deliveries
- **Admin Panel**: Manage areas, users, permissions, and analytics
- **Real-time Map**: Live visualization of needs and resources
- **Offline PWA**: Works in low-connectivity areas with background sync

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Node.js 18+ (for local development)
- Java 17+ (for local development)

### Development Setup

1. **Clone and start services:**
   ```bash
   git clone <repository-url>
   cd disaster-relief-platform
   docker-compose up -d
   ```

2. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - MinIO Console: http://localhost:9001 (minioadmin/minioadmin123)
   - Prometheus: http://localhost:9090 (monitoring profile)
   - Grafana: http://localhost:3001 (admin/admin, monitoring profile)

3. **Initialize database:**
   ```bash
   # Database migrations run automatically on startup
   # Check logs: docker-compose logs backend
   ```

### Production Deployment

```bash
# Build and start production stack
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# With monitoring
docker-compose --profile monitoring up -d
```

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React PWA     │    │  Spring Boot    │    │  PostgreSQL    │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   + PostGIS    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MapLibre      │    │     MinIO       │    │    Flyway      │
│   (Maps)        │    │   (Storage)     │    │ (Migrations)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Development

### Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

### Frontend (React)

```bash
cd frontend
npm install
npm start
```

### Database Migrations

```bash
# Create new migration
cd backend
mvn flyway:migrate

# Check migration status
mvn flyway:info
```

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## Monitoring

The platform includes comprehensive observability:

- **Metrics**: Prometheus + Micrometer
- **Logs**: Structured JSON logging
- **Traces**: OpenTelemetry instrumentation
- **Dashboards**: Grafana with pre-built dashboards

## Security

- JWT-based authentication
- Role-based access control (RBAC)
- Data privacy controls
- Audit logging
- Rate limiting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License - see LICENSE file for details

