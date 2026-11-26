package com.goatcompass.shared.application.exception;

/** Lỗi nghiệp vụ trả về cho client (400). */
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}
