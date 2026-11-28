package com.goatcompass.identity.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
    private AuthRequests() {
    }

    public record LoginRequest(
            @NotBlank(message = "Tên đăng nhập hoặc email không được để trống") String username,
            @NotBlank(message = "Mật khẩu không được để trống") String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "Tên đăng nhập không được để trống")
            @Pattern(regexp = "^[A-Za-z0-9._]{3,50}$",
                    message = "Tên đăng nhập gồm 3–50 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới")
            String username,
            @NotBlank(message = "Họ tên không được để trống")
            @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
            String fullName,
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            @Size(max = 255, message = "Email tối đa 255 ký tự")
            String email,
            @NotBlank(message = "Mật khẩu không được để trống") String password,
            @NotBlank(message = "Xác nhận mật khẩu không được để trống") String confirmPassword
    ) {
    }

    public record VerificationRequest(
            @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,
            @NotBlank(message = "Mã xác thực không được để trống")
            @Pattern(regexp = "^\\s*\\d{6}\\s*$", message = "Mã xác thực gồm 6 chữ số")
            String verificationCode
    ) {
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,
            @NotBlank(message = "Mật khẩu mới không được để trống") String password,
            @NotBlank(message = "Xác nhận mật khẩu mới không được để trống") String confirmPassword
    ) {
    }

    public record UpdatePasswordRequest(
            @NotBlank(message = "Mật khẩu hiện tại không được để trống") String currentPassword,
            @NotBlank(message = "Mật khẩu mới không được để trống") String newPassword,
            @NotBlank(message = "Xác nhận mật khẩu mới không được để trống") String confirmPassword
    ) {
    }

    public record PublicKeyResponse(String publicKey) {
    }
}
