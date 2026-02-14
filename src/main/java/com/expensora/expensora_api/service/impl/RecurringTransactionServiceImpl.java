package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.*;
import com.expensora.expensora_api.repository.RecurringTransactionRepository;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.service.RecurringTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public RecurringTransaction create(RecurringTransaction transaction) {
        transaction.setActive(true);
        return recurringTransactionRepository.save(transaction);
    }

    @Override
    public RecurringTransaction update(UUID id, RecurringTransaction transaction) {
        RecurringTransaction existing = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        existing.setAmount(transaction.getAmount());
        existing.setDescription(transaction.getDescription());
        existing.setFrequency(transaction.getFrequency());
        existing.setEndDate(transaction.getEndDate());
        existing.setActive(transaction.getActive());
        return recurringTransactionRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        recurringTransactionRepository.deleteById(id);
    }

    @Override
    public List<RecurringTransaction> findByUser(UUID userId) {
        return recurringTransactionRepository.findByUserIdAndActive(userId, true);
    }

    @Override
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> pending = recurringTransactionRepository.findPendingRecurringTransactions(today);

        for (RecurringTransaction rt : pending) {
            if (shouldExecute(rt, today)) {
                executeTransaction(rt, today);
                rt.setLastExecutionDate(today);
                recurringTransactionRepository.save(rt);
            }
        }
    }

    private boolean shouldExecute(RecurringTransaction rt, LocalDate today) {
        if (rt.getEndDate() != null && today.isAfter(rt.getEndDate())) {
            rt.setActive(false);
            return false;
        }

        if (rt.getLastExecutionDate() == null) {
            return !today.isBefore(rt.getStartDate());
        }

        return switch (rt.getFrequency()) {
            case DAILY -> rt.getLastExecutionDate().isBefore(today);
            case WEEKLY -> rt.getLastExecutionDate().plusWeeks(1).isBefore(today) ||
                    rt.getLastExecutionDate().plusWeeks(1).equals(today);
            case MONTHLY -> rt.getLastExecutionDate().plusMonths(1).isBefore(today) ||
                    rt.getLastExecutionDate().plusMonths(1).equals(today);
        };
    }

    private void executeTransaction(RecurringTransaction rt, LocalDate date) {
        if (rt.getTransactionType() == TransactionType.EXPENSE) {
            Expense expense = new Expense();
            expense.setUser(rt.getUser());
            expense.setCategory(rt.getCategory());
            expense.setAmount(rt.getAmount());
            expense.setDescription(rt.getDescription() + " (Recurring)");
            expense.setExpenseDate(date);
            expenseRepository.save(expense);
        } else {
            Income income = new Income();
            income.setUser(rt.getUser());
            income.setCategory(rt.getCategory());
            income.setAmount(rt.getAmount());
            income.setDescription(rt.getDescription() + " (Recurring)");
            income.setIncomeDate(date);
            incomeRepository.save(income);
        }
    }
}
