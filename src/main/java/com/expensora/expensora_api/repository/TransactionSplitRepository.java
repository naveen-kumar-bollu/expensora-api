package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.TransactionSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TransactionSplitRepository extends JpaRepository<TransactionSplit, UUID>, JpaSpecificationExecutor<TransactionSplit> {
    
    List<TransactionSplit> findByExpenseId(UUID expenseId);
    
    List<TransactionSplit> findByIncomeId(UUID incomeId);
    
    List<TransactionSplit> findByUserId(UUID userId);
    
}
