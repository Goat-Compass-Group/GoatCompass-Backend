/**
 * Port (outbound) cho hạ tầng kỹ thuật mà use case cần: token JWT, giải mã RSA, băm mật khẩu, Redis, email.
 * Hiện thực ở {@code infrastructure} (JwtAdapter, RsaAdapter, BCryptPasswordHasher, RedisAdapter,
 * EmailNotificationAdapter).
 */
package com.goatcompass.identity.application.port;
