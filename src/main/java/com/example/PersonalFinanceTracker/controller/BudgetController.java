package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.request.BudgetCreateRequest;
import com.example.PersonalFinanceTracker.dto.response.ApiResponse;
import com.example.PersonalFinanceTracker.dto.response.BudgetResponse;
import com.example.PersonalFinanceTracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> setBudget(
            Authentication authentication,
            @Valid @RequestBody BudgetCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        BudgetResponse data = budgetService.setBudget(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Budget set successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<BudgetResponse> data = budgetService.getAllForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Budgets fetched successfully", data));
    }
}

