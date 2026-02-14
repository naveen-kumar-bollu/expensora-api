package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface IncomeService {
    Page<Income> findIncomes(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, Pageable pageable);
    Income save(Income income);
    Optional<Income> findById(UUID id);
    void deleteById(UUID id);
    BigDecimal getMonthlySummary(UUID userId, int month, int year);
}
