#!/bin/bash

# Disaster Relief Platform - Admin Setup Script
# This script automates the initial admin setup process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PLATFORM_URL="https://disaster-relief.local"
ADMIN_EMAIL="admin@disaster-relief.local"
DEFAULT_PASSWORD="ChangeMe123!"

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Disaster Relief Platform - Admin Setup"
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
    
    # Check if curl is installed
    if ! command -v curl &> /dev/null; then
        print_error "curl is required but not installed"
        exit 1
    fi
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Test platform connectivity
test_connectivity() {
    print_step "2" "Testing platform connectivity..."
    
    if curl -s --connect-timeout 10 "$PLATFORM_URL/health" > /dev/null; then
        print_success "Platform is accessible"
    else
        print_error "Cannot connect to platform at $PLATFORM_URL"
        print_info "Please ensure the platform is running and accessible"
        exit 1
    fi
}

# Admin login
admin_login() {
    print_step "3" "Logging in as admin..."
    
    # Get login token
    LOGIN_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$DEFAULT_PASSWORD\"}")
    
    if echo "$LOGIN_RESPONSE" | jq -e '.token' > /dev/null; then
        TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
        print_success "Admin login successful"
    else
        print_error "Admin login failed"
        print_info "Response: $LOGIN_RESPONSE"
        exit 1
    fi
}

# Change default password
change_password() {
    print_step "4" "Changing default password..."
    
    read -s -p "Enter new admin password: " NEW_PASSWORD
    echo
    read -s -p "Confirm new admin password: " CONFIRM_PASSWORD
    echo
    
    if [ "$NEW_PASSWORD" != "$CONFIRM_PASSWORD" ]; then
        print_error "Passwords do not match"
        exit 1
    fi
    
    if [ ${#NEW_PASSWORD} -lt 12 ]; then
        print_error "Password must be at least 12 characters long"
        exit 1
    fi
    
    # Change password
    PASSWORD_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/auth/change-password" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"currentPassword\":\"$DEFAULT_PASSWORD\",\"newPassword\":\"$NEW_PASSWORD\"}")
    
    if echo "$PASSWORD_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Password changed successfully"
    else
        print_error "Failed to change password"
        print_info "Response: $PASSWORD_RESPONSE"
        exit 1
    fi
}

# Configure system settings
configure_system() {
    print_step "5" "Configuring system settings..."
    
    # Organization settings
    read -p "Enter organization name: " ORG_NAME
    read -p "Enter organization email: " ORG_EMAIL
    read -p "Enter organization phone: " ORG_PHONE
    read -p "Enter organization address: " ORG_ADDRESS
    
    # Configure organization
    ORG_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/system/organization" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"name\":\"$ORG_NAME\",
            \"email\":\"$ORG_EMAIL\",
            \"phone\":\"$ORG_PHONE\",
            \"address\":\"$ORG_ADDRESS\"
        }")
    
    if echo "$ORG_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Organization settings configured"
    else
        print_error "Failed to configure organization settings"
        print_info "Response: $ORG_RESPONSE"
    fi
    
    # Timezone settings
    read -p "Enter timezone (e.g., America/New_York): " TIMEZONE
    
    TIMEZONE_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/system/timezone" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"timezone\":\"$TIMEZONE\"}")
    
    if echo "$TIMEZONE_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Timezone configured"
    else
        print_error "Failed to configure timezone"
    fi
}

# Create initial users
create_initial_users() {
    print_step "6" "Creating initial users..."
    
    # Create dispatcher users
    read -p "Enter number of dispatcher users to create: " DISPATCHER_COUNT
    
    for i in $(seq 1 $DISPATCHER_COUNT); do
        echo "Creating dispatcher user $i..."
        read -p "Enter dispatcher $i full name: " DISPATCHER_NAME
        read -p "Enter dispatcher $i email: " DISPATCHER_EMAIL
        read -p "Enter dispatcher $i phone: " DISPATCHER_PHONE
        
        DISPATCHER_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/users" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "{
                \"fullName\":\"$DISPATCHER_NAME\",
                \"email\":\"$DISPATCHER_EMAIL\",
                \"phone\":\"$DISPATCHER_PHONE\",
                \"role\":\"DISPATCHER\",
                \"active\":true
            }")
        
        if echo "$DISPATCHER_RESPONSE" | jq -e '.id' > /dev/null; then
            print_success "Dispatcher $i created successfully"
        else
            print_error "Failed to create dispatcher $i"
            print_info "Response: $DISPATCHER_RESPONSE"
        fi
    done
    
    # Create helper users
    read -p "Enter number of helper users to create: " HELPER_COUNT
    
    for i in $(seq 1 $HELPER_COUNT); do
        echo "Creating helper user $i..."
        read -p "Enter helper $i full name: " HELPER_NAME
        read -p "Enter helper $i email: " HELPER_EMAIL
        read -p "Enter helper $i phone: " HELPER_PHONE
        
        HELPER_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/users" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "{
                \"fullName\":\"$HELPER_NAME\",
                \"email\":\"$HELPER_EMAIL\",
                \"phone\":\"$HELPER_PHONE\",
                \"role\":\"HELPER\",
                \"active\":true
            }")
        
        if echo "$HELPER_RESPONSE" | jq -e '.id' > /dev/null; then
            print_success "Helper $i created successfully"
        else
            print_error "Failed to create helper $i"
            print_info "Response: $HELPER_RESPONSE"
        fi
    done
}

# Configure inventory
configure_inventory() {
    print_step "7" "Configuring initial inventory..."
    
    # Create inventory hubs
    read -p "Enter number of inventory hubs to create: " HUB_COUNT
    
    for i in $(seq 1 $HUB_COUNT); do
        echo "Creating inventory hub $i..."
        read -p "Enter hub $i name: " HUB_NAME
        read -p "Enter hub $i address: " HUB_ADDRESS
        read -p "Enter hub $i latitude: " HUB_LAT
        read -p "Enter hub $i longitude: " HUB_LON
        
        HUB_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/inventory/hubs" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "{
                \"name\":\"$HUB_NAME\",
                \"address\":\"$HUB_ADDRESS\",
                \"latitude\":$HUB_LAT,
                \"longitude\":$HUB_LON,
                \"active\":true
            }")
        
        if echo "$HUB_RESPONSE" | jq -e '.id' > /dev/null; then
            print_success "Hub $i created successfully"
        else
            print_error "Failed to create hub $i"
            print_info "Response: $HUB_RESPONSE"
        fi
    done
}

# Configure notifications
configure_notifications() {
    print_step "8" "Configuring notifications..."
    
    # Email settings
    read -p "Enter SMTP server: " SMTP_SERVER
    read -p "Enter SMTP port: " SMTP_PORT
    read -p "Enter SMTP username: " SMTP_USERNAME
    read -s -p "Enter SMTP password: " SMTP_PASSWORD
    echo
    
    EMAIL_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/admin/system/email" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"smtpServer\":\"$SMTP_SERVER\",
            \"smtpPort\":$SMTP_PORT,
            \"smtpUsername\":\"$SMTP_USERNAME\",
            \"smtpPassword\":\"$SMTP_PASSWORD\",
            \"enabled\":true
        }")
    
    if echo "$EMAIL_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Email notifications configured"
    else
        print_error "Failed to configure email notifications"
        print_info "Response: $EMAIL_RESPONSE"
    fi
}

# Run security scan
run_security_scan() {
    print_step "9" "Running security scan..."
    
    # Check for common security issues
    SECURITY_RESPONSE=$(curl -s -X GET "$PLATFORM_URL/api/admin/security/scan" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$SECURITY_RESPONSE" | jq -e '.issues' > /dev/null; then
        ISSUES=$(echo "$SECURITY_RESPONSE" | jq -r '.issues | length')
        if [ "$ISSUES" -gt 0 ]; then
            print_error "Security scan found $ISSUES issues"
            echo "$SECURITY_RESPONSE" | jq -r '.issues[]'
        else
            print_success "Security scan passed"
        fi
    else
        print_info "Security scan not available"
    fi
}

# Generate setup report
generate_report() {
    print_step "10" "Generating setup report..."
    
    REPORT_FILE="admin-setup-report-$(date +%Y%m%d-%H%M%S).txt"
    
    cat > "$REPORT_FILE" << EOF
Disaster Relief Platform - Admin Setup Report
Generated: $(date)
Platform URL: $PLATFORM_URL
Admin Email: $ADMIN_EMAIL

Setup Steps Completed:
1. Prerequisites check - PASSED
2. Platform connectivity - PASSED
3. Admin login - PASSED
4. Password change - PASSED
5. System configuration - COMPLETED
6. Initial users created - COMPLETED
7. Inventory configured - COMPLETED
8. Notifications configured - COMPLETED
9. Security scan - COMPLETED
10. Setup report - COMPLETED

Next Steps:
1. Review system settings
2. Test all functionality
3. Train users
4. Begin operations

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
    
    check_prerequisites
    test_connectivity
    admin_login
    change_password
    configure_system
    create_initial_users
    configure_inventory
    configure_notifications
    run_security_scan
    generate_report
    
    echo
    print_success "Admin setup completed successfully!"
    print_info "Please review the setup report and test all functionality"
    print_info "Next steps:"
    print_info "1. Access the admin dashboard at $PLATFORM_URL/admin"
    print_info "2. Review and adjust system settings"
    print_info "3. Train your team on the platform"
    print_info "4. Begin disaster relief operations"
    echo
}

# Run main function
main "$@"



