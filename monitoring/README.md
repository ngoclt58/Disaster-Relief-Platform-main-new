# Disaster Relief Platform - Monitoring & Analytics

This directory contains the complete monitoring and analytics setup for the Disaster Relief Platform, including Prometheus, Grafana, and custom analytics dashboards.

## 📊 Overview

The monitoring stack provides:
- **Real-time Metrics**: System performance, business metrics, and user activity
- **Analytics Dashboards**: Comprehensive insights into disaster relief operations
- **Alerting**: Proactive notifications for critical issues
- **Data Export**: CSV/JSON export capabilities for external analysis

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │───▶│   Prometheus    │───▶│     Grafana     │
│   (Backend)     │    │   (Metrics)     │    │  (Dashboards)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       ▼                       │
         │              ┌─────────────────┐              │
         │              │  Alertmanager   │              │
         │              │   (Alerts)      │              │
         │              └─────────────────┘              │
         │                                               │
         ▼                                               ▼
┌─────────────────┐                            ┌─────────────────┐
│   Analytics     │                            │   Export APIs   │
│   Service       │                            │   (CSV/JSON)    │
└─────────────────┘                            └─────────────────┘
```

## 🚀 Quick Start

### 1. Start Monitoring Stack

```bash
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

### 2. Access Services

- **Grafana**: http://localhost:3001 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Alertmanager**: http://localhost:9093
- **Jaeger**: http://localhost:16686 (tracing)
- **Kibana**: http://localhost:5601 (logs)

### 3. Import Dashboards

The dashboards are automatically provisioned:
- **Overview Dashboard**: System overview and key metrics
- **Performance Dashboard**: Detailed performance metrics
- **Business Dashboard**: Business-specific metrics and KPIs

## 📈 Analytics Endpoints

### Backend Analytics API

All endpoints require `REPORTS_READ` permission:

```bash
# Get analytics overview
GET /analytics/overview?startDate=2024-01-01&endDate=2024-01-31

# Get needs trends
GET /analytics/needs/trends?granularity=day

# Get task performance
GET /analytics/tasks/performance

# Get user activity
GET /analytics/users/activity

# Get inventory status
GET /analytics/inventory/status

# Get response times
GET /analytics/response-times

# Get geographic distribution
GET /analytics/geographic/distribution

# Get severity breakdown
GET /analytics/severity/breakdown

# Export analytics data
GET /analytics/export?format=csv&startDate=2024-01-01&endDate=2024-01-31
```

### Frontend Analytics Dashboard

Access the analytics dashboard at `/analytics` (requires admin permissions).

## 📊 Key Metrics

### System Metrics
- **Request Rate**: Requests per second by endpoint
- **Response Time**: 95th percentile response times
- **Error Rate**: 5xx error rates
- **Memory Usage**: Application memory consumption
- **CPU Usage**: System CPU utilization

### Business Metrics
- **Needs Requests**: Total, active, and by category
- **Task Performance**: Completion rates and times
- **User Activity**: Active users and actions
- **Inventory Status**: Stock levels and alerts
- **Response Times**: By severity and region
- **Geographic Distribution**: Needs by location

### Custom Metrics
- **Disaster Relief Specific**: Needs, tasks, deliveries
- **User Behavior**: Actions by role and activity patterns
- **Operational Efficiency**: Response times and completion rates
- **Resource Utilization**: Inventory and capacity metrics

## 🔔 Alerting Rules

### Critical Alerts
- **System Down**: Backend service unavailable
- **Database Issues**: PostgreSQL connection lost
- **Storage Issues**: MinIO unavailable

### Warning Alerts
- **High Error Rate**: >10% error rate for 2 minutes
- **High Response Time**: >2s 95th percentile for 5 minutes
- **High Memory Usage**: >1GB memory consumption
- **Low Task Completion**: <50% completion rate
- **Low Inventory**: >5 items running low

### Business Alerts
- **High Needs Volume**: >10 requests/second
- **User Activity Drop**: <1 action/second for 30 minutes
- **Response Time Issues**: By region or severity

## 📋 Grafana Dashboards

### 1. Overview Dashboard
- **System Health**: Overall system status
- **Key Metrics**: Total needs, tasks, users
- **Request Patterns**: API usage trends
- **Performance**: Response times and throughput

### 2. Performance Dashboard
- **Endpoint Performance**: Response times by endpoint
- **Error Analysis**: Error rates and types
- **Resource Usage**: Memory, CPU, and storage
- **Business Metrics**: Needs and task trends

### 3. Business Dashboard
- **Needs Analysis**: By category, severity, and location
- **Task Management**: Status distribution and completion
- **User Activity**: Role-based activity patterns
- **Inventory Management**: Stock levels and alerts

## 🔧 Configuration

### Prometheus Configuration
- **Scrape Interval**: 15s (5s for backend)
- **Retention**: 200 hours
- **Targets**: Backend, PostgreSQL, MinIO, system metrics

### Grafana Configuration
- **Dashboards**: Auto-provisioned from JSON files
- **Datasources**: Prometheus as default
- **Users**: Admin access with secure passwords

### Alertmanager Configuration
- **Routing**: Critical vs warning alerts
- **Notifications**: Email and webhook support
- **Inhibition**: Prevents alert spam

## 📊 Data Export

### Supported Formats
- **CSV**: Comma-separated values for Excel
- **JSON**: Structured data for APIs
- **Prometheus**: Native metrics format

### Export Features
- **Date Range**: Custom start/end dates
- **Filtering**: By category, severity, region
- **Aggregation**: Daily, weekly, monthly views
- **Real-time**: Live data export

## 🛠️ Customization

### Adding New Metrics

1. **Backend**: Add metrics to `MetricsService`
2. **Prometheus**: Update scrape configuration
3. **Grafana**: Create new dashboard panels
4. **Alerts**: Add new alert rules

### Creating Custom Dashboards

1. Create JSON dashboard file in `grafana/dashboards/`
2. Update `dashboard.yml` provisioning
3. Restart Grafana to load new dashboard

### Adding Alert Rules

1. Create new rule file in `prometheus/rules/`
2. Update `prometheus.yml` to include new rules
3. Restart Prometheus to load new rules

## 🔍 Troubleshooting

### Common Issues

1. **Metrics Not Appearing**
   - Check Prometheus targets are up
   - Verify metrics are being generated
   - Check scrape configuration

2. **Dashboards Not Loading**
   - Verify datasource configuration
   - Check dashboard JSON syntax
   - Restart Grafana service

3. **Alerts Not Firing**
   - Check alert rule syntax
   - Verify metric names match
   - Check Alertmanager configuration

### Logs

```bash
# View Prometheus logs
docker logs disaster-relief-prometheus

# View Grafana logs
docker logs disaster-relief-grafana

# View Alertmanager logs
docker logs disaster-relief-alertmanager
```

## 📚 Additional Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🤝 Contributing

To add new metrics or dashboards:

1. Update the appropriate service classes
2. Add new dashboard JSON files
3. Update documentation
4. Test with sample data
5. Submit pull request

## 📄 License

This monitoring setup is part of the Disaster Relief Platform and follows the same license terms.



