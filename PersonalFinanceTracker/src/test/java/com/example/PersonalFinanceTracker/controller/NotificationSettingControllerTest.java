package com.example.PersonalFinanceTracker.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.PersonalFinanceTracker.dto.response.NotificationSettingsResponse;
import com.example.PersonalFinanceTracker.exception.GlobalExceptionHandler;
import com.example.PersonalFinanceTracker.service.NotificationSettingService;

class NotificationSettingControllerTest {

    private final NotificationSettingService service = org.mockito.Mockito.mock(NotificationSettingService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new NotificationSettingController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void getSettings_shouldReturnWrappedResponse() throws Exception {
        when(service.getSettings(1L)).thenReturn(new NotificationSettingsResponse(true, false, true));

        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(get("/api/notifications/settings").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyReminder").value(true))
                .andExpect(jsonPath("$.data.tipsEnabled").value(false))
                .andExpect(jsonPath("$.data.budgetAlert").value(true));
    }

    @Test
    void updateSettings_shouldReturnWrappedResponse() throws Exception {
        when(service.updateSettings(1L, new com.example.PersonalFinanceTracker.dto.request.NotificationSettingsUpdateRequest(false, true, false)))
                .thenReturn(new NotificationSettingsResponse(false, true, false));

        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(
                        put("/api/notifications/settings")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"dailyReminder":false,"tipsEnabled":true,"budgetAlert":false}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyReminder").value(false));
    }

    @Test
    void updateSettings_shouldReturn422_whenValidationFails() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(
                        put("/api/notifications/settings")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"dailyReminder":true,"tipsEnabled":false}
                                        """)
                )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("budgetAlert"));
    }
}

