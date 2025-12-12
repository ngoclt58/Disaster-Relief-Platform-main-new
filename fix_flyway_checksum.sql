-- Script để sửa lỗi Flyway checksum mismatch
-- Chạy script này trong pgAdmin hoặc psql

-- Kết nối đến database
\c relief_platform

-- Cách 1: Cập nhật checksum trong flyway_schema_history để khớp với file hiện tại
-- Lưu ý: Chỉ làm điều này nếu bạn chắc chắn đã sửa migration files đúng cách

-- Xem checksum hiện tại
SELECT version, description, type, installed_on, success, checksum 
FROM flyway_schema_history 
WHERE version IN ('1', '2')
ORDER BY version;

-- Cập nhật checksum cho V1 (checksum mới: -1619974402)
UPDATE flyway_schema_history 
SET checksum = -1619974402 
WHERE version = '1';

-- Cập nhật checksum cho V2 (checksum mới: -2126253793)
UPDATE flyway_schema_history 
SET checksum = -2126253793 
WHERE version = '2';

-- Cập nhật checksum cho V14 (checksum mới: -1061873559)
UPDATE flyway_schema_history 
SET checksum = -1061873559 
WHERE version = '14';

-- Kiểm tra kết quả
SELECT version, description, checksum 
FROM flyway_schema_history 
WHERE version IN ('1', '2', '14')
ORDER BY version;

SELECT 'Checksum updated successfully. You can now restart the application.' AS status;

