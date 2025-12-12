# Các bước tiếp theo sau khi cài PostGIS

## Bước 1: Kích hoạt PostGIS Extension trong Database

### Cách 1: Sử dụng pgAdmin (Dễ nhất)

1. **Mở pgAdmin**
2. **Kết nối đến PostgreSQL server**
   - Server: localhost
   - Port: 5432
   - Username: postgres
   - Password: postgres

3. **Chọn database `relief_platform`**
   - Mở rộng Databases → relief_platform

4. **Mở Query Tool**
   - Right-click vào `relief_platform` → Query Tool
   - Hoặc: Tools → Query Tool

5. **Chạy lệnh SQL:**
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```

6. **Kiểm tra PostGIS đã hoạt động:**
   ```sql
   SELECT PostGIS_version();
   ```
   Nếu trả về version (ví dụ: `3.6.1`), nghĩa là thành công!

### Cách 2: Sử dụng psql (Command Line)

1. **Mở Command Prompt hoặc PowerShell**

2. **Kết nối đến database:**
   ```bash
   psql -U postgres -d relief_platform
   ```
   (Nhập password khi được hỏi: `postgres`)

3. **Chạy lệnh:**
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```

4. **Kiểm tra:**
   ```sql
   SELECT PostGIS_version();
   \dx
   ```

5. **Thoát:**
   ```sql
   \q
   ```

## Bước 2: Chạy lại Migration của Ứng dụng

Sau khi kích hoạt PostGIS, chạy lại ứng dụng để migration tự động chạy:

### Cách 1: Chạy từ IDE (IntelliJ/Eclipse)

1. Mở project trong IDE
2. Tìm class `DisasterReliefPlatformApplication` (hoặc class có `@SpringBootApplication`)
3. Right-click → Run

### Cách 2: Chạy bằng Maven

```bash
cd backend
mvn spring-boot:run
```

### Cách 3: Build và chạy JAR

```bash
cd backend
mvn clean package
java -jar target/disaster-relief-platform-*.jar
```

## Bước 3: Kiểm tra Migration đã chạy thành công

Sau khi ứng dụng khởi động, kiểm tra:

1. **Xem log của ứng dụng:**
   - Tìm dòng "Flyway migration completed"
   - Không có lỗi về geometry type

2. **Kiểm tra trong database:**
   ```sql
   -- Xem các bảng đã được tạo
   \dt
   
   -- Kiểm tra bảng provinces có cột geom
   \d provinces
   
   -- Kiểm tra PostGIS extension
   SELECT * FROM pg_extension WHERE extname = 'postgis';
   ```

## Bước 4: Kiểm tra Ứng dụng hoạt động

1. **Kiểm tra health endpoint:**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

2. **Hoặc mở browser:**
   ```
   http://localhost:8080/api/actuator/health
   ```

## Troubleshooting

### Lỗi: "extension postgis is not available"
- Đảm bảo PostGIS đã được cài đặt trên PostgreSQL server (không chỉ trong database)
- Khởi động lại PostgreSQL service nếu cần

### Lỗi: "permission denied"
- Đảm bảo bạn đang dùng user `postgres` hoặc user có quyền superuser

### Lỗi: "database does not exist"
- Tạo database trước:
  ```sql
  CREATE DATABASE relief_platform;
  ```

### Migration vẫn báo lỗi geometry
- Đảm bảo đã chạy `CREATE EXTENSION postgis;` trong database `relief_platform`
- Kiểm tra lại: `SELECT PostGIS_version();`

## Kết quả mong đợi

Sau khi hoàn thành tất cả các bước:
- ✅ PostGIS extension đã được kích hoạt
- ✅ Tất cả migration đã chạy thành công
- ✅ Các bảng với cột geometry đã được tạo
- ✅ Ứng dụng khởi động thành công
- ✅ Health endpoint trả về status UP

