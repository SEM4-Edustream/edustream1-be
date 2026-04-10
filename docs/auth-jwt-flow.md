# Sơ đồ Luồng Vòng Đời JWT (JWT Lifecycle Flow)

Tài liệu này mô tả kiến trúc xác thực và vòng đời của JWT Token trong hệ thống EduStream, bao gồm cơ chế cấp phát, kiểm duyệt và thu hồi Token.

## 1. Flow Xác Thực và Phân Quyền (Login & API Call)

Luồng này giải thích cách user đăng nhập và cách hệ thống bảo vệ các API dùng `@PreAuthorize`.

```mermaid
sequenceDiagram
    actor User as Frontend / Client
    participant Auth as AuthController
    participant Serv as AuthService
    participant Dec as CustomJwtDecoder
    participant DB as InvalidatedToken DB
    participant API as Protected API (Controller)

    User->>Auth: POST /auth/login (Username, Password)
    Auth->>Serv: verify user info
    Serv-->>Auth: JWT (valid: 1h, jti: UUID_1)
    Auth-->>User: Token (UUID_1)

    User->>API: Gọi API yêu cầu ủy quyền + Bearer UUID_1
    API->>Dec: Spring Security chặn & Giải mã Token
    Dec->>Dec: Verify Signature & Expiry Time
    Dec->>DB: existsById(UUID_1)
    
    alt is Blacklisted (Token nằm trong danh sách đen)
        DB-->>Dec: True
        Dec-->>User: Lỗi 401 Unauthenticated
    else is Clean (Token hợp lệ)
        DB-->>Dec: False
        Dec-->>API: Pass Security Context
        API-->>User: Trả dữ liệu thành công
    end
```

## 2. Flow Xử Lý Thu Hồi (Logout / Refresh)

Để chống chiếm quyền và duy trì phiên đăng nhập lâu dài cho người dùng một cách bảo mật, hệ thống áp dụng cơ chế Refresh Token và Logout với bảng danh sách đen `invalidated_token`.

```mermaid
sequenceDiagram
    actor User as Frontend / Client
    participant Auth as AuthController
    participant Serv as AuthService
    participant DB as InvalidatedToken DB

    %% Logout Flow
    note over User, DB: Khi gọi LOGOUT (Chủ động hủy token)
    User->>Auth: POST /auth/logout (Gửi Token cũ)
    Auth->>Serv: verify & extract JTI
    Serv->>DB: INSERT (UUID_1, ExpiryTime)
    DB-->>User: 200 OK (Token đã bị vứt vào vùng rác)

    %% Refresh Flow
    note over User, DB: Khi gọi REFRESH (Lấy token mới)
    User->>Auth: POST /auth/refresh (Gửi Token UUID_1)
    Auth->>Serv: Verify expiration trong ngưỡng refreshable-duration (10h)
    Serv->>DB: existsById(UUID_1)
    
    alt Token đã bị Logout hoặc đã dùng để lấy Refresh trước đó
        DB-->>Serv: True (Phát hiện gian lận/Hết phiên)
        Serv-->>User: 401 UNAUTHENTICATED (Yêu cầu đăng nhập lại)
    else Token hợp lệ
        DB-->>Serv: False
        Serv->>DB: INSERT (UUID_1) - Thu hồi token cũ để tránh reuse
        Serv->>Serv: Generate JWT mới (jti: UUID_2)
        Serv-->>User: Trả Token mới (UUID_2)
    end
```

## 3. Lý do áp dụng kiến trúc
- **Bảo mật tuyệt đối (Revocation):** JWT theo bản chất không thể thu hồi. Khi sử dụng bảng `InvalidatedToken` kết hợp `CustomJwtDecoder`, Server chặn ngay lập tức bất kỳ token nào lọt vào tay hacker nếu người dùng chủ động bấm gửi `/auth/logout`.
- **Phát hiện đánh cắp (Token Reuse Detection):** Khi user bấm `/auth/refresh`, JTI cũ sẽ tự vào Blacklist. Nếu hacker dùng token cũ để refresh lần 2, hệ thống sẽ chối từ.
- **Tối ưu Server:** Không cần lưu danh sách mọi token đang "sống", Server chỉ lưu token rác. Mức độ query vào DB bằng `existsById` tốn nguồn lực cực thấp. Dữ liệu rác này có thể lập lịch định kỳ xóa dọn dựa trên `ExpiryTime`.
