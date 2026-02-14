package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.DebtPaymentCreateRequestDto;
import com.expensora.expensora_api.dto.DebtPaymentDto;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.DebtPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/debt-payments")
@Tag(name = "Debt Payments", description = "Debt payment tracking APIs")
public class DebtPaymentController {

    @Autowired
    private DebtPaymentService debtPaymentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Record a debt payment", description = "Record a payment made towards a debt")
    public ResponseEntity<DebtPaymentDto> create(@RequestBody DebtPaymentCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        DebtPaymentDto payment = debtPaymentService.createPayment(dto, userId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/debt/{debtId}")
    @Operation(summary = "Get payments for a debt", description = "Get all payments made towards a specific debt")
    public ResponseEntity<List<DebtPaymentDto>> getByDebt(@PathVariable UUID debtId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        List<DebtPaymentDto> payments = debtPaymentService.getPaymentsByDebt(debtId, userId);
        return ResponseEntity.ok(payments);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment", description = "Remove a debt payment record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found")).getId();

        debtPaymentService.deletePayment(id, userId);
        return ResponseEntity.noContent().build();
    }
}
