package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import com.example.PersonalFinanceTracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public ProfileResponse updateProfileByEmail(String email, ProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setAvatar(request.getAvatar());

        userRepository.save(user);

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar()
        );
    }
}