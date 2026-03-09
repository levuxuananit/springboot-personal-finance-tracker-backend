package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.request.BudgetCreateRequest;
import com.example.PersonalFinanceTracker.dto.response.BudgetResponse;
import com.example.PersonalFinanceTracker.entity.Budget;
import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableEntityException;
import com.example.PersonalFinanceTracker.repository.BudgetRepository;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse setBudget(Long userId, BudgetCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new UnprocessableEntityException("categoryId", "Category must be EXPENSE");
        }

        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId,
                request.categoryId(),
                request.month(),
                request.year()
        ).orElseGet(Budget::new);

        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setUser(user);
        budget.setCategory(category);

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllForUser(Long userId) {
        return budgetRepository.findAllByUserId(userId).stream()
                .sorted((a, b) -> {
                    int yearCmp = nullSafeInt(b.getYear()) - nullSafeInt(a.getYear());
                    if (yearCmp != 0) return yearCmp;
                    int monthCmp = nullSafeInt(b.getMonth()) - nullSafeInt(a.getMonth());
                    if (monthCmp != 0) return monthCmp;
                    return Long.compare(nullSafeLong(b.getId()), nullSafeLong(a.getId()));
                })
                .map(this::toResponse)
                .toList();
    }

    private BudgetResponse toResponse(Budget budget) {
        Long categoryId = budget.getCategory() == null ? null : budget.getCategory().getId();
        Long userId = budget.getUser() == null ? null : budget.getUser().getId();

        return new BudgetResponse(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                categoryId,
                userId
        );
    }

    private static int nullSafeInt(Integer value) {
        return value == null ? Integer.MIN_VALUE : value;
    }

    private static long nullSafeLong(Long value) {
        return value == null ? Long.MIN_VALUE : value;
    }
}

