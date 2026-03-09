package com.example.PersonalFinanceTracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Response khi GET /api/categories không có ?type
 * JSON keys phải là "EXPENSE" và "INCOME" (uppercase) theo API spec
 */
@Getter
@AllArgsConstructor
public class CategoryGroupedDTO {

    @JsonProperty("EXPENSE")
    private List<CategoryResponseDTO> expense;

    @JsonProperty("INCOME")
    private List<CategoryResponseDTO> income;
}
