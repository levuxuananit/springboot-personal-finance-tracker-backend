package com.example.PersonalFinanceTracker.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableException extends BusinessException {
    public UnprocessableException(String message) { super(message); }

    @Override
    public HttpStatus getStatus() { return HttpStatus.UNPROCESSABLE_ENTITY; }
}
