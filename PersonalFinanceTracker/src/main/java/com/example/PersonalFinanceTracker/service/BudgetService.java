package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.BudgetCreateRequest;
import com.example.PersonalFinanceTracker.dto.BudgetResponse;
import com.example.PersonalFinanceTracker.entity.Budget;
import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.repository.BudgetRepository;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import org.springframework.data.domain.Sort;
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
    public BudgetResponse create(BudgetCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Budget budget = new Budget();
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setUser(user);
        budget.setCategory(category);

        Budget saved = budgetRepository.save(budget);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "year", "month").and(Sort.by(Sort.Direction.DESC, "id"));
        return budgetRepository.findAll(sort).stream().map(this::toResponse).toList();
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
}