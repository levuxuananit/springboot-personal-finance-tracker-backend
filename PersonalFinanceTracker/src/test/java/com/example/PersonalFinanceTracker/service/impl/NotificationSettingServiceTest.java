package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.request.NotificationSettingsUpdateRequest;
import com.example.PersonalFinanceTracker.entity.NotificationSetting;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.repository.NotificationSettingRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationSettingServiceTest {

    private final NotificationSettingRepository notificationSettingRepository = mock(NotificationSettingRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final NotificationSettingService service = new NotificationSettingService(notificationSettingRepository, userRepository);

    @Test
    void getSettings_shouldCreateDefaultAndPersist_whenMissing() {
        User user = new User();
        user.setId(1L);

        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.save(any(NotificationSetting.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.getSettings(1L);

        assertTrue(res.dailyReminder());
        assertFalse(res.tipsEnabled());
        assertTrue(res.budgetAlert());
        verify(notificationSettingRepository).save(any(NotificationSetting.class));
    }

    @Test
    void getSettings_shouldReturnExisting_whenPresent() {
        NotificationSetting setting = new NotificationSetting();
        setting.setDailyReminder(false);
        setting.setTipsEnabled(true);
        setting.setBudgetAlert(false);

        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        var res = service.getSettings(1L);
        assertFalse(res.dailyReminder());
        assertTrue(res.tipsEnabled());
        assertFalse(res.budgetAlert());
        verify(notificationSettingRepository, never()).save(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void getSettings_shouldThrowNotFound_whenUserMissingAndNoSettings() {
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSettings(1L));
        verify(notificationSettingRepository, never()).save(any());
    }

    @Test
    void updateSettings_shouldCreateDefaultThenUpdate_whenMissing() {
        User user = new User();
        user.setId(1L);

        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationSettingRepository.save(any(NotificationSetting.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new NotificationSettingsUpdateRequest(false, true, false);
        var res = service.updateSettings(1L, req);

        assertFalse(res.dailyReminder());
        assertTrue(res.tipsEnabled());
        assertFalse(res.budgetAlert());
        verify(notificationSettingRepository, atLeastOnce()).save(any(NotificationSetting.class));
    }

    @Test
    void updateSettings_shouldUpdateExisting_whenPresent() {
        NotificationSetting setting = new NotificationSetting();
        setting.setDailyReminder(true);
        setting.setTipsEnabled(false);
        setting.setBudgetAlert(true);

        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));
        when(notificationSettingRepository.save(any(NotificationSetting.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new NotificationSettingsUpdateRequest(false, true, false);
        var res = service.updateSettings(1L, req);

        assertFalse(res.dailyReminder());
        assertTrue(res.tipsEnabled());
        assertFalse(res.budgetAlert());
        verifyNoInteractions(userRepository);
    }
}

