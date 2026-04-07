# RULE.MD - BA + Agent Rules Áp Dụng Cho Dự Án Tiếp Theo

Version: 1.0
Updated: 2026-04-07

## 1) Mục tiêu tài liệu
Thiết lập bộ quy tắc chuẩn cho Business Analysis nhằm:
- Giảm mơ hồ nghiệp vụ.
- Đồng bộ cách đặc tả giữa BA - Dev - QA.
- Tăng khả năng triển khai đúng ngay từ vòng đầu.
- Dễ tái sử dụng cho các dự án EdTech/SaaS có luồng thanh toán, học tập, nội dung số.

## 1.1) Cách đọc cấu trúc (quan trọng)
Tài liệu này dùng đồng thời 2 góc nhìn:
- Góc nhìn nghiệp vụ (domain/module): Auth, Booking, Payment, Progress, Tutor Profile...
- Góc nhìn source code (layer): controller -> service -> repository -> entity -> dto -> mapper.

Vì vậy từ "module" trong tài liệu không có nghĩa là thư mục/module vật lý tách riêng trong source, mà là nhóm nghiệp vụ được triển khai qua nhiều package layer.

## 1.2) Mapping theo source hiện tại
- API layer: `src/main/java/com/sep/educonnect/controller/**`
- Business layer: `src/main/java/com/sep/educonnect/service/**`
- Data access layer: `src/main/java/com/sep/educonnect/repository/**`
- Domain model: `src/main/java/com/sep/educonnect/entity/**`
- Contract model: `src/main/java/com/sep/educonnect/dto/**`
- Mapping layer: `src/main/java/com/sep/educonnect/mapper/**`
- Cross-cutting: `configuration`, `exception`, `validator`, `utils`, `helper`

## 2) Nguyên tắc cốt lõi
1. Rule-first, không feature-first.
Mỗi feature phải có business rules rõ ràng trước khi chốt API.

2. End-to-end trước, chi tiết sau.
Luôn mô tả luồng người dùng từ đầu đến cuối (happy path + exception path).

3. State machine là bắt buộc cho domain có trạng thái.
Booking, Payment, Enrollment, Progress, Verification phải có sơ đồ trạng thái.

4. Bất đồng bộ phải có chiến lược nhất quán.
Callback, event, webhook, retry, idempotency phải được mô tả ngay trong tài liệu BA.

5. Rule phải đo kiểm được.
Mỗi rule cần tiêu chí pass/fail rõ để QA viết test case.

## 3) Danh mục bắt buộc cho mỗi domain nghiệp vụ (triển khai xuyên layer)
Mỗi domain phải có đủ 10 phần sau:

1. Business Goal
- Bài toán cần giải quyết.
- KPI thành công.

2. Scope
- In scope.
- Out of scope.

3. Actors & Roles
- Vai trò tham gia.
- Quyền theo vai trò.

4. Preconditions
- Điều kiện trước khi thực hiện luồng.

5. Main Flow
- Từng bước xử lý theo trình tự.

6. Alternative/Exception Flows
- Các nhánh lỗi, từ chối, timeout, callback fail.

7. Business Rules
- Rule định lượng và định tính.
- Rule ưu tiên khi có xung đột.

8. Data Rules
- Required fields.
- Validation constraints.
- Mapping giữa trạng thái nghiệp vụ và dữ liệu.

9. API Contract Rules
- Endpoint, request, response, mã lỗi, idempotency.

10. Acceptance Criteria
- Given/When/Then hoặc checklist testable.

## 4) Quy tắc đặc tả luồng (Flow Specification)
1. Mỗi luồng phải có:
- Trigger.
- Input.
- Processing rules.
- Output.
- Side effects (notification, audit, event).

2. Mỗi bước phải trả lời được 3 câu hỏi:
- Ai thực hiện?
- Điều kiện nào cho phép thực hiện?
- Kết quả nào được ghi nhận?

3. Với mỗi luồng, BA bắt buộc viết thêm:
- 1 happy path.
- Tối thiểu 3 exception paths.
- 1 rollback hoặc compensation path nếu có giao dịch đa bước.

## 5) Quy tắc state machine
Áp dụng cho mọi domain có trạng thái.

1. Mỗi trạng thái phải khai báo:
- Ý nghĩa nghiệp vụ.
- Actor được phép chuyển trạng thái.
- Điều kiện chuyển trạng thái.
- Điều kiện chặn chuyển trạng thái.

2. Mỗi chuyển trạng thái phải có:
- Event kích hoạt.
- Validation tương ứng.
- Side effects (notification, create record, update progress).

3. Quy tắc bất biến (invariant):
- Không cho phép chuyển ngược nếu rule không định nghĩa.
- Trạng thái terminal phải được định nghĩa rõ.
- Không tồn tại trạng thái “mơ hồ” không dùng đến.

## 6) Quy tắc API và tích hợp ngoài
1. API Rules
- Không publish API nếu chưa có error catalog theo domain.
- Response schema phải ổn định giữa các endpoint cùng nhóm.
- Phân trang bắt buộc cho list API.

2. Webhook/Callback Rules
- Bắt buộc idempotency key hoặc cơ chế chống xử lý trùng.
- Có retry policy rõ ràng.
- Có cơ chế reconcile khi callback thất bại.

3. File/Media Rules
- Quy định kích thước file, loại file, thời gian hiệu lực URL ký.
- Tách rõ quyền truy cập file public và private.

4. Payment Rules
- Rule trước thanh toán, trong thanh toán, sau thanh toán.
- Mapping trạng thái cổng thanh toán sang trạng thái nội bộ.
- Chính sách hoàn tiền/hủy giao dịch phải có từ đầu.

## 7) Quy tắc bảo mật và tuân thủ
1. RBAC matrix là bắt buộc.
- Endpoint nào role nào được gọi.
- Điều kiện ownership dữ liệu.

2. Dữ liệu nhạy cảm
- Không log token, password, secret.
- Có quy tắc masking trong log và audit.

3. Input safety
- Rule validate cho mọi input từ client.
- Rule chuẩn hóa dữ liệu trước khi truy vấn/tìm kiếm.

## 8) Quy tắc phi chức năng (NFR)
Mỗi domain nghiệp vụ phải có NFR tối thiểu:
- Performance: SLA phản hồi API chính.
- Reliability: retry, timeout, circuit breaker (nếu có).
- Scalability: giới hạn phân trang, batch size, cache policy.
- Observability: audit events, metrics, alerting.
- Localization: định nghĩa message key cho lỗi và thông báo.

## 9) Quy tắc tài liệu hóa
1. Mỗi domain có 1 tài liệu chuẩn trong docs:
- module-name-guide.md.

2. Mỗi tài liệu domain phải chứa:
- Flow diagram.
- State machine.
- API summary.
- Error catalog.
- Test scenarios.

3. Bắt buộc có bảng trace theo layer của source:
- Controller nào thuộc domain này.
- Service nào xử lý nghiệp vụ chính.
- Repository/Entity nào là source of truth dữ liệu.
- DTO/Mapper nào là contract với client.

4. Khi có breaking change:
- Cập nhật version API.
- Cập nhật changelog.
- Cập nhật migration note cho QA/Frontend.

## 10) Quy tắc kiểm thử từ góc nhìn BA
1. BA phải cung cấp tối thiểu:
- Happy path test cases.
- Permission test cases.
- Validation test cases.
- Callback/retry test cases.

2. Với nghiệp vụ đa bước:
- Có test cho partial failure.
- Có test cho concurrent requests.
- Có test cho duplicate webhook/callback.

3. UAT readiness checklist bắt buộc:
- Rule đầy đủ.
- API và UI đồng nhất.
- Không còn trạng thái mơ hồ chưa định nghĩa.

## 11) Definition of Ready (DoR)
User story chỉ được dev khi đạt đủ:
- Có business goal rõ.
- Có state transitions rõ (nếu có trạng thái).
- Có API contract draft.
- Có acceptance criteria testable.
- Có dependency list và assumption list.

## 12) Definition of Done (DoD)
Feature chỉ được đóng khi đạt đủ:
- Rule đã được triển khai đúng.
- Test pass theo acceptance criteria.
- Logging/audit đủ để truy vết.
- Tài liệu module đã cập nhật.
- Không còn TODO nghiệp vụ chưa xử lý.

## 13) Checklist áp dụng nhanh cho dự án mới
Trước khi kickoff:
- Chốt domain map.
- Chốt state machine cho domain chính.
- Chốt error taxonomy theo domain.
- Chốt RBAC matrix.

Trước khi dev sprint:
- Mỗi story có flow + rules + AC.
- Mỗi API có request/response/errors rõ.

Trước khi release:
- Verify callback/retry/idempotency.
- Verify audit trail và dashboard monitoring.
- Verify tài liệu và test evidence.

## 14) Mẫu template gợi ý cho 1 module mới
Sử dụng cấu trúc sau:

1. Module Name
2. Business Goal
3. Actors
4. Main Flow
5. Exception Flows
6. State Machine
7. Business Rules
8. API Contracts
9. Data Validation Rules
10. Error Codes
11. Acceptance Criteria
12. Test Scenarios
13. Open Questions
14. Assumptions

Ghi chú bắt buộc khi dùng template:
- Thêm mục "Source Trace" ngay sau API Contracts, gồm danh sách controller/service/repository/entity/dto liên quan trong source.

## 15) Quy tắc cải tiến liên tục
1. Sau mỗi release phải có retrospective rule.
- Rule nào thiếu.
- Rule nào gây hiểu sai.
- Rule nào cần chuẩn hóa lại.

2. Cập nhật rule.md theo phiên bản.
- Gợi ý đặt version ở đầu file.
- Gợi ý ghi ngày cập nhật cuối.

3. Không thêm rule nếu không có case thực tế chứng minh.
- Ưu tiên rule ngắn, đo kiểm được, có ví dụ.

## 16) Agent Operating Rules (bắt buộc follow)
Mục tiêu: mọi agent làm việc nhất quán, có thể kiểm tra chéo, hạn chế bỏ sót yêu cầu.

1. Danh sách agent chuẩn
- BA Agent: phân tích nghiệp vụ, flow, state machine, AC.
- Solution Agent: ánh xạ rule sang thiết kế kỹ thuật và tích hợp.
- API Agent: chuẩn hóa contract API, error model, versioning.
- Data Agent: kiểm tra model dữ liệu, ràng buộc, nhất quán transaction.
- QA Agent: sinh test scenarios, test matrix, UAT criteria.
- Security Agent: rà soát quyền, input safety, dữ liệu nhạy cảm, audit.

2. Quy tắc đầu vào cho mọi agent
- Không bắt đầu nếu chưa có mục tiêu nghiệp vụ (business goal).
- Không bắt đầu nếu thiếu actor/role chính.
- Không bắt đầu nếu thiếu phạm vi (in scope/out of scope).
- Nếu thiếu dữ liệu, agent phải ghi rõ assumptions và open questions.

3. Quy tắc đầu ra chuẩn cho mọi agent
- Summary: tóm tắt tối đa 10 dòng.
- Findings: liệt kê theo mức độ CRITICAL/HIGH/MEDIUM/LOW.
- Evidence: chỉ rõ file, endpoint, rule liên quan.
- Recommendation: hành động cụ thể, đo kiểm được.
- Decision Needed: các điểm cần PO/BA chốt.

4. Quy tắc severity thống nhất
- CRITICAL: có nguy cơ sai tiền, sai quyền, mất dữ liệu, hoặc không triển khai được.
- HIGH: có khả năng gây lỗi nghiệp vụ hoặc regression lớn.
- MEDIUM: ảnh hưởng chất lượng hoặc maintainability.
- LOW: tối ưu thêm, không chặn release.

5. Quy tắc handoff giữa agents
- BA Agent -> Solution Agent: bàn giao flow + rules + state transitions.
- Solution Agent -> API Agent: bàn giao sequence xử lý + integration points.
- API Agent -> Data Agent: bàn giao payload, constraint, lifecycle dữ liệu.
- Data Agent -> QA Agent: bàn giao case biên, consistency checks.
- QA Agent -> Security Agent: bàn giao attack surface và abuse cases.
- Security Agent -> BA Agent: phản hồi gap và điều chỉnh rule nghiệp vụ.

6. Quy tắc stop-the-line
Agent phải dừng và raise blocker ngay khi gặp:
- Luồng thanh toán chưa có mapping trạng thái rõ.
- Callback/webhook chưa có idempotency hoặc retry rule.
- Flow có trạng thái nhưng không có state machine.
- Endpoint quan trọng chưa có RBAC matrix.
- Thiếu acceptance criteria testable.

7. Quy tắc cho phân tích bất đồng bộ
- Mọi event/callback phải có timeout và retry tối thiểu.
- Phải định nghĩa nguồn sự thật cuối cùng (source of truth).
- Phải có cơ chế reconcile khi lệch trạng thái giữa hệ thống trong/ngoài.

8. Quy tắc cho API contract
- API Agent bắt buộc cung cấp ví dụ request/response cho happy path và 2 error path.
- Mọi list API phải có pagination và max page size.
- Mọi mutation API phải nêu rõ tính idempotent hoặc non-idempotent.

9. Quy tắc cho kiểm thử
- QA Agent bắt buộc tạo test matrix theo: permission, validation, workflow, callback, concurrency.
- Với flow đa bước phải có test partial failure và compensation.
- UAT case phải trace được về business rule tương ứng.

10. Quy tắc bảo mật tối thiểu
- Security Agent phải kiểm tra ownership-level authorization cho mọi API đọc/ghi dữ liệu cá nhân.
- Không cho phép log secret/token/password ở mọi môi trường.
- Input từ client phải có validation + sanitize trước khi truy vấn/search.

11. Quy tắc chấp nhận đầu ra của agent
Chỉ accept khi output thỏa đủ:
- Có evidence rõ ràng.
- Có đề xuất sửa cụ thể.
- Có mức độ ưu tiên.
- Có owner đề xuất (BA/Dev/QA/PO).
- Có tiêu chí kiểm chứng sau khi sửa.

12. Mẫu prompt ngắn cho agent (copy nhanh)
"Analyze theo RULE.MD. Trả về: Summary, Findings theo severity, Evidence, Recommendation, Decision Needed. Bắt buộc kiểm tra: state machine, RBAC, idempotency, callback retry, acceptance criteria testable."

---

## Final Note
File này là baseline BA governance cho dự án kế tiếp. Có thể mở rộng theo domain đặc thù (Marketplace, LMS, Fintech) nhưng phải giữ nguyên 5 trụ cột:
- Flow rõ
- State rõ
- Rule rõ
- Contract rõ
- Test rõ
