package com.goatcompass.identity.infrastructure.web.controller;

import static com.goatcompass.shared.application.security.AuthTokenContract.ACCESS_TOKEN_COOKIE;
import static com.goatcompass.shared.application.security.AuthTokenContract.REFRESH_TOKEN_COOKIE;

import com.goatcompass.identity.application.query.AuthSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class AuthCookies {
    @Value("${app.jwt.access-token.expiration-second}")
    private long accessTokenExpirationSecond;
    @Value("${app.jwt.refresh-token.expiration-second}")
    private long refreshTokenExpirationSecond;
    @Value("${app.cookie.domain}")
    private String cookieDomain;
    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    void write(HttpServletResponse response, AuthSession session) {
        add(response, create(ACCESS_TOKEN_COOKIE, session.accessToken(), accessTokenExpirationSecond));
        add(response, create(REFRESH_TOKEN_COOKIE, session.refreshToken(), refreshTokenExpirationSecond));
    }

    void clear(HttpServletResponse response) {
        add(response, create(ACCESS_TOKEN_COOKIE, "", 0));
        add(response, create(REFRESH_TOKEN_COOKIE, "", 0));
    }

    private ResponseCookie create(String name, String value, long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    private static void add(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
