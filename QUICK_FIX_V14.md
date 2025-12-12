# Hướng dẫn nhanh sửa lỗi V14 Checksum

## Vấn đề
Checksum của migration V14 trong database không khớp với file hiện tại.

## Giải pháp nhanh

### Bước 1: Mở pgAdmin

1. Mở pgAdmin
2. Kết nối đến PostgreSQL server
3. Mở rộng: Servers → PostgreSQL → Databases → **relief_platform**

### Bước 2: Chạy SQL

1. Click phải vào database `relief_platform`
2. Chọn **Query Tool**
3. Copy và paste đoạn SQL sau:

```sql
UPDATE flyway_schema_history 
SET checksum = -1061873559 
WHERE version = '14';
```

4. Nhấn **Execute** (hoặc F5)

### Bước 3: Kiểm tra

Chạy SQL này để kiểm tra:

```sql
SELECT version, description, checksum 
FROM flyway_schema_history 
WHERE version = '14';
```

Bạn sẽ thấy `checksum = -1061873559` nếu đã cập nhật thành công.

### Bước 4: Chạy lại ứng dụng

```bash
cd backend
mvn spring-boot:run
```

## Hoặc dùng file script

1. Mở file `update_v14_checksum.sql` trong pgAdmin
2. Execute (F5)
3. Chạy lại ứng dụng

## Lưu ý

- Đảm bảo bạn đang kết nối đến đúng database: `relief_platform`
- Nếu không thấy bảng `flyway_schema_history`, có thể migration chưa chạy lần nào

