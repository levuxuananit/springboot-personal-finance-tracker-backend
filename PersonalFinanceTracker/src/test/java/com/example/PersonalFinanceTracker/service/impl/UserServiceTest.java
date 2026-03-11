package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ProfileServiceImpl userService;

    private User user;
    private ProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(123L);
        user.setFullName("Old Name");
        user.setEmail("user@example.com");
        user.setAvatar("https://cdn.example.com/avatars/old.png");

        profileRequest = new ProfileRequest();
        profileRequest.setFullName("Nguyễn Văn A");
        profileRequest.setAvatar("https://cdn.example.com/avatars/user123.png");
    }

    // Update Profile By Email

    @Test
    void updateProfileByEmail_whenValid_shouldUpdateAndReturn() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = userService.updateProfileByEmail("user@example.com", profileRequest);

        assertEquals(123L, result.getUseId());
        assertEquals("Nguyễn Văn A", result.getFullName());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("https://cdn.example.com/avatars/user123.png", result.getAvatar());

        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository).save(user);
        assertEquals("Nguyễn Văn A", user.getFullName());
        assertEquals("https://cdn.example.com/avatars/user123.png", user.getAvatar());
    }

    @Test
    void updateProfileByEmail_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateProfileByEmail("notfound@example.com", profileRequest));

        verify(userRepository).findByEmail("notfound@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileByEmail_whenAvatarNull_shouldUpdateOnlyFullName() {
        profileRequest.setAvatar(null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = userService.updateProfileByEmail("user@example.com", profileRequest);

        assertEquals("Nguyễn Văn A", result.getFullName());
        assertNull(result.getAvatar());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfileByEmail_shouldPersistChangesToDatabase() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfileByEmail("user@example.com", profileRequest);

        verify(userRepository, times(1)).save(user);
        assertEquals("Nguyễn Văn A", user.getFullName());
        assertEquals("https://cdn.example.com/avatars/user123.png", user.getAvatar());
    }

    @Test
    void updateProfileByEmail_shouldReturnCorrectResponseStructure() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = userService.updateProfileByEmail("user@example.com", profileRequest);

        assertNotNull(result);
        assertNotNull(result.getUseId());
        assertNotNull(result.getFullName());
        assertNotNull(result.getEmail());
        assertNotNull(result.getAvatar());
    }

    @Test
    void updateProfileByEmail_whenMultipleUpdates_shouldKeepLatestValues() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // First update
        ProfileRequest firstRequest = new ProfileRequest();
        firstRequest.setFullName("First Name");
        firstRequest.setAvatar("https://cdn.example.com/avatars/first.png");
        userService.updateProfileByEmail("user@example.com", firstRequest);

        // Second update
        ProfileRequest secondRequest = new ProfileRequest();
        secondRequest.setFullName("Second Name");
        secondRequest.setAvatar("https://cdn.example.com/avatars/second.png");
        ProfileResponse result = userService.updateProfileByEmail("user@example.com", secondRequest);

        assertEquals("Second Name", result.getFullName());
        assertEquals("https://cdn.example.com/avatars/second.png", result.getAvatar());
        verify(userRepository, times(2)).save(user);
    }

    @Test
    void updateProfileByEmail_shouldNotChangeEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = userService.updateProfileByEmail("user@example.com", profileRequest);

        assertEquals("user@example.com", result.getEmail());
        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    void updateProfileByEmail_shouldNotChangeUserId() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = userService.updateProfileByEmail("user@example.com", profileRequest);

        assertEquals(123L, result.getUseId());
        assertEquals(123L, user.getId());
    }
}
