package com.example.PersonalFinanceTracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception dùng cho lỗi:
 * - Sai mật khẩu
 * - Chưa đăng nhập
 * -> HTTP 401
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}