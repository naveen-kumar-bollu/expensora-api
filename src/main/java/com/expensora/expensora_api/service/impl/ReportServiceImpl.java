package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.DashboardSummaryDto;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.service.DashboardService;
import com.expensora.expensora_api.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private DashboardService dashboardService;

    @Override
    public ByteArrayOutputStream exportExpensesToCsv(UUID userId, Integer month, Integer year) {
        LocalDate startDate = month != null && year != null ? LocalDate.of(year, month, 1) : null;
        LocalDate endDate = startDate != null ? startDate.plusMonths(1).minusDays(1) : null;

        List<Expense> expenses = expenseRepository.findAll(
                (root, query, cb) -> {
                    if (startDate != null && endDate != null) {
                        return cb.and(
                                cb.equal(root.get("user").get("id"), userId),
                                cb.between(root.get("expenseDate"), startDate, endDate)
                        );
                    }
                    return cb.equal(root.get("user").get("id"), userId);
                }
        );

        return generateCsv(expenses, "Expense");
    }

    @Override
    public ByteArrayOutputStream exportIncomeToCsv(UUID userId, Integer month, Integer year) {
        LocalDate startDate = month != null && year != null ? LocalDate.of(year, month, 1) : null;
        LocalDate endDate = startDate != null ? startDate.plusMonths(1).minusDays(1) : null;

        List<Income> incomes = incomeRepository.findAll(
                (root, query, cb) -> {
                    if (startDate != null && endDate != null) {
                        return cb.and(
                                cb.equal(root.get("user").get("id"), userId),
                                cb.between(root.get("incomeDate"), startDate, endDate)
                        );
                    }
                    return cb.equal(root.get("user").get("id"), userId);
                }
        );

        return generateIncomeCsv(incomes);
    }

    @Override
    public ByteArrayOutputStream generateMonthlySummaryReport(UUID userId, Integer month, Integer year) {
        DashboardSummaryDto summary = dashboardService.getMonthlySummary(userId, month, year);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Monthly Financial Summary Report");
        writer.println("Month: " + month + "/" + year);
        writer.println("================================");
        writer.println("Total Income: $" + summary.getMonthlyIncome());
        writer.println("Total Expenses: $" + summary.getMonthlyExpenses());
        writer.println("Net Savings: $" + summary.getNetSavings());
        writer.println("================================");

        writer.flush();
        return out;
    }

    private ByteArrayOutputStream generateCsv(List<Expense> expenses, String type) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Date,Category,Amount,Description,Notes,Tags");
        for (Expense expense : expenses) {
            writer.printf("%s,%s,%.2f,%s,%s,%s%n",
                    expense.getExpenseDate(),
                    expense.getCategory().getName(),
                    expense.getAmount(),
                    expense.getDescription() != null ? expense.getDescription() : "",
                    expense.getNotes() != null ? expense.getNotes() : "",
                    expense.getTags() != null ? expense.getTags() : ""
            );
        }

        writer.flush();
        return out;
    }

    private ByteArrayOutputStream generateIncomeCsv(List<Income> incomes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Date,Category,Amount,Description,Notes,Tags");
        for (Income income : incomes) {
            writer.printf("%s,%s,%.2f,%s,%s,%s%n",
                    income.getIncomeDate(),
                    income.getCategory().getName(),
                    income.getAmount(),
                    income.getDescription() != null ? income.getDescription() : "",
                    income.getNotes() != null ? income.getNotes() : "",
                    income.getTags() != null ? income.getTags() : ""
            );
        }

        writer.flush();
        return out;
    }
}
