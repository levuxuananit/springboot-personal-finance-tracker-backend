package com.example.PersonalFinanceTracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception để trả status + message theo spec
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}