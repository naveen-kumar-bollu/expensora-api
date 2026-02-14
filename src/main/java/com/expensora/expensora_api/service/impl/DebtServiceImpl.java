package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.DebtCreateRequestDto;
import com.expensora.expensora_api.dto.DebtDto;
import com.expensora.expensora_api.entity.Debt;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.entity.Account;
import com.expensora.expensora_api.repository.DebtRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.repository.AccountRepository;
import com.expensora.expensora_api.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DebtServiceImpl implements DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public DebtDto createDebt(DebtCreateRequestDto request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Debt debt = new Debt();
        debt.setName(request.getName());
        debt.setDebtType(request.getDebtType());
        debt.setPrincipalAmount(request.getPrincipalAmount());
        debt.setCurrentBalance(request.getCurrentBalance());
        debt.setInterestRate(request.getInterestRate());
        debt.setMinimumPayment(request.getMinimumPayment());
        debt.setStartDate(request.getStartDate());
        debt.setTargetPayoffDate(request.getTargetPayoffDate());
        debt.setNotes(request.getNotes());
        debt.setIsActive(true);
        debt.setUser(user);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                .orElse(null);
            debt.setAccount(account);
        }

        debt = debtRepository.save(debt);
        return mapToDto(debt);
    }

    @Override
    public DebtDto updateDebt(UUID id, DebtCreateRequestDto request, UUID userId) {
        Debt debt = debtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Debt not found"));

        if (!debt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        debt.setName(request.getName());
        debt.setDebtType(request.getDebtType());
        debt.setPrincipalAmount(request.getPrincipalAmount());
        debt.setCurrentBalance(request.getCurrentBalance());
        debt.setInterestRate(request.getInterestRate());
        debt.setMinimumPayment(request.getMinimumPayment());
        debt.setStartDate(request.getStartDate());
        debt.setTargetPayoffDate(request.getTargetPayoffDate());
        debt.setNotes(request.getNotes());

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                .orElse(null);
            debt.setAccount(account);
        }

        debt = debtRepository.save(debt);
        return mapToDto(debt);
    }

    @Override
    public void deleteDebt(UUID id, UUID userId) {
        Debt debt = debtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Debt not found"));

        if (!debt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        debtRepository.delete(debt);
    }

    @Override
    public DebtDto getDebt(UUID id, UUID userId) {
        Debt debt = debtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Debt not found"));

        if (!debt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToDto(debt);
    }

    @Override
    public List<DebtDto> getAllDebts(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return debtRepository.findByUser(user).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<DebtDto> getActiveDebts(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return debtRepository.findByUserAndIsActiveTrue(user).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private DebtDto mapToDto(Debt debt) {
        DebtDto dto = new DebtDto();
        dto.setId(debt.getId());
        dto.setName(debt.getName());
        dto.setDebtType(debt.getDebtType());
        dto.setPrincipalAmount(debt.getPrincipalAmount());
        dto.setCurrentBalance(debt.getCurrentBalance());
        dto.setInterestRate(debt.getInterestRate());
        dto.setMinimumPayment(debt.getMinimumPayment());
        dto.setStartDate(debt.getStartDate());
        dto.setTargetPayoffDate(debt.getTargetPayoffDate());
        dto.setNotes(debt.getNotes());
        dto.setIsActive(debt.getIsActive());
        dto.setUserId(debt.getUser().getId());
        
        if (debt.getAccount() != null) {
            dto.setAccountId(debt.getAccount().getId());
            dto.setAccountName(debt.getAccount().getName());
        }
        
        dto.setCreatedAt(debt.getCreatedAt());
        dto.setUpdatedAt(debt.getUpdatedAt());
        return dto;
    }
}
