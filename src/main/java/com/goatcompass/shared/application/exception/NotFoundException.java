package com.goatcompass.shared.application.exception;

/** Không tìm thấy dữ liệu được yêu cầu (404). */
public class NotFoundException extends ServiceException {
    public NotFoundException(String message) {
        super(message);
    }
}
