package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID>, JpaSpecificationExecutor<Income> {
    long countByUserId(UUID userId);
    List<Income> findByUserIdAndIncomeDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
}
