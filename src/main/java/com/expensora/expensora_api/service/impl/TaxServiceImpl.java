package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.TaxReportDto;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaxServiceImpl implements TaxService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<TaxReportDto> getTaxReport(UUID userId, Integer year) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        Specification<Expense> spec = Specification.where((root, query, cb) -> cb.conjunction());
        spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
        spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
        spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isTaxDeductible")));

        List<Expense> expenses = expenseRepository.findAll(spec);

        Map<String, List<Expense>> grouped = expenses.stream()
            .collect(Collectors.groupingBy(e -> 
                e.getTaxCategory() != null ? e.getTaxCategory() : "Uncategorized"));

        return grouped.entrySet().stream()
            .map(entry -> {
                TaxReportDto dto = new TaxReportDto();
                dto.setTaxCategory(entry.getKey());
                dto.setTransactionCount(entry.getValue().size());
                dto.setTotalAmount(entry.getValue().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxReportDto> getQuarterlyTaxReport(UUID userId, Integer year, Integer quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = LocalDate.of(year, endMonth, 1).plusMonths(1).minusDays(1);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Specification<Expense> spec = Specification.where((root, query, cb) -> cb.conjunction());
        spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
        spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
        spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isTaxDeductible")));

        List<Expense> expenses = expenseRepository.findAll(spec);

        Map<String, List<Expense>> grouped = expenses.stream()
            .collect(Collectors.groupingBy(e -> 
                e.getTaxCategory() != null ? e.getTaxCategory() : "Uncategorized"));

        return grouped.entrySet().stream()
            .map(entry -> {
                TaxReportDto dto = new TaxReportDto();
                dto.setTaxCategory(entry.getKey());
                dto.setTransactionCount(entry.getValue().size());
                dto.setTotalAmount(entry.getValue().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                return dto;
            })
            .collect(Collectors.toList());
    }
}
