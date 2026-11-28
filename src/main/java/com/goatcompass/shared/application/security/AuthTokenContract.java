package com.goatcompass.shared.application.security;

public final class AuthTokenContract {
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SESSION = "sid";
    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private AuthTokenContract() {
    }

    public static String accessBlacklistKey(String accessToken) {
        return "blacklist:" + accessToken;
    }

    public static String refreshBlacklistKey(String refreshToken) {
        return "blacklist_refresh:" + refreshToken;
    }

    public static String refreshKey(Object userId) {
        return "user:" + userId + ":refresh";
    }

    public static String sessionKey(Object userId) {
        return "user:" + userId + ":session";
    }
}
