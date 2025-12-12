# Hướng dẫn sửa lỗi Flyway Checksum Mismatch

## Vấn đề
Flyway phát hiện các file migration V1 và V2 đã bị thay đổi sau khi đã chạy trong database, dẫn đến checksum không khớp.

## Nguyên nhân
- File migration đã được sửa sau khi chạy (ví dụ: xóa bảng geofences khỏi V1)
- Flyway kiểm tra checksum để đảm bảo migration files không bị thay đổi sau khi đã chạy

## Giải pháp

### Cách 1: Cập nhật checksum trong database (Khuyến nghị)

**Bước 1: Chạy script SQL**

1. Mở pgAdmin hoặc psql
2. Kết nối đến database `relief_platform`
3. Chạy file `fix_flyway_checksum.sql` hoặc chạy SQL sau:

```sql
-- Cập nhật checksum cho V1
UPDATE flyway_schema_history 
SET checksum = -1619974402 
WHERE version = '1';

-- Cập nhật checksum cho V2
UPDATE flyway_schema_history 
SET checksum = -2126253793 
WHERE version = '2';
```

**Bước 2: Chạy lại ứng dụng**

```bash
cd backend
mvn spring-boot:run
```

### Cách 2: Sử dụng Flyway Repair Command

Nếu bạn có Flyway CLI cài đặt:

```bash
cd backend
mvn flyway:repair
```

Hoặc nếu dùng Flyway command line:

```bash
flyway repair -url=jdbc:postgresql://localhost:5432/relief_platform -user=postgres -password=postgres
```

### Cách 3: Cấu hình Flyway để bỏ qua validation (Không khuyến nghị)

Chỉ dùng cho development, không dùng cho production!

Thêm vào `application.yml`:

```yaml
spring:
  flyway:
    validate-on-migrate: false
```

**⚠️ CẢNH BÁO:** Cách này sẽ tắt validation hoàn toàn, có thể gây vấn đề về tính nhất quán của database.

### Cách 4: Xóa migration history và chạy lại (Chỉ dùng khi không có dữ liệu quan trọng)

⚠️ **CẢNH BÁO:** Cách này sẽ xóa toàn bộ migration history. Chỉ dùng khi:
- Database mới, chưa có dữ liệu quan trọng
- Hoặc bạn đã backup toàn bộ dữ liệu

```sql
-- Xóa toàn bộ migration history
TRUNCATE TABLE flyway_schema_history;

-- Xóa tất cả các bảng (nếu cần)
-- LƯU Ý: Chỉ làm nếu bạn chắc chắn muốn xóa toàn bộ dữ liệu!
```

Sau đó chạy lại ứng dụng, Flyway sẽ chạy lại tất cả migrations từ đầu.

## Kiểm tra

Sau khi sửa, kiểm tra:

```sql
-- Xem migration history
SELECT version, description, type, installed_on, success, checksum 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

## Khuyến nghị

**Cho Development:**
- Dùng Cách 1 (cập nhật checksum) hoặc Cách 2 (flyway repair)

**Cho Production:**
- Không bao giờ sửa migration files đã chạy
- Tạo migration mới (V14, V15, ...) để thay đổi schema
- Nếu bắt buộc phải sửa, backup database trước và dùng Cách 1

## Lưu ý

- Checksum được tính dựa trên nội dung file migration
- Khi bạn sửa file migration, checksum sẽ thay đổi
- Flyway kiểm tra checksum để đảm bảo tính nhất quán
- Sau khi cập nhật checksum, đảm bảo migration files đã được sửa đúng cách

