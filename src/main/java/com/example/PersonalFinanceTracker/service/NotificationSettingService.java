package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.request.NotificationSettingsUpdateRequest;
import com.example.PersonalFinanceTracker.dto.response.NotificationSettingsResponse;
import com.example.PersonalFinanceTracker.entity.NotificationSetting;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.repository.NotificationSettingRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSettingService {

    private static final boolean DEFAULT_DAILY_REMINDER = true;
    private static final boolean DEFAULT_TIPS_ENABLED = false;
    private static final boolean DEFAULT_BUDGET_ALERT = true;

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    public NotificationSettingService(
            NotificationSettingRepository notificationSettingRepository,
            UserRepository userRepository
    ) {
        this.notificationSettingRepository = notificationSettingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationSettingsResponse getSettings(Long userId) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        return toResponse(setting);
    }

    @Transactional
    public NotificationSettingsResponse updateSettings(Long userId, NotificationSettingsUpdateRequest request) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));

        setting.setDailyReminder(request.dailyReminder());
        setting.setTipsEnabled(request.tipsEnabled());
        setting.setBudgetAlert(request.budgetAlert());

        NotificationSetting saved = notificationSettingRepository.save(setting);
        return toResponse(saved);
    }

    private NotificationSetting createDefault(Long userId) {
        NotificationSetting setting = buildDefault(userId);
        return notificationSettingRepository.save(setting);
    }

    private NotificationSetting buildDefault(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        NotificationSetting setting = new NotificationSetting();
        setting.setUser(user);
        setting.setDailyReminder(DEFAULT_DAILY_REMINDER);
        setting.setTipsEnabled(DEFAULT_TIPS_ENABLED);
        setting.setBudgetAlert(DEFAULT_BUDGET_ALERT);
        return setting;
    }

    private NotificationSettingsResponse toResponse(NotificationSetting setting) {
        return new NotificationSettingsResponse(
                setting.getDailyReminder(),
                setting.getTipsEnabled(),
                setting.getBudgetAlert()
        );
    }
}

