#!/bin/bash

# Disaster Relief Platform - Dispatcher Setup Script
# This script automates the initial dispatcher setup process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PLATFORM_URL="https://disaster-relief.local"
DISPATCHER_EMAIL=""
DISPATCHER_PASSWORD=""

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Disaster Relief Platform - Dispatcher Setup"
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

# Get dispatcher credentials
get_credentials() {
    print_step "2" "Getting dispatcher credentials..."
    
    read -p "Enter your dispatcher email: " DISPATCHER_EMAIL
    read -s -p "Enter your dispatcher password: " DISPATCHER_PASSWORD
    echo
    
    if [ -z "$DISPATCHER_EMAIL" ] || [ -z "$DISPATCHER_PASSWORD" ]; then
        print_error "Email and password are required"
        exit 1
    fi
    
    print_success "Credentials obtained"
}

# Test platform connectivity
test_connectivity() {
    print_step "3" "Testing platform connectivity..."
    
    if curl -s --connect-timeout 10 "$PLATFORM_URL/health" > /dev/null; then
        print_success "Platform is accessible"
    else
        print_error "Cannot connect to platform at $PLATFORM_URL"
        print_info "Please ensure the platform is running and accessible"
        exit 1
    fi
}

# Dispatcher login
dispatcher_login() {
    print_step "4" "Logging in as dispatcher..."
    
    # Get login token
    LOGIN_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$DISPATCHER_EMAIL\",\"password\":\"$DISPATCHER_PASSWORD\"}")
    
    if echo "$LOGIN_RESPONSE" | jq -e '.token' > /dev/null; then
        TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
        USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.id')
        print_success "Dispatcher login successful"
    else
        print_error "Dispatcher login failed"
        print_info "Response: $LOGIN_RESPONSE"
        exit 1
    fi
}

# Configure dispatcher profile
configure_profile() {
    print_step "5" "Configuring dispatcher profile..."
    
    read -p "Enter your full name: " FULL_NAME
    read -p "Enter your phone number: " PHONE
    read -p "Enter your address: " ADDRESS
    read -p "Enter your working hours (e.g., 9:00-17:00): " WORKING_HOURS
    read -p "Enter your timezone (e.g., America/New_York): " TIMEZONE
    
    # Update profile
    PROFILE_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/profile" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"fullName\":\"$FULL_NAME\",
            \"phone\":\"$PHONE\",
            \"address\":\"$ADDRESS\",
            \"workingHours\":\"$WORKING_HOURS\",
            \"timezone\":\"$TIMEZONE\"
        }")
    
    if echo "$PROFILE_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Profile configured successfully"
    else
        print_error "Failed to configure profile"
        print_info "Response: $PROFILE_RESPONSE"
    fi
}

# Configure notification preferences
configure_notifications() {
    print_step "6" "Configuring notification preferences..."
    
    echo "Select notification preferences:"
    echo "1. Email notifications"
    echo "2. SMS notifications"
    echo "3. Push notifications"
    echo "4. All notifications"
    
    read -p "Enter your choice (1-4): " NOTIFICATION_CHOICE
    
    case $NOTIFICATION_CHOICE in
        1)
            NOTIFICATION_TYPES="email"
            ;;
        2)
            NOTIFICATION_TYPES="sms"
            ;;
        3)
            NOTIFICATION_TYPES="push"
            ;;
        4)
            NOTIFICATION_TYPES="email,sms,push"
            ;;
        *)
            NOTIFICATION_TYPES="email"
            ;;
    esac
    
    # Configure notifications
    NOTIFICATION_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/notifications" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"types\":\"$NOTIFICATION_TYPES\",
            \"enabled\":true,
            \"urgentOnly\":false
        }")
    
    if echo "$NOTIFICATION_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Notification preferences configured"
    else
        print_error "Failed to configure notifications"
        print_info "Response: $NOTIFICATION_RESPONSE"
    fi
}

# Configure coverage area
configure_coverage() {
    print_step "7" "Configuring coverage area..."
    
    read -p "Enter your coverage area name: " COVERAGE_NAME
    read -p "Enter coverage radius in kilometers: " COVERAGE_RADIUS
    read -p "Enter center latitude: " CENTER_LAT
    read -p "Enter center longitude: " CENTER_LON
    
    # Configure coverage area
    COVERAGE_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/coverage" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"name\":\"$COVERAGE_NAME\",
            \"radius\":$COVERAGE_RADIUS,
            \"centerLatitude\":$CENTER_LAT,
            \"centerLongitude\":$CENTER_LON,
            \"active\":true
        }")
    
    if echo "$COVERAGE_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Coverage area configured"
    else
        print_error "Failed to configure coverage area"
        print_info "Response: $COVERAGE_RESPONSE"
    fi
}

# Configure dashboard preferences
configure_dashboard() {
    print_step "8" "Configuring dashboard preferences..."
    
    echo "Select dashboard layout:"
    echo "1. Standard layout"
    echo "2. Compact layout"
    echo "3. Detailed layout"
    echo "4. Custom layout"
    
    read -p "Enter your choice (1-4): " LAYOUT_CHOICE
    
    case $LAYOUT_CHOICE in
        1)
            LAYOUT="standard"
            ;;
        2)
            LAYOUT="compact"
            ;;
        3)
            LAYOUT="detailed"
            ;;
        4)
            LAYOUT="custom"
            ;;
        *)
            LAYOUT="standard"
            ;;
    esac
    
    # Configure dashboard
    DASHBOARD_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/dashboard" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"layout\":\"$LAYOUT\",
            \"autoRefresh\":true,
            \"refreshInterval\":30,
            \"showMap\":true,
            \"showTasks\":true,
            \"showInventory\":true
        }")
    
    if echo "$DASHBOARD_RESPONSE" | jq -e '.success' > /dev/null; then
        print_success "Dashboard preferences configured"
    else
        print_error "Failed to configure dashboard"
        print_info "Response: $DASHBOARD_RESPONSE"
    fi
}

# Test dispatcher functionality
test_functionality() {
    print_step "9" "Testing dispatcher functionality..."
    
    # Test needs access
    NEEDS_RESPONSE=$(curl -s -X GET "$PLATFORM_URL/api/dispatcher/needs" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$NEEDS_RESPONSE" | jq -e '.needs' > /dev/null; then
        print_success "Needs access working"
    else
        print_error "Needs access failed"
        print_info "Response: $NEEDS_RESPONSE"
    fi
    
    # Test tasks access
    TASKS_RESPONSE=$(curl -s -X GET "$PLATFORM_URL/api/dispatcher/tasks" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$TASKS_RESPONSE" | jq -e '.tasks' > /dev/null; then
        print_success "Tasks access working"
    else
        print_error "Tasks access failed"
        print_info "Response: $TASKS_RESPONSE"
    fi
    
    # Test inventory access
    INVENTORY_RESPONSE=$(curl -s -X GET "$PLATFORM_URL/api/dispatcher/inventory" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$INVENTORY_RESPONSE" | jq -e '.inventory' > /dev/null; then
        print_success "Inventory access working"
    else
        print_error "Inventory access failed"
        print_info "Response: $INVENTORY_RESPONSE"
    fi
}

# Create sample data
create_sample_data() {
    print_step "10" "Creating sample data for testing..."
    
    # Create sample needs
    SAMPLE_NEEDS=(
        '{"description":"Emergency food supplies needed","category":"food","severity":3,"location":"123 Main St, City, State","latitude":40.7128,"longitude":-74.0060}'
        '{"description":"Medical assistance required","category":"medical","severity":4,"location":"456 Oak Ave, City, State","latitude":40.7589,"longitude":-73.9851}'
        '{"description":"Shelter needed for family","category":"shelter","severity":2,"location":"789 Pine St, City, State","latitude":40.7505,"longitude":-73.9934}'
    )
    
    for need in "${SAMPLE_NEEDS[@]}"; do
        NEED_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/needs" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "$need")
        
        if echo "$NEED_RESPONSE" | jq -e '.id' > /dev/null; then
            print_success "Sample need created"
        else
            print_error "Failed to create sample need"
            print_info "Response: $NEED_RESPONSE"
        fi
    done
    
    # Create sample tasks
    SAMPLE_TASKS=(
        '{"title":"Deliver food supplies","description":"Deliver emergency food to affected families","priority":"high","category":"delivery"}'
        '{"title":"Medical assessment","description":"Conduct medical assessment of injured persons","priority":"urgent","category":"medical"}'
        '{"title":"Shelter setup","description":"Set up temporary shelter for displaced families","priority":"medium","category":"shelter"}'
    )
    
    for task in "${SAMPLE_TASKS[@]}"; do
        TASK_RESPONSE=$(curl -s -X POST "$PLATFORM_URL/api/dispatcher/tasks" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "$task")
        
        if echo "$TASK_RESPONSE" | jq -e '.id' > /dev/null; then
            print_success "Sample task created"
        else
            print_error "Failed to create sample task"
            print_info "Response: $TASK_RESPONSE"
        fi
    done
}

# Generate setup report
generate_report() {
    print_step "11" "Generating setup report..."
    
    REPORT_FILE="dispatcher-setup-report-$(date +%Y%m%d-%H%M%S).txt"
    
    cat > "$REPORT_FILE" << EOF
Disaster Relief Platform - Dispatcher Setup Report
Generated: $(date)
Platform URL: $PLATFORM_URL
Dispatcher Email: $DISPATCHER_EMAIL

Setup Steps Completed:
1. Prerequisites check - PASSED
2. Credentials obtained - PASSED
3. Platform connectivity - PASSED
4. Dispatcher login - PASSED
5. Profile configuration - COMPLETED
6. Notification preferences - COMPLETED
7. Coverage area - COMPLETED
8. Dashboard preferences - COMPLETED
9. Functionality test - COMPLETED
10. Sample data creation - COMPLETED
11. Setup report - COMPLETED

Next Steps:
1. Access the dispatcher dashboard
2. Review and test all functionality
3. Familiarize yourself with the interface
4. Begin managing disaster relief operations

Support Information:
- Documentation: https://docs.disaster-relief.local
- Support: support@disaster-relief.local
- Training: training@disaster-relief.local
EOF
    
    print_success "Setup report generated: $REPORT_FILE"
}

# Main execution
main() {
    print_header
    
    check_prerequisites
    get_credentials
    test_connectivity
    dispatcher_login
    configure_profile
    configure_notifications
    configure_coverage
    configure_dashboard
    test_functionality
    create_sample_data
    generate_report
    
    echo
    print_success "Dispatcher setup completed successfully!"
    print_info "Please review the setup report and test all functionality"
    print_info "Next steps:"
    print_info "1. Access the dispatcher dashboard at $PLATFORM_URL/dispatcher"
    print_info "2. Review your profile and preferences"
    print_info "3. Familiarize yourself with the interface"
    print_info "4. Begin managing disaster relief operations"
    echo
}

# Run main function
main "$@"



