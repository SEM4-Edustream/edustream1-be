package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.payment.response.PaymentLinkResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.enums.BookingStatus;
import sem4.edustreambe.enums.NotificationType;
import sem4.edustreambe.enums.TransactionStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.BookingRepository;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.PaymentTransactionRepository;
import sem4.edustreambe.repository.UserRepository;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PayOS payOS;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    PaymentTransactionRepository transactionRepository;
    EnrollmentRepository enrollmentRepository;
    EnrollmentService enrollmentService;
    EmailService emailService;
    NotificationService notificationService;


    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:3000}")
    @lombok.experimental.NonFinal
    String frontendUrl;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public PaymentLinkResponse createPaymentLink(String bookingId) {
        User student = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(student.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            return PaymentLinkResponse.builder()
                    .bookingId(bookingId)
                    .isPaid(true)
                    .build();
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        // 1. Cancel ALL previous pending transactions for this booking to avoid conflicts
        List<PaymentTransaction> pendingTxs = transactionRepository.findAllByBookingIdAndStatus(bookingId, TransactionStatus.PENDING);
        for (PaymentTransaction pTx : pendingTxs) {
            pTx.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(pTx);
        }

        // 2. Generate unique Order Code (9 digits fits in Long and int, PayOS SDK v2.0.1 compatible)
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 12));
        Long amount = booking.getAmount().longValue();

        // 3. Set Expiration Time (e.g., 30 minutes from now)
        // PayOS expects Unix Timestamp in seconds
        long expiredAt = (System.currentTimeMillis() / 1000) + (30 * 60); 

        // Build line items
        List<PaymentLinkItem> paymentItems = booking.getItems().stream()
                .map(item -> PaymentLinkItem.builder()
                        .name(item.getCourse().getTitle().length() > 50
                                ? item.getCourse().getTitle().substring(0, 50)
                                : item.getCourse().getTitle())
                        .quantity(1)
                        .price(item.getPrice().longValue())
                        .build())
                .toList();

        String returnUrl = frontendUrl + "/payment/success?courseId=" + booking.getItems().get(0).getCourse().getId() + "&bookingId=" + booking.getId();
        String cancelUrl = frontendUrl + "/payment/cancel?courseId=" + booking.getItems().get(0).getCourse().getId() + "&bookingId=" + booking.getId();

        CreatePaymentLinkRequest.CreatePaymentLinkRequestBuilder requestBuilder = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description("EduStream #" + orderCode)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .expiredAt(expiredAt); // Thêm thời gian hết hạn ở đây

        // Add items
        for (PaymentLinkItem item : paymentItems) {
            requestBuilder.item(item);
        }

        try {
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(requestBuilder.build());

            PaymentTransaction tx = PaymentTransaction.builder()
                    .booking(booking)
                    .orderCode(orderCode)
                    .amount(booking.getAmount())
                    .status(TransactionStatus.PENDING)
                    .build();
            transactionRepository.save(tx);

            return PaymentLinkResponse.builder()
                    .checkoutUrl(data.getCheckoutUrl())
                    .qrCode(data.getQrCode())
                    .orderCode(orderCode)
                    .bookingId(bookingId)
                    .build();

        } catch (Exception e) {
            log.error("PayOS createPaymentLink error", e);
            throw new RuntimeException("Lỗi từ PayOS: " + e.getMessage());
        }
    }

    // NOT @Transactional at this level — each step manages its own transaction
    public Map<String, Object> handlePayOSWebhook(Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        String paidBookingId = null;

        try {
            log.info("Received PayOS Webhook: {}", body);

            // Verify webhook data - SDK requires Map<String,Object>
            vn.payos.model.webhooks.WebhookData data = payOS.webhooks().verify(body);
            log.info("Webhook verified. code={}, orderCode={}, desc={}",
                    data.getCode(), data.getOrderCode(), data.getDescription());

            if (!"00".equals(data.getCode())) {
                log.warn("Webhook code {} is not success. Skipping.", data.getCode());
                response.put("error", 0);
                response.put("message", "Ok");
                return response;
            }

            Long orderCode = data.getOrderCode();

            // Ignore mock/test webhooks
            if (orderCode == 123L || "test webhook".equalsIgnoreCase(data.getDescription())) {
                log.info("Received test webhook. Ignoring.");
                response.put("error", 0);
                response.put("message", "Ok");
                return response;
            }

            // Phase 1: Update DB status in its own @Transactional
            paidBookingId = markTransactionAndBookingAsPaid(orderCode, data.getReference());

            response.put("error", 0);
            response.put("message", "Ok");

        } catch (Exception e) {
            log.error("PayOS Webhook handling FAILED: {}", e.getMessage(), e);
            response.put("error", -1);
            response.put("message", "Webhook handling failed: " + e.getMessage());
        }

        // Phase 2: Enrollment in its own REQUIRES_NEW transaction
        // This runs AFTER Phase 1 transaction has committed to DB
        if (paidBookingId != null) {
            final String finalBookingId = paidBookingId;
            try {
                Booking freshBooking = bookingRepository.findById(finalBookingId)
                        .orElseThrow(() -> new RuntimeException("Booking not found after payment: " + finalBookingId));
                enrollmentService.enrollAfterPayment(freshBooking);
            } catch (Exception e) {
                log.error("enrollAfterPayment failed for booking {}. PAID status is already saved. Error: {}",
                        finalBookingId, e.getMessage(), e);
            }
        }

        return response;
    }

    /**
     * Phase 1: Atomically update Transaction + Booking status to PAID.
     * Returns bookingId if updated, null if already PAID or not found.
     */
    @Transactional
    String markTransactionAndBookingAsPaid(Long orderCode, String reference) {
        Optional<PaymentTransaction> txOpt = transactionRepository.findByOrderCode(orderCode);
        if (txOpt.isEmpty()) {
            log.error("No transaction found for orderCode: {}", orderCode);
            return null;
        }

        PaymentTransaction tx = txOpt.get();
        if (tx.getStatus() == TransactionStatus.PAID) {
            log.info("Transaction {} already PAID. Ignoring.", orderCode);
            return null;
        }

        tx.setStatus(TransactionStatus.PAID);
        tx.setPayosTransactionId(reference);
        transactionRepository.save(tx);
        log.info("Transaction {} → PAID", orderCode);

        Booking booking = tx.getBooking();
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        log.info("Booking {} → PAID", booking.getId());

        return booking.getId();
    }

    private void processPostPayment(Booking booking) {
        log.info("Processing post-payment for Booking ID: {}. Items count: {}", booking.getId(), booking.getItems() != null ? booking.getItems().size() : 0);
        // Create Enrollment for ALL courses in the booking
        for (BookingItem item : booking.getItems()) {
            Course course = item.getCourse();
            UUID userId = booking.getUser().getId();
            String courseId = course.getId();
            
            log.info("Checking enrollment for User ID: {} and Course ID: {}", userId, courseId);
            
            if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
                Enrollment enrollment = Enrollment.builder()
                        .user(booking.getUser())
                        .course(course)
                        .enrolledAt(LocalDateTime.now())
                        .progressPercentage(0)
                        .build();
                enrollmentRepository.save(enrollment);
                log.info("SUCCESS: Auto-enrolled User ID {} to Course: {}", userId, course.getTitle());

                // Welcome Notification
                try {
                    if (course.getWelcomeMessage() != null && !course.getWelcomeMessage().isBlank()) {
                        notificationService.sendNotification(
                                booking.getUser(),
                                "Welcome to " + course.getTitle(),
                                course.getWelcomeMessage(),
                                sem4.edustreambe.enums.NotificationType.COURSE_UPDATE,
                                "/course/" + course.getId() + "/learn"
                        );
                    }
                } catch (Exception e) {
                    log.error("Welcome notification failed for course {}", course.getTitle(), e);
                }
            }
        }

        // General Payment Notification
        try {
            notificationService.sendNotification(
                booking.getUser(),
                "Thanh toán thành công",
                "Bạn đã thanh toán thành công cho đơn hàng #" + booking.getId(),
                sem4.edustreambe.enums.NotificationType.PAYMENT,
                "/my-learning"
            );
        } catch (Exception e) {
            log.error("Payment notification failed for booking {}", booking.getId(), e);
        }

        // Confirmation Email
        try {
            List<String> courseTitles = booking.getItems().stream()
                    .map(item -> item.getCourse().getTitle())
                    .toList();
            emailService.sendOrderConfirmation(
                    booking.getUser().getEmail(),
                    booking.getUser().getFullName() != null ? booking.getUser().getFullName() : booking.getUser().getUsername(),
                    courseTitles,
                    booking.getAmount().doubleValue()
            );
        } catch (Exception e) {
            log.error("Email sending failed", e);
        }
    }
    
    @Transactional
    public void verifyPayment(Long orderCode) {
        log.info("Proactively verifying payment for orderCode: {}", orderCode);
        
        try {
            var data = payOS.paymentRequests().get(orderCode);
            log.info("PayOS status for order {}: {}", orderCode, data.getStatus());

            if ("PAID".equals(data.getStatus())) {
                Optional<PaymentTransaction> txOpt = transactionRepository.findByOrderCode(orderCode);
                if (txOpt.isPresent()) {
                    PaymentTransaction tx = txOpt.get();
                    if (tx.getStatus() != TransactionStatus.PAID) {
                        log.info("Proactive check: Order {} is PAID. Updating local DB...", orderCode);

                        tx.setStatus(TransactionStatus.PAID);
                        transactionRepository.save(tx);

                        Booking booking = tx.getBooking();
                        if (booking.getStatus() != BookingStatus.PAID) {
                            booking.setStatus(BookingStatus.PAID);
                            bookingRepository.save(booking);
                            log.info("Proactive check: Booking {} updated to PAID", booking.getId());

                            // Enrollment in separate transaction
                            try {
                                enrollmentService.enrollAfterPayment(booking);
                            } catch (Exception e) {
                                log.error("enrollAfterPayment failed during proactive verify: {}", e.getMessage(), e);
                            }
                        }
                    } else {
                        log.info("Order {} already PAID in DB. No update needed.", orderCode);
                    }
                } else {
                    log.error("Proactive verify: No transaction found for orderCode={}", orderCode);
                }
            } else {
                log.info("Proactive verify: PayOS status={} for order {}. Not PAID yet.", data.getStatus(), orderCode);
            }
        } catch (Exception e) {
            log.error("Error during proactive payment verification for order {}: {}", orderCode, e.getMessage(), e);
            throw new RuntimeException("Không thể xác minh thanh toán: " + e.getMessage());
        }
    }
}
