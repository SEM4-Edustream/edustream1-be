# Phân tích cấu trúc dự án và luồng nghiệp vụ (BA)

## 1. Mục tiêu tài liệu
Tài liệu này mô tả:
- Cấu trúc kiến trúc backend của EduConnect.
- Các domain nghiệp vụ chính và luồng end-to-end.
- Thành phần tích hợp ngoài hệ thống.
- Các rủi ro và điểm cần làm rõ từ góc nhìn Business Analyst.

Phạm vi phân tích dựa trên source code backend Spring Boot và tài liệu trong thư mục `docs`.

## 2. Snapshot kiến trúc nhanh
- Nền tảng: Spring Boot 3.4.9, Java 21, Maven.
- Kiểu kiến trúc: Layered Architecture (Controller -> Service -> Repository -> Entity).
- Quy mô hiện tại:
  - Controllers: 31
  - Services: 45
  - Entities: 41
  - Repositories: 41
- Cơ sở dữ liệu: MySQL + JPA/Hibernate (`ddl-auto: update`).
- Tìm kiếm: OpenSearch.
- Bảo mật: JWT/OAuth2 Resource Server.
- Tích hợp ngoài: AWS S3, CloudFront, AWS Lambda, PayOS, Mailgun, Zoom, OpenAI.

## 3. Cấu trúc mã nguồn theo package
## 3.1 Java source chính
`src/main/java/com/sep/educonnect/`

- `configuration/`: cấu hình hệ thống (Security, CORS, WebSocket, S3, OpenSearch, PayOS...).
- `controller/`: API endpoints REST, gồm cả `controller/admin/`.
- `service/`: xử lý nghiệp vụ cốt lõi.
- `repository/`: truy cập dữ liệu qua Spring Data JPA.
- `entity/`: mô hình domain và ánh xạ DB.
- `dto/`: request/response DTO và wrapper trả về.
- `mapper/`: MapStruct mapping giữa entity và DTO.
- `exception/`: `AppException`, `ErrorCode`, xử lý lỗi tập trung.
- `enums/`: trạng thái nghiệp vụ (BookingStatus, CourseStatus, PaymentStatus...).
- `validator/`, `utils/`, `helper/`, `wrapper/`: tiện ích và hỗ trợ chung.

## 3.2 Tài nguyên và tài liệu
- `src/main/resources/application.yaml`: cấu hình runtime (DB, JWT, rate limit, S3, PayOS...).
- `src/main/resources/messages.properties`, `messages_en.properties`: i18n (VI/EN).
- `src/main/resources/mail/*.html`: email template nghiệp vụ.
- `docs/`: hướng dẫn nghiệp vụ và tích hợp theo từng module (VOD, progress, notification, exam...).
- `lambda/video-transcoder.py`: xử lý transcoding video cho VOD.

## 4. Bản đồ domain nghiệp vụ
Các nhóm nghiệp vụ lớn theo controller/service:

1. Authentication & Account
- Đăng nhập JWT, refresh token, logout, introspect.
- Quên mật khẩu, reset mật khẩu, xác thực email.

2. Tutor Profile & Verification
- Tạo/cập nhật hồ sơ tutor.
- Quản lý tài liệu xác minh.
- Quy trình duyệt hồ sơ (admin/staff).

3. Course/Syllabus/Module/Lesson
- Quản lý cấu trúc học liệu.
- Quản lý lesson/video lesson.

4. Booking & Class Enrollment
- Đặt lớp học theo khóa học.
- Join class/trial booking.
- Duyệt hoặc từ chối booking.

5. Payment
- Tạo payment link qua PayOS.
- Trả về/callback trạng thái thanh toán.
- Đồng bộ booking status và enrollment khi thanh toán thành công.

6. Progress Tracking
- Theo dõi course progress, lesson progress.
- Cập nhật tiến độ khi học video/làm bài.

7. Exam
- Quản lý đề thi, bài làm học viên.
- Kết quả và thống kê cho tutor.

8. Search & Discovery
- Tìm kiếm tutor theo OpenSearch + filter.

9. Notification
- Thông báo realtime qua WebSocket.
- Action link cho nghiệp vụ booking/invite.

10. File/Media
- Upload/download qua S3 presigned URL.
- Streaming video qua CloudFront signed URL.

11. Quản trị
- Dashboard và các luồng duyệt/điều phối ở `controller/admin`.

## 5. Luồng nghiệp vụ chính (End-to-End)
## 5.1 Luồng đăng nhập và xác thực
1. Client gọi `POST /api/auth/token` với username/password.
2. `AuthenticationService.authenticate` kiểm tra user và hash password.
3. Hệ thống tạo JWT chứa scope + userId + trạng thái verified.
4. Client dùng bearer token cho API protected.
5. Refresh token qua `POST /api/auth/refresh`; token cũ bị invalidated.
6. Logout qua `POST /api/auth/logout`; JTI được lưu vào bảng invalidated token.

Giá trị BA:
- Flow có đủ login-refresh-logout-introspect.
- Có kiểm soát token revoke.

## 5.2 Luồng tutor tạo profile và duyệt hồ sơ
1. Tutor tạo/cập nhật profile và tài liệu.
2. Hồ sơ vào trạng thái chờ xử lý theo quy trình verification.
3. Admin/staff duyệt hoặc từ chối, có comment review.
4. Tutor nhận trạng thái và thông báo.

Giá trị BA:
- Có workflow kiểm duyệt rõ ràng.
- Phù hợp nghiệp vụ marketplace giáo dục có kiểm soát chất lượng tutor.

## 5.3 Luồng tạo khóa học và nội dung bài học
1. Tutor tạo course (gắn syllabus/subject và metadata).
2. Tạo module -> lesson theo cấu trúc chương trình.
3. Nếu lesson có video, hệ thống tạo video lesson metadata.
4. Client lấy presigned upload URL để đẩy file trực tiếp lên S3.

Giá trị BA:
- Tối ưu tải lên lớn bằng direct S3 upload.
- Tách metadata lesson/video rõ ràng, hỗ trợ mở rộng.

## 5.4 Luồng booking lớp học
1. Student gửi yêu cầu booking khóa học (`POST /api/booking`).
2. Hệ thống kiểm tra điều kiện: course tồn tại, trạng thái course, trùng booking, xung đột lịch.
3. Booking tạo ở trạng thái `PENDING` (hoặc `APPROVED` cho self-paced tùy rule).
4. Thành viên nhóm nhận booking invite notification.
5. Admin/staff duyệt hoặc từ chối booking.

Giá trị BA:
- Hỗ trợ cả cá nhân/nhóm và trial.
- Có lớp kiểm tra xung đột lịch trước khi chốt.

## 5.5 Luồng thanh toán PayOS và ghi nhận học
1. Sau khi booking `APPROVED`, student tạo payment link (`POST /api/payments`).
2. Hệ thống tạo transaction `PENDING` và trả checkout URL.
3. Người dùng thanh toán trên cổng PayOS.
4. Hệ thống xử lý return/callback, xác minh trạng thái từ PayOS.
5. Nếu `PAID`:
   - Transaction -> `PAID`.
   - Booking -> `PAID`.
   - Với self-paced: tạo class enrollment, tạo course progress ban đầu.
   - Gửi notification xác nhận thanh toán.

Giá trị BA:
- Flow payment liên kết trực tiếp đến quyền học.
- Có cơ chế hủy transaction pending cũ trước khi tạo link mới.

## 5.6 Luồng progress tracking
1. Student truy cập tiến độ khóa học theo enrollment.
2. Khi học lesson/video hoặc nộp bài, API cập nhật lesson progress.
3. Hệ thống tính lại phần trăm hoàn thành course.
4. Tutor/student xem trạng thái hoàn thành theo thời gian.

Giá trị BA:
- Dữ liệu tiến độ có thể dùng cho dashboard học tập và nhắc học tự động.

## 5.7 Luồng video on demand (VOD)
1. Client upload video gốc lên S3 qua presigned URL.
2. Sự kiện S3 kích hoạt Lambda transcoder (FFmpeg) -> HLS.
3. Backend cập nhật trạng thái video sau xử lý.
4. Client lấy signed stream URL qua CloudFront để phát.

Giá trị BA:
- Tối ưu performance, bảo mật nội dung, và khả năng mở rộng.

## 5.8 Luồng thông báo realtime
1. Sự kiện nghiệp vụ xảy ra (booking invite, payment success, approval...).
2. `NotificationService` lưu notification + push qua WebSocket.
3. Frontend nhận message theo user session và hiển thị action link.

Giá trị BA:
- Tăng tỉ lệ phản hồi user theo thời gian thực.

## 6. Thành phần hạ tầng kỹ thuật quan trọng
1. Security
- `SecurityConfig` khai báo public endpoints, OAuth2 resource server, CORS.
- JWT được xác minh qua decoder + authority converter.

2. Internationalization
- Message file song ngữ VI/EN.
- Error và success message hỗ trợ nội địa hóa.

3. Rate limiting
- Có cấu hình rate limit trong `application.yaml`, dùng cho endpoint nhạy cảm.

4. Logging & Observability
- Logback cấu hình logging theo mức.
- Có actuator + prometheus endpoint.

5. Payment
- Cấu hình PayOS (`client-id`, `api-key`, `check-sum`, return/cancel URL).

6. Storage & Media
- S3 cho file/video.
- CloudFront signed URL cho stream.
- Lambda tách riêng pipeline transcode.

## 7. Mô hình dữ liệu nghiệp vụ cốt lõi
Các entity trung tâm:
- Người dùng: `User`, `Role`.
- Tutor domain: `TutorProfile`, `TutorDocument`, `VerificationProcess`.
- Học liệu: `Course`, `Syllabus`, `Module`, `Lesson`, `VideoLesson`.
- Lớp học: `TutorClass`, `ClassSession`, `ClassEnrollment`.
- Đặt chỗ/thanh toán: `Booking`, `BookingMember`, `Transaction`.
- Theo dõi học tập: `CourseProgress`, `LessonProgress`.
- Đánh giá/thảo luận: `TutorRating`, `CourseReview`, `Discussion`.
- Thông báo: `Notification`.

Ý nghĩa BA:
- Domain model phủ khá đầy đủ vòng đời học viên từ khám phá -> mua -> học -> đánh giá.

## 8. Điểm mạnh kiến trúc
- Tách layer rõ ràng, dễ mở rộng team theo module.
- Nghiệp vụ lớn đều có service chuyên trách.
- Tích hợp ngoài theo hướng component hóa.
- Có i18n, logging, monitoring và rate-limit cho vận hành production.
- Có tài liệu kỹ thuật bổ trợ trong `docs/`.

## 9. Rủi ro và điểm cần làm rõ (BA focus)
1. Rule nghiệp vụ cho trial/self-paced
- Cần chuẩn hóa rõ điều kiện tạo/duyệt/trả phí giữa trial và regular.
- Cần tài liệu hóa logic auto-approval/auto-enrollment cho self-paced.

2. Tính nhất quán dữ liệu đa bước
- Luồng booking -> payment -> enrollment -> progress là chuỗi nhiều bước.
- Nên xác nhận chiến lược bù lỗi khi một bước giữa chừng thất bại.

3. Quản lý callback bất đồng bộ
- Video/Lambda và payment callback phụ thuộc hệ thống ngoài.
- Cần quy định timeout/retry/đồng bộ trạng thái cho frontend.

4. Ràng buộc đầu vào
- Một số nghiệp vụ cần làm rõ thêm về hạn mức (size upload, page size, max members).

5. Truy vấn và hiệu năng
- Các màn list/search nên thống nhất giới hạn phân trang và chiến lược cache.
- Các flow có vòng lặp mapping dữ liệu cần được theo dõi N+1 query.

## 10. Khuyến nghị BA cho giai đoạn tiếp theo
1. Chuẩn hóa bộ Business Rules
- Booking state machine.
- Payment state machine.
- Enrollment/progress state machine.

2. Viết BPMN hoặc sequence diagram cho 5 luồng trọng tâm
- Auth, Booking, Payment, VOD, Progress.

3. Xây ma trận quyền (RBAC)
- Student/Tutor/Admin/Staff theo từng API chính.

4. Chuẩn hóa API contract
- Danh mục mã lỗi theo domain.
- Mẫu response thống nhất cho lỗi callback bất đồng bộ.

5. Bổ sung NFR checklist
- SLA callback, retry policy, idempotency key, audit log chuẩn cho thanh toán.

## 11. Phụ lục tham chiếu nhanh (file tiêu biểu)
- Security: `src/main/java/com/sep/educonnect/configuration/SecurityConfig.java`
- Auth: `src/main/java/com/sep/educonnect/controller/AuthenticationController.java`, `src/main/java/com/sep/educonnect/service/AuthenticationService.java`
- Booking: `src/main/java/com/sep/educonnect/controller/BookingController.java`, `src/main/java/com/sep/educonnect/service/BookingService.java`
- Payment: `src/main/java/com/sep/educonnect/controller/PaymentController.java`, `src/main/java/com/sep/educonnect/service/PaymentService.java`
- VOD: `src/main/java/com/sep/educonnect/controller/VideoLessonController.java`, `src/main/java/com/sep/educonnect/service/VideoService.java`, `lambda/video-transcoder.py`
- Progress: `src/main/java/com/sep/educonnect/controller/ProgressController.java`, `src/main/java/com/sep/educonnect/service/ProgressService.java`
- Cấu hình hệ thống: `src/main/resources/application.yaml`
- Tài liệu chức năng: `docs/vod-complete-guide.md`, `docs/progress-tracking.md`, `docs/notification-backend-integration.md`, `docs/tutor-exam-apis.md`
