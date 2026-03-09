package com.example.PersonalFinanceTracker.controller;

import com.example.PersonalFinanceTracker.dto.PagedResponseDTO;
import com.example.PersonalFinanceTracker.dto.TransactionRequestDTO;
import com.example.PersonalFinanceTracker.dto.TransactionResponseDTO;
import com.example.PersonalFinanceTracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.create(request);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Transaction added successfully");
        body.put("data", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponseDTO<TransactionResponseDTO> result =
                transactionService.getHistory(startDate, endDate, type, categoryId, page, size);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Transaction history fetched successfully");
        body.put("data", result.getData());
        body.put("pagination", result.getPagination());
        return ResponseEntity.ok(body);
    }
}