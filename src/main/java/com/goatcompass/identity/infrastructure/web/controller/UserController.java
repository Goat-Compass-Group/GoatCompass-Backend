package com.goatcompass.identity.infrastructure.web.controller;

import com.goatcompass.identity.application.command.AuthCommands.ChangePasswordCommand;
import com.goatcompass.identity.application.service.UserAccountService;
import com.goatcompass.identity.infrastructure.web.dto.AuthRequests.UpdatePasswordRequest;
import com.goatcompass.shared.application.exception.ServiceException;
import com.goatcompass.shared.infrastructure.security.SecurityContextHelper;
import com.goatcompass.shared.infrastructure.web.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserAccountService userAccountService;
    private final SecurityContextHelper securityContextHelper;

    @PutMapping("/password")
    @ApiMessage("Đổi mật khẩu thành công")
    public void changePassword(@Valid @RequestBody UpdatePasswordRequest request) throws ServiceException {
        userAccountService.changePassword(securityContextHelper.requireCurrentUserId(),
                new ChangePasswordCommand(request.currentPassword(), request.newPassword(), request.confirmPassword()));
    }
}
