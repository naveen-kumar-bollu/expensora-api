package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.RecurringTransaction;

import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {
    RecurringTransaction create(RecurringTransaction transaction);
    RecurringTransaction update(UUID id, RecurringTransaction transaction);
    void delete(UUID id);
    List<RecurringTransaction> findByUser(UUID userId);
    void processRecurringTransactions();
}
