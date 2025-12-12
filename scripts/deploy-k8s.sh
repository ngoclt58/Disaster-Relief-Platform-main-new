#!/bin/bash

# Disaster Relief Platform - Kubernetes Deployment Script
# This script deploys the platform using Kubernetes manifests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="disaster-relief-platform"
MANIFESTS_DIR="./k8s"

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Disaster Relief Platform - K8s Deployment"
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
        kubectl apply -f "$MANIFESTS_DIR/namespace.yaml"
        print_success "Namespace $NAMESPACE created"
    fi
}

# Deploy secrets
deploy_secrets() {
    print_step "3" "Deploying secrets..."
    
    kubectl apply -f "$MANIFESTS_DIR/secrets.yaml"
    print_success "Secrets deployed"
}

# Deploy TLS configuration
deploy_tls() {
    print_step "4" "Deploying TLS configuration..."
    
    # Check if cert-manager is installed
    if ! kubectl get crd certificates.cert-manager.io &> /dev/null; then
        print_error "cert-manager is required but not installed"
        print_info "Please install cert-manager first:"
        print_info "kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml"
        exit 1
    fi
    
    kubectl apply -f "$MANIFESTS_DIR/tls-config.yaml"
    print_success "TLS configuration deployed"
}

# Deploy backend
deploy_backend() {
    print_step "5" "Deploying backend..."
    
    kubectl apply -f "$MANIFESTS_DIR/backend-deployment.yaml"
    kubectl apply -f "$MANIFESTS_DIR/backend-service.yaml"
    print_success "Backend deployed"
}

# Deploy frontend
deploy_frontend() {
    print_step "6" "Deploying frontend..."
    
    kubectl apply -f "$MANIFESTS_DIR/frontend-deployment.yaml"
    kubectl apply -f "$MANIFESTS_DIR/frontend-service.yaml"
    print_success "Frontend deployed"
}

# Deploy ingress
deploy_ingress() {
    print_step "7" "Deploying ingress..."
    
    kubectl apply -f "$MANIFESTS_DIR/ingress.yaml"
    print_success "Ingress deployed"
}

# Wait for deployment
wait_for_deployment() {
    print_step "8" "Waiting for deployment to be ready..."
    
    print_info "Waiting for backend deployment..."
    kubectl wait --for=condition=available --timeout=300s deployment/disaster-relief-backend -n "$NAMESPACE"
    
    print_info "Waiting for frontend deployment..."
    kubectl wait --for=condition=available --timeout=300s deployment/disaster-relief-frontend -n "$NAMESPACE"
    
    print_success "All deployments are ready"
}

# Verify deployment
verify_deployment() {
    print_step "9" "Verifying deployment..."
    
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
    print_step "10" "Getting deployment information..."
    
    echo
    print_info "Deployment Information:"
    echo "  Namespace: $NAMESPACE"
    echo "  Manifests Directory: $MANIFESTS_DIR"
    echo
    
    print_info "Access URLs:"
    echo "  Platform: https://disaster-relief.local"
    echo "  Admin Dashboard: https://disaster-relief.local/admin"
    echo "  Dispatcher Dashboard: https://disaster-relief.local/dispatcher"
    echo "  API Documentation: https://disaster-relief.local/api/docs"
    echo
    
    print_info "Useful Commands:"
    echo "  View logs: kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=disaster-relief-platform"
    echo "  Scale backend: kubectl scale -n $NAMESPACE deployment/disaster-relief-backend --replicas=5"
    echo "  Restart backend: kubectl rollout restart -n $NAMESPACE deployment/disaster-relief-backend"
    echo "  Delete platform: kubectl delete namespace $NAMESPACE"
    echo
}

# Main execution
main() {
    print_header
    
    check_prerequisites
    create_namespace
    deploy_secrets
    deploy_tls
    deploy_backend
    deploy_frontend
    deploy_ingress
    wait_for_deployment
    verify_deployment
    get_deployment_info
    
    echo
    print_success "Kubernetes deployment completed successfully!"
    print_info "The Disaster Relief Platform is now running in your Kubernetes cluster"
    print_info "Please configure your DNS to point disaster-relief.local to your cluster's ingress IP"
    echo
}

# Run main function
main "$@"



