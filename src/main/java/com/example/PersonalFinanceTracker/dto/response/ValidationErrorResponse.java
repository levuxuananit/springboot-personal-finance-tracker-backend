package com.example.PersonalFinanceTracker.dto.response;

import java.util.List;

public record ValidationErrorResponse(
        boolean success,
        String message,
        List<ValidationError> errors
) {}

