package com.example.PersonalFinanceTracker.dto.report;

import lombok.Data;

@Data
public class ExportReportRequest {
    private int month;
    private int year;
    private boolean includeChart;
    private boolean includeTopExpenses;
    private String reportType; // SUMMARY

}
