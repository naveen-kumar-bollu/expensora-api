package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.CategoryBreakdownDto;
import com.expensora.expensora_api.dto.DashboardSummaryDto;
import com.expensora.expensora_api.dto.MonthlyTrendDto;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public DashboardSummaryDto getMonthlySummary(UUID userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        BigDecimal monthlyIncome = incomeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.between(root.get("incomeDate"), startDate, endDate)
                )
        ).stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyExpenses = expenseRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.between(root.get("expenseDate"), startDate, endDate)
                )
        ).stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = monthlyIncome.subtract(monthlyExpenses);

        return new DashboardSummaryDto(monthlyIncome, monthlyExpenses, netSavings);
    }

    @Override
    public List<CategoryBreakdownDto> getCategoryBreakdown(UUID userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Expense> expenses = expenseRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.between(root.get("expenseDate"), startDate, endDate)
                )
        );

        BigDecimal total = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return categoryTotals.entrySet().stream()
                .map(entry -> {
                    BigDecimal amount = entry.getValue();
                    Double percentage = total.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                            amount.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
                    return new CategoryBreakdownDto(entry.getKey(), amount, percentage);
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyTrendDto> getMonthlyTrend(UUID userId, int year) {
        List<MonthlyTrendDto> trends = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            BigDecimal income = incomeRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("user").get("id"), userId),
                            cb.between(root.get("incomeDate"), startDate, endDate)
                    )
            ).stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = expenseRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("user").get("id"), userId),
                            cb.between(root.get("expenseDate"), startDate, endDate)
                    )
            ).stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(new MonthlyTrendDto(month, year, income, expense));
        }

        return trends;
    }

    @Override
    public String getTopSpendingCategory(UUID userId, int month, int year) {
        List<CategoryBreakdownDto> breakdown = getCategoryBreakdown(userId, month, year);
        return breakdown.isEmpty() ? "N/A" : breakdown.get(0).getCategoryName();
    }
}
