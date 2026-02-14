package com.expensora.expensora_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensora.expensora_api.dto.CategoryBreakdownDto;
import com.expensora.expensora_api.dto.DashboardSummaryDto;
import com.expensora.expensora_api.dto.MonthlyTrendDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Dashboard analytics and summary APIs")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    @Operation(summary = "Get monthly summary", description = "Get dashboard summary with total income, expenses, and balance for a month")
    public ResponseEntity<DashboardSummaryDto> getSummary(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        DashboardSummaryDto summary = dashboardService.getMonthlySummary(user.getId(), month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Get category breakdown", description = "Get expense breakdown by category for a specific month")
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<CategoryBreakdownDto> breakdown = dashboardService.getCategoryBreakdown(user.getId(), month, year);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/monthly-trend")
    @Operation(summary = "Get monthly trend", description = "Get income and expense trends for all months in a year")
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrend(@RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<MonthlyTrendDto> trend = dashboardService.getMonthlyTrend(user.getId(), year);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/top-spending-category")
    @Operation(summary = "Get top spending category", description = "Get the category with the highest spending for a specific month")
    public ResponseEntity<String> getTopSpendingCategory(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String topCategory = dashboardService.getTopSpendingCategory(user.getId(), month, year);
        return ResponseEntity.ok(topCategory);
    }
}
