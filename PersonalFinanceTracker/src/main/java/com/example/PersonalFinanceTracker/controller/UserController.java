package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.*;
import com.example.PersonalFinanceTracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileRequest request
    ) {

        String email = authentication.getName();

        ProfileResponse response = userService.updateProfileByEmail(email, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User profile updated successfully",
                        response
                )
        );
    }
}