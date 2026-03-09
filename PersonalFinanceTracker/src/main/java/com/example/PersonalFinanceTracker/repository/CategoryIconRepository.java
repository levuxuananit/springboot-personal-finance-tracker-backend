package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.CategoryIcon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryIconRepository extends JpaRepository<CategoryIcon, Long> {
    Optional<CategoryIcon> findByCategoryNameIgnoreCase(String categoryName);
    Optional<CategoryIcon> findByEmoji(String emoji);
}
