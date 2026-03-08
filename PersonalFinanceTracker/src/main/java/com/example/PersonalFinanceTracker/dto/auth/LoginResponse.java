package com.example.PersonalFinanceTracker.dto.auth;

import lombok.Getter;

/**
 * DTO trả về kết quả đăng nhập theo format leader yêu cầu
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "data": {
 *       "accessToken": "...",
 *       "expire": "..."
 *   }
 * }
 */
@Getter
public class LoginResponse {

    private final boolean success;
    private final String message;
    private final TokenData data;

    @Getter
    public static class TokenData {
        private final String accessToken;
        private final String expire;

        public TokenData(String accessToken, String expire) {
            this.accessToken = accessToken;
            this.expire = expire;
        }
    }

    public LoginResponse(boolean success, String message, String accessToken, String expire) {
        this.success = success;
        this.message = message;
        this.data = new TokenData(accessToken, expire);
    }
}
