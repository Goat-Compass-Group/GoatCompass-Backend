package com.goatcompass.shared.infrastructure.security;

import com.goatcompass.shared.application.exception.UnauthenticatedException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {
    public Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof String userId) || userId.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public UUID requireCurrentUserId() throws UnauthenticatedException {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthenticatedException("Bạn chưa đăng nhập hoặc phiên đã hết hạn"));
    }
}