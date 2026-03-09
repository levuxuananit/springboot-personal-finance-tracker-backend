package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.CategoryGroupedDTO;
import com.example.PersonalFinanceTracker.dto.CategoryRequestDTO;
import com.example.PersonalFinanceTracker.dto.CategoryResponseDTO;
import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.CategoryIcon;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ConflictException;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.CategoryIconRepository;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryIconRepository categoryIconRepository;
    private final AuthUtil authUtil;


    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        User user = authUtil.getCurrentUser();

        // Validate type trước khi insert
        CategoryType type;
        try {
            type = CategoryType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnprocessableException("Type must be INCOME or EXPENSE");
        }

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.getName())) {
            throw new ConflictException("Category name already exists");
        }

        CategoryIcon icon = resolveIcon(request);

        Category category = new Category();
        category.setName(request.getName());
        category.setCategoryIcon(icon);
        category.setUser(user);
        category.setType(type);

        return toDTO(categoryRepository.save(category));
    }

    private CategoryIcon resolveIcon(CategoryRequestDTO request) {
        if (request.getEmoji() != null && !request.getEmoji().isBlank()) {
            return categoryIconRepository.findByEmoji(request.getEmoji())
                    .orElseGet(() -> findByNameOrDefault(request.getName()));
        }
        return findByNameOrDefault(request.getName());
    }

    private CategoryIcon findByNameOrDefault(String name) {
        return categoryIconRepository.findByCategoryNameIgnoreCase(name)
                .orElseGet(() -> categoryIconRepository
                        .findByCategoryNameIgnoreCase("Other")
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Default icon not found. Please seed category_icons table.")));
    }

    private CategoryResponseDTO toDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(), category.getName(), category.getCategoryIcon().getEmoji(), category.getCategoryIcon().getIconUrl()
        );
    }
}
