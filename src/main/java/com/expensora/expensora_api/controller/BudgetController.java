package com.expensora.expensora_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensora.expensora_api.dto.BudgetCreateRequestDto;
import com.expensora.expensora_api.dto.BudgetDto;
import com.expensora.expensora_api.entity.Budget;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/budgets")
@Tag(name = "Budgets", description = "Budget planning and tracking APIs")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a budget", description = "Set a budget limit for a category in a specific month/year")
    public ResponseEntity<Budget> create(@RequestBody BudgetCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(dto.getAmount());
        budget.setBudgetMonth(dto.getBudgetMonth());
        budget.setBudgetYear(dto.getBudgetYear());

        Budget saved = budgetService.createBudget(budget);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Get budgets for a month", description = "Get all budgets with spending information for a specific month/year")
    public ResponseEntity<List<BudgetDto>> getBudgets(@RequestParam int month, @RequestParam int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<BudgetDto> budgets = budgetService.getBudgetsForMonth(user.getId(), month, year);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/history")
    @Operation(summary = "Get budget history", description = "Get budget history for a specific category across multiple months")
    public ResponseEntity<List<Budget>> getBudgetHistory(@RequestParam UUID categoryId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> history = budgetService.getBudgetHistory(user.getId(), categoryId);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget", description = "Update the budget amount for an existing budget")
    public ResponseEntity<Budget> update(@PathVariable UUID id, @RequestBody BudgetCreateRequestDto dto) {
        Budget budget = new Budget();
        budget.setAmount(dto.getAmount());

        Budget updated = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget", description = "Delete a budget entry")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
