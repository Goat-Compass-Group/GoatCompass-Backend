package com.goatcompass.identity.application.service;

import static com.goatcompass.shared.application.security.AuthTokenContract.TOKEN_TYPE_ACCESS;
import static com.goatcompass.shared.application.security.AuthTokenContract.TOKEN_TYPE_REFRESH;
import static com.goatcompass.shared.application.security.AuthTokenContract.accessBlacklistKey;
import static com.goatcompass.shared.application.security.AuthTokenContract.refreshBlacklistKey;
import static com.goatcompass.shared.application.security.AuthTokenContract.refreshKey;
import static com.goatcompass.shared.application.security.AuthTokenContract.sessionKey;

import com.goatcompass.identity.application.command.AuthCommands.ForgotPasswordCommand;
import com.goatcompass.identity.application.command.AuthCommands.LoginCommand;
import com.goatcompass.identity.application.command.AuthCommands.RegisterCommand;
import com.goatcompass.identity.application.command.AuthCommands.VerifyCommand;
import com.goatcompass.identity.application.exception.AccountStatusException;
import com.goatcompass.identity.application.port.EmailNotificationPort;
import com.goatcompass.identity.application.port.PasswordHasherPort;
import com.goatcompass.identity.application.port.RedisPort;
import com.goatcompass.identity.application.port.RsaPort;
import com.goatcompass.identity.application.port.TokenPort;
import com.goatcompass.identity.application.port.TokenPort.TokenClaims;
import com.goatcompass.identity.application.query.AuthSession;
import com.goatcompass.identity.application.query.UserView;
import com.goatcompass.identity.domain.event.UserRegistered;
import com.goatcompass.identity.domain.event.UserVerified;
import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.identity.domain.model.enumeration.UserStatus;
import com.goatcompass.identity.domain.repository.UserAccountRepository;
import com.goatcompass.shared.application.exception.ServiceException;
import com.goatcompass.shared.application.exception.UnauthenticatedException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String OTP_PREFIX = "otp:";
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final String LOGIN_OTP_RATE_PREFIX = "rate:login-activation-otp:";
    private static final int LOGIN_OTP_MAX_PER_HOUR = 3;
    private static final String BAD_CREDENTIALS = "Tên đăng nhập/email hoặc mật khẩu không chính xác";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountRepository users;
    private final TokenPort tokens;
    private final RsaPort rsa;
    private final PasswordInput passwordInput;
    private final PasswordHasherPort passwordHasher;
    private final RedisPort redis;
    private final EmailNotificationPort email;
    private final AuthRateLimiter rateLimiter;
    private final ApplicationEventPublisher events;

    @Value("${app.jwt.refresh-token.expiration-second}")
    private long refreshTokenExpirationSecond;
    @Value("${app.verify-code-validity-in-second}")
    private long verificationCodeValidityInSecond;

    public String publicKey() {
        return rsa.getPublicKey();
    }

    @Transactional
    public UserView register(RegisterCommand command) throws ServiceException {
        String username = command.username().trim();
        String normalizedEmail = UserAccount.normalizeEmail(command.email());
        if (users.existsByUsernameOrEmail(username, normalizedEmail)) {
            throw new ServiceException("Tên đăng nhập hoặc email đã được sử dụng");
        }
        String rawPassword = passwordInput.decryptNewPassword(command.password(), command.confirmPassword());

        UserAccount user = users.save(
                UserAccount.register(username, normalizedEmail, command.fullName(),
                passwordHasher.hash(rawPassword))
        );

        events.publishEvent(new UserRegistered(user.getId(), user.getUsername(), user.getEmail(), user.getFullName()));
        sendVerificationCode(user);

        return UserView.from(user);
    }

    @Transactional
    public AuthSession login(LoginCommand command) throws ServiceException {
        rateLimiter.checkLogin(command.identifier());
        String rawPassword = passwordInput.decrypt(command.password());

        UserAccount user = users.findByUsernameOrEmail(command.identifier().trim())
                .filter(candidate -> passwordHasher.matches(rawPassword, candidate.getPasswordHash()))
                .orElseThrow(() -> new ServiceException(BAD_CREDENTIALS));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw inactive(user);
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw AccountStatusException.blocked(user.getEmail());
        }

        return issueSession(user);
    }

    @Transactional
    public void verify(VerifyCommand command) throws ServiceException {
        String normalizedEmail = UserAccount.normalizeEmail(command.email());
        rateLimiter.checkVerify(normalizedEmail);
        UserAccount user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ServiceException("Người dùng không tồn tại"));

        String expected = redis.getValue(otpKey(user.getId()));
        if (expected == null) {
            throw new ServiceException("Mã xác thực đã hết hạn");
        }
        if (!expected.equals(command.verificationCode().trim())) {
            throw new ServiceException("Mã xác thực không chính xác");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw AccountStatusException.blocked(user.getEmail());
        }

        boolean firstActivation = user.getStatus() == UserStatus.INACTIVE;
        String pendingPasswordHash = redis.getValue(passwordResetKey(user.getId()));
        if (pendingPasswordHash != null) {
            user.changePassword(pendingPasswordHash);
            revokeSession(user.getId());
        }

        user.activate();
        users.save(user);

        redis.deleteKey(otpKey(user.getId()));
        redis.deleteKey(passwordResetKey(user.getId()));
        if (firstActivation) {
            events.publishEvent(new UserVerified(user.getId()));
        }
    }

    public void resendVerificationCode(String rawEmail) throws ServiceException {
        String normalizedEmail = UserAccount.normalizeEmail(rawEmail);
        rateLimiter.checkOtpResend(normalizedEmail);

        UserAccount user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ServiceException("Người dùng không tồn tại"));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw AccountStatusException.blocked(user.getEmail());
        }
        if (user.isActive() && !redis.hasKey(passwordResetKey(user.getId()))) {
            throw new ServiceException("Tài khoản đã được kích hoạt");
        }

        sendVerificationCode(user);
    }

    public AuthSession refresh(String refreshToken) throws UnauthenticatedException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthenticatedException("Hết hạn đăng nhập");
        }
        TokenClaims claims = tokens.parse(refreshToken)
                .filter(parsed -> TOKEN_TYPE_REFRESH.equals(parsed.type()))
                .orElseThrow(() -> new UnauthenticatedException("Refresh token không hợp lệ hoặc đã hết hạn"));
        String userId = claims.userId();

        if (redis.hasKey(refreshBlacklistKey(refreshToken))) {
            revokeSession(userId);
            log.warn("Cảnh báo bảo mật: refresh token của người dùng {} bị dùng lại, đã hủy phiên.", userId);
            throw new UnauthenticatedException("Cảnh báo bảo mật: Phiên làm việc đã bị xâm phạm. Vui lòng đăng nhập lại.");
        }
        if (!refreshToken.equals(redis.getValue(refreshKey(userId)))) {
            throw new UnauthenticatedException("Refresh token đã hết hạn");
        }

        UserAccount user = users.findById(UUID.fromString(userId))
                .filter(UserAccount::isActive)
                .orElse(null);
        if (user == null) {
            revokeSession(userId);
            throw new UnauthenticatedException("Tài khoản không còn hoạt động");
        }

        String sessionId = claims.sessionId() != null ? claims.sessionId() : UUID.randomUUID().toString();
        AuthSession session = storeSession(user, sessionId);
        if (claims.remainingSeconds() > 0) {
            redis.saveWithTtl(refreshBlacklistKey(refreshToken), "used", claims.remainingSeconds(), TimeUnit.SECONDS);
        }
        return session;
    }

    public void logout(String accessToken, String refreshToken) {
        Optional<TokenClaims> access = parse(accessToken, TOKEN_TYPE_ACCESS);
        access.filter(claims -> claims.remainingSeconds() > 0).ifPresent(claims -> redis.saveWithTtl(
                accessBlacklistKey(accessToken), "revoked", claims.remainingSeconds(), TimeUnit.SECONDS));

        Optional<TokenClaims> refresh = parse(refreshToken, TOKEN_TYPE_REFRESH);
        if (refresh.isPresent()) {
            String userId = refresh.get().userId();
            if (refreshToken.equals(redis.getValue(refreshKey(userId)))) {
                revokeSession(userId);
            }
        } else {
            access.filter(claims -> claims.sessionId() != null
                            && claims.sessionId().equals(redis.getValue(sessionKey(claims.userId()))))
                    .ifPresent(claims -> revokeSession(claims.userId()));
        }
    }

    public void forgotPassword(ForgotPasswordCommand command) throws ServiceException {
        String normalizedEmail = UserAccount.normalizeEmail(command.email());
        rateLimiter.checkPasswordReset(normalizedEmail);

        UserAccount user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ServiceException("Người dùng không tồn tại"));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw AccountStatusException.blocked(user.getEmail());
        }

        String rawPassword = passwordInput.decryptNewPassword(command.password(), command.confirmPassword());

        redis.saveWithTtl(passwordResetKey(user.getId()), passwordHasher.hash(rawPassword),
                verificationCodeValidityInSecond, TimeUnit.SECONDS);

        sendVerificationCode(user);
    }

    public UserView currentUser(UUID userId) throws UnauthenticatedException {
        return users.findById(userId).map(UserView::from)
                .orElseThrow(() -> new UnauthenticatedException("Người dùng không tồn tại"));
    }

    /** Tài khoản chưa xác thực đăng nhập đúng mật khẩu: gửi OTP (có giới hạn) và báo client mở bước nhập OTP. */
    private AccountStatusException inactive(UserAccount user) {
        boolean sent = redis.incrementWithTtl(LOGIN_OTP_RATE_PREFIX + user.getId(), 1, TimeUnit.HOURS)
                <= LOGIN_OTP_MAX_PER_HOUR;
        if (sent) {
            sendVerificationCode(user);
        }
        return AccountStatusException.inactive(user.getEmail(), sent);
    }

    private AuthSession issueSession(UserAccount user) {
        return storeSession(user, UUID.randomUUID().toString());
    }

    private AuthSession storeSession(UserAccount user, String sessionId) {
        String accessToken = tokens.generateAccessToken(user, sessionId);
        String refreshToken = tokens.generateRefreshToken(user, sessionId);

        redis.saveWithTtl(refreshKey(user.getId()), refreshToken, refreshTokenExpirationSecond, TimeUnit.SECONDS);
        redis.saveWithTtl(sessionKey(user.getId()), sessionId, refreshTokenExpirationSecond, TimeUnit.SECONDS);

        return new AuthSession(UserView.from(user), accessToken, refreshToken);
    }

    private void revokeSession(Object userId) {
        redis.deleteKey(refreshKey(userId));
        redis.deleteKey(sessionKey(userId));
    }

    private void sendVerificationCode(UserAccount user) {
        String code = String.valueOf(RANDOM.nextInt(900_000) + 100_000);
        redis.saveWithTtl(otpKey(user.getId()), code, verificationCodeValidityInSecond, TimeUnit.SECONDS);
        email.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }

    private Optional<TokenClaims> parse(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return tokens.parse(token).filter(claims -> expectedType.equals(claims.type()));
    }

    private static String otpKey(UUID userId) {
        return OTP_PREFIX + userId;
    }

    private static String passwordResetKey(UUID userId) {
        return PASSWORD_RESET_PREFIX + userId;
    }
}
