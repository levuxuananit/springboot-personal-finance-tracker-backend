package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.CategoryGroupedDTO;
import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.entity.*;
import com.example.PersonalFinanceTracker.exception.ConflictException;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.CategoryIconRepository;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import com.example.PersonalFinanceTracker.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock CategoryIconRepository categoryIconRepository;
    @Mock AuthUtil authUtil;
    @InjectMocks CategoryService categoryService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.defaultUser();
        expenseCategory = TestDataFactory.expenseCategory(user);
        incomeCategory = TestDataFactory.incomeCategory(user);
        when(authUtil.getCurrentUser()).thenReturn(user);
    }

    // getByType

    @Test
    void getByType_whenExpense_shouldReturnExpenseList() {
        when(categoryRepository.findByUserIdOrderByIdAsc(user.getId(), CategoryType.EXPENSE))
                .thenReturn(List.of(expenseCategory));

        List<CategoryResponseDTO> result = categoryService.getByType(CategoryType.EXPENSE);

        assertEquals(1, result.size());
        assertEquals("EXPENSE", result.get(0).getType());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void getByType_whenIncome_shouldReturnIncomeList() {
        when(categoryRepository.findByUserIdOrderByIdAsc(user.getId(), CategoryType.INCOME))
                .thenReturn(List.of(incomeCategory));

        List<CategoryResponseDTO> result = categoryService.getByType(CategoryType.INCOME);

        assertEquals(1, result.size());
        assertEquals("INCOME", result.get(0).getType());
    }

    @Test
    void getByType_whenNoneFound_shouldReturnEmptyList() {
        when(categoryRepository.findByUserIdOrderByIdAsc(any(), any())).thenReturn(List.of());

        List<CategoryResponseDTO> result = categoryService.getByType(CategoryType.EXPENSE);

        assertEquals(0, result.size());
    }

    // getAll

    @Test
    void getAll_shouldReturnGroupedExpenseAndIncome() {
        when(categoryRepository.findByUserIdOrderByIdAsc(user.getId()))
                .thenReturn(List.of(expenseCategory, incomeCategory));

        CategoryGroupedDTO result = categoryService.getAll();

        assertEquals(1, result.getExpense().size());
        assertEquals(1, result.getIncome().size());
        assertEquals("EXPENSE", result.getExpense().get(0).getType());
        assertEquals("INCOME", result.getIncome().get(0).getType());
    }

    @Test
    void getAll_whenNoCategoriesExist_shouldReturnEmptyGroups() {
        when(categoryRepository.findByUserIdOrderByIdAsc(user.getId())).thenReturn(List.of());

        CategoryGroupedDTO result = categoryService.getAll();

        assertEquals(0, result.getExpense().size());
        assertEquals(0, result.getIncome().size());
    }

    // create

    @Test
    void create_whenNameNotExists_shouldSaveAndReturn() {
        CategoryIcon icon = TestDataFactory.buildCategoryIcon(3L, "Transportation",
                "🚗", "https://cdn.example.com/icons/transportation.png");

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Transportation")).thenReturn(false);
        when(categoryIconRepository.findByCategoryNameIgnoreCase("Transportation"))
                .thenReturn(Optional.of(icon));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(8L);
            return c;
        });

        CategoryResponseDTO result = categoryService.create(
                TestDataFactory.buildCategoryRequest("Transportation", "EXPENSE"));

        assertEquals("Transportation", result.getName());
        assertEquals("EXPENSE", result.getType());
        assertEquals("🚗", result.getIcon());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_whenNameAlreadyExists_shouldThrowConflict() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Food")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> categoryService.create(
                        TestDataFactory.buildCategoryRequest("Food", "EXPENSE")));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_whenTypeInvalid_shouldThrowUnprocessable() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Test")).thenReturn(false);

        assertThrows(UnprocessableException.class,
                () -> categoryService.create(
                        TestDataFactory.buildCategoryRequest("Test", "INVALID")));
    }

    @Test
    void create_whenEmojiProvided_shouldResolveIconByEmoji() {
        CategoryIcon icon = TestDataFactory.buildCategoryIcon(3L, "Transportation",
                "🚗", "https://cdn.example.com/icons/transportation.png");

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Transportation")).thenReturn(false);
        when(categoryIconRepository.findByEmoji("🚗")).thenReturn(Optional.of(icon));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(8L);
            return c;
        });

        CategoryResponseDTO result = categoryService.create(
                TestDataFactory.buildCategoryRequest("Transportation", "EXPENSE", "🚗"));

        assertEquals("🚗", result.getIcon());
        verify(categoryIconRepository).findByEmoji("🚗");
    }

    @Test
    void create_whenIconNotFound_shouldFallbackToOtherIcon() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Unknown")).thenReturn(false);
        when(categoryIconRepository.findByCategoryNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());
        when(categoryIconRepository.findByCategoryNameIgnoreCase("Other"))
                .thenReturn(Optional.of(TestDataFactory.otherIcon()));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(9L);
            return c;
        });

        assertNotNull(categoryService.create(
                TestDataFactory.buildCategoryRequest("Unknown", "EXPENSE")));
    }

    @Test
    void create_whenDefaultIconMissing_shouldThrowNotFound() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(), "Unknown")).thenReturn(false);
        when(categoryIconRepository.findByCategoryNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());
        when(categoryIconRepository.findByCategoryNameIgnoreCase("Other"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.create(
                        TestDataFactory.buildCategoryRequest("Unknown", "EXPENSE")));
    }
}
