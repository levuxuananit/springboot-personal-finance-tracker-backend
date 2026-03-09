package com.example.PersonalFinanceTracker.dto.response;

public record NotificationSettingsResponse(
        Boolean dailyReminder,
        Boolean tipsEnabled,
        Boolean budgetAlert
) {}

