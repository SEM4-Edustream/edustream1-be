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
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {

    JavaMailSender mailSender;
    TemplateEngine templateEngine;
    MessageSource messageSource;

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
        Locale locale = LocaleContextHolder.getLocale();
        String subject = messageSource.getMessage("email.welcome.subject", null, locale);
        Map<String, Object> variables = Map.of(
                "name", name,
                "loginUrl", "https://edu-stream.dev/login"
        );
        sendEmail(to, subject, "welcome-email", variables);
    }

    public void sendOrderConfirmation(String to, String name, List<String> courseTitles, double totalAmount) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = messageSource.getMessage("email.order.subject", null, locale);
        Map<String, Object> variables = Map.of(
                "name", name,
                "courses", courseTitles,
                "totalAmount", totalAmount
        );
        sendEmail(to, subject, "order-confirmation", variables);
    }

    public void sendTutorApprovalEmail(String to, String name) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = messageSource.getMessage("email.tutor_approval.subject", null, locale);
        Map<String, Object> variables = Map.of(
                "name", name,
                "dashboardUrl", "https://edu-stream.dev/tutor/dashboard"
        );
        sendEmail(to, subject, "tutor-approval", variables);
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = messageSource.getMessage("email.password_reset.subject", new Object[]{}, "Password Reset Request", locale);
        Map<String, Object> variables = Map.of(
                "name", name,
                "resetLink", resetLink
        );
        sendEmail(to, subject, "password-reset", variables);
    }
}
