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
import java.util.List;
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

        // Cancel previous pending transactions for this booking to avoid spam
        Optional<PaymentTransaction> existingTx = transactionRepository.findByBookingId(bookingId);
        if (existingTx.isPresent() && existingTx.get().getStatus() == TransactionStatus.PENDING) {
            existingTx.get().setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(existingTx.get());
        }

        // Generate unique Order Code from current timestamp (safe range for PayOS)
        Long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 12));

        Long amount = booking.getAmount().longValue();

        // Build line items from BookingItems
        List<PaymentLinkItem> paymentItems = booking.getItems().stream()
                .map(item -> PaymentLinkItem.builder()
                        .name(item.getCourse().getTitle().length() > 50
                                ? item.getCourse().getTitle().substring(0, 50)
                                : item.getCourse().getTitle())
                        .quantity(1)
                        .price(item.getPrice().longValue())
                        .build())
                .toList();

        // For multi-item bookings, use the booking ID in URLs
        String returnUrl = frontendUrl + "/payment/success?bookingId=" + booking.getId();
        String cancelUrl = frontendUrl + "/payment/cancel?bookingId=" + booking.getId();

        CreatePaymentLinkRequest.CreatePaymentLinkRequestBuilder requestBuilder = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description("EduStream Course")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl);

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

    @Transactional
    @SuppressWarnings("unchecked")
    public com.fasterxml.jackson.databind.node.ObjectNode handlePayOSWebhook(
            com.fasterxml.jackson.databind.node.ObjectNode body) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode response = mapper.createObjectNode();

        try {
            // Verify webhook data and extract payment info using SDK v2
            vn.payos.model.webhooks.WebhookData data = payOS.webhooks().verify(body);

            if (!"00".equals(data.getCode())) {
                log.warn("Webhook received code {}, ignoring.", data.getCode());
                response.put("error", 0);
                response.put("message", "Ok");
                response.set("data", null);
                return response;
            }

            Long orderCode = data.getOrderCode();

            // Check if mock order code from PayOS test webhook dashboard
            if (String.valueOf(orderCode).equals("123") || "test webhook".equalsIgnoreCase(data.getDescription())) {
                log.info("Received mock webhook from PayOS. Returning 200 OK.");
                response.put("error", 0);
                response.put("message", "Ok");
                response.set("data", null);
                return response;
            }

            Optional<PaymentTransaction> txOpt = transactionRepository.findByOrderCode(orderCode);
            if (txOpt.isEmpty()) {
                log.warn("Webhook received for unknown orderCode {}. Ignored.", orderCode);
                response.put("error", 0);
                response.put("message", "Ok");
                response.set("data", null);
                return response;
            }

            PaymentTransaction tx = txOpt.get();

            if (tx.getStatus() == TransactionStatus.PAID) {
                response.put("error", 0);
                response.put("message", "Ok");
                response.set("data", null);
                return response;
            }

            // Update Transaction
            tx.setStatus(TransactionStatus.PAID);
            tx.setPayosTransactionId(data.getReference());
            transactionRepository.save(tx);

            // Update Booking
            Booking booking = tx.getBooking();
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            // Create Enrollment for ALL courses in the booking
            for (BookingItem item : booking.getItems()) {
                Course course = item.getCourse();
                if (!enrollmentRepository.existsByUserIdAndCourseId(booking.getUser().getId(), course.getId())) {
                    Enrollment enrollment = Enrollment.builder()
                            .user(booking.getUser())
                            .course(course)
                            .enrolledAt(LocalDateTime.now())
                            .progressPercentage(0)
                            .build();
                    enrollmentRepository.save(enrollment);
                    log.info("Auto-enrolled user {} to course {} upon successful PayOS payment.",
                            booking.getUser().getUsername(), course.getTitle());

                    // Gửi Welcome Message từ Course (nếu có)
                    if (course.getWelcomeMessage() != null && !course.getWelcomeMessage().isBlank()) {
                        notificationService.sendNotification(
                                booking.getUser(),
                                "Welcome to " + course.getTitle(),
                                course.getWelcomeMessage(),
                                sem4.edustreambe.enums.NotificationType.COURSE_UPDATE,
                                "/course/" + course.getId() + "/learn"
                        );
                    }
                }
            }

            // Gửi thông báo In-app về thanh toán thành công
            notificationService.sendNotification(
                booking.getUser(),
                "Thanh toán thành công",
                "Bạn đã thanh toán thành công " + booking.getAmount() + "đ cho đơn hàng #" + booking.getId(),
                sem4.edustreambe.enums.NotificationType.PAYMENT,
                "/my-learning"
            );

            // Send order confirmation email
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
                log.error("Failed to send order confirmation email for booking {}", booking.getId(), e);
            }

            response.put("error", 0);
            response.put("message", "Ok");
            response.set("data", null);
            return response;
        } catch (Exception e) {
            log.error("PayOS Webhook handling failed: {}", e.getMessage());
            // PayOS requires 200 OK but we can return error in json wrapper
            response.put("error", -1);
            response.put("message", "Webhook verified failed or error: " + e.getMessage());
            response.set("data", null);
            return response;
        }
    }
}
