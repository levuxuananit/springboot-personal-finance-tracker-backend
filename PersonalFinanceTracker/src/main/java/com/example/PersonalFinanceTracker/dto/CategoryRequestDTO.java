package com.example.PersonalFinanceTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Category type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Category type must be INCOME or EXPENSE")
    private String type;

    private String emoji;
}
