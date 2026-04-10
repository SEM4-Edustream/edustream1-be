package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.payment.response.PaymentLinkResponse;
import sem4.edustreambe.service.PaymentService;

@RestController
@RequestMapping("/api/student/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
public class StudentPaymentController {

    PaymentService paymentService;

    @PostMapping("/create-link/{bookingId}")
    public ApiResponse<PaymentLinkResponse> createPaymentLink(@PathVariable String bookingId) {
        return ApiResponse.<PaymentLinkResponse>builder()
                .result(paymentService.createPaymentLink(bookingId))
                .build();
    }
}
