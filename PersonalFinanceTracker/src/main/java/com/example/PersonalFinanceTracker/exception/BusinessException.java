package com.example.PersonalFinanceTracker.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
    public abstract HttpStatus getStatus();
}
