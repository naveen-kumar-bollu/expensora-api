package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.CategoryBreakdownDto;
import com.expensora.expensora_api.dto.DashboardSummaryDto;
import com.expensora.expensora_api.dto.InsightsDto;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.service.DashboardService;
import com.expensora.expensora_api.service.InsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InsightsServiceImpl implements InsightsService {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public InsightsDto generateInsights(UUID userId, int month, int year) {
        List<String> insights = new ArrayList<>();

        // Current month summary
        DashboardSummaryDto currentSummary = dashboardService.getMonthlySummary(userId, month, year);

        // Previous month summary
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;
        DashboardSummaryDto prevSummary = dashboardService.getMonthlySummary(userId, prevMonth, prevYear);

        // Spending spike detection
        if (currentSummary.getMonthlyExpenses().compareTo(prevSummary.getMonthlyExpenses()) > 0) {
            BigDecimal increase = currentSummary.getMonthlyExpenses().subtract(prevSummary.getMonthlyExpenses());
            Double increasePercent = increase.divide(prevSummary.getMonthlyExpenses(), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            if (increasePercent > 20) {
                insights.add("⚠️ Spending spike detected! Your expenses increased by " + String.format("%.1f", increasePercent) + "% compared to last month.");
            }
        }

        // Category increase comparison
        List<CategoryBreakdownDto> currentBreakdown = dashboardService.getCategoryBreakdown(userId, month, year);
        List<CategoryBreakdownDto> prevBreakdown = dashboardService.getCategoryBreakdown(userId, prevMonth, prevYear);
        
        Map<String, BigDecimal> prevCategoryMap = prevBreakdown.stream()
                .collect(Collectors.toMap(CategoryBreakdownDto::getCategoryName, CategoryBreakdownDto::getAmount));

        for (CategoryBreakdownDto current : currentBreakdown) {
            BigDecimal prevAmount = prevCategoryMap.getOrDefault(current.getCategoryName(), BigDecimal.ZERO);
            if (prevAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal increase = current.getAmount().subtract(prevAmount);
                Double increasePercent = increase.divide(prevAmount, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue();
                if (increasePercent > 30) {
                    insights.add("📊 Your spending in '" + current.getCategoryName() + "' increased by " + 
                                 String.format("%.1f", increasePercent) + "% this month.");
                }
            }
        }

        // Savings trend analysis
        if (currentSummary.getNetSavings().compareTo(BigDecimal.ZERO) > 0) {
            insights.add("✅ Great job! You saved $" + currentSummary.getNetSavings() + " this month.");
        } else if (currentSummary.getNetSavings().compareTo(BigDecimal.ZERO) < 0) {
            insights.add("⚠️ You spent more than you earned this month. Consider reviewing your expenses.");
        }

        // Financial health score calculation
        Double healthScore = calculateFinancialHealthScore(currentSummary, currentBreakdown);

        if (insights.isEmpty()) {
            insights.add("✨ Your spending is consistent. Keep up the good work!");
        }

        return new InsightsDto(insights, healthScore);
    }

    private Double calculateFinancialHealthScore(DashboardSummaryDto summary, List<CategoryBreakdownDto> breakdown) {
        double score = 100.0;

        // Deduct points for negative savings
        if (summary.getNetSavings().compareTo(BigDecimal.ZERO) <= 0) {
            score -= 30;
        }

        // Deduct points if savings rate is low
        if (summary.getMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
            double savingsRate = summary.getNetSavings().divide(summary.getMonthlyIncome(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            if (savingsRate < 10) {
                score -= 20;
            } else if (savingsRate < 20) {
                score -= 10;
            }
        }

        // Deduct points for unbalanced spending
        for (CategoryBreakdownDto cat : breakdown) {
            if (cat.getPercentage() > 40) { // One category taking more than 40%
                score -= 15;
                break;
            }
        }

        return Math.max(0.0, Math.min(100.0, score));
    }
}
