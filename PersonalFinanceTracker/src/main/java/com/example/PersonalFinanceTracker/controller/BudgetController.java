package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.BudgetCreateRequest;
import com.example.PersonalFinanceTracker.dto.BudgetResponse;
import com.example.PersonalFinanceTracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetCreateRequest request) {
        BudgetResponse created = budgetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAll() {
        return ResponseEntity.ok(budgetService.getAll());
    }
}