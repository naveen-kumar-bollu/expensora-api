package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    Account createAccount(Account account);
    List<Account> findByUserId(UUID userId);
    List<Account> findActiveByUserId(UUID userId);
    Optional<Account> findById(UUID id);
    Optional<Account> findDefaultAccountByUserId(UUID userId);
    Account updateAccount(UUID id, Account account);
    void deleteAccount(UUID id);
    void updateBalance(UUID accountId, BigDecimal amount, boolean isCredit);
}
