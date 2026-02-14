package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Transfer;
import com.expensora.expensora_api.repository.TransferRepository;
import com.expensora.expensora_api.service.AccountService;
import com.expensora.expensora_api.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountService accountService;

    @Override
    @Transactional
    public Transfer createTransfer(Transfer transfer) {
        // Debit from source account
        accountService.updateBalance(transfer.getFromAccount().getId(), transfer.getAmount(), false);
        
        // Credit to destination account
        accountService.updateBalance(transfer.getToAccount().getId(), transfer.getAmount(), true);
        
        return transferRepository.save(transfer);
    }

    @Override
    public List<Transfer> findByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transferRepository.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Transfer> findByAccountId(UUID accountId) {
        return transferRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<Transfer> findById(UUID id) {
        return transferRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteTransfer(UUID id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        
        // Reverse the transfer
        accountService.updateBalance(transfer.getFromAccount().getId(), transfer.getAmount(), true);
        accountService.updateBalance(transfer.getToAccount().getId(), transfer.getAmount(), false);
        
        transferRepository.delete(transfer);
    }
}
