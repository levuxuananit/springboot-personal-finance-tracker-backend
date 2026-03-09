package com.example.PersonalFinanceTracker.dto.response;

public record ValidationError(
        String field,
        String message
) {}

