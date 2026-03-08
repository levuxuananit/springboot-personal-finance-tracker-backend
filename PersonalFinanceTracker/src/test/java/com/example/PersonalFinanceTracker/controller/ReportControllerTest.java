package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import com.example.PersonalFinanceTracker.security.JwtAuthenticationFilter;
import com.example.PersonalFinanceTracker.security.JwtService;
import com.example.PersonalFinanceTracker.service.impl.ReportServiceImp;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportServiceImp reportService;

    @MockBean
    private UserRepository userRepository;

    // mock security
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void testExportPdf_success() throws Exception {

        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(userEntity));

        when(reportService.exportSummaryPdf(any(Long.class), any()))
                .thenReturn("reports/report.pdf");

        mockMvc.perform(post("/api/reports/export/pdf")
                        .contentType("application/json")
                        .content("""
                    {
                      "month":3,
                      "year":2026
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}