package com.goatcompass.identity.application.port;

public interface EmailNotificationPort {
    void sendVerificationEmail(String email, String fullName, String verificationCode);
}
