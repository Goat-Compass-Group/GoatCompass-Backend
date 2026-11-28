package com.goatcompass.identity.infrastructure.notification;

import com.goatcompass.identity.application.port.EmailNotificationPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {
    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;
    @Value("${app.verify-code-validity-in-second}")
    private long verificationCodeValidityInSecond;

    @Override
    public void sendVerificationEmail(String email, String fullName, String verificationCode) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("validityMinutes", Math.max(verificationCodeValidityInSecond / 60, 1));
        send(email, "Mã xác thực GoatCompass: " + verificationCode, templateEngine.process("otp", context));
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailPreparationException("Không tạo được email gửi tới " + to, e);
        }
    }
}
