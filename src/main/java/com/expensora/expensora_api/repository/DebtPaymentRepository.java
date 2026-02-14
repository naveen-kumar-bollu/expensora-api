package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Debt;
import com.expensora.expensora_api.entity.DebtPayment;
import com.expensora.expensora_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {
    List<DebtPayment> findByDebtOrderByPaymentDateDesc(Debt debt);
    List<DebtPayment> findByUser(User user);
}
