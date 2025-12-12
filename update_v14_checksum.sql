-- Script đơn giản để cập nhật checksum cho V14
-- Copy và chạy toàn bộ script này trong pgAdmin Query Tool

-- Kết nối đến database relief_platform
\c relief_platform

-- Cập nhật checksum cho V14
UPDATE flyway_schema_history 
SET checksum = -1061873559 
WHERE version = '14';

-- Kiểm tra kết quả
SELECT version, description, checksum, success
FROM flyway_schema_history 
WHERE version = '14';

-- Nếu thấy checksum = -1061873559, nghĩa là đã cập nhật thành công!

