package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.*;
import com.example.PersonalFinanceTracker.entity.Category;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.Transaction;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.CategoryRepository;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AuthUtil authUtil;

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO request) {
        User user = authUtil.getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setDate(parseDate(request.getDate()));

        return toDTO(transactionRepository.save(transaction));
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new UnprocessableException("Invalid date format. Expected yyyy-MM-dd, got: " + dateStr);
        }
    }

    private TransactionResponseDTO toDTO(Transaction t) {
        Category category = t.getCategory();

        CategoryResponseForTransactionDTO categoryDTO = new CategoryResponseForTransactionDTO(
                category.getId(),
                category.getName(),
                category.getCategoryIcon().getEmoji(),
                category.getCategoryIcon().getIconUrl(),
                category.getType().name()
        );

        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(t.getId());
        dto.setAmount(t.getAmount());
        dto.setNote(t.getNote());
        dto.setCategory(categoryDTO);
        dto.setDate(t.getDate().toString());
        return dto;
    }
}
