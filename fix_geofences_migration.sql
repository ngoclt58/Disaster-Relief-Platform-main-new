-- Script để sửa lỗi migration geofences
-- Chạy script này trong pgAdmin hoặc psql

-- Kết nối đến database
\c relief_platform

-- Xóa bảng geofences cũ và các bảng liên quan (nếu có)
-- LƯU Ý: Nếu bạn có dữ liệu quan trọng trong các bảng này, hãy backup trước!
DROP TABLE IF EXISTS geofence_alerts CASCADE;
DROP TABLE IF EXISTS geofence_events CASCADE;
DROP TABLE IF EXISTS geofences CASCADE;

-- Xóa các views liên quan (nếu có)
DROP VIEW IF EXISTS active_geofence_alerts CASCADE;
DROP VIEW IF EXISTS active_geofence_events CASCADE;
DROP VIEW IF EXISTS geofence_summary CASCADE;

-- Xóa các functions liên quan (nếu có)
DROP FUNCTION IF EXISTS get_geofence_alert_statistics(TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS get_geofence_event_statistics(BIGINT, TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS get_geofence_statistics(TIMESTAMP, TIMESTAMP) CASCADE;
DROP FUNCTION IF EXISTS check_point_in_geofences(DOUBLE PRECISION, DOUBLE PRECISION) CASCADE;

-- Xóa migration history của V7 để chạy lại
DELETE FROM flyway_schema_history WHERE version = '7';

-- Kiểm tra kết quả
SELECT 'Geofences tables and migration history cleaned. You can now restart the application.' AS status;

