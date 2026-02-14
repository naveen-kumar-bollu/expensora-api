package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Page<Expense> findExpenses(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId,
                                       String search, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        Specification<Expense> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
        }

        if (minAmount != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }

        if (maxAmount != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }

        return expenseRepository.findAll(spec, pageable);
    }

    @Override
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        return expenseRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        expenseRepository.deleteById(id);
    }
}