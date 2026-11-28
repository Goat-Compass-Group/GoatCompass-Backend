package com.goatcompass.identity.application.port;

import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import java.util.Optional;

public interface TokenPort {
    String generateAccessToken(UserAccount user, String sessionId);

    String generateRefreshToken(UserAccount user, String sessionId);

    Optional<TokenClaims> parse(String token);

    record TokenClaims(String userId, String role, String sessionId, String type, long remainingSeconds) {
    }
}
