package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryResponseForTransactionDTO {
    private Long id;
    private String name;
    private String icon;
    private String iconUrl;
    private String type;
}