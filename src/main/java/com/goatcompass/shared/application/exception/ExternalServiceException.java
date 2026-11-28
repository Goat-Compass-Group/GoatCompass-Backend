package com.goatcompass.shared.application.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    private final String userMessage;

    public ExternalServiceException(String userMessage, String message, Throwable cause) {
        super(message, cause);
        this.userMessage = userMessage;
    }

}