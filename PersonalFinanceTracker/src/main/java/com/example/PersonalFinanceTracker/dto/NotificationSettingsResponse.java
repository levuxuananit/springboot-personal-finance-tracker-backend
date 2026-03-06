package com.example.PersonalFinanceTracker.dto;

public record NotificationSettingsResponse(
        Boolean dailyReminder,
        Boolean tipsEnabled,
        Boolean budgetAlert
) {}
