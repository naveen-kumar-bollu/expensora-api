package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Transfer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferService {
    Transfer createTransfer(Transfer transfer);
    List<Transfer> findByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate);
    List<Transfer> findByAccountId(UUID accountId);
    Optional<Transfer> findById(UUID id);
    void deleteTransfer(UUID id);
}
