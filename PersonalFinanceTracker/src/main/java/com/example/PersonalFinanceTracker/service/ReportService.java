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

    // ── GET /api/reports/monthly ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReportMonthlyDTO getMonthlyReport(Integer year, Integer month) {
        User user = authUtil.getCurrentUser();

        // year bắt buộc theo spec
        if (year == null) {
            throw new UnprocessableException("year is required");
        }

        LocalDate now = LocalDate.now();
        int selectedMonth = (month != null) ? month : now.getMonthValue();

        // Lấy tất cả transaction trong cả năm
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear   = LocalDate.of(year, 12, 31);

        List<Transaction> allYear = transactionRepository
                .findByUserAndYear(user.getId(), startOfYear, endOfYear);

        // Nhóm theo tháng → tính income/expense từng tháng
        // Key: số tháng (1-12), Value: [income, expense]
        Map<Integer, double[]> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthMap.put(i, new double[]{0.0, 0.0});
        }

        for (Transaction t : allYear) {
            int m = t.getDate().getMonthValue();
            double[] values = monthMap.get(m);
            if (t.getCategory().getType() == CategoryType.INCOME) {
                values[0] += t.getAmount().doubleValue();
            } else {
                values[1] += t.getAmount().doubleValue();
            }
        }

        // Build chart — chỉ trả về các tháng đã có data (hoặc đến tháng hiện tại nếu cùng năm)
        // Theo spec response mẫu chỉ show đến tháng hiện tại trong năm
        int maxMonth = (year == now.getYear()) ? now.getMonthValue() : 12;

        List<MonthlyChartItem> chart = new ArrayList<>();
        for (int i = 1; i <= maxMonth; i++) {
            double[] values = monthMap.get(i);
            String monthLabel = Month.of(i)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    .toUpperCase(); // "JAN", "FEB", ...
            chart.add(new MonthlyChartItem(monthLabel, values[0], values[1]));
        }

        // Summary — tổng thu/chi của tháng được chọn
        double[] selectedValues = monthMap.get(selectedMonth);
        String summaryMonthLabel = Month.of(selectedMonth)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year; // "April 2024"

        MonthlySummary summary = new MonthlySummary(
                summaryMonthLabel,
                selectedValues[0],
                selectedValues[1]
        );

        return new ReportMonthlyDTO(chart, summary);
    }
}