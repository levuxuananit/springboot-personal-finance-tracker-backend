package com.example.PersonalFinanceTracker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response trả về sau khi login / register thành công
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private Object data;
}