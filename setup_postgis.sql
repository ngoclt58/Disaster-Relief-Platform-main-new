-- Script để kích hoạt PostGIS extension trong database
-- Chạy script này trong pgAdmin hoặc psql

-- Kết nối đến database relief_platform
\c relief_platform

-- Tạo PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Kiểm tra PostGIS đã được cài đặt
SELECT PostGIS_version();

-- Kiểm tra các extensions đã được cài
\dx

