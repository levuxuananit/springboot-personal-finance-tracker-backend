package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Wrapper response có pagination theo API spec:
 * { "data": [...], "pagination": { "currentPage", "totalPages", "totalItems" } }
 */
@Data
@AllArgsConstructor
public class PagedResponseDTO<T> {

    private List<T> data;
    private PaginationMeta pagination;

    @Data
    @AllArgsConstructor
    public static class PaginationMeta {
        private int currentPage;
        private int totalPages;
        private long totalItems;
    }
}