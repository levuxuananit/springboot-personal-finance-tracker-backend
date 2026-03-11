package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;
import com.example.PersonalFinanceTracker.security.JwtAuthenticationEntryPoint;
import com.example.PersonalFinanceTracker.security.JwtAuthenticationFilter;
import com.example.PersonalFinanceTracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // mock security dependencies để Spring context load được
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= SUCCESS TEST =================
    @Test
    @WithMockUser(username = "user@example.com")
    void updateProfile_success() throws Exception {

        ProfileRequest request = new ProfileRequest();
        request.setFullName("Nguyễn Văn A");
        request.setAvatar("https://cdn.example.com/avatars/user123.png");

        ProfileResponse response =
                new ProfileResponse(
                        123L,
                        "Nguyễn Văn A",
                        "user@example.com",
                        "https://cdn.example.com/avatars/user123.png"
                );

        Mockito.when(
                userService.updateProfileByEmail(
                        Mockito.eq("user@example.com"),
                        Mockito.any(ProfileRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/api/user/profile")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(123))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    // ================= VALIDATION TEST =================
    @Test
    @WithMockUser(username = "user@example.com")
    void updateProfile_validationError() throws Exception {

        ProfileRequest request = new ProfileRequest();
        request.setFullName("");       // invalid: NotBlank
        request.setAvatar("abc");      // invalid: Pattern

        mockMvc.perform(
                        put("/api/user/profile")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    // ================= UNAUTHORIZED TEST =================
    @Test
    void updateProfile_unauthorized() throws Exception {

        ProfileRequest request = new ProfileRequest();
        request.setFullName("Nguyễn Văn A");
        request.setAvatar("https://cdn.example.com/avatar.png");

        mockMvc.perform(
                        put("/api/user/profile")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }
}