package com.goatcompass.identity.application.service;

import com.goatcompass.identity.application.command.AuthCommands.ChangePasswordCommand;
import com.goatcompass.identity.application.port.PasswordHasherPort;
import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.identity.domain.repository.UserAccountRepository;
import com.goatcompass.shared.application.exception.ServiceException;
import com.goatcompass.shared.application.exception.UnauthenticatedException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository users;
    private final PasswordHasherPort passwordHasher;
    private final PasswordInput passwordInput;

    @Transactional
    public void changePassword(UUID userId, ChangePasswordCommand command) throws ServiceException {
        UserAccount user = users.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Người dùng không tồn tại"));

        String rawCurrentPassword = passwordInput.decrypt(command.currentPassword());
        if (rawCurrentPassword == null || rawCurrentPassword.isBlank()) {
            throw new ServiceException("Mật khẩu hiện tại không được để trống");
        }
        String rawNewPassword = passwordInput.decryptNewPassword(command.newPassword(), command.confirmPassword());
        if (!passwordHasher.matches(rawCurrentPassword, user.getPasswordHash())) {
            throw new ServiceException("Mật khẩu hiện tại không chính xác");
        }

        user.changePassword(passwordHasher.hash(rawNewPassword));
        users.save(user);
    }
}
