package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.ReportCategoryDTO;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReportService reportService;

    private ReportCategoryDTO categoryReport;
    private ReportMonthlyDTO monthlyReport;

    @BeforeEach
    void setUp() {
        List<ReportCategoryDTO.CategoryItem> items = List.of(
                new ReportCategoryDTO.CategoryItem("Housing", 700.0, 50.0),
                new ReportCategoryDTO.CategoryItem("Food",    420.0, 30.0),
                new ReportCategoryDTO.CategoryItem("Shopping",280.0, 20.0)
        );
        categoryReport = new ReportCategoryDTO("EXPENSE", 1400.0, items);

        List<ReportMonthlyDTO.MonthlyChartItem> chart = List.of(
                new ReportMonthlyDTO.MonthlyChartItem("JAN", 1200.0, 1500.0),
                new ReportMonthlyDTO.MonthlyChartItem("APR", 3500.0, 2400.0)
        );
        ReportMonthlyDTO.MonthlySummary summary =
                new ReportMonthlyDTO.MonthlySummary("April 2024", 3500.0, 2400.0);
        monthlyReport = new ReportMonthlyDTO(chart, summary);
    }

    // ─── GET /api/reports/category ────────────────────────────────────────────

    @Test
    void getCategoryReport_shouldReturn200WithAllFields() throws Exception {
        when(reportService.getCategoryReport(eq("EXPENSE"), eq(4), eq(2024)))
                .thenReturn(categoryReport);

        mockMvc.perform(get("/api/reports/category")
                        .param("type", "EXPENSE")
                        .param("month", "4")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category breakdown fetched successfully"))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"))
                .andExpect(jsonPath("$.data.total").value(1400.0))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories.length()").value(3));
    }

    @Test
    void getCategoryReport_shouldPassParamsToService() throws Exception {
        when(reportService.getCategoryReport(any(), any(), any()))
                .thenReturn(categoryReport);

        mockMvc.perform(get("/api/reports/category")
                        .param("type", "INCOME")
                        .param("month", "3")
                        .param("year", "2024"))
                .andExpect(status().isOk());

        verify(reportService).getCategoryReport("INCOME", 3, 2024);
    }

    @Test
    void getCategoryReport_categoriesSortedDescByAmount() throws Exception {
        when(reportService.getCategoryReport(any(), any(), any()))
                .thenReturn(categoryReport);

        mockMvc.perform(get("/api/reports/category")
                        .param("type", "EXPENSE"))
                .andExpect(jsonPath("$.data.categories[0].category").value("Housing"))
                .andExpect(jsonPath("$.data.categories[0].percentage").value(50.0))
                .andExpect(jsonPath("$.data.categories[1].category").value("Food"))
                .andExpect(jsonPath("$.data.categories[2].category").value("Shopping"));
    }

    @Test
    void getCategoryReport_whenTypeNull_shouldReturn422() throws Exception {
        when(reportService.getCategoryReport(isNull(), any(), any()))
                .thenThrow(new UnprocessableException("type is required (INCOME or EXPENSE)"));

        mockMvc.perform(get("/api/reports/category"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getCategoryReport_whenTypeInvalid_shouldReturn422() throws Exception {
        when(reportService.getCategoryReport(eq("INVALID"), any(), any()))
                .thenThrow(new UnprocessableException("type must be INCOME or EXPENSE"));

        mockMvc.perform(get("/api/reports/category")
                        .param("type", "INVALID"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("type must be INCOME or EXPENSE"));
    }

    @Test
    void getCategoryReport_whenNoTransactions_shouldReturn200WithEmptyList() throws Exception {
        ReportCategoryDTO empty = new ReportCategoryDTO("EXPENSE", 0.0, List.of());
        when(reportService.getCategoryReport(any(), any(), any())).thenReturn(empty);

        mockMvc.perform(get("/api/reports/category").param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0.0))
                .andExpect(jsonPath("$.data.categories").isEmpty());
    }

    // ─── GET /api/reports/monthly ─────────────────────────────────────────────

    @Test
    void getMonthlyReport_shouldReturn200WithAllFields() throws Exception {
        when(reportService.getMonthlyReport(eq(2024), eq(4))).thenReturn(monthlyReport);

        mockMvc.perform(get("/api/reports/monthly")
                        .param("year", "2024")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Monthly financial report fetched successfully"))
                .andExpect(jsonPath("$.data.chart").isArray())
                .andExpect(jsonPath("$.data.chart.length()").value(2))
                .andExpect(jsonPath("$.data.summary.month").value("April 2024"))
                .andExpect(jsonPath("$.data.summary.income").value(3500.0))
                .andExpect(jsonPath("$.data.summary.expense").value(2400.0));
    }

    @Test
    void getMonthlyReport_shouldPassParamsToService() throws Exception {
        when(reportService.getMonthlyReport(any(), any())).thenReturn(monthlyReport);

        mockMvc.perform(get("/api/reports/monthly")
                        .param("year", "2024")
                        .param("month", "4"))
                .andExpect(status().isOk());

        verify(reportService).getMonthlyReport(2024, 4);
    }

    @Test
    void getMonthlyReport_chartShouldHaveMonthLabel() throws Exception {
        when(reportService.getMonthlyReport(any(), any())).thenReturn(monthlyReport);

        mockMvc.perform(get("/api/reports/monthly").param("year", "2024"))
                .andExpect(jsonPath("$.data.chart[0].month").value("JAN"))
                .andExpect(jsonPath("$.data.chart[0].income").value(1200.0))
                .andExpect(jsonPath("$.data.chart[0].expense").value(1500.0));
    }

    @Test
    void getMonthlyReport_whenYearNull_shouldReturn422() throws Exception {
        when(reportService.getMonthlyReport(isNull(), any()))
                .thenThrow(new UnprocessableException("year is required"));

        mockMvc.perform(get("/api/reports/monthly"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("year is required"));
    }

    @Test
    void getMonthlyReport_whenMonthNotProvided_shouldStillReturn200() throws Exception {
        when(reportService.getMonthlyReport(eq(2024), isNull())).thenReturn(monthlyReport);

        mockMvc.perform(get("/api/reports/monthly").param("year", "2024"))
                .andExpect(status().isOk());

        verify(reportService).getMonthlyReport(2024, null);
    }
}