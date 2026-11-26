package com.goatcompass.shared.application.exception;

/** Chưa đăng nhập (401). */
public class UnauthenticatedException extends ServiceException {
    public UnauthenticatedException(String message) {
        super(message);
    }
}
