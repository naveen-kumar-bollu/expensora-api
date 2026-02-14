package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.TransferCreateRequestDto;
import com.expensora.expensora_api.dto.TransferDto;
import com.expensora.expensora_api.entity.Account;
import com.expensora.expensora_api.entity.Transfer;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.TransferMapper;
import com.expensora.expensora_api.repository.AccountRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfers", description = "Money transfer between accounts APIs")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferMapper transferMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a transfer", description = "Transfer money from one account to another")
    public ResponseEntity<TransferDto> create(@RequestBody TransferCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Account fromAccount = accountRepository.findById(dto.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));
        Account toAccount = accountRepository.findById(dto.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        // Verify accounts belong to user
        if (!fromAccount.getUser().getId().equals(user.getId()) || !toAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        Transfer transfer = new Transfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setAmount(dto.getAmount());
        transfer.setDescription(dto.getDescription());
        transfer.setTransferDate(dto.getTransferDate());

        Transfer saved = transferService.createTransfer(transfer);
        return ResponseEntity.ok(transferMapper.toDto(saved));
    }

    @GetMapping
    @Operation(summary = "Get transfers", description = "Get transfers for the authenticated user within a date range")
    public ResponseEntity<List<TransferDto>> getTransfers(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<Transfer> transfers = transferService.findByUserAndDateRange(user.getId(), start, end);
        List<TransferDto> dtos = transfers.stream().map(transferMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transfers by account", description = "Get all transfers for a specific account")
    public ResponseEntity<List<TransferDto>> getByAccount(@PathVariable UUID accountId) {
        List<Transfer> transfers = transferService.findByAccountId(accountId);
        List<TransferDto> dtos = transfers.stream().map(transferMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transfer", description = "Delete a transfer and reverse the balance changes")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transferService.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }
}
