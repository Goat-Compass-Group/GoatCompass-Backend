package com.goatcompass.identity.domain.event;

import java.util.UUID;

public record UserRegistered(UUID userId, String username, String email, String fullName) {
}
