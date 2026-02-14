package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.RecurringTransactionCreateRequestDto;
import com.expensora.expensora_api.dto.RecurringTransactionDto;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.RecurringTransaction;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recurring-transactions")
@Tag(name = "Recurring Transactions", description = "Recurring income and expense management APIs")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create recurring transaction", description = "Set up a recurring income or expense transaction (daily, weekly, or monthly)")
    public ResponseEntity<RecurringTransaction> create(@RequestBody RecurringTransactionCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringTransaction transaction = new RecurringTransaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionType(dto.getTransactionType());
        transaction.setFrequency(dto.getFrequency());
        transaction.setStartDate(dto.getStartDate());
        transaction.setEndDate(dto.getEndDate());

        RecurringTransaction saved = recurringTransactionService.create(transaction);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Get all recurring transactions", description = "Get all recurring transactions for the authenticated user")
    public ResponseEntity<List<RecurringTransactionDto>> getAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<RecurringTransaction> transactions = recurringTransactionService.findByUser(user.getId());
        List<RecurringTransactionDto> dtos = transactions.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recurring transaction", description = "Update an existing recurring transaction")
    public ResponseEntity<RecurringTransaction> update(@PathVariable UUID id, @RequestBody RecurringTransactionCreateRequestDto dto) {
        RecurringTransaction transaction = new RecurringTransaction();
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setFrequency(dto.getFrequency());
        transaction.setEndDate(dto.getEndDate());

        RecurringTransaction updated = recurringTransactionService.update(id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recurring transaction", description = "Delete a recurring transaction")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private RecurringTransactionDto toDto(RecurringTransaction rt) {
        RecurringTransactionDto dto = new RecurringTransactionDto();
        dto.setId(rt.getId());
        dto.setUserId(rt.getUser().getId());
        dto.setCategoryId(rt.getCategory().getId());
        dto.setCategoryName(rt.getCategory().getName());
        dto.setAmount(rt.getAmount());
        dto.setDescription(rt.getDescription());
        dto.setTransactionType(rt.getTransactionType());
        dto.setFrequency(rt.getFrequency());
        dto.setStartDate(rt.getStartDate());
        dto.setEndDate(rt.getEndDate());
        dto.setLastExecutionDate(rt.getLastExecutionDate());
        dto.setActive(rt.getActive());
        dto.setCreatedAt(rt.getCreatedAt());
        return dto;
    }
}
