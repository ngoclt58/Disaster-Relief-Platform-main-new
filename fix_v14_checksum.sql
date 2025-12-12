-- Script để sửa lỗi Flyway checksum mismatch cho V14
-- Chạy script này trong pgAdmin hoặc psql

-- Kết nối đến database
\c relief_platform

-- Xem checksum hiện tại của V14
SELECT version, description, type, installed_on, success, checksum 
FROM flyway_schema_history 
WHERE version = '14';

-- Cập nhật checksum cho V14 (checksum mới: -1061873559)
UPDATE flyway_schema_history 
SET checksum = -1061873559 
WHERE version = '14';

-- Kiểm tra kết quả
SELECT version, description, checksum 
FROM flyway_schema_history 
WHERE version = '14';

SELECT 'V14 checksum updated successfully. You can now restart the application.' AS status;

