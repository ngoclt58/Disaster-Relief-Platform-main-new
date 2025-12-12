@echo off
REM Disaster Relief Platform - Admin Setup Script (Windows)
REM This script automates the initial admin setup process

setlocal enabledelayedexpansion

REM Colors for output (Windows doesn't support colors in batch files)
set "SUCCESS=✓"
set "ERROR=✗"
set "INFO=ℹ"
set "WARNING=⚠"

REM Configuration
set "PLATFORM_URL=https://disaster-relief.local"
set "ADMIN_EMAIL=admin@disaster-relief.local"
set "DEFAULT_PASSWORD=ChangeMe123!"

echo.
echo ==================================================
echo   Disaster Relief Platform - Admin Setup
echo ==================================================
echo.

REM Check prerequisites
echo [STEP 1] Checking prerequisites...
where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERROR% curl is required but not installed
    echo %INFO% Please install curl from https://curl.se/download.html
    exit /b 1
)

where jq >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERROR% jq is required but not installed
    echo %INFO% Please install jq from https://stedolan.github.io/jq/download/
    exit /b 1
)

echo %SUCCESS% Prerequisites check passed

REM Test platform connectivity
echo [STEP 2] Testing platform connectivity...
curl -s --connect-timeout 10 "%PLATFORM_URL%/health" >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERROR% Cannot connect to platform at %PLATFORM_URL%
    echo %INFO% Please ensure the platform is running and accessible
    exit /b 1
)

echo %SUCCESS% Platform is accessible

REM Admin login
echo [STEP 3] Logging in as admin...
curl -s -X POST "%PLATFORM_URL%/api/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"%ADMIN_EMAIL%\",\"password\":\"%DEFAULT_PASSWORD%\"}" > temp_login.json
if %errorlevel% neq 0 (
    echo %ERROR% Admin login failed
    exit /b 1
)

REM Extract token from response
for /f "tokens=2 delims=:" %%a in ('findstr "token" temp_login.json') do (
    set "TOKEN=%%a"
    set "TOKEN=!TOKEN:"=!"
    set "TOKEN=!TOKEN:,=!"
    set "TOKEN=!TOKEN: =!"
)

if "%TOKEN%"=="" (
    echo %ERROR% Failed to extract login token
    type temp_login.json
    exit /b 1
)

echo %SUCCESS% Admin login successful

REM Change default password
echo [STEP 4] Changing default password...
set /p "NEW_PASSWORD=Enter new admin password: "
set /p "CONFIRM_PASSWORD=Confirm new admin password: "

if not "%NEW_PASSWORD%"=="%CONFIRM_PASSWORD%" (
    echo %ERROR% Passwords do not match
    exit /b 1
)

if %NEW_PASSWORD:~0,12% lss 12 (
    echo %ERROR% Password must be at least 12 characters long
    exit /b 1
)

curl -s -X POST "%PLATFORM_URL%/api/auth/change-password" -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"currentPassword\":\"%DEFAULT_PASSWORD%\",\"newPassword\":\"%NEW_PASSWORD%\"}" > temp_password.json
if %errorlevel% neq 0 (
    echo %ERROR% Failed to change password
    exit /b 1
)

echo %SUCCESS% Password changed successfully

REM Configure system settings
echo [STEP 5] Configuring system settings...
set /p "ORG_NAME=Enter organization name: "
set /p "ORG_EMAIL=Enter organization email: "
set /p "ORG_PHONE=Enter organization phone: "
set /p "ORG_ADDRESS=Enter organization address: "

curl -s -X POST "%PLATFORM_URL%/api/admin/system/organization" -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"name\":\"%ORG_NAME%\",\"email\":\"%ORG_EMAIL%\",\"phone\":\"%ORG_PHONE%\",\"address\":\"%ORG_ADDRESS%\"}" > temp_org.json
if %errorlevel% neq 0 (
    echo %ERROR% Failed to configure organization settings
    exit /b 1
)

echo %SUCCESS% Organization settings configured

REM Create initial users
echo [STEP 6] Creating initial users...
set /p "DISPATCHER_COUNT=Enter number of dispatcher users to create: "

for /l %%i in (1,1,%DISPATCHER_COUNT%) do (
    echo Creating dispatcher user %%i...
    set /p "DISPATCHER_NAME=Enter dispatcher %%i full name: "
    set /p "DISPATCHER_EMAIL=Enter dispatcher %%i email: "
    set /p "DISPATCHER_PHONE=Enter dispatcher %%i phone: "
    
    curl -s -X POST "%PLATFORM_URL%/api/admin/users" -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"fullName\":\"!DISPATCHER_NAME!\",\"email\":\"!DISPATCHER_EMAIL!\",\"phone\":\"!DISPATCHER_PHONE!\",\"role\":\"DISPATCHER\",\"active\":true}" > temp_dispatcher_%%i.json
    if %errorlevel% neq 0 (
        echo %ERROR% Failed to create dispatcher %%i
    ) else (
        echo %SUCCESS% Dispatcher %%i created successfully
    )
)

REM Generate setup report
echo [STEP 7] Generating setup report...
set "REPORT_FILE=admin-setup-report-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%.txt"
set "REPORT_FILE=%REPORT_FILE: =0%"

(
echo Disaster Relief Platform - Admin Setup Report
echo Generated: %date% %time%
echo Platform URL: %PLATFORM_URL%
echo Admin Email: %ADMIN_EMAIL%
echo.
echo Setup Steps Completed:
echo 1. Prerequisites check - PASSED
echo 2. Platform connectivity - PASSED
echo 3. Admin login - PASSED
echo 4. Password change - PASSED
echo 5. System configuration - COMPLETED
echo 6. Initial users created - COMPLETED
echo 7. Setup report - COMPLETED
echo.
echo Next Steps:
echo 1. Review system settings
echo 2. Test all functionality
echo 3. Train users
echo 4. Begin operations
echo.
echo Support Information:
echo - Documentation: https://docs.disaster-relief.local
echo - Support: support@disaster-relief.local
echo - Emergency: +1-555-EMERGENCY
) > "%REPORT_FILE%"

echo %SUCCESS% Setup report generated: %REPORT_FILE%

REM Cleanup
del temp_*.json >nul 2>&1

echo.
echo %SUCCESS% Admin setup completed successfully!
echo %INFO% Please review the setup report and test all functionality
echo %INFO% Next steps:
echo %INFO% 1. Access the admin dashboard at %PLATFORM_URL%/admin
echo %INFO% 2. Review and adjust system settings
echo %INFO% 3. Train your team on the platform
echo %INFO% 4. Begin disaster relief operations
echo.

pause



