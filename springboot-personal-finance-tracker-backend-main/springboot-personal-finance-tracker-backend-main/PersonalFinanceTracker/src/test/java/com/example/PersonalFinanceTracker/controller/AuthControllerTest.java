package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.auth.LoginRequest;
import com.example.PersonalFinanceTracker.dto.auth.LoginResponse;
import com.example.PersonalFinanceTracker.dto.auth.RegisterRequest;
import com.example.PersonalFinanceTracker.dto.auth.RegisterResponse;
import com.example.PersonalFinanceTracker.exception.ApiException;
import com.example.PersonalFinanceTracker.exception.UnauthorizedException;
import com.example.PersonalFinanceTracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test Controller Auth (register + login)
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com.example.PersonalFinanceTracker.security.*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test đăng ký thành công
     */
    @Test
    void register_success() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("user@example.com");
        request.setPassword("abc12345");

        RegisterResponse response = new RegisterResponse(
                true,
                "Registration successful",
                new RegisterResponse.RegisterData(
                        1L,
                        "Nguyen Van A",
                        "user@example.com"
                )
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    /**
     * Test đăng ký thất bại do email đã tồn tại
     */
    @Test
    void register_fail_email_exists() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("user@example.com");
        request.setPassword("abc12345");

        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "Email already exists"))
                .when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test login thành công
     */
    @Test
    void login_success() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("abc12345");

        LoginResponse response = new LoginResponse(
                true,
                "Login successful",
                "jwt_token_123",
                "2025-05-12T15:30:00Z"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt_token_123"));
    }

    /**
     * Test login sai mật khẩu
     */
    @Test
    void login_wrong_password() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong123");

        doThrow(new UnauthorizedException("Wrong password"))
                .when(authService).login(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}