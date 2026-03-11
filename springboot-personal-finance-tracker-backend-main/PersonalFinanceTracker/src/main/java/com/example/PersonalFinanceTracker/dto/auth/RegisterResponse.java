package com.example.PersonalFinanceTracker.dto.auth;

import lombok.Getter;

/**
 * Response trả về khi đăng ký thành công
 */
@Getter
public class RegisterResponse {

    private final boolean success;
    private final String message;
    private final RegisterData data;

    @Getter
    public static class RegisterData {
        private final Long userId;
        private final String fullName;
        private final String email;

        public RegisterData(Long userId, String fullName, String email) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
        }
    }

    public RegisterResponse(boolean success, String message, RegisterData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}