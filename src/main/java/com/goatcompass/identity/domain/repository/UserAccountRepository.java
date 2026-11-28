package com.goatcompass.identity.domain.repository;

import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {
    Optional<UserAccount> findById(UUID id);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsernameOrEmail(String identifier);

    boolean existsByUsernameOrEmail(String username, String email);

    UserAccount save(UserAccount user);
}
