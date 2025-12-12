# Hướng dẫn sửa lỗi Migration Geofences

## Vấn đề
Bảng `geofences` đã được tạo trong V1__baseline.sql nhưng V7__Create_geofencing_tables.sql cũng cố gắng tạo lại, gây lỗi "relation already exists".

## Giải pháp

### Cách 1: Chạy script SQL tự động (Khuyến nghị)

1. **Mở pgAdmin hoặc psql**

2. **Kết nối đến database `relief_platform`**

3. **Chạy script:**
   ```sql
   -- Xóa bảng cũ
   DROP TABLE IF EXISTS geofence_alerts CASCADE;
   DROP TABLE IF EXISTS geofence_events CASCADE;
   DROP TABLE IF EXISTS geofences CASCADE;
   
   -- Xóa migration history của V7
   DELETE FROM flyway_schema_history WHERE version = '7';
   ```

4. **Hoặc chạy file script:**
   - Mở file `fix_geofences_migration.sql` trong pgAdmin
   - Chạy toàn bộ script

5. **Chạy lại ứng dụng:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Cách 2: Sử dụng script đã tạo

File `fix_geofences_migration.sql` đã được tạo sẵn với tất cả các lệnh cần thiết.

**Các bước:**
1. Mở pgAdmin
2. Kết nối đến PostgreSQL → database `relief_platform`
3. Tools → Query Tool
4. File → Open → Chọn `fix_geofences_migration.sql`
5. Execute (F5)
6. Chạy lại ứng dụng

### Cách 3: Xóa thủ công trong pgAdmin

1. Mở pgAdmin
2. Kết nối đến database `relief_platform`
3. Schemas → public → Tables
4. Right-click vào các bảng sau → Delete/Drop:
   - `geofence_alerts`
   - `geofence_events`
   - `geofences`
5. Schemas → public → Views → Xóa các view liên quan
6. Schemas → public → Functions → Xóa các function liên quan
7. Schemas → public → Tables → `flyway_schema_history` → Right-click → View/Edit Data
8. Xóa dòng có `version = '7'`
9. Chạy lại ứng dụng

## Lưu ý

⚠️ **QUAN TRỌNG:** Nếu bạn có dữ liệu quan trọng trong các bảng geofences, hãy backup trước khi xóa:

```sql
-- Backup dữ liệu (nếu cần)
CREATE TABLE geofences_backup AS SELECT * FROM geofences;
CREATE TABLE geofence_events_backup AS SELECT * FROM geofence_events;
CREATE TABLE geofence_alerts_backup AS SELECT * FROM geofence_alerts;
```

## Sau khi sửa

Sau khi chạy script và restart ứng dụng:
- ✅ Migration V7 sẽ chạy lại và tạo bảng geofences với cấu trúc đầy đủ
- ✅ Tất cả các bảng, indexes, constraints, functions, views sẽ được tạo đúng
- ✅ Không còn lỗi "relation already exists"

## Kiểm tra

Sau khi migration chạy xong, kiểm tra:

```sql
-- Kiểm tra bảng đã được tạo
\dt geofences
\dt geofence_events
\dt geofence_alerts

-- Kiểm tra cấu trúc bảng
\d geofences

-- Kiểm tra migration history
SELECT * FROM flyway_schema_history WHERE version = '7';
```

