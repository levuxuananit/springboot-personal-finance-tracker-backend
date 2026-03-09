package com.example.PersonalFinanceTracker.repository;

import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByIdAsc(Long userId);

    List<Category> findByUserIdAndTypeOrderByIdAsc(Long userId, CategoryType type);

    @Query("SELECT c FROM Category c JOIN c.categoryIcon ci WHERE c.user.id = :userId AND LOWER(ci.categoryName) = LOWER(:name)")
    Optional<Category> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
