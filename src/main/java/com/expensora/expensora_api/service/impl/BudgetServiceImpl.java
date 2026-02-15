package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.BudgetDto;
import com.expensora.expensora_api.entity.Budget;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.repository.BudgetRepository;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget updateBudget(UUID id, Budget budget) {
        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        existing.setAmount(budget.getAmount());
        return budgetRepository.save(existing);
    }

    @Override
    public void deleteBudget(UUID id) {
        budgetRepository.deleteById(id);
    }

    @Override
    public List<BudgetDto> getBudgetsForMonth(UUID userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndBudgetMonthAndBudgetYear(userId, month, year);
        
        return budgets.stream().map(budget -> {
            BigDecimal spent = getSpentAmount(userId, budget.getCategory().getId(), month, year);
            BudgetDto dto = new BudgetDto();
            dto.setId(budget.getId());
            dto.setUserId(budget.getUser().getId());
            dto.setCategoryId(budget.getCategory().getId());
            dto.setCategoryName(budget.getCategory().getName());
            dto.setAmount(budget.getAmount());
            dto.setBudgetMonth(budget.getBudgetMonth());
            dto.setBudgetYear(budget.getBudgetYear());
            dto.setSpent(spent);
            dto.setPercentage(calculatePercentage(spent, budget.getAmount()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Budget> getBudgetHistory(UUID userId, UUID categoryId) {
        return budgetRepository.findBudgetHistory(userId, categoryId);
    }

    private BigDecimal getSpentAmount(UUID userId, UUID categoryId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return expenseRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.equal(root.get("category").get("id"), categoryId),
                        cb.between(root.get("expenseDate"), startDate, endDate)
                )
        ).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double calculatePercentage(BigDecimal spent, BigDecimal budget) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spent.divide(budget, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }
}
