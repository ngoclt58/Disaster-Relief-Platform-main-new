# Infrastructure Deployment Guide

## 🏗️ Overview

This guide provides comprehensive instructions for deploying the Disaster Relief Platform using infrastructure-as-code (IaC) with Helm charts and Kubernetes manifests, including TLS configuration and secure secrets management.

## 📋 Prerequisites

### System Requirements

- **Kubernetes Cluster**: Version 1.24 or higher
- **kubectl**: Version 1.24 or higher
- **helm**: Version 3.8 or higher
- **cert-manager**: For TLS certificate management
- **nginx-ingress**: For ingress controller
- **external-secrets**: For secrets management (optional)

### Cluster Requirements

- **CPU**: Minimum 4 cores, recommended 8+ cores
- **Memory**: Minimum 8GB, recommended 16+ GB
- **Storage**: Minimum 100GB, recommended 500+ GB
- **Network**: LoadBalancer or NodePort support for ingress

## 🚀 Deployment Options

### Option 1: Helm Deployment (Recommended)

Helm provides a comprehensive package management solution with dependency management, templating, and lifecycle management.

#### Quick Start

```bash
# Clone the repository
git clone https://github.com/disaster-relief/platform.git
cd platform

# Make scripts executable
chmod +x scripts/*.sh

# Deploy using Helm
./scripts/deploy-helm.sh
```

#### Manual Helm Deployment

```bash
# Add required Helm repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add jetstack https://charts.jetstack.io
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add external-secrets https://charts.external-secrets.io

# Update repositories
helm repo update

# Install dependencies
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.0 \
  --set installCRDs=true

helm install nginx-ingress ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --version 4.4.2

helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets-system \
  --create-namespace \
  --version 0.9.11

# Deploy the platform
helm install disaster-relief-platform ./helm/disaster-relief-platform \
  --namespace disaster-relief-platform \
  --create-namespace \
  --values ./helm/disaster-relief-platform/values.yaml
```

### Option 2: Kubernetes Manifests

Direct deployment using Kubernetes YAML manifests for maximum control and customization.

#### Quick Start

```bash
# Clone the repository
git clone https://github.com/disaster-relief/platform.git
cd platform

# Make scripts executable
chmod +x scripts/*.sh

# Deploy using Kubernetes manifests
./scripts/deploy-k8s.sh
```

#### Manual Kubernetes Deployment

```bash
# Create namespace
kubectl create namespace disaster-relief-platform

# Deploy secrets
kubectl apply -f k8s/secrets.yaml

# Deploy TLS configuration
kubectl apply -f k8s/tls-config.yaml

# Deploy backend
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml

# Deploy frontend
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml

# Deploy ingress
kubectl apply -f k8s/ingress.yaml

# Deploy monitoring
kubectl apply -f k8s/monitoring.yaml
```

## 🔐 TLS Configuration

### Automatic TLS with Let's Encrypt

The platform is configured to automatically obtain and renew TLS certificates using Let's Encrypt.

#### Cert-Manager Configuration

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@disaster-relief.local
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

#### Certificate Configuration

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: disaster-relief-tls
  namespace: disaster-relief-platform
spec:
  secretName: disaster-relief-tls
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
    - disaster-relief.local
  commonName: disaster-relief.local
```

### Manual TLS Configuration

For environments where automatic certificate management is not available:

```bash
# Generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout tls.key \
  -out tls.crt \
  -subj "/CN=disaster-relief.local"

# Create TLS secret
kubectl create secret tls disaster-relief-tls \
  --key tls.key \
  --cert tls.crt \
  --namespace disaster-relief-platform
```

## 🔒 Secrets Management

### Built-in Secrets

The platform includes built-in secret management for development and testing:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: disaster-relief-db-secret
  namespace: disaster-relief-platform
type: Opaque
data:
  password: ZGlzYXN0ZXItcmVsaWVmLXBhc3N3b3Jk  # disaster-relief-password
```

### External Secrets Integration

For production environments, integrate with external secret management systems:

#### HashiCorp Vault Integration

```yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: disaster-relief-secret-store
  namespace: disaster-relief-platform
spec:
  provider:
    vault:
      server: "https://vault.disaster-relief.local"
      path: "secret/disaster-relief"
      version: "v2"
      auth:
        kubernetes:
          mountPath: "kubernetes"
          role: "disaster-relief-platform"
          serviceAccountRef:
            name: disaster-relief-platform
```

#### AWS Secrets Manager Integration

```yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: disaster-relief-aws-secret-store
  namespace: disaster-relief-platform
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-west-2
      auth:
        secretRef:
          accessKeyID:
            name: aws-credentials
            key: access-key-id
          secretAccessKey:
            name: aws-credentials
            key: secret-access-key
```

### Secret Rotation

Configure automatic secret rotation:

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: disaster-relief-external-secret
  namespace: disaster-relief-platform
spec:
  refreshInterval: 1h  # Rotate every hour
  secretStoreRef:
    name: disaster-relief-secret-store
    kind: SecretStore
  target:
    name: disaster-relief-external-secret
    creationPolicy: Owner
```

## 🌐 Ingress Configuration

### Nginx Ingress Controller

The platform uses Nginx as the ingress controller with comprehensive security headers and rate limiting.

#### Ingress Configuration

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: disaster-relief-platform-ingress
  namespace: disaster-relief-platform
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    nginx.ingress.kubernetes.io/proxy-body-size: "100m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "300"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "300"
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - disaster-relief.local
      secretName: disaster-relief-tls
  rules:
    - host: disaster-relief.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: disaster-relief-frontend
                port:
                  number: 3000
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: disaster-relief-backend
                port:
                  number: 8080
```

### Security Headers

The ingress controller is configured with comprehensive security headers:

- **X-Frame-Options**: DENY
- **X-Content-Type-Options**: nosniff
- **X-XSS-Protection**: 1; mode=block
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains
- **Referrer-Policy**: strict-origin-when-cross-origin

### Rate Limiting

Configure rate limiting to prevent abuse:

```yaml
annotations:
  nginx.ingress.kubernetes.io/rate-limit: "100"
  nginx.ingress.kubernetes.io/rate-limit-window: "1m"
  nginx.ingress.kubernetes.io/rate-limit-connections: "10"
  nginx.ingress.kubernetes.io/rate-limit-requests: "100"
```

## 📊 Monitoring and Observability

### Prometheus Configuration

The platform includes comprehensive Prometheus monitoring:

```yaml
apiVersion: v1
kind: ServiceMonitor
metadata:
  name: disaster-relief-backend-monitor
  namespace: disaster-relief-platform
spec:
  selector:
    matchLabels:
      component: backend
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 30s
      scrapeTimeout: 10s
```

### Grafana Dashboards

Pre-configured Grafana dashboards for:

- **System Overview**: CPU, memory, and network usage
- **Application Metrics**: Request rates, response times, error rates
- **Business Metrics**: User activity, needs processing, task completion
- **Security Metrics**: Authentication, authorization, rate limiting

### Alerting Rules

Comprehensive alerting rules for:

- **Service Availability**: Pod down, service unavailable
- **Performance**: High response times, high error rates
- **Resource Usage**: High CPU, memory, disk usage
- **Security**: Failed authentication, rate limit violations

## 🔧 Configuration Management

### Environment-Specific Configuration

Create environment-specific values files:

```yaml
# values-dev.yaml
global:
  domain: "dev.disaster-relief.local"
  tls:
    enabled: true
    secretName: "disaster-relief-dev-tls"

backend:
  replicaCount: 1
  resources:
    limits:
      cpu: 500m
      memory: 1Gi
    requests:
      cpu: 250m
      memory: 512Mi

# values-prod.yaml
global:
  domain: "disaster-relief.local"
  tls:
    enabled: true
    secretName: "disaster-relief-tls"

backend:
  replicaCount: 5
  resources:
    limits:
      cpu: 2000m
      memory: 4Gi
    requests:
      cpu: 1000m
      memory: 2Gi
```

### Configuration Updates

Update configuration without downtime:

```bash
# Update using Helm
helm upgrade disaster-relief-platform ./helm/disaster-relief-platform \
  --namespace disaster-relief-platform \
  --values ./helm/disaster-relief-platform/values-prod.yaml

# Update using kubectl
kubectl apply -f k8s/updated-config.yaml
```

## 🚀 Scaling and Performance

### Horizontal Pod Autoscaling

Configure automatic scaling based on CPU and memory usage:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: disaster-relief-backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: disaster-relief-backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

### Vertical Pod Autoscaling

Configure automatic resource adjustment:

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: disaster-relief-backend-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: disaster-relief-backend
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: backend
      minAllowed:
        cpu: 100m
        memory: 128Mi
      maxAllowed:
        cpu: 2000m
        memory: 4Gi
```

## 🔄 Backup and Recovery

### Database Backup

Configure automated database backups:

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: disaster-relief-db-backup
  namespace: disaster-relief-platform
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:15
            command:
            - /bin/bash
            - -c
            - |
              pg_dump -h postgresql -U disaster_relief disaster_relief > /backup/disaster-relief-$(date +%Y%m%d).sql
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: disaster-relief-db-secret
                  key: password
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: disaster-relief-backup-pvc
          restartPolicy: OnFailure
```

### Application Backup

Configure application state backups:

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: disaster-relief-app-backup
  namespace: disaster-relief-platform
spec:
  schedule: "0 3 * * *"  # Daily at 3 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: disaster-relief/backup:1.0.0
            command:
            - /bin/bash
            - -c
            - |
              # Backup application data
              kubectl get all -n disaster-relief-platform -o yaml > /backup/app-state-$(date +%Y%m%d).yaml
              
              # Backup secrets
              kubectl get secrets -n disaster-relief-platform -o yaml > /backup/secrets-$(date +%Y%m%d).yaml
              
              # Backup configmaps
              kubectl get configmaps -n disaster-relief-platform -o yaml > /backup/configmaps-$(date +%Y%m%d).yaml
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: disaster-relief-backup-pvc
          restartPolicy: OnFailure
```

## 🛠️ Troubleshooting

### Common Issues

#### Pod Startup Issues

```bash
# Check pod status
kubectl get pods -n disaster-relief-platform

# Check pod logs
kubectl logs -n disaster-relief-platform -l component=backend

# Check pod events
kubectl describe pod -n disaster-relief-platform <pod-name>
```

#### Service Connectivity Issues

```bash
# Check service status
kubectl get services -n disaster-relief-platform

# Test service connectivity
kubectl run test-pod --image=busybox --rm -it --restart=Never -- nslookup disaster-relief-backend.disaster-relief-platform.svc.cluster.local
```

#### Ingress Issues

```bash
# Check ingress status
kubectl get ingress -n disaster-relief-platform

# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Test ingress connectivity
curl -H "Host: disaster-relief.local" http://<ingress-ip>/
```

#### Certificate Issues

```bash
# Check certificate status
kubectl get certificates -n disaster-relief-platform

# Check certificate details
kubectl describe certificate disaster-relief-tls -n disaster-relief-platform

# Check cert-manager logs
kubectl logs -n cert-manager -l app.kubernetes.io/name=cert-manager
```

### Performance Optimization

#### Resource Optimization

```bash
# Check resource usage
kubectl top pods -n disaster-relief-platform
kubectl top nodes

# Check resource requests and limits
kubectl describe pod -n disaster-relief-platform <pod-name>
```

#### Network Optimization

```bash
# Check network policies
kubectl get networkpolicies -n disaster-relief-platform

# Check service endpoints
kubectl get endpoints -n disaster-relief-platform
```

## 📚 Additional Resources

### Documentation

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [cert-manager Documentation](https://cert-manager.io/docs/)
- [Nginx Ingress Documentation](https://kubernetes.github.io/ingress-nginx/)
- [External Secrets Documentation](https://external-secrets.io/)

### Community Support

- [Kubernetes Slack](https://kubernetes.slack.com/)
- [Helm Slack](https://kubernetes.slack.com/channels/helm)
- [Disaster Relief Platform GitHub](https://github.com/disaster-relief/platform)

### Professional Support

- **Technical Support**: support@disaster-relief.local
- **Emergency Support**: +1-555-EMERGENCY
- **Consulting Services**: consulting@disaster-relief.local

---

**Last Updated**: 2024-01-15  
**Version**: 1.0  
**Next Review**: 2024-04-15



