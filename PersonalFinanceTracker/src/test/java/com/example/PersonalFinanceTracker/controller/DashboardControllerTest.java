package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.DashboardResponseDTO;
import com.example.PersonalFinanceTracker.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    private DashboardResponseDTO dashboardResponse;

    @BeforeEach
    void setUp() {
        List<DashboardResponseDTO.PieChartItem> pieChart = List.of(
                new DashboardResponseDTO.PieChartItem("Salary", new BigDecimal("2000.00"),
                        "INCOME", "💰", "https://cdn.example.com/icons/salary.png"),
                new DashboardResponseDTO.PieChartItem("Food", new BigDecimal("120.00"),
                        "EXPENSE", "🍔", "https://cdn.example.com/icons/food.png")
        );
        List<DashboardResponseDTO.RecentTransaction> recentTransactions = List.of(
                new DashboardResponseDTO.RecentTransaction(101L, "Salary", "💰",
                        "https://cdn.example.com/icons/salary.png",
                        new BigDecimal("2000.00"), "2024-04-24", "INCOME"),
                new DashboardResponseDTO.RecentTransaction(102L, "Food", "🍔",
                        "https://cdn.example.com/icons/food.png",
                        new BigDecimal("-120.00"), "2024-04-24", "EXPENSE")
        );
        dashboardResponse = new DashboardResponseDTO(
                new BigDecimal("2600.00"),
                new BigDecimal("1400.00"),
                new BigDecimal("1200.00"),
                pieChart,
                recentTransactions);
    }

    // ─── GET /api/dashboard ───────────────────────────────────────────────────

    @Test
    void getDashboard_shouldReturn200WithAllFields() throws Exception {
        when(dashboardService.getDashboard(any(), any())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dashboard data fetched successfully"))
                .andExpect(jsonPath("$.data.income").value(2600.0))
                .andExpect(jsonPath("$.data.expenses").value(1400.0))
                .andExpect(jsonPath("$.data.balance").value(1200.0))
                .andExpect(jsonPath("$.data.pieChart").isArray())
                .andExpect(jsonPath("$.data.recentTransactions").isArray());
    }

    @Test
    void getDashboard_withMonthAndYear_shouldPassParamsToService() throws Exception {
        when(dashboardService.getDashboard(4, 2024)).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/dashboard")
                        .param("month", "4")
                        .param("year", "2024"))
                .andExpect(status().isOk());

        verify(dashboardService).getDashboard(4, 2024);
    }

    @Test
    void getDashboard_withoutParams_shouldCallServiceWithNull() throws Exception {
        when(dashboardService.getDashboard(null, null)).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk());

        verify(dashboardService).getDashboard(null, null);
    }

    @Test
    void getDashboard_whenMonthInvalid_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/dashboard").param("month", "13"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getDashboard_pieChart_shouldHaveIconUrl() throws Exception {
        when(dashboardService.getDashboard(any(), any())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(jsonPath("$.data.pieChart[0].iconUrl").isNotEmpty());
    }

    @Test
    void getDashboard_recentTransactions_expenseAmountShouldBeNegative() throws Exception {
        when(dashboardService.getDashboard(any(), any())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(jsonPath("$.data.recentTransactions[1].amount").value(-120.0))
                .andExpect(jsonPath("$.data.recentTransactions[1].type").value("EXPENSE"));
    }
}
