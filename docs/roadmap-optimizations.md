# Roadmap Tối Ưu, Cải Tiến & Mở Rộng Hệ Thống (Scaling & Optimizations)

Tài liệu này ghi chú lại danh sách các **Technical Debt (Nợ kỹ thuật)** cũng như các định hướng nâng cấp hệ thống EduStream Backend trong tương lai để đạt tiêu chuẩn cấp doanh nghiệp (Enterprise-level).

Mã nguồn hiện tại đã đáp ứng tốt tiêu chuẩn MVP (Minimum Viable Product). Các mục dưới đây là "Nice-to-have" và nên triển khai khi dự án có lượng lớn người dùng hoăc cần tăng cường tính ổn định, tự động hóa cao.

---

## 1. Luồng Thanh Toán & Webhook (Payment Module)

### 1.1. Xử lý bất đồng bộ (Asynchronous Processing)
- **Tình trạng hiện tại:** Quá trình Webhook hoàn tất thanh toán đang chạy đồng bộ (Synchronous). Hệ thống nhận ping từ PayOS, truy vấn Database, cập nhật trạng thái Transaction, cập nhật Booking, và tạo tài khoản Course Enrollment trên một thread duy nhất.
- **Vấn đề:** Nếu sau này tích hợp thêm luồng phụ trợ như Gửi Email Hóa Đơn, Bắn Noti Realtime qua WebSocket, API có thể phản hồi chậm hoặc Timout. PayOS yêu cầu trả về HTTP `200 OK` nhanh nhất có thể.
- **Hướng tối ưu:**
  - Ứng dụng Annotation `@Async` trong Spring Boot hoặc tích hợp Message Broker (Kafka / RabbitMQ).
  - Tách sự kiện "Tạo Enrollment & Gửi email" vào một background job chạy đằng sau.

### 1.2. Race Condition & Data Consistency (Bảo toàn dữ liệu)
- **Tình trạng hiện tại:** Đã có check trạng thái `if (tx.getStatus() == TransactionStatus.PAID)` để tránh xử lý đúp 1 Transaction.
- **Tối ưu tương lai:**
  - Để cam kết 100% khi PayOS bắn cùng lúc 2 requests cho cùng ID vào chung 1 mili-giây, cần tích hợp Pessimistic Lock ở Database (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) trên Query `findByOrderCode`.

### 1.3. Booking Cleanup Job (Dọn rác giao dịch Pending)
- Kích hoạt Spring Batch hoặc cronjob `@Scheduled` (`BookingCleanupJob.java`) để định kỳ hàng đêm quét và chuyển trạng thái `PENDING` -> `CANCELLED` đối với các đơn hàng bị treo (người mua tắt trình duyệt không thanh toán) sau 24h.

---

## 2. Hệ thống Xác Thực (Authentication & Security)

### 2.1. Phân luồng Invalidated Token từ DB lên Redis
- **Tình trạng hiện tại:** Cơ chế Stateless JWT rất nhanh, nhưng quá trình kiểm tra "Token đã Logout chưa" đang phải truy vấn bảng `InvalidatedToken` trong PostgreSQL.
- **Hướng tối ưu:** Chuyển Blacklist hoặc cơ chế kiểm tra chặn JWT sang **Redis**. Với In-memory Caching, tốc độ verify token cải thiện gấp hàng chục lần mà không làm tăng tải cho Database chính yếu.

### 2.2. Dọn rác Session
- Tạo Scheduler Job tự động xóa bỏ các Token đã hết Date Expiry khỏi bảng lưu trữ để giải phóng bộ nhớ.

---

## 3. Kiến Trúc & Vận Hành (DevOps / Infrastructure)

### 3.1. Tự động hóa quá trình Release (CI/CD Pipeline)
- **Tình trạng hiện tại:** Deploy đang thực hiện bằng tay qua luồng SSH kết hợp mã lệnh Docker Compose thủ công (`git pull` -> `build`).
- **Hướng tối ưu:** Triển khai **GitHub Actions** hoặc **GitLab CI/CD**.
  - Pipeline tự động chạy Unit Tests khi lập trình viên Push code (Continuous Integration).
  - Tự động Build ảnh vào Docker Registry, login vào trỏ tín hiệu cho VPS tự Pull và Reload Nginx không ngắt quãng Zero-downtime Deployment (Continuous Deployment).

### 3.2. Quản lý Logs (Centralized Logging) & Monitoring
- **Tình trạng hiện tại:** Trích xuất log qua lệnh `docker logs ...`. Rất khó khăn để phân tích khi logs nhiều ngày dồn lại.
- **Hướng tối ưu:** Tích hợp bộ Prometheus + Grafana để có Dashboard theo dõi tải CPU/RAM và TPS (Transactions Per Second). Dùng ELK Stack (Logstash, ElasticSearch, Kibana) hoặc Loki để quản lý tập trung hóa lịch sử lỗi, có thông báo Alert qua Telegram chặn sự cố (Incident Response).

### 3.3. Lưu trữ Video dung lượng lớn (Video Core Streaming)
- Đối với module Video VOD tương lai, CẤM upload vật lý file .mp4 thẳng vào ổ cứng VPS vì chi phí dung lượng tĩnh đắt và băng thông không tải được. 
- **Hướng tối ưu:** Tích hợp AWS S3 buckets + AWS CloudFront (CDN) / HLS Streaming cho hệ thống Course Resources để đảm bảo Load balancer mượt trên nhiều vũng địa lý và cản được Tools Download lậu.
