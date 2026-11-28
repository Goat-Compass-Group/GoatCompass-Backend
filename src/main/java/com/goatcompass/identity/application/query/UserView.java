package com.goatcompass.identity.application.query;

import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.identity.domain.model.enumeration.Role;
import com.goatcompass.identity.domain.model.enumeration.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserView(
        UUID userId, String username, String email, String fullName, UserStatus status, Role role,
        LocalDateTime createdAt
) {
    public static UserView from(UserAccount user) {
        return new UserView(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getStatus(),
                user.getRole(), user.getCreatedAt());
    }
}
