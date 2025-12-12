# Hướng dẫn cài đặt PostGIS trên Windows

## Cách 1: Sử dụng Stack Builder (Khuyến nghị)

1. **Mở Stack Builder**
   - Vào Start Menu → Tìm "Stack Builder"
   - Hoặc: Start Menu → PostgreSQL → Stack Builder

2. **Chọn PostgreSQL Server**
   - Chọn version PostgreSQL bạn đã cài đặt
   - Nhấn "Next"

3. **Cài đặt PostGIS**
   - Mở rộng mục "Spatial Extensions"
   - Chọn "PostGIS Bundle for PostgreSQL"
   - Nhấn "Next" và làm theo hướng dẫn
   - Chọn thư mục cài đặt (thường là cùng thư mục với PostgreSQL)
   - Hoàn tất cài đặt

## Cách 2: Tải trực tiếp từ trang chủ

1. **Tải PostGIS**
   - Truy cập: https://postgis.net/windows_downloads/
   - Chọn version phù hợp với PostgreSQL của bạn
   - Tải file installer (.exe)

2. **Xử lý cảnh báo Windows Defender SmartScreen**
   - Khi chạy file .exe, Windows có thể hiện cảnh báo "Windows protected your PC"
   - **Đây là cảnh báo bình thường** vì PostGIS chưa được Microsoft xác thực
   - Nhấn **"More info"** (Thông tin khác)
   - Sau đó nhấn **"Run anyway"** (Vẫn chạy) hoặc **"Run"** (Chạy)
   - PostGIS là phần mềm mã nguồn mở an toàn từ trang chủ chính thức

3. **Cài đặt**
   - Chạy file .exe đã tải
   - Chọn PostgreSQL server để cài PostGIS
   - Làm theo hướng dẫn

## Sau khi cài đặt - Kích hoạt PostGIS trong database

### Cách 1: Sử dụng pgAdmin

1. Mở pgAdmin
2. Kết nối đến PostgreSQL server
3. Chọn database của bạn (ví dụ: `disaster_relief`)
4. Mở Query Tool (Tools → Query Tool)
5. Chạy lệnh:
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```
6. Kiểm tra:
   ```sql
   SELECT * FROM pg_extension WHERE extname = 'postgis';
   ```

### Cách 2: Sử dụng psql (Command Line)

1. Mở Command Prompt hoặc PowerShell
2. Kết nối đến database:
   ```bash
   psql -U postgres -d disaster_relief
   ```
   (Thay `disaster_relief` bằng tên database của bạn)

3. Chạy lệnh:
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```

4. Kiểm tra:
   ```sql
   \dx
   ```
   Bạn sẽ thấy `postgis` trong danh sách extensions

## Kiểm tra PostGIS đã hoạt động

Chạy lệnh SQL sau để kiểm tra:

```sql
SELECT PostGIS_version();
```

Nếu trả về version (ví dụ: `3.3.3`), nghĩa là PostGIS đã được cài đặt thành công!

## Khởi động lại PostgreSQL (nếu cần)

Sau khi cài PostGIS, bạn có thể cần khởi động lại PostgreSQL service:

1. Mở Services (services.msc)
2. Tìm "postgresql-x64-XX" (XX là version)
3. Right-click → Restart

## Lưu ý

- Đảm bảo version PostGIS tương thích với version PostgreSQL
- Bạn cần quyền superuser để cài đặt extension
- Sau khi cài PostGIS, chạy lại migration của ứng dụng

## Troubleshooting

### Lỗi: Windows Defender SmartScreen chặn file cài đặt

**Hiện tượng:** Windows hiện cảnh báo "Windows protected your PC" khi chạy file PostGIS installer.

**Giải pháp:**
1. Nhấn **"More info"** (Thông tin khác) trong dialog cảnh báo
2. Nhấn **"Run anyway"** (Vẫn chạy) hoặc **"Run"** (Chạy)
3. PostGIS là phần mềm mã nguồn mở an toàn từ trang chủ chính thức (postgis.net)

**Lưu ý:** Nếu không thấy nút "Run anyway":
- Right-click vào file .exe → Properties
- Ở cuối dialog, tick vào "Unblock" (Bỏ chặn)
- Nhấn OK, sau đó chạy lại file

### Lỗi: "CRYPT_E_REVOCATION_OFFLINE" hoặc "Couldn't access the URL" trong Stack Builder

**Nguyên nhân:** Windows không thể kiểm tra chứng chỉ SSL vì revocation server offline.

**Giải pháp 1: Tải trực tiếp từ trang chủ (Khuyến nghị)**
1. Truy cập: https://postgis.net/windows_downloads/
2. Tìm version phù hợp với PostgreSQL của bạn (ví dụ: PostgreSQL 18 → PostGIS 3.6.1)
3. Tải file `.exe` installer trực tiếp
4. Chạy file .exe và cài đặt như bình thường

**Giải pháp 2: Tắt kiểm tra certificate revocation tạm thời**
1. Mở Command Prompt với quyền Administrator
2. Chạy lệnh:
   ```cmd
   reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\WinTrust\Trust Providers\Software Publishing" /v State /t REG_DWORD /d 0x00023e00 /f
   ```
3. Thử lại Stack Builder
4. Sau khi cài xong, bật lại:
   ```cmd
   reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\WinTrust\Trust Providers\Software Publishing" /v State /t REG_DWORD /d 0x00023c00 /f
   ```

**Giải pháp 3: Sử dụng VPN hoặc thay đổi DNS**
- Đôi khi vấn đề do mạng/firewall
- Thử dùng VPN hoặc đổi DNS (8.8.8.8, 1.1.1.1)

### Lỗi: "extension postgis is not available"
- Đảm bảo PostGIS đã được cài đặt trên server (không chỉ trong database)
- Kiểm tra PostgreSQL version và PostGIS version có tương thích không

### Lỗi: "permission denied"
- Đảm bảo bạn đang dùng user có quyền superuser (thường là `postgres`)

### Không tìm thấy Stack Builder
- Cài lại PostgreSQL với đầy đủ components
- Hoặc tải PostGIS trực tiếp từ trang chủ

