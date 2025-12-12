# Hướng Dẫn Đơn Giản - Không Cần MinIO

## ✅ Giải Pháp Đã Được Cài Đặt

Tôi đã cấu hình ứng dụng để **tự động sử dụng file system storage** thay vì MinIO khi MinIO không chạy.

**Bạn KHÔNG cần cài Docker hay tải MinIO!**

## 🚀 Cách Sử Dụng

### Bước 1: Restart Backend

```powershell
# Dừng backend nếu đang chạy (Ctrl+C)
# Sau đó chạy lại:
cd backend
mvn spring-boot:run
```

### Bước 2: Kiểm Tra Cấu Hình

File `backend/src/main/resources/application.yml` đã được cấu hình:
```yaml
storage:
  type: filesystem  # Tự động dùng file system
  filesystem:
    path: ./storage/media  # Thư mục lưu file
```

### Bước 3: Test Upload

1. Mở frontend: http://localhost:3000
2. Đăng nhập
3. Tạo Need Request và upload ảnh
4. File sẽ được lưu vào thư mục `backend/storage/media/`

## 📁 Vị Trí File

Files được lưu tại:
```
backend/
  └── storage/
      └── media/
          └── {userId}/
              └── {filename}
```

## 🔄 Chuyển Sang MinIO (Tùy Chọn)

Nếu sau này bạn muốn dùng MinIO:

1. Khởi động MinIO server
2. Sửa `application.yml`:
   ```yaml
   storage:
     type: minio  # Đổi từ filesystem sang minio
   ```
3. Restart backend

## ✨ Lợi Ích

- ✅ Không cần cài Docker
- ✅ Không cần tải MinIO
- ✅ Hoạt động ngay lập tức
- ✅ Files lưu trực tiếp trên máy
- ✅ Dễ dàng chuyển sang MinIO sau

## 🐛 Nếu Vẫn Có Lỗi

1. **Kiểm tra backend đã restart chưa**
2. **Kiểm tra thư mục `backend/storage/media` có được tạo tự động không**
3. **Xem log backend để biết lỗi cụ thể**

---

**Bây giờ bạn có thể test upload file mà không cần MinIO!** 🎉


