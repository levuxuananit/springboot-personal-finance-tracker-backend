package com.example.PersonalFinanceTracker.dto;

public record BudgetResponse(
        Long id,
        Double amount,
        Integer month,
        Integer year,
        Long categoryId,
        Long userId
) {}