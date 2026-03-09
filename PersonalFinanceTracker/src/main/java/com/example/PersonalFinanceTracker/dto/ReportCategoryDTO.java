package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportCategoryDTO {
    private String type;
    private Double total;
    private List<CategoryItem> categories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {
        private String category;
        private Double amount;
        private Double percentage;
    }
}
