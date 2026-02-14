package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Account;
import com.expensora.expensora_api.repository.AccountRepository;
import com.expensora.expensora_api.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        // Set initial values
        if (account.getActive() == null) {
            account.setActive(true);
        }
        if (account.getCurrentBalance() == null) {
            account.setCurrentBalance(account.getInitialBalance());
        }
        
        // If this is set as default, unset other default accounts
        if (Boolean.TRUE.equals(account.getIsDefault())) {
            Optional<Account> existingDefault = accountRepository.findByUserIdAndIsDefaultTrue(account.getUser().getId());
            existingDefault.ifPresent(acc -> {
                acc.setIsDefault(false);
                accountRepository.save(acc);
            });
        }
        
        return accountRepository.save(account);
    }

    @Override
    public List<Account> findByUserId(UUID userId) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Account> findActiveByUserId(UUID userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> findDefaultAccountByUserId(UUID userId) {
        return accountRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    @Transactional
    public Account updateAccount(UUID id, Account account) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        existing.setName(account.getName());
        existing.setAccountType(account.getAccountType());
        existing.setIcon(account.getIcon());
        existing.setColor(account.getColor());
        existing.setCurrency(account.getCurrency());
        
        // If this is set as default, unset other default accounts
        if (Boolean.TRUE.equals(account.getIsDefault()) && !Boolean.TRUE.equals(existing.getIsDefault())) {
            Optional<Account> existingDefault = accountRepository.findByUserIdAndIsDefaultTrue(existing.getUser().getId());
            existingDefault.ifPresent(acc -> {
                if (!acc.getId().equals(id)) {
                    acc.setIsDefault(false);
                    accountRepository.save(acc);
                }
            });
        }
        existing.setIsDefault(account.getIsDefault());
        
        return accountRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateBalance(UUID accountId, BigDecimal amount, boolean isCredit) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (isCredit) {
            account.setCurrentBalance(account.getCurrentBalance().add(amount));
        } else {
            account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        }
        
        accountRepository.save(account);
    }
}
