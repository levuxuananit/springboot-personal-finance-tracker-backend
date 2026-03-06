package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;
import com.example.PersonalFinanceTracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public ProfileResponse updateProfile(
            @RequestParam Long userId,
            @Valid @RequestBody ProfileRequest request
    ) {
        return userService.updateProfile(userId, request);
    }
}
