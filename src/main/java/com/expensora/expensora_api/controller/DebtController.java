package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.DebtCreateRequestDto;
import com.expensora.expensora_api.dto.DebtDto;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/debts")
@Tag(name = "Debts", description = "Debt and loan tracking APIs")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new debt", description = "Record a new debt or loan")
    public ResponseEntity<DebtDto> create(@RequestBody DebtCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        DebtDto debt = debtService.createDebt(dto, userId);
        return ResponseEntity.ok(debt);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a debt", description = "Update existing debt information")
    public ResponseEntity<DebtDto> update(@PathVariable UUID id, @RequestBody DebtCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        DebtDto debt = debtService.updateDebt(id, dto, userId);
        return ResponseEntity.ok(debt);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a debt", description = "Remove a debt record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        debtService.deleteDebt(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get debt details", description = "Get detailed information about a specific debt")
    public ResponseEntity<DebtDto> getDebt(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        DebtDto debt = debtService.getDebt(id, userId);
        return ResponseEntity.ok(debt);
    }

    @GetMapping
    @Operation(summary = "Get all debts", description = "Get all debts for the current user")
    public ResponseEntity<List<DebtDto>> getAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<DebtDto> debts = debtService.getAllDebts(userId);
        return ResponseEntity.ok(debts);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active debts", description = "Get only active debts")
    public ResponseEntity<List<DebtDto>> getActive() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<DebtDto> debts = debtService.getActiveDebts(userId);
        return ResponseEntity.ok(debts);
    }
}
