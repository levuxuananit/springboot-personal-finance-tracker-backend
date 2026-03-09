package com.example.PersonalFinanceTracker.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.PersonalFinanceTracker.dto.response.BudgetResponse;
import com.example.PersonalFinanceTracker.exception.GlobalExceptionHandler;
import com.example.PersonalFinanceTracker.service.BudgetService;

class BudgetControllerTest {

    private final BudgetService budgetService = org.mockito.Mockito.mock(BudgetService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new BudgetController(budgetService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void setBudget_shouldReturnWrappedResponse() throws Exception {
        when(budgetService.setBudget(1L, new com.example.PersonalFinanceTracker.dto.request.BudgetCreateRequest(100.0, 3, 2026, 10L)))
                .thenReturn(new BudgetResponse(99L, 100.0, 3, 2026, 10L, 1L));

        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(
                        post("/api/budgets")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"amount":100.0,"month":3,"year":2026,"categoryId":10}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void setBudget_shouldReturn422_whenValidationFails() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(
                        post("/api/budgets")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"amount":100.0,"month":3,"year":2026}
                                        """)
                )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("categoryId"));
    }

    @Test
    void getAll_shouldReturnWrappedList() throws Exception {
        when(budgetService.getAllForUser(1L)).thenReturn(List.of(
                new BudgetResponse(1L, 100.0, 3, 2026, 10L, 1L),
                new BudgetResponse(2L, 200.0, 4, 2026, 11L, 1L)
        ));
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        mockMvc.perform(get("/api/budgets").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
}

