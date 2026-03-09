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
