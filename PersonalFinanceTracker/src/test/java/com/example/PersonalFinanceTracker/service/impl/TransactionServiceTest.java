package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.PagedResponseDTO;
import com.example.PersonalFinanceTracker.dto.TransactionRequestDTO;
import com.example.PersonalFinanceTracker.dto.TransactionResponseDTO;
import com.example.PersonalFinanceTracker.entity.*;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import com.example.PersonalFinanceTracker.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    AuthUtil authUtil;

    @InjectMocks
    TransactionService transactionService;

    private User user;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.defaultUser();
        expenseCategory = TestDataFactory.expenseCategory(user);
        when(authUtil.getCurrentUser()).thenReturn(user);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_whenValid_shouldSaveAndReturn() {
        TransactionRequestDTO request = TestDataFactory.buildTransactionRequest(
                new BigDecimal("120.00"), "Lunch", 1L, "2024-04-24");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(101L);
            return t;
        });

        TransactionResponseDTO result = transactionService.create(request);

        assertEquals(101L, result.getId());
        assertEquals(new BigDecimal("120.00"), result.getAmount());
        assertEquals("Food", result.getCategory().getName());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void create_whenCategoryNotBelongToUser_shouldThrowNotFound() {
        TransactionRequestDTO request = TestDataFactory.buildTransactionRequest(
                new BigDecimal("100.00"), 1L, "2024-04-24");

        User anotherUser = TestDataFactory.buildUser(99L, "other@example.com");
        expenseCategory.setUser(anotherUser);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(expenseCategory));

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.create(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_whenCategoryNotFound_shouldThrowNotFound() {
        TransactionRequestDTO request = TestDataFactory.buildTransactionRequest(
                new BigDecimal("100.00"), 999L, "2024-04-24");

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.create(request));
    }

    @Test
    void create_whenDateFormatInvalid_shouldThrowUnprocessable() {
        TransactionRequestDTO request = TestDataFactory.buildTransactionRequest(
                new BigDecimal("100.00"), 1L, "28-04-2024");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(expenseCategory));

        assertThrows(UnprocessableException.class,
                () -> transactionService.create(request));
    }

    // ─── getHistory ───────────────────────────────────────────────────────────

    @Test
    void getHistory_whenNoFilter_shouldReturnPagedResult() {
        Transaction transaction = TestDataFactory.expenseTransaction(user, expenseCategory);

        when(transactionRepository.findHistory(
                eq(user.getId()), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        PagedResponseDTO<TransactionResponseDTO> result =
                transactionService.getHistory(null, null, null, null, 1, 10);

        assertEquals(1, result.getData().size());
        assertEquals(1, result.getPagination().getCurrentPage());
        assertEquals(1, result.getPagination().getTotalItems());
    }

    @Test
    void getHistory_whenStartDateAfterEndDate_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> transactionService.getHistory(
                        "2024-04-30", "2024-04-01", null, null, 1, 10));
    }

    @Test
    void getHistory_whenTypeInvalid_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> transactionService.getHistory(
                        null, null, "INVALID", null, 1, 10));
    }

    @Test
    void getHistory_whenPageLessThan1_shouldThrowUnprocessable() {
        assertThrows(UnprocessableException.class,
                () -> transactionService.getHistory(
                        null, null, null, null, 0, 10));
    }

    @Test
    void getHistory_whenFilterByType_shouldPassTypeToRepository() {
        when(transactionRepository.findHistory(
                eq(user.getId()), any(), any(),
                eq(CategoryType.EXPENSE), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PagedResponseDTO<TransactionResponseDTO> result =
                transactionService.getHistory(null, null, "EXPENSE", null, 1, 10);

        assertEquals(0, result.getData().size());
        verify(transactionRepository).findHistory(
                eq(user.getId()), any(), any(),
                eq(CategoryType.EXPENSE), any(), any(Pageable.class));
    }
}
