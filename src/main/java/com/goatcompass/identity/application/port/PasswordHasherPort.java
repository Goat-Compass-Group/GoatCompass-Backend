package com.goatcompass.identity.application.port;

public interface PasswordHasherPort {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
