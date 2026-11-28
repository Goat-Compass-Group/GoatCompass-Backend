package com.goatcompass.identity.infrastructure.notification;

import com.goatcompass.identity.application.port.EmailNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {
    private final ResendEmailSender emailSender;
    private final ITemplateEngine templateEngine;

    @Value("${app.verify-code-validity-in-second}")
    private long verificationCodeValidityInSecond;

    @Override
    public void sendVerificationEmail(String email, String fullName, String verificationCode) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("validityMinutes", Math.max(verificationCodeValidityInSecond / 60, 1));
        emailSender.send(email, "Mã xác thực GoatCompass: " + verificationCode, templateEngine.process("otp", context));
    }
}