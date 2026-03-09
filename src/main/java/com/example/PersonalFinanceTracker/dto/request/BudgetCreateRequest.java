package com.example.PersonalFinanceTracker.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetCreateRequest(
        @NotNull @Positive Double amount,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(1970) @Max(3000) Integer year,
        @NotNull @Positive Long categoryId
) {}

