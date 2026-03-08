package com.example.PersonalFinanceTracker.service.impl;

import com.example.PersonalFinanceTracker.dto.report.ExportReportRequest;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.service.impl.ReportServiceImp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportServiceImpTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportServiceImp reportService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExportSummaryPdf_success() throws Exception {

        Long userId = 1L;

        ExportReportRequest request = new ExportReportRequest();
        request.setMonth(3);
        request.setYear(2026);

        when(transactionRepository.getTotalIncome(userId, 3, 2026))
                .thenReturn(5000.0);

        when(transactionRepository.getTotalExpense(userId, 3, 2026))
                .thenReturn(2000.0);

        when(transactionRepository.getTotalIncome(userId, 2, 2026))
                .thenReturn(4000.0);

        when(transactionRepository.getTotalExpense(userId, 2, 2026))
                .thenReturn(1500.0);

        List<Object[]> topExpenses = List.of(
                new Object[]{"Food", 800.0},
                new Object[]{"Shopping", 500.0},
                new Object[]{"Transport", 300.0}
        );

        when(transactionRepository.getTopExpenses(eq(userId), eq(3), eq(2026), any()))
                .thenReturn(topExpenses);

        String result = reportService.exportSummaryPdf(userId, request);

        assertNotNull(result);
        assertTrue(result.contains("reports"));

        File file = new File(result);
        assertTrue(file.exists());

        verify(transactionRepository).getTotalIncome(userId, 3, 2026);
        verify(transactionRepository).getTotalExpense(userId, 3, 2026);
        verify(transactionRepository).getTopExpenses(eq(userId), eq(3), eq(2026), any());
    }

    @Test
    void testExportSummaryPdf_noExpense() throws Exception {

        Long userId = 1L;

        ExportReportRequest request = new ExportReportRequest();
        request.setMonth(3);
        request.setYear(2026);

        when(transactionRepository.getTotalIncome(userId, 3, 2026))
                .thenReturn(5000.0);

        when(transactionRepository.getTotalExpense(userId, 3, 2026))
                .thenReturn(0.0);

        when(transactionRepository.getTopExpenses(eq(userId), eq(3), eq(2026), any()))
                .thenReturn(List.of());

        String result = reportService.exportSummaryPdf(userId, request);

        assertNotNull(result);
    }

    @Test
    void testExportSummaryPdf_nullValues() throws Exception {

        Long userId = 1L;

        ExportReportRequest request = new ExportReportRequest();
        request.setMonth(3);
        request.setYear(2026);

        when(transactionRepository.getTotalIncome(userId, 3, 2026))
                .thenReturn(null);

        when(transactionRepository.getTotalExpense(userId, 3, 2026))
                .thenReturn(null);

        when(transactionRepository.getTopExpenses(eq(userId), eq(3), eq(2026), any()))
                .thenReturn(List.of());

        String result = reportService.exportSummaryPdf(userId, request);

        assertNotNull(result);
    }
}