package com.goatcompass.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goatcompass.TestcontainersConfiguration;
import com.goatcompass.identity.application.port.EmailNotificationPort;
import com.goatcompass.shared.application.security.AuthTokenContract;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Luồng xác thực đầy đủ trên Postgres + Redis thật (Testcontainers). Email được mock để lấy mã OTP.
 * Mật khẩu được mã hóa RSA bằng public key của server như frontend làm.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {
    private static final String PASSWORD = "Goat@2026";
    private static final String NEW_PASSWORD = "Compass#2027";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EmailNotificationPort emailNotification;

    private PublicKey publicKey;

    @BeforeEach
    void loadPublicKey() throws Exception {
        String body = mvc.perform(get("/api/v1/auth/public-key")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        byte[] der = Base64.getDecoder().decode(JsonPath.<String>read(body, "$.data.publicKey"));
        publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    @Test
    void registerVerifyLoginMeRefreshLogout() throws Exception {
        Account account = newAccount();
        register(account).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        // Chưa xác thực: mật khẩu đúng thì 403 ACCOUNT_INACTIVE và tự gửi lại OTP
        login(account.username(), PASSWORD).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_INACTIVE"))
                .andExpect(jsonPath("$.data.email").value(account.email()))
                .andExpect(jsonPath("$.data.codeSent").value(true));

        verifyOtp(account.email(), "000000").andExpect(status().isBadRequest());
        verifyOtp(account.email(), lastOtp(account.email())).andExpect(status().isOk());

        MockHttpServletResponse loggedIn = login(account.email(), PASSWORD).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(account.username()))
                .andReturn().getResponse();
        Cookie access = loggedIn.getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);
        Cookie refresh = loggedIn.getCookie(AuthTokenContract.REFRESH_TOKEN_COOKIE);
        assertThat(access).isNotNull();
        assertThat(access.isHttpOnly()).isTrue();
        assertThat(refresh).isNotNull();

        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(account.email()))
                .andExpect(jsonPath("$.data.role").value("USER"));
        // Refresh token không dùng thay access token được
        mvc.perform(get("/api/v1/auth/me").cookie(new Cookie(AuthTokenContract.ACCESS_TOKEN_COOKIE, refresh.getValue())))
                .andExpect(status().isUnauthorized());

        MockHttpServletResponse refreshed = mvc.perform(get("/api/v1/auth/refresh").cookie(refresh))
                .andExpect(status().isOk()).andReturn().getResponse();
        Cookie newAccess = refreshed.getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);
        Cookie newRefresh = refreshed.getCookie(AuthTokenContract.REFRESH_TOKEN_COOKIE);
        assertThat(newRefresh.getValue()).isNotEqualTo(refresh.getValue());
        mvc.perform(get("/api/v1/auth/me").cookie(newAccess)).andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/logout").cookie(newAccess, newRefresh)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").cookie(newAccess)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/refresh").cookie(newRefresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void reusedRefreshTokenRevokesTheWholeSession() throws Exception {
        Account account = activeAccount();
        MockHttpServletResponse loggedIn = login(account.username(), PASSWORD).andReturn().getResponse();
        Cookie oldRefresh = loggedIn.getCookie(AuthTokenContract.REFRESH_TOKEN_COOKIE);

        MockHttpServletResponse refreshed = mvc.perform(get("/api/v1/auth/refresh").cookie(oldRefresh))
                .andExpect(status().isOk()).andReturn().getResponse();
        Cookie newAccess = refreshed.getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);
        Cookie newRefresh = refreshed.getCookie(AuthTokenContract.REFRESH_TOKEN_COOKIE);

        // Kẻ trộm dùng lại refresh token cũ: bị từ chối và phiên của chủ tài khoản cũng bị hủy
        mvc.perform(get("/api/v1/auth/refresh").cookie(oldRefresh)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/refresh").cookie(newRefresh)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(newAccess)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginOnAnotherDeviceEndsThePreviousSession() throws Exception {
        Account account = activeAccount();
        Cookie firstAccess = login(account.username(), PASSWORD).andReturn().getResponse()
                .getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);
        Cookie secondAccess = login(account.username(), PASSWORD).andReturn().getResponse()
                .getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);

        mvc.perform(get("/api/v1/auth/me").cookie(firstAccess)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(secondAccess)).andExpect(status().isOk());
    }

    @Test
    void rejectsWeakPasswordDuplicateAccountAndWrongCredentials() throws Exception {
        Account account = activeAccount();
        Account weak = newAccount();
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"%s","fullName":"Yếu","email":"%s","password":"%s","confirmPassword":"%s"}
                        """.formatted(weak.username(), weak.email(), encrypt("password"), encrypt("password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Mật khẩu phải có ít nhất 8 ký tự, chữ hoa, chữ thường, số và ký tự đặc biệt"));
        register(new Account(account.username().toUpperCase(), "khac-" + account.email()))
                .andExpect(status().isBadRequest());
        login(account.username(), "Sai@12345").andExpect(status().isBadRequest());
        login("khong-ton-tai", PASSWORD).andExpect(status().isBadRequest());
    }

    @Test
    void tooManyLoginAttemptsAreBlocked() throws Exception {
        Account account = activeAccount();
        for (int attempt = 0; attempt < 5; attempt++) {
            login(account.username(), "Sai@12345").andExpect(status().isBadRequest());
        }
        login(account.username(), PASSWORD).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Vượt ngưỡng số lần thử"));
    }

    @Test
    void forgotPasswordAppliesNewPasswordOnlyAfterOtp() throws Exception {
        Account account = activeAccount();
        mvc.perform(put("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"%s","password":"%s","confirmPassword":"%s"}
                        """.formatted(account.email(), encrypt(NEW_PASSWORD), encrypt(NEW_PASSWORD))))
                .andExpect(status().isOk());

        // Người lạ biết email không đổi được mật khẩu: chưa nhập OTP thì mật khẩu cũ vẫn dùng được
        login(account.username(), PASSWORD).andExpect(status().isOk());

        verifyOtp(account.email(), lastOtp(account.email())).andExpect(status().isOk());
        login(account.username(), PASSWORD).andExpect(status().isBadRequest());
        login(account.username(), NEW_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void changePasswordRequiresLoginAndCurrentPassword() throws Exception {
        Account account = activeAccount();
        String body = """
                {"currentPassword":"%s","newPassword":"%s","confirmPassword":"%s"}
                """;
        mvc.perform(put("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(encrypt(PASSWORD), encrypt(NEW_PASSWORD), encrypt(NEW_PASSWORD))))
                .andExpect(status().isUnauthorized());

        Cookie access = login(account.username(), PASSWORD).andReturn().getResponse()
                .getCookie(AuthTokenContract.ACCESS_TOKEN_COOKIE);
        mvc.perform(put("/api/v1/users/password").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(encrypt("Sai@12345"), encrypt(NEW_PASSWORD), encrypt(NEW_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mật khẩu hiện tại không chính xác"));
        mvc.perform(put("/api/v1/users/password").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(encrypt(PASSWORD), encrypt(NEW_PASSWORD), encrypt(NEW_PASSWORD))))
                .andExpect(status().isOk());

        login(account.username(), NEW_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void resendIsRateLimitedAndRejectedForActiveAccounts() throws Exception {
        Account account = newAccount();
        register(account).andExpect(status().isCreated());
        for (int attempt = 0; attempt < 3; attempt++) {
            mvc.perform(post("/api/v1/auth/resend").param("email", account.email())).andExpect(status().isOk());
        }
        mvc.perform(post("/api/v1/auth/resend").param("email", account.email()))
                .andExpect(status().isTooManyRequests());

        Account active = activeAccount();
        mvc.perform(post("/api/v1/auth/resend").param("email", active.email())).andExpect(status().isBadRequest());
    }

    private record Account(String username, String email) {
    }

    private static Account newAccount() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new Account("user_" + suffix, "user." + suffix + "@example.com");
    }

    private Account activeAccount() throws Exception {
        Account account = newAccount();
        register(account).andExpect(status().isCreated());
        verifyOtp(account.email(), lastOtp(account.email())).andExpect(status().isOk());
        return account;
    }

    private ResultActions register(Account account) throws Exception {
        return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","fullName":"Nguyễn Văn A","email":"%s","password":"%s","confirmPassword":"%s"}
                """.formatted(account.username(), account.email(), encrypt(PASSWORD), encrypt(PASSWORD))));
    }

    private ResultActions login(String identifier, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","password":"%s"}
                """.formatted(identifier, encrypt(password))));
    }

    private ResultActions verifyOtp(String email, String code) throws Exception {
        return mvc.perform(post("/api/v1/auth/verify").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","verificationCode":"%s"}
                """.formatted(email, code)));
    }

    private String lastOtp(String email) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailNotification, atLeastOnce()).sendVerificationEmail(eq(email), any(), code.capture());
        return code.getValue();
    }

    private String encrypt(String raw) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
    }
}
