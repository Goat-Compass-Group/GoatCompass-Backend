package com.goatcompass.identity.application.query;

public record AuthSession(UserView user, String accessToken, String refreshToken) {
}
