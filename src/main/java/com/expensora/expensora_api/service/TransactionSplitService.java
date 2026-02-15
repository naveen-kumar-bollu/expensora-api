package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.TransactionSplitCreateRequestDto;
import com.expensora.expensora_api.dto.TransactionSplitDto;

import java.util.List;
import java.util.UUID;

public interface TransactionSplitService {
    TransactionSplitDto createSplit(TransactionSplitCreateRequestDto request, UUID userId);
    List<TransactionSplitDto> getSplitsByExpenseId(UUID expenseId);
    List<TransactionSplitDto> getSplitsByIncomeId(UUID incomeId);
    List<TransactionSplitDto> getSplitsByUserId(UUID userId);
    void deleteSplit(UUID id, UUID userId);
}
