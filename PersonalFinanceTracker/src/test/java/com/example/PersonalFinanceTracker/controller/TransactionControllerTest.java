package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.dto.PagedResponseDTO;
import com.example.PersonalFinanceTracker.dto.TransactionResponseDTO;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionService transactionService;

    private TransactionResponseDTO transactionResponse;

    @BeforeEach
    void setUp() {
        CategoryResponseDTO category = new CategoryResponseDTO(3L, "Housing", "🏠",
                "https://cdn.example.com/icons/housing.png", "EXPENSE");

        transactionResponse = new TransactionResponseDTO();
        transactionResponse.setId(101L);
        transactionResponse.setAmount(new BigDecimal("400.00"));
        transactionResponse.setNote("Paid monthly rent");
        transactionResponse.setCategory(category);
        transactionResponse.setDate("2024-04-28");
    }

    // ─── POST /api/transactions ───────────────────────────────────────────────

    @Test
    void create_shouldReturn201() throws Exception {
        when(transactionService.create(any())).thenReturn(transactionResponse);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 400.00,
                                    "note": "Paid monthly rent",
                                    "categoryId": 3,
                                    "date": "2024-04-28"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction added successfully"))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.amount").value(400.0))
                .andExpect(jsonPath("$.data.category.name").value("Housing"))
                .andExpect(jsonPath("$.data.category.iconUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.date").value("2024-04-28"));
    }

    @Test
    void create_whenAmountMissing_shouldReturn422() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "note": "rent",
                                    "categoryId": 3,
                                    "date": "2024-04-28"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("amount"));
    }

    @Test
    void create_whenAmountIsZero_shouldReturn422() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 0,
                                    "categoryId": 3,
                                    "date": "2024-04-28"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("amount"));
    }

    @Test
    void create_whenDateFormatWrong_shouldReturn422() throws Exception {
        when(transactionService.create(any()))
                .thenThrow(new UnprocessableException("Invalid date format. Expected yyyy-MM-dd"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100.00,
                                    "categoryId": 1,
                                    "date": "28-04-2024"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_whenCategoryNotFound_shouldReturn404() throws Exception {
        when(transactionService.create(any()))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100.00,
                                    "categoryId": 999,
                                    "date": "2024-04-28"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    // ─── GET /api/transactions/history ───────────────────────────────────────

    @Test
    void getHistory_shouldReturn200WithPagination() throws Exception {
        PagedResponseDTO.PaginationMeta meta =
                new PagedResponseDTO.PaginationMeta(1, 5, 50);
        PagedResponseDTO<TransactionResponseDTO> paged =
                new PagedResponseDTO<>(List.of(transactionResponse), meta);

        when(transactionService.getHistory(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/transactions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction history fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].category.iconUrl").isNotEmpty())
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.totalPages").value(5))
                .andExpect(jsonPath("$.pagination.totalItems").value(50));
    }

    @Test
    void getHistory_withDateRangeAndType_shouldReturn200() throws Exception {
        PagedResponseDTO<TransactionResponseDTO> paged =
                new PagedResponseDTO<>(List.of(transactionResponse),
                        new PagedResponseDTO.PaginationMeta(1, 1, 1));

        when(transactionService.getHistory(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/transactions/history")
                        .param("startDate", "2024-04-01")
                        .param("endDate", "2024-04-30")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getHistory_whenStartDateAfterEndDate_shouldReturn422() throws Exception {
        when(transactionService.getHistory(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new UnprocessableException("startDate must not be after endDate"));

        mockMvc.perform(get("/api/transactions/history")
                        .param("startDate", "2024-04-30")
                        .param("endDate", "2024-04-01"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getHistory_withCustomPageAndSize_shouldReturn200() throws Exception {
        PagedResponseDTO<TransactionResponseDTO> paged =
                new PagedResponseDTO<>(List.of(),
                        new PagedResponseDTO.PaginationMeta(2, 10, 100));

        when(transactionService.getHistory(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/transactions/history")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.currentPage").value(2));
    }
}
