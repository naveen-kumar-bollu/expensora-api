package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findByUserIdAndActive(UUID userId, Boolean active);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.active = true AND (r.lastExecutionDate IS NULL OR r.lastExecutionDate < :date)")
    List<RecurringTransaction> findPendingRecurringTransactions(LocalDate date);

}
