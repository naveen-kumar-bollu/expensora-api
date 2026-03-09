package com.expensora.expensora_api.service;

import java.util.List;
import java.util.UUID;

import com.expensora.expensora_api.dto.DebtCreateRequestDto;
import com.expensora.expensora_api.dto.DebtDto;

public interface DebtService {
    DebtDto createDebt(DebtCreateRequestDto request, UUID userId);
    DebtDto updateDebt(UUID id, DebtCreateRequestDto request, UUID userId);
    void deleteDebt(UUID id, UUID userId);
    DebtDto getDebt(UUID id, UUID userId);
    List<DebtDto> getAllDebts(UUID userId);
    List<DebtDto> getActiveDebts(UUID userId);
}
