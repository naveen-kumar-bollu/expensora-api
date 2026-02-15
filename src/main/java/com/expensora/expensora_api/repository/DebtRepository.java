package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Debt;
import com.expensora.expensora_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DebtRepository extends JpaRepository<Debt, UUID> {
    List<Debt> findByUserId(UUID userId);
    List<Debt> findByUserAndIsActiveTrue(User user);
    List<Debt> findByUser(User user);
}
