package sem4.edustreambe.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {

    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("EduStream <no-reply@edustream.dev>");

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void sendWelcomeEmail(String to, String name) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "loginUrl", "https://edu-stream.dev/login"
        );
        sendEmail(to, "Welcome to EduStream!", "welcome-email", variables);
    }

    public void sendOrderConfirmation(String to, String name, List<String> courseTitles, double totalAmount) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "courses", courseTitles,
                "totalAmount", totalAmount
        );
        sendEmail(to, "Purchase Confirmation - EduStream", "order-confirmation", variables);
    }

    public void sendTutorApprovalEmail(String to, String name) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "dashboardUrl", "https://edu-stream.dev/tutor/dashboard"
        );
        sendEmail(to, "Congratulations! Your Tutor Application is Approved", "tutor-approval", variables);
    }
}
