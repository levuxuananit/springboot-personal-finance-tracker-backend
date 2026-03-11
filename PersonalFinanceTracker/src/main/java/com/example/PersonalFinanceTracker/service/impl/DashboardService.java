package com.example.PersonalFinanceTracker.service;

import com.example.PersonalFinanceTracker.dto.DashboardResponseDTO;
import com.example.PersonalFinanceTracker.entity.CategoryType;
import com.example.PersonalFinanceTracker.entity.Transaction;
import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.TransactionRepository;
import com.example.PersonalFinanceTracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard(Integer month, Integer year) {
        User user = authUtil.getCurrentUser();

        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year  != null) ? year  : now.getYear();

        // Dùng date range thay vì MONTH()/YEAR() để có thể dùng index
        YearMonth ym = YearMonth.of(y, m);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate   = ym.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository
                .sumByUserAndTypeAndDateBetween(user.getId(), CategoryType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository
                .sumByUserAndTypeAndDateBetween(user.getId(), CategoryType.EXPENSE, startDate, endDate);

        if (totalIncome  == null) totalIncome  = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<Transaction> all = transactionRepository
                .findByUserAndDateBetween(user.getId(), startDate, endDate);
        List<DashboardResponseDTO.PieChartItem> pieChart = buildPieChart(all);

        List<Transaction> recent = transactionRepository
                .findTop3ByUserAndDateBetween(user.getId(), startDate, endDate);
        List<DashboardResponseDTO.RecentTransaction> recentList = recent.stream()
                .map(this::toRecentTransaction)
                .collect(Collectors.toList());

        return new DashboardResponseDTO(totalIncome, totalExpense, balance, pieChart, recentList);
    }

    private List<DashboardResponseDTO.PieChartItem> buildPieChart(List<Transaction> transactions) {
        Map<Long, BigDecimal> sumMap = new LinkedHashMap<>();
        Map<Long, Transaction> refMap = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            Long catId = t.getCategory().getId();
            sumMap.merge(catId, t.getAmount(), BigDecimal::add);
            refMap.putIfAbsent(catId, t);
        }

        return sumMap.entrySet().stream()
                .map(entry -> {
                    Transaction ref = refMap.get(entry.getKey());
                    return new DashboardResponseDTO.PieChartItem(
                            ref.getCategory().getName(),
                            entry.getValue(),
                            ref.getCategory().getType().name(),
                            ref.getCategory().getCategoryIcon().getEmoji(),
                            ref.getCategory().getCategoryIcon().getIconUrl()
                    );
                })
                .collect(Collectors.toList());
    }

    private DashboardResponseDTO.RecentTransaction toRecentTransaction(Transaction t) {
        BigDecimal amount = t.getCategory().getType() == CategoryType.EXPENSE
                ? t.getAmount().negate()
                : t.getAmount();

        return new DashboardResponseDTO.RecentTransaction(
                t.getId(),
                t.getCategory().getName(),
                t.getCategory().getCategoryIcon().getEmoji(),
                t.getCategory().getCategoryIcon().getIconUrl(),
                amount,
                t.getDate().toString(),
                t.getCategory().getType().name()
        );
    }
}

