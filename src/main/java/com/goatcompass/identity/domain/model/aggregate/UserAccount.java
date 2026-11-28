package com.goatcompass.identity.domain.model.aggregate;

import com.goatcompass.identity.domain.model.enumeration.Role;
import com.goatcompass.identity.domain.model.enumeration.UserStatus;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccount {
    private final UUID id;
    private final String username;
    private final String email;
    private String passwordHash;
    private final String fullName;
    private UserStatus status;
    private final Role role;
    private final LocalDateTime createdAt;

    public static UserAccount register(String username, String email, String fullName, String passwordHash) {
        return UserAccount.builder()
                .username(username.trim())
                .email(normalizeEmail(email))
                .fullName(fullName.trim())
                .passwordHash(passwordHash)
                .status(UserStatus.INACTIVE)
                .role(Role.USER)
                .build();
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void activate() {
        if (status == UserStatus.BLOCKED) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        status = UserStatus.ACTIVE;
    }

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        passwordHash = newPasswordHash;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
