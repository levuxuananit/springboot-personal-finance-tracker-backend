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

    /**
     * Lấy lịch sử giao dịch có phân trang.
     * page bắt đầu từ 1 (theo spec), chuyển sang 0-based cho Spring Pageable.
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<TransactionResponseDTO> getHistory(
            String startDate, String endDate,
            String type, Long categoryId,
            int page, int size) {

        User user = authUtil.getCurrentUser();

        LocalDate start = (startDate != null) ? parseDate(startDate) : null;
        LocalDate end   = (endDate   != null) ? parseDate(endDate)   : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new UnprocessableException("startDate must not be after endDate");
        }

        if (page < 1) throw new UnprocessableException("Page must be >= 1");
        if (size < 1) throw new UnprocessableException("Size must be >= 1");

        CategoryType categoryType = null;
        if (type != null && !type.isBlank()) {
            try {
                categoryType = CategoryType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new UnprocessableException("Type must be INCOME or EXPENSE");
            }
        }

        // Spec dùng page bắt đầu từ 1, Spring Pageable bắt đầu từ 0
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Transaction> pageResult = transactionRepository
                .findHistory(user.getId(), start, end, categoryType, categoryId, pageable);

        List<TransactionResponseDTO> data = pageResult.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PagedResponseDTO.PaginationMeta meta = new PagedResponseDTO.PaginationMeta(
                page,
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );

        return new PagedResponseDTO<>(data, meta);
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
