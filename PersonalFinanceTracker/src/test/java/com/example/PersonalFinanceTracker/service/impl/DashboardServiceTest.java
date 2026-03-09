package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.DashboardResponseDTO;
import com.example.PersonalFinanceTracker.entity.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AuthUtil authUtil;

    @InjectMocks
    DashboardService dashboardService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;

    private final LocalDate startApr = LocalDate.of(2024, 4, 1);
    private final LocalDate endApr   = LocalDate.of(2024, 4, 30);

    @BeforeEach
    void setUp() {
        user = TestDataFactory.defaultUser();
        expenseCategory = TestDataFactory.expenseCategory(user);
        incomeCategory  = TestDataFactory.incomeCategory(user);
        when(authUtil.getCurrentUser()).thenReturn(user);
    }

    // ─── getDashboard ─────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldReturnCorrectIncomeExpenseBalance() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                user.getId(), CategoryType.INCOME, startApr, endApr))
                .thenReturn(new BigDecimal("2600.00"));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                user.getId(), CategoryType.EXPENSE, startApr, endApr))
                .thenReturn(new BigDecimal("1400.00"));
        when(transactionRepository.findByUserAndDateBetween(user.getId(), startApr, endApr))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(user.getId(), startApr, endApr))
                .thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        assertEquals(new BigDecimal("2600.00"), result.getIncome());
        assertEquals(new BigDecimal("1400.00"), result.getExpenses());
        assertEquals(new BigDecimal("1200.00"), result.getBalance());
    }

    @Test
    void getDashboard_whenDbReturnsNull_shouldTreatAsZero() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(any(), any(), any(), any()))
                .thenReturn(null);
        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        assertEquals(BigDecimal.ZERO, result.getIncome());
        assertEquals(BigDecimal.ZERO, result.getExpenses());
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void getDashboard_whenExpenseExceedsIncome_shouldReturnNegativeBalance() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                user.getId(), CategoryType.INCOME, startApr, endApr))
                .thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                user.getId(), CategoryType.EXPENSE, startApr, endApr))
                .thenReturn(new BigDecimal("1500.00"));
        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        assertTrue(result.getBalance().compareTo(BigDecimal.ZERO) < 0);
        assertEquals(new BigDecimal("-1000.00"), result.getBalance());
    }

    @Test
    void getDashboard_whenParamsNull_shouldUseCurrentMonth() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        // Không throw exception là đúng
        assertDoesNotThrow(() -> dashboardService.getDashboard(null, null));
    }

    @Test
    void getDashboard_pieChart_shouldAggregateAmountByCategory() {
        Transaction t1 = TestDataFactory.buildTransaction(1L, user, expenseCategory,
                new BigDecimal("100.00"), LocalDate.of(2024, 4, 1));
        Transaction t2 = TestDataFactory.buildTransaction(2L, user, expenseCategory,
                new BigDecimal("200.00"), LocalDate.of(2024, 4, 2));

        when(transactionRepository.sumByUserAndTypeAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserAndDateBetween(user.getId(), startApr, endApr))
                .thenReturn(List.of(t1, t2));
        when(transactionRepository.findTop3ByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        // 2 giao dịch cùng category → gộp thành 1 item
        assertEquals(1, result.getPieChart().size());
        assertEquals(new BigDecimal("300.00"), result.getPieChart().get(0).getAmount());
    }

    @Test
    void getDashboard_recentTransactions_expenseAmountShouldBeNegative() {
        Transaction t = TestDataFactory.expenseTransaction(user, expenseCategory);

        when(transactionRepository.sumByUserAndTypeAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(user.getId(), startApr, endApr))
                .thenReturn(List.of(t));

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        assertEquals(1, result.getRecentTransactions().size());
        assertTrue(result.getRecentTransactions().get(0).getAmount().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void getDashboard_recentTransactions_incomeAmountShouldBePositive() {
        Transaction t = TestDataFactory.incomeTransaction(user, incomeCategory);

        when(transactionRepository.sumByUserAndTypeAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(transactionRepository.findTop3ByUserAndDateBetween(user.getId(), startApr, endApr))
                .thenReturn(List.of(t));

        DashboardResponseDTO result = dashboardService.getDashboard(4, 2024);

        assertTrue(result.getRecentTransactions().get(0).getAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
