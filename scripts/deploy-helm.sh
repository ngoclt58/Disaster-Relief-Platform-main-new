#!/bin/bash

# Disaster Relief Platform - Helm Deployment Script
# This script deploys the platform using Helm charts

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="disaster-relief-platform"
CHART_PATH="./helm/disaster-relief-platform"
RELEASE_NAME="disaster-relief-platform"
VALUES_FILE="./helm/disaster-relief-platform/values.yaml"

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Disaster Relief Platform - Helm Deployment"
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

# Check prerequisites
check_prerequisites() {
    print_step "1" "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is required but not installed"
        exit 1
    fi
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        print_error "helm is required but not installed"
        exit 1
    fi
    
    # Check if cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Create namespace
create_namespace() {
    print_step "2" "Creating namespace..."
    
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_info "Namespace $NAMESPACE already exists"
    else
        kubectl create namespace "$NAMESPACE"
        print_success "Namespace $NAMESPACE created"
    fi
}

# Add Helm repositories
add_helm_repos() {
    print_step "3" "Adding Helm repositories..."
    
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    helm repo add jetstack https://charts.jetstack.io
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo add external-secrets https://charts.external-secrets.io
    
    helm repo update
    
    print_success "Helm repositories added and updated"
}

# Install dependencies
install_dependencies() {
    print_step "4" "Installing dependencies..."
    
    # Install cert-manager
    if ! helm list -n cert-manager | grep cert-manager &> /dev/null; then
        print_info "Installing cert-manager..."
        helm install cert-manager jetstack/cert-manager \
            --namespace cert-manager \
            --create-namespace \
            --version v1.13.0 \
            --set installCRDs=true
        print_success "cert-manager installed"
    else
        print_info "cert-manager already installed"
    fi
    
    # Install nginx-ingress
    if ! helm list -n ingress-nginx | grep nginx-ingress &> /dev/null; then
        print_info "Installing nginx-ingress..."
        helm install nginx-ingress ingress-nginx/ingress-nginx \
            --namespace ingress-nginx \
            --create-namespace \
            --version 4.4.2
        print_success "nginx-ingress installed"
    else
        print_info "nginx-ingress already installed"
    fi
    
    # Install external-secrets
    if ! helm list -n external-secrets-system | grep external-secrets &> /dev/null; then
        print_info "Installing external-secrets..."
        helm install external-secrets external-secrets/external-secrets \
            --namespace external-secrets-system \
            --create-namespace \
            --version 0.9.11
        print_success "external-secrets installed"
    else
        print_info "external-secrets already installed"
    fi
}

# Deploy the platform
deploy_platform() {
    print_step "5" "Deploying the platform..."
    
    # Update dependencies
    helm dependency update "$CHART_PATH"
    
    # Deploy the platform
    helm upgrade --install "$RELEASE_NAME" "$CHART_PATH" \
        --namespace "$NAMESPACE" \
        --values "$VALUES_FILE" \
        --wait \
        --timeout 10m
    
    print_success "Platform deployed successfully"
}

# Verify deployment
verify_deployment() {
    print_step "6" "Verifying deployment..."
    
    # Check pods
    print_info "Checking pod status..."
    kubectl get pods -n "$NAMESPACE"
    
    # Check services
    print_info "Checking service status..."
    kubectl get services -n "$NAMESPACE"
    
    # Check ingress
    print_info "Checking ingress status..."
    kubectl get ingress -n "$NAMESPACE"
    
    # Check certificates
    print_info "Checking certificate status..."
    kubectl get certificates -n "$NAMESPACE"
    
    print_success "Deployment verification completed"
}

# Get deployment information
get_deployment_info() {
    print_step "7" "Getting deployment information..."
    
    echo
    print_info "Deployment Information:"
    echo "  Namespace: $NAMESPACE"
    echo "  Release Name: $RELEASE_NAME"
    echo "  Chart Path: $CHART_PATH"
    echo "  Values File: $VALUES_FILE"
    echo
    
    print_info "Access URLs:"
    echo "  Platform: https://disaster-relief.local"
    echo "  Admin Dashboard: https://disaster-relief.local/admin"
    echo "  Dispatcher Dashboard: https://disaster-relief.local/dispatcher"
    echo "  API Documentation: https://disaster-relief.local/api/docs"
    echo
    
    print_info "Monitoring URLs:"
    echo "  Prometheus: kubectl port-forward -n $NAMESPACE svc/prometheus-server 9090:80"
    echo "  Grafana: kubectl port-forward -n $NAMESPACE svc/grafana 3000:80"
    echo "  Alertmanager: kubectl port-forward -n $NAMESPACE svc/prometheus-alertmanager 9093:80"
    echo
    
    print_info "Useful Commands:"
    echo "  View logs: kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=disaster-relief-platform"
    echo "  Scale backend: kubectl scale -n $NAMESPACE deployment/disaster-relief-backend --replicas=5"
    echo "  Restart backend: kubectl rollout restart -n $NAMESPACE deployment/disaster-relief-backend"
    echo "  Delete platform: helm uninstall $RELEASE_NAME -n $NAMESPACE"
    echo
}

# Main execution
main() {
    print_header
    
    check_prerequisites
    create_namespace
    add_helm_repos
    install_dependencies
    deploy_platform
    verify_deployment
    get_deployment_info
    
    echo
    print_success "Helm deployment completed successfully!"
    print_info "The Disaster Relief Platform is now running in your Kubernetes cluster"
    print_info "Please configure your DNS to point disaster-relief.local to your cluster's ingress IP"
    echo
}

# Run main function
main "$@"



