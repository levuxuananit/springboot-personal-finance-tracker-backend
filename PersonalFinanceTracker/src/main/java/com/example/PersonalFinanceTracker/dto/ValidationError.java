package com.example.PersonalFinanceTracker.dto;

public record ValidationError(
        String field,
        String message
) {}