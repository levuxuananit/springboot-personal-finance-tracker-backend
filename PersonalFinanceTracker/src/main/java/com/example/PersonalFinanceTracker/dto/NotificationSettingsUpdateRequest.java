package com.example.PersonalFinanceTracker.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingsUpdateRequest(
        @NotNull Boolean dailyReminder,
        @NotNull Boolean tipsEnabled,
        @NotNull Boolean budgetAlert
) {}
