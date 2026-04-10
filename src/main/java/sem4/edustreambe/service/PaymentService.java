package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.payment.response.PaymentLinkResponse;
import sem4.edustreambe.entity.Booking;
import sem4.edustreambe.entity.Enrollment;
import sem4.edustreambe.entity.PaymentTransaction;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.BookingStatus;
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
import vn.payos.service.blocking.webhooks.WebhooksService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        // Cancel previous pending transactions for this booking to avoid spam
        Optional<PaymentTransaction> existingTx = transactionRepository.findByBookingId(bookingId);
        if (existingTx.isPresent() && existingTx.get().getStatus() == TransactionStatus.PENDING) {
            existingTx.get().setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(existingTx.get());
        }

        // Generate unique Order Code from current timestamp (safe range for PayOS)
        Long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 12));

        Long amount = booking.getAmount().longValue();

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(booking.getCourse().getTitle())
                .quantity(1)
                .price(amount)
                .build();

        String returnUrl = "http://localhost:3000/payment/success";
        String cancelUrl = "http://localhost:3000/payment/cancel";

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description("EduStream Course")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        try {
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

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

    @Transactional
    @SuppressWarnings("unchecked")
    public String handlePayOSWebhook(Object body) {
        try {
            // Verify webhook data and extract payment info using SDK v2
            vn.payos.model.webhooks.WebhookData data = payOS.webhooks().verify(body);

            if (!"00".equals(data.getCode())) {
                log.warn("Webhook received code {}, ignoring.", data.getCode());
                return "OK";
            }

            Long orderCode = data.getOrderCode();

            PaymentTransaction tx = transactionRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

            if (tx.getStatus() == TransactionStatus.PAID) {
                return "OK";
            }

            // Update Transaction
            tx.setStatus(TransactionStatus.PAID);
            tx.setPayosTransactionId(data.getReference());
            transactionRepository.save(tx);

            // Update Booking
            Booking booking = tx.getBooking();
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            // Create Enrollment
            if (!enrollmentRepository.existsByUserIdAndCourseId(booking.getUser().getId(), booking.getCourse().getId())) {
                Enrollment enrollment = Enrollment.builder()
                        .user(booking.getUser())
                        .course(booking.getCourse())
                        .enrolledAt(LocalDateTime.now())
                        .progressPercentage(0)
                        .build();
                enrollmentRepository.save(enrollment);
                log.info("Auto-enrolled user {} to course {} upon successful PayOS payment.",
                        booking.getUser().getUsername(), booking.getCourse().getTitle());
            }

            return "OK";
        } catch (Exception e) {
            log.error("PayOS Webhook handling failed: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_WEBHOOK_DATA);
        }
    }
}
