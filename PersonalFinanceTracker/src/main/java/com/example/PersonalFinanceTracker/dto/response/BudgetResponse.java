package com.example.PersonalFinanceTracker.dto.response;

public record BudgetResponse(
        Long id,
        Double amount,
        Integer month,
        Integer year,
        Long categoryId,
        Long userId
) {}

