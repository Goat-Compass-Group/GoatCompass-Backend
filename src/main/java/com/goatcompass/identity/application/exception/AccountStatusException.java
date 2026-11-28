package com.goatcompass.identity.application.exception;

import com.goatcompass.shared.application.exception.ServiceException;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class AccountStatusException extends ServiceException {
    public static final String INACTIVE = "ACCOUNT_INACTIVE";
    public static final String BLOCKED = "ACCOUNT_BLOCKED";

    private final String code;
    private final Map<String, Object> data;

    private AccountStatusException(String code, String message, Map<String, Object> data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public static AccountStatusException inactive(String email, boolean codeSent) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", email);
        data.put("codeSent", codeSent);
        return new AccountStatusException(INACTIVE, "Tài khoản chưa được kích hoạt", data);
    }

    public static AccountStatusException blocked(String email) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", email);
        return new AccountStatusException(BLOCKED, "Tài khoản đã bị khóa", data);
    }
}