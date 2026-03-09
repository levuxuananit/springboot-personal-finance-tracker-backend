package com.example.PersonalFinanceTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PersonalFinanceTracker.dto.request.NotificationSettingsUpdateRequest;
import com.example.PersonalFinanceTracker.dto.response.ApiResponse;
import com.example.PersonalFinanceTracker.dto.response.NotificationSettingsResponse;
import com.example.PersonalFinanceTracker.service.NotificationSettingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    public NotificationSettingController(NotificationSettingService notificationSettingService) {
        this.notificationSettingService = notificationSettingService;
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getSettings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingsResponse data = notificationSettingService.getSettings(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification settings fetched successfully", data));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateSettings(
            Authentication authentication,
            @Valid @RequestBody NotificationSettingsUpdateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingsResponse data = notificationSettingService.updateSettings(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification settings updated successfully", data));
    }
}

