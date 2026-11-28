package com.goatcompass.identity.infrastructure.persistence.repository.adapter;

import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.identity.domain.repository.UserAccountRepository;
import com.goatcompass.identity.infrastructure.persistence.entity.UserAccountEntity;
import com.goatcompass.identity.infrastructure.persistence.mapper.UserAccountEntityMapper;
import com.goatcompass.identity.infrastructure.persistence.repository.UserAccountJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class UserAccountRepositoryAdapter implements UserAccountRepository {
    private final UserAccountEntityMapper mapper;
    private final UserAccountJpaRepository jpa;

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsernameOrEmail(String identifier) {
        return jpa.findFirstByUsernameIgnoreCaseOrEmail(identifier, UserAccount.normalizeEmail(identifier))
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return jpa.existsByUsernameIgnoreCaseOrEmail(username, email);
    }

    @Override
    public UserAccount save(UserAccount user) {
        UserAccountEntity entity = user.getId() == null
                ? new UserAccountEntity()
                : jpa.findById(user.getId()).orElseThrow(
                        () -> new IllegalStateException("Không tìm thấy tài khoản " + user.getId()));
        mapper.updateEntity(user, entity);
        return mapper.toDomain(jpa.save(entity));
    }
}
