package com.example.PersonalFinanceTracker.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {}
