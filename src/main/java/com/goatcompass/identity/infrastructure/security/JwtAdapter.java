package com.goatcompass.identity.infrastructure.security;

import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_EMAIL;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_ROLE;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_SESSION;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_TOKEN_TYPE;
import static com.goatcompass.shared.application.security.AuthTokenContract.CLAIM_USERNAME;
import static com.goatcompass.shared.application.security.AuthTokenContract.TOKEN_TYPE_ACCESS;
import static com.goatcompass.shared.application.security.AuthTokenContract.TOKEN_TYPE_REFRESH;

import com.goatcompass.identity.application.port.TokenPort;
import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.shared.infrastructure.config.JwtConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAdapter implements TokenPort {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.jwt.access-token.expiration-second}")
    private long accessTokenExpirationSecond;
    @Value("${app.jwt.refresh-token.expiration-second}")
    private long refreshTokenExpirationSecond;

    @Override
    public String generateAccessToken(UserAccount user, String sessionId) {
        return buildToken(user, sessionId, TOKEN_TYPE_ACCESS, accessTokenExpirationSecond);
    }

    @Override
    public String generateRefreshToken(UserAccount user, String sessionId) {
        return buildToken(user, sessionId, TOKEN_TYPE_REFRESH, refreshTokenExpirationSecond);
    }

    @Override
    public Optional<TokenClaims> parse(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            long remaining = expiresAt == null ? 0 : Math.max(Duration.between(Instant.now(), expiresAt).toSeconds(), 0);
            return Optional.of(new TokenClaims(jwt.getSubject(), jwt.getClaimAsString(CLAIM_ROLE),
                    jwt.getClaimAsString(CLAIM_SESSION), jwt.getClaimAsString(CLAIM_TOKEN_TYPE), remaining));
        } catch (JwtException e) {
            log.debug("JWT không hợp lệ: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildToken(UserAccount user, String sessionId, String type, long expirationSecond) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSecond))
                .subject(user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_SESSION, sessionId)
                .claim(CLAIM_TOKEN_TYPE, type)
                .build();
        JwsHeader header = JwsHeader.with(JwtConfig.JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
