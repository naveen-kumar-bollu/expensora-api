package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.AccountCreateRequestDto;
import com.expensora.expensora_api.dto.AccountDto;
import com.expensora.expensora_api.entity.Account;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.AccountMapper;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.AccountService;
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
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account and wallet management APIs")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create an account", description = "Create a new account/wallet (bank, credit card, cash, etc.)")
    public ResponseEntity<AccountDto> create(@RequestBody AccountCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setName(dto.getName());
        account.setAccountType(dto.getAccountType());
        account.setInitialBalance(dto.getInitialBalance());
        account.setCurrency(dto.getCurrency());
        account.setIcon(dto.getIcon());
        account.setColor(dto.getColor());
        account.setIsDefault(dto.getIsDefault());

        Account saved = accountService.createAccount(account);
        return ResponseEntity.ok(accountMapper.toDto(saved));
    }

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Get all accounts for the authenticated user")
    public ResponseEntity<List<AccountDto>> getAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountService.findByUserId(user.getId());
        List<AccountDto> dtos = accounts.stream().map(accountMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active accounts", description = "Get all active accounts for the authenticated user")
    public ResponseEntity<List<AccountDto>> getActive() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountService.findActiveByUserId(user.getId());
        List<AccountDto> dtos = accounts.stream().map(accountMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Get a specific account by its ID")
    public ResponseEntity<AccountDto> getById(@PathVariable UUID id) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an account", description = "Update an existing account")
    public ResponseEntity<AccountDto> update(@PathVariable UUID id, @RequestBody AccountCreateRequestDto dto) {
        Account account = new Account();
        account.setName(dto.getName());
        account.setAccountType(dto.getAccountType());
        account.setCurrency(dto.getCurrency());
        account.setIcon(dto.getIcon());
        account.setColor(dto.getColor());
        account.setIsDefault(dto.getIsDefault());

        Account updated = accountService.updateAccount(id, account);
        return ResponseEntity.ok(accountMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account", description = "Deactivate an account (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
