package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.service.PaymentService;

@RestController
@RequestMapping("/api/webhooks/payos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebhookController {

    PaymentService paymentService;

    @PostMapping
    public String handlePayOSWebhook(@RequestBody Object body) {
        return paymentService.handlePayOSWebhook(body);
    }
}
