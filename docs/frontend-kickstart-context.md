# 🧠 Hạt giống Trí nhớ Backend (Backend Context Memory)

**Mục đích:** File này chứa toàn bộ Context và "ký ức" của hệ thống Spring Boot Backend. Khi bạn setup dự án Frontend mới bằng Next.js, hãy thả file này vào dự án đó để AI nắm được toàn bộ bức tranh kiến trúc ngay lập tức.

---

## 🏗️ 1. Giao trình Công nghệ Frontend (Tech Stack Rules)
Dựa theo `rules.md` và mong đợi từ ban đầu:
- **Framework:** Next.js (App Router).
- **Ngôn ngữ:** TypeScript.
- **Styling:** Tailwind CSS.
- **UI Components:** Shadcn UI.
- **Thiết kế chủ đạo:** "Geeks UI" Design System (Màu sắc hiện đại, chuyên nghiệp, đổ bóng và đường viền sắc nét, mang hơi hướm Education/LMS).
- **Quản lý Form / Fetch:** React Hook Form + Zod, Axios Interceptor hoặc SWR/React Query.

---

## 🔌 2. Quy tắc Giao tiếp API (API Contract)

### 2.1 Base URL
- Local: `http://localhost:8080/`
- Production: `https://api.edu-stream.dev/` (hoặc tuỳ chỉnh trong `NEXT_PUBLIC_API_URL`).

### 2.2 Response Wrapper Standard (Vỏ bọc JSON chung)
Mọi API thành công hay thất bại đều bị Backend nhét vào 1 cái khuôn (Wrapper) này:
```json
{
  "code": 1000, 
  "message": "Nội dung hệ thống",
  "result": { ... dữ liệu thực sự nằm ở đây ... }
}
```
*Lưu ý FrontEnd: Phải viết một Axios Interceptor để trích xuất `data.result` thay vì chỉ gọi `data` thông thường. Chặn mọi Error có `code != 1000` rồi nổ Toast.*

### 2.3 Bảo mật (Security & JWT)
- API Cấp phát Token: `POST /auth/login`. Kết quả trả ra `result.token`.
- Token này là **JWT Stateless Stateless**. Cần lưu vào `LocalStorage` hoặc `Cookies`.
- Khi gọi các API Protected (như mua khóa học, upload), bắt buộc nhét vào Request Headers:
  `Authorization: Bearer <dải_token-siêu-dài>`

---

## 🗂️ 3. Danh mục API Các Domain Chính Đã Xong

### 🧩 Auth & Profile
1. `POST /auth/login`: Lộ trình Login cơ bản.
2. `POST /auth/register`: Tạo tài khoản học sinh.

### 🎓 Tutor Control
1. Đăng ký trở thành giáo viên (Tạo Tutor Profile).
2. Tải tài liệu xác minh (Bằng cấp, Chứng chỉ).

### 📚 Course (Khóa học) & Media S3
1. **Quản lý khóa:** CRUD Khóa học, tạo Modules, tạo Lessons bên trong Module.
2. **Upload Nhanh AWS S3:** 
   - Gọi `GET /api/files/presigned-url?fileName=...&contentType=...` -> Lấy Link S3 màu xanh.
   - Gọi HTTP `PUT` ném File Video vào Link màu xanh đó.
   - Nhét cái link ngược lại vào phần Giao diện "URL Video" thay vì bắt Server Java phải upload hộ.

### 💰 Payment & Enrollment
1. Gọi `POST /api/bookings` -> Tạo booking tạm.
2. Gọi `POST /api/payments` -> Sinh ra mã link checkout **PayOS**. Mở màn hình QR Code.
3. Học sinh thanh toán xong -> Webhook PayOS bắn tự động về báo hiệu "Đã nạp tiền".
4. Khóa học được Mở khoá tự động, hệ thống sinh ra `Enrollment` cho sinh viên đó.

### 📈 Progress Tracking (Tiến độ học)
1. Trong màn hình xem Video Bài giảng, học sinh bấm nút "Hoàn Thành".
2. Gọi `POST /api/progress/lessons/{lessonId}/complete`.
3. Hệ thống sẽ thay đổi con số `progressPercentage` của Khóa học từ 0% nảy dần lên 100%. Frontend lôi con số này ra vẽ biểu đồ thanh ngang (Progress Bar).

---

## 💡 4. Nhắc nhở Dành cho Phía Frontend
1. **Xử lý Error Sonner Toast:** Hãy set up thư viện `sonner` ngay từ đầu. Tất cả lỗi Network hoặc lỗi 401 thì gọi `toast.error("Hết phiên")` và đá văng về `/login`.
2. **Swagger:** Nếu cần Type cụ thể, hãy mở `http://localhost:8080/swagger-ui/index.html` copy JSON Schema.
3. **Màu sắc chính:** Lấy màu xanh (Brand Blue) của Geeks UI làm chủ đạo.

*(Nắm được File này, thì Frontend sẽ kết nối với Backend một cách hoàn hảo zero bug!)*
