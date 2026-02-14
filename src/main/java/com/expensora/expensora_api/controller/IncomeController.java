package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.IncomeCreateRequestDto;
import com.expensora.expensora_api.dto.IncomeDto;
import com.expensora.expensora_api.dto.IncomeUpdateRequestDto;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.IncomeMapper;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/incomes")
@Tag(name = "Income", description = "Income tracking and management APIs")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeMapper incomeMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new income", description = "Record a new income transaction")
    public ResponseEntity<IncomeDto> create(@RequestBody IncomeCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        Income income = new Income();
        income.setAmount(dto.getAmount());
        income.setDescription(dto.getDescription());
        income.setCategory(category);
        income.setUser(user);
        income.setIncomeDate(dto.getIncomeDate());
        income.setNotes(dto.getNotes());
        income.setTags(dto.getTags());

        Income saved = incomeService.save(income);
        return ResponseEntity.ok(incomeMapper.toDto(saved));
    }

    @GetMapping
    @Operation(summary = "Get incomes with filters", description = "Get paginated incomes with optional filters (date range, category)")
    public ResponseEntity<Page<IncomeDto>> getIncomes(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Page<Income> incomes = incomeService.findIncomes(user.getId(), startDate, endDate, categoryId, pageable);
        Page<IncomeDto> dtos = incomes.map(incomeMapper::toDto);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get monthly income summary", description = "Get total income for a specific month and year")
    public ResponseEntity<BigDecimal> getMonthlySummary(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal summary = incomeService.getMonthlySummary(user.getId(), month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get income by ID", description = "Get a specific income by its ID")
    public ResponseEntity<IncomeDto> getIncome(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Income income = incomeService.findById(id).orElseThrow(() -> new RuntimeException("Income not found"));
        if (!income.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return ResponseEntity.ok(incomeMapper.toDto(income));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an income", description = "Update an existing income transaction")
    public ResponseEntity<IncomeDto> update(@PathVariable UUID id, @RequestBody IncomeUpdateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Income income = incomeService.findById(id).orElseThrow(() -> new RuntimeException("Income not found"));
        if (!income.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (dto.getAmount() != null) income.setAmount(dto.getAmount());
        if (dto.getDescription() != null) income.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
            income.setCategory(category);
        }
        if (dto.getIncomeDate() != null) income.setIncomeDate(dto.getIncomeDate());
        if (dto.getNotes() != null) income.setNotes(dto.getNotes());
        if (dto.getTags() != null) income.setTags(dto.getTags());

        Income saved = incomeService.save(income);
        return ResponseEntity.ok(incomeMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an income", description = "Delete a specific income transaction")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Income income = incomeService.findById(id).orElseThrow(() -> new RuntimeException("Income not found"));
        if (!income.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        incomeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
