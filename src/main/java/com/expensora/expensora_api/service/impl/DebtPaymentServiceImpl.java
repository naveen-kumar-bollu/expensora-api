package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.DebtPaymentCreateRequestDto;
import com.expensora.expensora_api.dto.DebtPaymentDto;
import com.expensora.expensora_api.entity.Debt;
import com.expensora.expensora_api.entity.DebtPayment;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.DebtPaymentRepository;
import com.expensora.expensora_api.repository.DebtRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.DebtPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DebtPaymentServiceImpl implements DebtPaymentService {

    @Autowired
    private DebtPaymentRepository debtPaymentRepository;

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public DebtPaymentDto createPayment(DebtPaymentCreateRequestDto request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Debt debt = debtRepository.findById(request.getDebtId())
            .orElseThrow(() -> new RuntimeException("Debt not found"));

        if (!debt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(request.getAmount());
        payment.setPrincipalPaid(request.getPrincipalPaid());
        payment.setInterestPaid(request.getInterestPaid());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setNotes(request.getNotes());
        payment.setUser(user);

        payment = debtPaymentRepository.save(payment);

        // Update debt current balance
        BigDecimal newBalance = debt.getCurrentBalance().subtract(request.getPrincipalPaid());
        debt.setCurrentBalance(newBalance);
        debtRepository.save(debt);

        return mapToDto(payment);
    }

    @Override
    public List<DebtPaymentDto> getPaymentsByDebt(UUID debtId, UUID userId) {
        Debt debt = debtRepository.findById(debtId)
            .orElseThrow(() -> new RuntimeException("Debt not found"));

        if (!debt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return debtPaymentRepository.findByDebtOrderByPaymentDateDesc(debt).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public void deletePayment(UUID id, UUID userId) {
        DebtPayment payment = debtPaymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Restore debt balance
        Debt debt = payment.getDebt();
        BigDecimal newBalance = debt.getCurrentBalance().add(payment.getPrincipalPaid());
        debt.setCurrentBalance(newBalance);
        debtRepository.save(debt);

        debtPaymentRepository.delete(payment);
    }

    private DebtPaymentDto mapToDto(DebtPayment payment) {
        DebtPaymentDto dto = new DebtPaymentDto();
        dto.setId(payment.getId());
        dto.setDebtId(payment.getDebt().getId());
        dto.setDebtName(payment.getDebt().getName());
        dto.setAmount(payment.getAmount());
        dto.setPrincipalPaid(payment.getPrincipalPaid());
        dto.setInterestPaid(payment.getInterestPaid());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());
        dto.setUserId(payment.getUser().getId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}
