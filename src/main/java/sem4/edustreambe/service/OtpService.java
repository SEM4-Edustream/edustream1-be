package sem4.edustreambe.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OtpService {

    EmailService emailService;

    static final long OTP_TTL_MINUTES = 5; // 5 phút — BR-002

    // Khởi tạo Caffeine Cache thay cho Redis
    // Key: email, Value: otp string
    Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_TTL_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    /**
     * UC-AUTH-OTP-01: Sinh OTP, lưu Caffeine, gửi email bất đồng bộ.
     */
    public void generateAndSendOtp(String email) {
        String otp = generateOtp();

        try {
            otpCache.put(email, otp);
            log.info("[OTP] Generated and stored in Caffeine for email: {}", email);
        } catch (Exception e) {
            log.error("[OTP] Failed to store OTP in Caffeine for email: {}", email, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Gửi email bất đồng bộ (@Async trong EmailService)
        emailService.sendEmail(
                email,
                "[EduStream] Mã xác thực OTP đăng nhập",
                "otp-email",
                Map.of(
                        "otp", otp,
                        "expiryMinutes", OTP_TTL_MINUTES
                )
        );
    }

    /**
     * UC-AUTH-OTP-02: Xác thực mã OTP.
     * - OTP không tồn tại trong Cache → OTP_EXPIRED (đã hết TTL)
     * - OTP không khớp → OTP_INVALID
     * - OTP đúng → xóa khỏi Cache (single-use — BR-003)
     */
    public void verifyOtp(String email, String otp) {
        String savedOtp = otpCache.getIfPresent(email);

        if (savedOtp == null) {
            log.warn("[OTP] OTP expired or not found for email: {}", email);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!savedOtp.equals(otp)) {
            log.warn("[OTP] Invalid OTP attempt for email: {}", email);
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        // Single-use: xóa ngay sau khi xác thực thành công — BR-003
        otpCache.invalidate(email);
        log.info("[OTP] OTP verified and invalidated for email: {}", email);
    }

    /**
     * Sinh mã OTP 6 số ngẫu nhiên dùng SecureRandom — BR-001.
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
