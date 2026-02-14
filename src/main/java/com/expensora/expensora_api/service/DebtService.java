package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.DebtCreateRequestDto;
import com.expensora.expensora_api.dto.DebtDto;
import com.expensora.expensora_api.entity.Debt;

import java.util.List;
import java.util.UUID;

public interface DebtService {
    DebtDto createDebt(DebtCreateRequestDto request, UUID userId);
    DebtDto updateDebt(UUID id, DebtCreateRequestDto request, UUID userId);
    void deleteDebt(UUID id, UUID userId);
    DebtDto getDebt(UUID id, UUID userId);
    List<DebtDto> getAllDebts(UUID userId);
    List<DebtDto> getActiveDebts(UUID userId);
}
