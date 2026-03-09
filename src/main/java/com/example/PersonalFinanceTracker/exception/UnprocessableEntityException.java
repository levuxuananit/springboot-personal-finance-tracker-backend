package com.example.PersonalFinanceTracker.exception;

public class UnprocessableEntityException extends RuntimeException {
    private final String field;

    public UnprocessableEntityException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

