#!/bin/bash

echo "Starting Disaster Relief Platform..."
echo

echo "========================================"
echo "Disaster Relief Platform"
echo "========================================"
echo
echo "Please start services manually:"
echo
echo "1. Start PostgreSQL (with PostGIS):"
echo "   - Ensure PostgreSQL is running on port 5432"
echo "   - Database: relief_platform"
echo "   - User: postgres"
echo "   - Password: postgres"
echo
echo "2. Start MinIO (optional, for file storage):"
echo "   - Ensure MinIO is running on port 9000"
echo "   - Console: http://localhost:9001"
echo
echo "3. Start Backend:"
echo "   cd backend"
echo "   mvn spring-boot:run"
echo
echo "4. Start Frontend (in another terminal):"
echo "   cd frontend"
echo "   npm install"
echo "   npm start"
echo
echo "Services:"
echo "- Frontend: http://localhost:3000"
echo "- Backend API: http://localhost:8080"
echo "- API Documentation: http://localhost:8080/swagger-ui.html"
echo "- MinIO Console: http://localhost:9001 (if running)"
echo
echo "Default admin credentials:"
echo "- Email: admin@relief.local"
echo "- Password: admin123"
echo
