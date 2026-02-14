package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.CategoryBreakdownDto;
import com.expensora.expensora_api.dto.DashboardSummaryDto;
import com.expensora.expensora_api.dto.MonthlyTrendDto;

import java.util.List;
import java.util.UUID;

public interface DashboardService {
    DashboardSummaryDto getMonthlySummary(UUID userId, int month, int year);
    List<CategoryBreakdownDto> getCategoryBreakdown(UUID userId, int month, int year);
    List<MonthlyTrendDto> getMonthlyTrend(UUID userId, int year);
    String getTopSpendingCategory(UUID userId, int month, int year);
}
