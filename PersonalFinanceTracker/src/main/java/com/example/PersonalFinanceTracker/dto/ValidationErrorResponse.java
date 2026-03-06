package com.example.PersonalFinanceTracker.dto;

import java.util.List;

public record ValidationErrorResponse(
        boolean success,
        String message,
        List<ValidationError> errors
) {}