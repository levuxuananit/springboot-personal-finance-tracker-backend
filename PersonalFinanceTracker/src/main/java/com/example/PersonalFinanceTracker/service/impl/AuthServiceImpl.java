package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.auth.*;
import com.example.PersonalFinanceTracker.entity.Role;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ApiException;
import com.example.PersonalFinanceTracker.exception.UnauthorizedException;
import com.example.PersonalFinanceTracker.repository.RoleRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import com.example.PersonalFinanceTracker.security.JwtService;
import com.example.PersonalFinanceTracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Service xử lý đăng ký & đăng nhập + JWT
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // chỉ dùng để gán role mặc định USER
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Đăng ký tài khoản + trả JWT
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {

        // 1. Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        // 2. Lấy role USER mặc định
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        return new RegisterResponse(
                true,
                "Registration successful",
                new RegisterResponse.RegisterData(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail()
                )
        );
    }

    /**
     * Đăng nhập + trả JWT
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password")
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        String expire = jwtService.getExpireTime(token);

        return new LoginResponse(
                true,
                "Login successful",
                token,
                expire
        );
    }
}