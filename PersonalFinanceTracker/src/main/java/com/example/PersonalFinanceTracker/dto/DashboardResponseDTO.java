package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal balance;
    private List<PieChartItem> pieChart;
    private List<RecentTransaction> recentTransactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieChartItem {
        private String category;
        private BigDecimal amount;
        private String type;
        private String icon;
        private String iconUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransaction {
        private Long id;
        private String category;
        private String icon;
        private String iconUrl;
        private BigDecimal amount;
        private String date;
        private String type;
    }
}
