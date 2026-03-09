package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.ReportCategoryDTO;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO;
import com.example.PersonalFinanceTracker.entity.*;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import com.example.PersonalFinanceTracker.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AuthUtil authUtil;
    @InjectMocks ReportService reportService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.defaultUser();
        expenseCategory = TestDataFactory.expenseCategory(user);
        incomeCategory  = TestDataFactory.incomeCategory(user);
        when(authUtil.getCurrentUser()).thenReturn(user);
    }

    // ─── getCategoryReport ────────────────────────────────────────────────────

    @Test
    void getCategoryReport_whenExpense_shouldReturnGroupedByCategory() {
        Transaction t1 = TestDataFactory.buildTransaction(1L, user, expenseCategory,
                new BigDecimal("700.00"), LocalDate.of(2024, 4, 10));
        Transaction t2 = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("420.00"), LocalDate.of(2024, 4, 15));

        when(transactionRepository.findByUserTypeAndDateBetween(
                eq(user.getId()), eq(CategoryType.EXPENSE), any(), any()))
                .thenReturn(List.of(t1, t2));

        ReportCategoryDTO result = reportService.getCategoryReport("EXPENSE", 4, 2024);

        assertEquals("EXPENSE", result.getType());
        assertEquals(1120.0, result.getTotal());
        assertEquals(1, result.getCategories().size());
        assertEquals(100.0, result.getCategories().get(0).getPercentage());
    }

    @Test
    void getCategoryReport_whenMultipleCategories_shouldCalculatePercentageCorrectly() {
        // Housing: 700, Food: 420, Shopping: 280 → total 1400
        CategoryIcon housingIcon = TestDataFactory.buildCategoryIcon(
                3L, "Housing", "🏠", "https://cdn.example.com/icons/housing.png");
        CategoryIcon shoppingIcon = TestDataFactory.buildCategoryIcon(
                4L, "Shopping", "🛍", "https://cdn.example.com/icons/shopping.png");

        Category housingCategory = TestDataFactory.buildCategory(3L, user, housingIcon, CategoryType.EXPENSE);
        Category shoppingCategory = TestDataFactory.buildCategory(4L, user, shoppingIcon, CategoryType.EXPENSE);

        Transaction t1 = TestDataFactory.buildTransaction(1L, user, housingCategory,
                new BigDecimal("700.00"), LocalDate.of(2024, 4, 1));
        Transaction t2 = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("420.00"), LocalDate.of(2024, 4, 2));
        Transaction t3 = TestDataFactory.buildTransaction(3L, user, shoppingCategory,
                new BigDecimal("280.00"), LocalDate.of(2024, 4, 3));

        when(transactionRepository.findByUserTypeAndDateBetween(
                eq(user.getId()), eq(CategoryType.EXPENSE), any(), any()))
                .thenReturn(List.of(t1, t2, t3));

        ReportCategoryDTO result = reportService.getCategoryReport("EXPENSE", 4, 2024);

        assertEquals(1400.0, result.getTotal());
        assertEquals(3, result.getCategories().size());

        // Sắp xếp DESC → Housing 50%, Food 30%, Shopping 20%
        assertEquals("Housing",  result.getCategories().get(0).getCategory());
        assertEquals(50.0,       result.getCategories().get(0).getPercentage());
        assertEquals("Food",     result.getCategories().get(1).getCategory());
        assertEquals(30.0,       result.getCategories().get(1).getPercentage());
        assertEquals("Shopping", result.getCategories().get(2).getCategory());
        assertEquals(20.0,       result.getCategories().get(2).getPercentage());
    }

    @Test
    void getCategoryReport_whenNoTransactions_shouldReturnZeroTotal() {
        when(transactionRepository.findByUserTypeAndDateBetween(
                any(), any(), any(), any()))
                .thenReturn(List.of());

        ReportCategoryDTO result = reportService.getCategoryReport("EXPENSE", 4, 2024);

        assertEquals(0.0, result.getTotal());
        assertTrue(result.getCategories().isEmpty());
    }

    @Test
    void getCategoryReport_whenTypeNull_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> reportService.getCategoryReport(null, 4, 2024));
        verify(transactionRepository, never()).findByUserTypeAndDateBetween(any(), any(), any(), any());
    }

    @Test
    void getCategoryReport_whenTypeInvalid_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> reportService.getCategoryReport("INVALID", 4, 2024));
    }

    @Test
    void getCategoryReport_whenMonthAndYearNull_shouldUseCurrentDate() {
        when(transactionRepository.findByUserTypeAndDateBetween(
                any(), any(), any(), any()))
                .thenReturn(List.of());

        // Không throw → dùng tháng/năm hiện tại
        assertDoesNotThrow(() -> reportService.getCategoryReport("INCOME", null, null));
        verify(transactionRepository).findByUserTypeAndDateBetween(any(), any(), any(), any());
    }

    @Test
    void getCategoryReport_whenIncome_shouldReturnIncomeType() {
        Transaction t = TestDataFactory.buildTransaction(1L, user, incomeCategory,
                new BigDecimal("2000.00"), LocalDate.of(2024, 4, 1));

        when(transactionRepository.findByUserTypeAndDateBetween(
                eq(user.getId()), eq(CategoryType.INCOME), any(), any()))
                .thenReturn(List.of(t));

        ReportCategoryDTO result = reportService.getCategoryReport("INCOME", 4, 2024);

        assertEquals("INCOME", result.getType());
        assertEquals(2000.0, result.getTotal());
    }

    // ─── getMonthlyReport ─────────────────────────────────────────────────────

    @Test
    void getMonthlyReport_whenYearNull_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> reportService.getMonthlyReport(null, 4));
        verify(transactionRepository, never()).findByUserAndYear(any(), any(), any());
    }

    @Test
    void getMonthlyReport_shouldGroupByMonthCorrectly() {
        // Income tháng 4: 2000, Expense tháng 4: 120
        Transaction income = TestDataFactory.buildTransaction(1L, user, incomeCategory,
                new BigDecimal("2000.00"), LocalDate.of(2024, 4, 1));
        Transaction expense = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("120.00"), LocalDate.of(2024, 4, 15));

        when(transactionRepository.findByUserAndYear(
                eq(user.getId()), any(), any()))
                .thenReturn(List.of(income, expense));

        ReportMonthlyDTO result = reportService.getMonthlyReport(2024, 4);

        assertNotNull(result.getChart());
        assertFalse(result.getChart().isEmpty());

        // Tìm tháng APR trong chart
        ReportMonthlyDTO.MonthlyChartItem apr = result.getChart().stream()
                .filter(c -> c.getMonth().equals("APR"))
                .findFirst().orElseThrow();

        assertEquals(2000.0, apr.getIncome());
        assertEquals(120.0,  apr.getExpense());
    }

    @Test
    void getMonthlyReport_summary_shouldMatchSelectedMonth() {
        Transaction income = TestDataFactory.buildTransaction(1L, user, incomeCategory,
                new BigDecimal("3500.00"), LocalDate.of(2024, 4, 1));
        Transaction expense = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("2400.00"), LocalDate.of(2024, 4, 20));

        when(transactionRepository.findByUserAndYear(any(), any(), any()))
                .thenReturn(List.of(income, expense));

        ReportMonthlyDTO result = reportService.getMonthlyReport(2024, 4);

        assertEquals("April 2024", result.getSummary().getMonth());
        assertEquals(3500.0,       result.getSummary().getIncome());
        assertEquals(2400.0,       result.getSummary().getExpense());
    }

    @Test
    void getMonthlyReport_whenNoTransactions_shouldReturnZeroValues() {
        when(transactionRepository.findByUserAndYear(any(), any(), any()))
                .thenReturn(List.of());

        ReportMonthlyDTO result = reportService.getMonthlyReport(2024, 4);

        assertNotNull(result.getChart());
        assertEquals(0.0, result.getSummary().getIncome());
        assertEquals(0.0, result.getSummary().getExpense());
    }

    @Test
    void getMonthlyReport_whenMonthNull_shouldUseCurrentMonth() {
        when(transactionRepository.findByUserAndYear(any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> reportService.getMonthlyReport(2024, null));
        verify(transactionRepository).findByUserAndYear(any(), any(), any());
    }

    @Test
    void getMonthlyReport_chart_shouldHaveCorrectMonthLabels() {
        when(transactionRepository.findByUserAndYear(any(), any(), any()))
                .thenReturn(List.of());

        ReportMonthlyDTO result = reportService.getMonthlyReport(2024, 4);

        // Chart luôn bắt đầu từ JAN
        assertEquals("JAN", result.getChart().get(0).getMonth());
    }

    @Test
    void getMonthlyReport_transactionsDifferentMonths_shouldGroupCorrectly() {
        Transaction janIncome = TestDataFactory.buildTransaction(1L, user, incomeCategory,
                new BigDecimal("1200.00"), LocalDate.of(2024, 1, 5));
        Transaction aprExpense = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("500.00"), LocalDate.of(2024, 4, 10));

        when(transactionRepository.findByUserAndYear(any(), any(), any()))
                .thenReturn(List.of(janIncome, aprExpense));

        ReportMonthlyDTO result = reportService.getMonthlyReport(2024, 4);

        ReportMonthlyDTO.MonthlyChartItem jan = result.getChart().get(0); // index 0 = JAN
        assertEquals(1200.0, jan.getIncome());
        assertEquals(0.0,    jan.getExpense());

        ReportMonthlyDTO.MonthlyChartItem apr = result.getChart().stream()
                .filter(c -> c.getMonth().equals("APR")).findFirst().orElseThrow();
        assertEquals(0.0,   apr.getIncome());
        assertEquals(500.0, apr.getExpense());
    }
}