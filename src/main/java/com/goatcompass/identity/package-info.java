/**
 * Module Identity: tài khoản người dùng và xác thực (đăng ký, OTP qua email, đăng nhập, refresh token, đăng xuất,
 * quên/đổi mật khẩu). Cách làm giống auth-service của GoatSports nhưng chỉ có đăng nhập bằng mật khẩu
 * (không Google, không Keycloak).
 * <p>
 * Module khác không gọi thẳng vào đây: người dùng hiện tại đọc qua {@code SecurityContextHelper} (module shared),
 * sự kiện tài khoản nghe qua {@code domain.event}.
 * Cấu trúc: application / domain / infrastructure (Hexagonal + DDD). Xem docs/architecture.md.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Identity")
package com.goatcompass.identity;
