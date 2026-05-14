# Hướng dẫn Deploy AWS Lambda: Video Duration Extractor

AWS Lambda này tự động lấy thời lượng video ngay khi file được đẩy lên S3 và gọi về EduStream Backend.

## 1. Chuẩn bị (Packaging)
Do Lambda sử dụng thư viện bên thứ 3 (`ffprobe-installer`, `axios`, `fluent-ffmpeg`), bạn phải đóng gói mã nguồn cùng với thư mục `node_modules` thành một file `.zip`.

1. Mở terminal và di chuyển vào thư mục code:
   ```bash
   cd e:\edustream-be\infrastructure\lambda\video-duration-extractor
   ```
2. Cài đặt các thư viện (phải cài đặt trên môi trường Linux/Mac hoặc dùng WSL để đảm bảo thư viện tương thích với môi trường AWS Linux 2):
   ```bash
   npm install
   ```
3. Nén tất cả các file lại (KHÔNG nén thư mục cha, chỉ nén ruột bên trong):
   ```bash
   zip -r function.zip index.js package.json node_modules/
   ```

## 2. Tạo AWS Lambda Function
1. Truy cập AWS Console -> **Lambda**.
2. Nhấn **Create function**.
3. Chọn **Author from scratch**.
4. Tên function: `edustream-video-duration-extractor`.
5. Runtime: **Node.js 18.x** (hoặc 20.x).
6. Architecture: **x86_64** (Quan trọng vì `@ffprobe-installer` thường hoạt động ổn định trên x86_64 của AWS).
7. Nhấn **Create function**.

## 3. Tải Code lên và Cấu hình
1. Tại tab **Code**, nhấn **Upload from** -> **.zip file** và tải file `function.zip` vừa tạo lên.
2. Sang tab **Configuration** -> **Environment variables**, thêm 2 biến:
   * `BACKEND_API_URL`: `https://api.edu-stream.dev/api/webhooks/video-duration` *(Đổi thành domain production của bạn)*
   * `WEBHOOK_SECRET`: `lamda-secret-edustream-2026`
3. Tại tab **Configuration** -> **General configuration** -> Edit:
   * Tăng **Timeout** lên ít nhất **30 giây** (mặc định 3s là không đủ để ffprobe kéo header).
   * Tăng **Memory** lên khoảng **256 MB** hoặc **512 MB**.

## 4. Cấp Quyền (IAM Permissions)
Lambda cần quyền đọc file từ S3 để tạo Presigned URL:
1. Tại tab **Configuration** -> **Permissions** -> Click vào Role Name.
2. Add permissions -> Attach policies.
3. Tìm và gắn policy `AmazonS3ReadOnlyAccess`.

## 5. Thêm S3 Trigger
Để Lambda tự chạy khi có video mới:
1. Tại giao diện Lambda (Overview), nhấn **Add trigger**.
2. Chọn nguồn là **S3**.
3. Chọn Bucket mà bạn lưu trữ Video (VD: `edustream-video-storage-xxx`).
4. Event types: `All object create events` hoặc cụ thể hơn là `PUT`.
5. Suffix (tùy chọn): `.mp4` (Khuyên dùng để tránh Lambda chạy nhầm khi upload file ảnh).
6. Tích chọn "I acknowledge..." và nhấn **Add**.

---
🎉 **Hoàn tất!** Giờ đây mọi video giảng viên upload lên có đuôi mp4 sẽ tự động được quét thời lượng và báo về Backend.
