package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseService {
    Page<Expense> findExpenses(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, 
                                String search, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Expense save(Expense expense);
    Optional<Expense> findById(UUID id);
    void deleteById(UUID id);
}