package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.ReportCategoryDTO;
import com.example.PersonalFinanceTracker.dto.ReportCategoryDTO.CategoryItem;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO.MonthlyChartItem;
import com.example.PersonalFinanceTracker.dto.ReportMonthlyDTO.MonthlySummary;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.Transaction;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.exception.UnprocessableException;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    // ── GET /api/reports/category ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReportCategoryDTO getCategoryReport(String type, Integer month, Integer year) {
        User user = authUtil.getCurrentUser();

        // Validate type
        if (type == null || type.isBlank()) {
            throw new UnprocessableException("type is required (INCOME or EXPENSE)");
        }
        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnprocessableException("type must be INCOME or EXPENSE");
        }

        // Xác định tháng/năm
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year  != null) ? year  : now.getYear();

        YearMonth ym = YearMonth.of(y, m);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate   = ym.atEndOfMonth();

        // Lấy transactions theo type + date range
        List<Transaction> transactions = transactionRepository
                .findByUserTypeAndDateBetween(user.getId(), categoryType, startDate, endDate);

        // Nhóm theo category, tính tổng từng nhóm
        Map<String, Double> sumByCategory = new LinkedHashMap<>();
        Map<String, Transaction> refByCategory = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            sumByCategory.merge(catName, t.getAmount().doubleValue(), Double::sum);
            refByCategory.putIfAbsent(catName, t);
        }

        // Tổng toàn bộ
        double total = sumByCategory.values().stream()
                .mapToDouble(Double::doubleValue).sum();

        // Build category items với percentage
        List<CategoryItem> categories = sumByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> {
                    double percentage = total > 0
                            ? Math.round((entry.getValue() / total * 100) * 10.0) / 10.0
                            : 0.0;
                    return new CategoryItem(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());

        return new ReportCategoryDTO(categoryType.name(), total, categories);
    }

}