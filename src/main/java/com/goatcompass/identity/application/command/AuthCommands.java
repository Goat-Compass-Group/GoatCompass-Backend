package com.goatcompass.identity.application.command;

public final class AuthCommands {
    private AuthCommands() {}

    public record RegisterCommand(
            String username, String fullName, String email, String password, String confirmPassword
    ) {}

    public record LoginCommand(String identifier, String password) {
    }

    public record VerifyCommand(String email, String verificationCode) {
    }

    public record ForgotPasswordCommand(String email, String password, String confirmPassword) {
    }

    public record ChangePasswordCommand(String currentPassword, String newPassword, String confirmPassword) {
    }
}
