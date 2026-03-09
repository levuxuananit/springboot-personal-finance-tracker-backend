package com.example.PersonalFinanceTracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String note;
    private CategoryResponseForTransactionDTO category;
    private String date;
}
