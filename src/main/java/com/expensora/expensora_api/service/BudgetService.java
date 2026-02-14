package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.BudgetDto;
import com.expensora.expensora_api.entity.Budget;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    Budget createBudget(Budget budget);
    Budget updateBudget(UUID id, Budget budget);
    void deleteBudget(UUID id);
    List<BudgetDto> getBudgetsForMonth(UUID userId, int month, int year);
    List<Budget> getBudgetHistory(UUID userId, UUID categoryId);
}
