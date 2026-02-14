package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.DebtPaymentCreateRequestDto;
import com.expensora.expensora_api.dto.DebtPaymentDto;

import java.util.List;
import java.util.UUID;

public interface DebtPaymentService {
    DebtPaymentDto createPayment(DebtPaymentCreateRequestDto request, UUID userId);
    List<DebtPaymentDto> getPaymentsByDebt(UUID debtId, UUID userId);
    void deletePayment(UUID id, UUID userId);
}
