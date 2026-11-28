package com.goatcompass.shared.infrastructure.filter;

import static com.goatcompass.shared.application.security.AuthTokenContract.ACCESS_TOKEN_COOKIE;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_ROLE;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_SESSION;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_TOKEN_TYPE;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_USERNAME;
import static com.goatcompass.shared.application.security.AuthTokenContract.TOKEN_TYPE_ACCESS;
import static com.goatcompass.shared.application.security.AuthTokenContract.accessBlacklistKey;
import static com.goatcompass.shared.application.security.AuthTokenContract.sessionKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;
    private final StringRedisTemplate redis;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException
    {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (!TOKEN_TYPE_ACCESS.equals(jwt.getClaimAsString(CLAIM_TOKEN_TYPE)) || isRevoked(token, jwt)) {
                return;
            }
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    Objects.requireNonNull(jwt.getClaimAsString(CLAIM_USERNAME)), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString(CLAIM_ROLE))));
            authentication.setDetails(jwt.getSubject());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            log.debug("Access token không hợp lệ: {}", e.getMessage());
        } catch (DataAccessException e) {
            log.warn("Không kiểm tra được phiên đăng nhập trên Redis: {}", e.getMessage());
        }
    }

    private boolean isRevoked(String token, Jwt jwt) {
        if (Boolean.TRUE.equals(redis.hasKey(accessBlacklistKey(token)))) {
            return true;
        }
        String currentSession = redis.opsForValue().get(sessionKey(jwt.getSubject()));
        return currentSession == null || !currentSession.equals(jwt.getClaimAsString(CLAIM_SESSION));
    }

    private static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }
}
