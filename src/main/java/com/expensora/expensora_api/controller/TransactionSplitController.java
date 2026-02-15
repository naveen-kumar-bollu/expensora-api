package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.TransactionSplitCreateRequestDto;
import com.expensora.expensora_api.dto.TransactionSplitDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.TransactionSplitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction-splits")
@Tag(name = "Transaction Splits", description = "Split transactions across categories APIs")
public class TransactionSplitController {

    @Autowired
    private TransactionSplitService transactionSplitService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a transaction split", description = "Split a transaction across multiple categories")
    public ResponseEntity<TransactionSplitDto> createSplit(@RequestBody TransactionSplitCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        TransactionSplitDto created = transactionSplitService.createSplit(dto, user.getId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/expense/{expenseId}")
    @Operation(summary = "Get splits by expense", description = "Get all splits for a specific expense")
    public ResponseEntity<List<TransactionSplitDto>> getSplitsByExpense(@PathVariable UUID expenseId) {
        List<TransactionSplitDto> splits = transactionSplitService.getSplitsByExpenseId(expenseId);
        return ResponseEntity.ok(splits);
    }

    @GetMapping("/income/{incomeId}")
    @Operation(summary = "Get splits by income", description = "Get all splits for a specific income")
    public ResponseEntity<List<TransactionSplitDto>> getSplitsByIncome(@PathVariable UUID incomeId) {
        List<TransactionSplitDto> splits = transactionSplitService.getSplitsByIncomeId(incomeId);
        return ResponseEntity.ok(splits);
    }

    @GetMapping("/my-splits")
    @Operation(summary = "Get my splits", description = "Get all splits for the current user")
    public ResponseEntity<List<TransactionSplitDto>> getMySplits() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<TransactionSplitDto> splits = transactionSplitService.getSplitsByUserId(user.getId());
        return ResponseEntity.ok(splits);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a split", description = "Delete a transaction split")
    public ResponseEntity<Void> deleteSplit(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        transactionSplitService.deleteSplit(id, user.getId());
        return ResponseEntity.ok().build();
    }
}
