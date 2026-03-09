package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportMonthlyDTO {
    private List<MonthlyChartItem> chart;
    private MonthlySummary summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyChartItem {
        private String month;    // "JAN", "FEB", ...
        private Double income;
        private Double expense;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummary {
        private String month;    // "April 2024"
        private Double income;
        private Double expense;
    }
}
