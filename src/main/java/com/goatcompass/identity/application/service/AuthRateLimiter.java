package com.goatcompass.identity.application.service;

import com.goatcompass.identity.application.exception.RateLimitExceededException;
import com.goatcompass.identity.application.port.RedisPort;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthRateLimiter {
    private static final String KEY_PREFIX = "ratelimit:";

    private final RedisPort redisPort;

    @Value("${app.rate-limit.login.max-attempts}")
    private int loginMaxAttempts;
    @Value("${app.rate-limit.login.window-seconds}")
    private long loginWindowSeconds;

    @Value("${app.rate-limit.otp.max-attempts}")
    private int otpMaxAttempts;
    @Value("${app.rate-limit.otp.window-seconds}")
    private long otpWindowSeconds;

    @Value("${app.rate-limit.verify.max-attempts}")
    private int verifyMaxAttempts;
    @Value("${app.rate-limit.verify.window-seconds}")
    private long verifyWindowSeconds;

    @Value("${app.rate-limit.password-reset.max-attempts}")
    private int passwordResetMaxAttempts;
    @Value("${app.rate-limit.password-reset.window-seconds}")
    private long passwordResetWindowSeconds;

    public void checkLogin(String identifier) {
        check("login", identifier, loginMaxAttempts, loginWindowSeconds,
                "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau ít phút.");
    }

    public void checkOtpResend(String email) {
        check("otp", email, otpMaxAttempts, otpWindowSeconds,
                "Bạn đã yêu cầu mã xác thực quá nhiều lần. Vui lòng thử lại sau.");
    }

    public void checkVerify(String email) {
        check("verify", email, verifyMaxAttempts, verifyWindowSeconds,
                "Bạn đã nhập sai mã xác thực quá nhiều lần. Vui lòng thử lại sau.");
    }

    public void checkPasswordReset(String email) {
        check("password-reset", email, passwordResetMaxAttempts, passwordResetWindowSeconds,
                "Bạn đã yêu cầu đặt lại mật khẩu quá nhiều lần. Vui lòng thử lại sau.");
    }

    private void check(String action, String identity, int maxAttempts, long windowSeconds, String message) {
        if (!StringUtils.hasText(identity)) {
            return;
        }
        String key = KEY_PREFIX + action + ":" + identity.trim().toLowerCase(Locale.ROOT);
        if (redisPort.incrementWithTtl(key, windowSeconds, TimeUnit.SECONDS) > maxAttempts) {
            throw new RateLimitExceededException(message);
        }
    }
}
