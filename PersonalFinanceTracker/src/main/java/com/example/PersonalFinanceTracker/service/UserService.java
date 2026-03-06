package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;

public interface UserService {
    ProfileResponse updateProfile(Long userId, ProfileRequest resquest);
}
