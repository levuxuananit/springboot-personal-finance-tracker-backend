package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {}
