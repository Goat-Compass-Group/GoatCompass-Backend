package com.goatcompass.identity.application.service;

import com.goatcompass.identity.application.port.RsaPort;
import com.goatcompass.identity.domain.service.PasswordPolicy;
import com.goatcompass.shared.application.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PasswordInput {
    private final RsaPort rsa;

    String decrypt(String encryptedPassword) {
        return rsa.decrypt(encryptedPassword);
    }

    String decryptNewPassword(String encryptedPassword, String encryptedConfirm) throws ServiceException {
        String rawPassword = rsa.decrypt(encryptedPassword);
        String rawConfirmPassword = rsa.decrypt(encryptedConfirm);
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ServiceException("Mật khẩu không được để trống");
        }
        if (!PasswordPolicy.isStrong(rawPassword)) {
            throw new ServiceException(PasswordPolicy.STRENGTH_MESSAGE);
        }
        if (!rawPassword.equals(rawConfirmPassword)) {
            throw new ServiceException("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        return rawPassword;
    }
}
