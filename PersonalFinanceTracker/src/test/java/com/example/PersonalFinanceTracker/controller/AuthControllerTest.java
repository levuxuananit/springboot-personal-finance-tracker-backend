package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.auth.LoginRequest;
import com.example.PersonalFinanceTracker.dto.auth.LoginResponse;
import com.example.PersonalFinanceTracker.dto.auth.RegisterRequest;
import com.example.PersonalFinanceTracker.dto.auth.RegisterResponse;
import com.example.PersonalFinanceTracker.exception.ApiException;
import com.example.PersonalFinanceTracker.exception.UnauthorizedException;
import com.example.PersonalFinanceTracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for AuthController
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

    // =================================================
    // REGISTER TESTS
    // =================================================

    @Test
    @DisplayName("Register success")
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

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    @DisplayName("Register fail when email already exists")
    void register_email_exists() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("user@example.com");
        request.setPassword("abc12345");

        doThrow(new ApiException(HttpStatus.CONFLICT, "Email is already registered"))
                .when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Register fail when email format invalid")
    void register_invalid_email() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("invalid_email");
        request.setPassword("abc12345");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Register fail when password too short")
    void register_password_too_short() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("user@example.com");
        request.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Register fail when email missing")
    void register_missing_email() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setPassword("abc12345");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    // =================================================
    // LOGIN TESTS
    // =================================================

    @Test
    @DisplayName("Login success")
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

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt_token_123"));
    }

    @Test
    @DisplayName("Login fail when wrong password")
    void login_wrong_password() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong123");

        doThrow(new UnauthorizedException("Invalid email or password"))
                .when(authService).login(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login fail when email not found")
    void login_email_not_found() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("abc12345");

        doThrow(new UnauthorizedException("Invalid email or password"))
                .when(authService).login(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login fail when password missing")
    void login_missing_password() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Login fail when request body empty")
    void login_empty_body() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
