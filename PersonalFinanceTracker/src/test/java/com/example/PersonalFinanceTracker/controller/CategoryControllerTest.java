package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.CategoryGroupedDTO;
import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.exception.ConflictException;
import com.example.PersonalFinanceTracker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    private CategoryResponseDTO expenseDTO;
    private CategoryResponseDTO incomeDTO;

    @BeforeEach
    void setUp() {
        expenseDTO = new CategoryResponseDTO(1L, "Food", "🍔",
                "https://cdn.example.com/icons/food.png", "EXPENSE");
        incomeDTO = new CategoryResponseDTO(2L, "Salary", "💰",
                "https://cdn.example.com/icons/salary.png", "INCOME");
    }

    // ─── GET /api/categories ──────────────────────────────────────────────────

    @Test
    void getAll_shouldReturn200WithGroupedData() throws Exception {
        when(categoryService.getAll())
                .thenReturn(new CategoryGroupedDTO(List.of(expenseDTO), List.of(incomeDTO)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.EXPENSE[0].name").value("Food"))
                .andExpect(jsonPath("$.data.INCOME[0].name").value("Salary"));
    }

    @Test
    void getByType_whenExpense_shouldReturn200WithFlatList() throws Exception {
        when(categoryService.getByType(any())).thenReturn(List.of(expenseDTO));

        mockMvc.perform(get("/api/categories").param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category list fetched successfully"))
                .andExpect(jsonPath("$.data[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.data[0].iconUrl").isNotEmpty());
    }

    @Test
    void getByType_whenNoCategories_shouldReturn200WithEmptyList() throws Exception {
        when(categoryService.getByType(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/categories").param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No categories found for the given type"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getByType_whenTypeInvalid_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/categories").param("type", "INVALID"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /api/categories ─────────────────────────────────────────────────

    @Test
    void create_shouldReturn201() throws Exception {
        CategoryResponseDTO response = new CategoryResponseDTO(8L, "Transportation",
                "🚗", "https://cdn.example.com/icons/transportation.png", "EXPENSE");
        when(categoryService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Transportation",
                                    "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category created successfully"))
                .andExpect(jsonPath("$.data.id").value(8))
                .andExpect(jsonPath("$.data.name").value("Transportation"))
                .andExpect(jsonPath("$.data.iconUrl").isNotEmpty());
    }

    @Test
    void create_whenNameExists_shouldReturn409() throws Exception {
        when(categoryService.create(any()))
                .thenThrow(new ConflictException("Category name already exists"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Food",
                                    "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category name already exists"));
    }

    @Test
    void create_whenNameBlank_shouldReturn422() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void create_whenMultipleFieldsInvalid_shouldReturn422WithErrorsList() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "type": ""
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_withEmoji_shouldReturn201() throws Exception {
        CategoryResponseDTO response = new CategoryResponseDTO(8L, "Transportation",
                "🚗", "https://cdn.example.com/icons/transportation.png", "EXPENSE");
        when(categoryService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Transportation",
                                    "type": "EXPENSE",
                                    "emoji": "🚗"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.icon").value("🚗"));
    }
}
