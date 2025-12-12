# Hướng Dẫn Khởi Động MinIO Đơn Giản (Không Cần Docker)

## Giải Pháp Đơn Giản Nhất

### Bước 1: Tải MinIO Binary

Chạy script tự động:
```powershell
.\download-minio.ps1
```

Hoặc tải thủ công:
1. Truy cập: https://min.io/download
2. Chọn "Windows" → "AMD64"
3. Tải file `minio.exe`
4. Đặt vào thư mục `minio-bin` trong project

### Bước 2: Khởi Động MinIO

Chạy script:
```powershell
.\start-minio-local.ps1
```

Hoặc chạy thủ công:
```powershell
# Tạo thư mục lưu trữ
New-Item -ItemType Directory -Path "minio-data" -Force

# Chạy MinIO (thay đường dẫn nếu cần)
.\minio-bin\minio.exe server .\minio-data --console-address ":9001"
```

### Bước 3: Xác Nhận MinIO Đang Chạy

1. Mở trình duyệt: http://localhost:9001
2. Đăng nhập:
   - Username: `minioadmin`
   - Password: `minioadmin123`

### Bước 4: Restart Backend

Sau khi MinIO chạy, restart backend Spring Boot để kết nối.

## Lưu Ý

- MinIO sẽ chạy trong cửa sổ PowerShell riêng
- Để dừng MinIO, đóng cửa sổ đó hoặc nhấn `Ctrl+C`
- Dữ liệu được lưu trong thư mục `minio-data`


