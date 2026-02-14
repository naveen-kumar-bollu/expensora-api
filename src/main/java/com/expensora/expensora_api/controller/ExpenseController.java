package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.ExpenseCreateRequestDto;
import com.expensora.expensora_api.dto.ExpenseDto;
import com.expensora.expensora_api.dto.ExpenseUpdateRequestDto;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.ExpenseMapper;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expenses", description = "Expense tracking and management APIs")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Record a new expense transaction")
    public ResponseEntity<ExpenseDto> create(@RequestBody ExpenseCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = new Expense();
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setCategory(category);
        expense.setUser(user);
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setNotes(dto.getNotes());
        expense.setTags(dto.getTags());

        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @PostMapping("/bulk-delete")
    @Operation(summary = "Bulk delete expenses", description = "Delete multiple expenses at once by providing a list of IDs")
    public ResponseEntity<Void> bulkDelete(@RequestBody java.util.List<UUID> ids) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        for (UUID id : ids) {
            Expense expense = expenseService.findById(id).orElse(null);
            if (expense != null && expense.getUser().getId().equals(user.getId())) {
                expenseService.deleteById(id);
            }
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get expenses with filters", description = "Get paginated expenses with optional filters (date range, category, search, amount range)")
    public ResponseEntity<Page<ExpenseDto>> getExpenses(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Page<Expense> expenses = expenseService.findExpenses(user.getId(), startDate, endDate, categoryId, search, minAmount, maxAmount, pageable);
        Page<ExpenseDto> dtos = expenses.map(expenseMapper::toDto);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Get a specific expense by its ID")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return ResponseEntity.ok(expenseMapper.toDto(expense));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense", description = "Update an existing expense")
    public ResponseEntity<ExpenseDto> update(@PathVariable UUID id, @RequestBody ExpenseUpdateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (dto.getAmount() != null) expense.setAmount(dto.getAmount());
        if (dto.getDescription() != null) expense.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
            expense.setCategory(category);
        }
        if (dto.getExpenseDate() != null) expense.setExpenseDate(dto.getExpenseDate());
        if (dto.getNotes() != null) expense.setNotes(dto.getNotes());
        if (dto.getTags() != null) expense.setTags(dto.getTags());

        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Delete a specific expense")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        expenseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}