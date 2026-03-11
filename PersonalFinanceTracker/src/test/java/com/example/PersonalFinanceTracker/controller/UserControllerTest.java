package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.ProfileRequest;
import com.example.PersonalFinanceTracker.dto.ProfileResponse;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user@example.com")
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    private ProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        profileResponse = new ProfileResponse(
                123L,
                "Nguyễn Văn A",
                "user@example.com",
                "https://cdn.example.com/avatars/user123.png"
        );
    }

    // PUT /api/user/profile

    @Test
    void updateProfile_shouldReturn200WithUpdatedData() throws Exception {
        when(userService.updateProfileByEmail(eq("user@example.com"), any(ProfileRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Nguyễn Văn A",
                                    "avatar": "https://cdn.example.com/avatars/user123.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User profile updated successfully"))
                .andExpect(jsonPath("$.data.useId").value(123))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.avatar").value("https://cdn.example.com/avatars/user123.png"));
    }

    @Test
    void updateProfile_whenFullNameBlank_shouldReturn422() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "",
                                    "avatar": "https://cdn.example.com/avatars/user123.png"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("fullName"))
                .andExpect(jsonPath("$.errors[0].message").value("Full name is required"));
    }

    @Test
    void updateProfile_whenFullNameNull_shouldReturn422() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "avatar": "https://cdn.example.com/avatars/user123.png"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("fullName"));
    }

    @Test
    void updateProfile_whenAvatarInvalidUrl_shouldReturn422() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Nguyễn Văn A",
                                    "avatar": "invalid-url"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("avatar"))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid avatar URL"));
    }

    @Test
    void updateProfile_whenMultipleFieldsInvalid_shouldReturn422WithErrorsList() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "",
                                    "avatar": "invalid-url"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void updateProfile_whenAvatarNull_shouldReturn200() throws Exception {
        ProfileResponse responseWithoutAvatar = new ProfileResponse(
                123L,
                "Nguyễn Văn A",
                "user@example.com",
                null
        );

        when(userService.updateProfileByEmail(eq("user@example.com"), any(ProfileRequest.class)))
                .thenReturn(responseWithoutAvatar);

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Nguyễn Văn A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Văn A"));
    }

    @Test
    @WithMockUser(username = "notfound@example.com")
    void updateProfile_whenUserNotFound_shouldReturn404() throws Exception {
        when(userService.updateProfileByEmail(eq("notfound@example.com"), any(ProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Nguyễn Văn A",
                                    "avatar": "https://cdn.example.com/avatars/user123.png"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
