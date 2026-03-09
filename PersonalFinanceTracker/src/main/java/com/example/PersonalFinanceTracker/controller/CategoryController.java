package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.CategoryRequestDTO;
import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.service.CategoryService;
import com.example.PersonalFinanceTracker.dto.CategoryGroupedDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getCategories(@RequestParam(required = false) String type) {

        if (type != null && !type.isBlank()) {
            CategoryType categoryType;
            try {
                categoryType = CategoryType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new UnprocessableException("Type must be INCOME or EXPENSE");
            }

            List<CategoryResponseDTO> list = categoryService.getByType(categoryType);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("message", list.isEmpty()
                    ? "No categories found for the given type"
                    : "Category list fetched successfully");
            body.put("data", list);
            return ResponseEntity.ok(body);
        }

        CategoryGroupedDTO grouped = categoryService.getAll();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Category list fetched successfully");
        body.put("data", grouped);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.create(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Category created successfully");
        body.put("data", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
