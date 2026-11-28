package com.goatcompass.identity.domain.service;

import java.util.regex.Pattern;

public final class PasswordPolicy {
    public static final String STRENGTH_MESSAGE =
            "Mật khẩu phải có ít nhất 8 ký tự, chữ hoa, chữ thường, số và ký tự đặc biệt";

    private static final Pattern STRONG = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private PasswordPolicy() {
    }

    public static boolean isStrong(String rawPassword) {
        return rawPassword != null && STRONG.matcher(rawPassword).matches();
    }
}
