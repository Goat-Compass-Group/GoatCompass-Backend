package com.goatcompass.identity.infrastructure.web.advice;

import com.goatcompass.identity.application.exception.AccountStatusException;
import com.goatcompass.identity.application.exception.RateLimitExceededException;
import com.goatcompass.shared.application.dto.response.base.BaseResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityExceptionHandler {
    @ExceptionHandler(AccountStatusException.class)
    ResponseEntity<BaseResponse<Object>> accountStatus(AccountStatusException exception) {
        BaseResponse<Object> response = new BaseResponse<>();
        response.setStatusCode(HttpStatus.FORBIDDEN.value());
        response.setMessage(exception.getMessage());
        response.setError(exception.getCode());
        response.setData(exception.getData());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<BaseResponse<Object>> rateLimitExceeded(RateLimitExceededException exception) {
        BaseResponse<Object> response = new BaseResponse<>();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setMessage(exception.getMessage());
        response.setError("Vượt ngưỡng số lần thử");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
