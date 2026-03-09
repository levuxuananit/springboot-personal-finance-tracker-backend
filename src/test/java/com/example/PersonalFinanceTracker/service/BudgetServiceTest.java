package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.request.BudgetCreateRequest;
import com.example.PersonalFinanceTracker.entity.Budget;
import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableEntityException;
import com.example.PersonalFinanceTracker.repository.BudgetRepository;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BudgetServiceTest {

    private final BudgetRepository budgetRepository = mock(BudgetRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final BudgetService service = new BudgetService(budgetRepository, categoryRepository, userRepository);

    @Test
    void setBudget_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BudgetCreateRequest req = new BudgetCreateRequest(100.0, 3, 2026, 10L);

        assertThrows(ResourceNotFoundException.class, () -> service.setBudget(1L, req));
        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(budgetRepository);
    }

    @Test
    void setBudget_shouldThrowNotFound_whenCategoryMissing() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        BudgetCreateRequest req = new BudgetCreateRequest(100.0, 3, 2026, 10L);

        assertThrows(ResourceNotFoundException.class, () -> service.setBudget(1L, req));
        verifyNoInteractions(budgetRepository);
    }

    @Test
    void setBudget_shouldThrow422_whenCategoryNotExpense() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setType(CategoryType.INCOME);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        BudgetCreateRequest req = new BudgetCreateRequest(100.0, 3, 2026, 10L);

        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> service.setBudget(1L, req)
        );
        assertEquals("categoryId", ex.getField());
        verifyNoInteractions(budgetRepository);
    }

    @Test
    void setBudget_shouldCreateNew_whenNoExistingBudget() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setType(CategoryType.EXPENSE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 10L, 3, 2026))
                .thenReturn(Optional.empty());

        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        BudgetCreateRequest req = new BudgetCreateRequest(200.0, 3, 2026, 10L);
        var res = service.setBudget(1L, req);

        assertEquals(99L, res.id());
        assertEquals(200.0, res.amount());
        assertEquals(3, res.month());
        assertEquals(2026, res.year());
        assertEquals(10L, res.categoryId());
        assertEquals(1L, res.userId());
    }

    @Test
    void setBudget_shouldUpdateExisting_whenBudgetExists() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setType(CategoryType.EXPENSE);

        Budget existing = new Budget();
        existing.setId(50L);
        existing.setAmount(123.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 10L, 3, 2026))
                .thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetCreateRequest req = new BudgetCreateRequest(999.0, 3, 2026, 10L);
        var res = service.setBudget(1L, req);

        assertEquals(50L, res.id());
        assertEquals(999.0, res.amount());
    }

    @Test
    void getAllForUser_shouldSortByYearMonthIdDesc() {
        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setYear(2025);
        b1.setMonth(12);

        Budget b2 = new Budget();
        b2.setId(2L);
        b2.setYear(2026);
        b2.setMonth(1);

        Budget b3 = new Budget();
        b3.setId(3L);
        b3.setYear(2026);
        b3.setMonth(1);

        when(budgetRepository.findAllByUserId(1L)).thenReturn(List.of(b1, b2, b3));

        List<Long> ids = service.getAllForUser(1L).stream().map(r -> r.id()).toList();
        assertEquals(List.of(3L, 2L, 1L), ids);
    }
}

