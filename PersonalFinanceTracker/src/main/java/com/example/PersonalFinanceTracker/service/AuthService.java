package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.auth.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}