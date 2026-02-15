package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByUserId(UUID userId);
    long countByUserId(UUID userId);
    List<Expense> findByUserIdAndExpenseDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    List<Expense> findByUserIdAndCategoryIdAndExpenseDateBetween(UUID userId, UUID categoryId, LocalDateTime startDate, LocalDateTime endDate);
}