package com.goatcompass.identity.infrastructure.web.controller;

import static com.goatcompass.shared.application.security.AuthTokenContract.ACCESS_TOKEN_COOKIE;
import static com.goatcompass.shared.application.security.AuthTokenContract.REFRESH_TOKEN_COOKIE;

import com.goatcompass.identity.application.command.AuthCommands.ForgotPasswordCommand;
import com.goatcompass.identity.application.command.AuthCommands.LoginCommand;
import com.goatcompass.identity.application.command.AuthCommands.RegisterCommand;
import com.goatcompass.identity.application.command.AuthCommands.VerifyCommand;
import com.goatcompass.identity.application.query.AuthSession;
import com.goatcompass.identity.application.query.UserView;
import com.goatcompass.identity.application.service.AuthenticationService;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.ForgotPasswordRequest;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.LoginRequest;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.PublicKeyResponse;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.RegisterRequest;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.VerificationRequest;
import com.goatcompass.shared.application.exception.ServiceException;
import com.goatcompass.shared.application.exception.UnauthenticatedException;
import com.goatcompass.shared.infrastructure.security.SecurityContextHelper;
import com.goatcompass.shared.infrastructure.web.annotation.ApiMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final SecurityContextHelper securityContextHelper;
    private final AuthCookies authCookies;

    @GetMapping("/public-key")
    @ApiMessage("Lấy public key thành công")
    public PublicKeyResponse publicKey() {
        return new PublicKeyResponse(authenticationService.publicKey());
    }

    @PostMapping("/login")
    @ApiMessage("Đăng nhập thành công")
    public UserView login(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response
    ) throws ServiceException
    {
        AuthSession session = authenticationService.login(new LoginCommand(request.username(), request.password()));
        authCookies.write(response, session);
        return session.user();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiMessage("Đăng ký thành công, vui lòng kiểm tra email để lấy mã xác thực")
    public UserView register(@Valid @RequestBody RegisterRequest request) throws ServiceException {
        return authenticationService.register(new RegisterCommand(request.username(), request.fullName(),
                request.email(), request.password(), request.confirmPassword()));
    }

    @PostMapping("/verify")
    @ApiMessage("Xác thực thành công")
    public void verify(@Valid @RequestBody VerificationRequest request) throws ServiceException {
        authenticationService.verify(new VerifyCommand(request.email(), request.verificationCode()));
    }

    @PostMapping("/resend")
    @ApiMessage("Đã gửi lại mã xác thực")
    public void resend(@RequestParam String email) throws ServiceException {
        authenticationService.resendVerificationCode(email);
    }

    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public void logout(
            @CookieValue(value = ACCESS_TOKEN_COOKIE, required = false) String accessToken,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authenticationService.logout(accessToken, refreshToken);
        authCookies.clear(response);
    }

    @GetMapping("/refresh")
    @ApiMessage("Làm mới phiên đăng nhập thành công")
    public ResponseEntity<UserView> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            AuthSession session = authenticationService.refresh(refreshToken);
            authCookies.write(response, session);
            return ResponseEntity.ok(session.user());
        } catch (UnauthenticatedException e) {
            log.debug("Refresh thất bại: {}", e.getMessage());
            authCookies.clear(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/forgot-password")
    @ApiMessage("Đã gửi mã xác thực, nhập mã để hoàn tất đặt lại mật khẩu")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws ServiceException {
        authenticationService.forgotPassword(
                new ForgotPasswordCommand(request.email(), request.password(), request.confirmPassword()));
    }

    @GetMapping("/me")
    @ApiMessage("Lấy thông tin người dùng thành công")
    public UserView me() throws UnauthenticatedException {
        return authenticationService.currentUser(securityContextHelper.requireCurrentUserId());
    }
}
