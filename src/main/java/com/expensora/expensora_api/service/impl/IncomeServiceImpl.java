package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.service.IncomeService;
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
public class IncomeServiceImpl implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public Page<Income> findIncomes(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, Pageable pageable) {
        Specification<Income> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("incomeDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("incomeDate"), endDate));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        return incomeRepository.findAll(spec, pageable);
    }

    @Override
    public Income save(Income income) {
        return incomeRepository.save(income);
    }

    @Override
    public Optional<Income> findById(UUID id) {
        return incomeRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        incomeRepository.deleteById(id);
    }

    @Override
    public BigDecimal getMonthlySummary(UUID userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return incomeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.between(root.get("incomeDate"), startDate, endDate)
                )
        ).stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
