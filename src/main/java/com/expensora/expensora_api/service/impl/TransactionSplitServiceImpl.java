package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.TransactionSplitCreateRequestDto;
import com.expensora.expensora_api.dto.TransactionSplitDto;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.entity.TransactionSplit;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.repository.TransactionSplitRepository;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.TransactionSplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionSplitServiceImpl implements TransactionSplitService {

    @Autowired
    private TransactionSplitRepository transactionSplitRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public TransactionSplitDto createSplit(TransactionSplitCreateRequestDto request, UUID userId) {
        TransactionSplit split = new TransactionSplit();
        
        User user = userRepository.findById(userId).orElseThrow();
        split.setUser(user);

        if (request.getExpenseId() != null) {
            Expense expense = expenseRepository.findById(request.getExpenseId()).orElseThrow();
            split.setExpense(expense);
        }

        if (request.getIncomeId() != null) {
            Income income = incomeRepository.findById(request.getIncomeId()).orElseThrow();
            split.setIncome(income);
        }

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
        split.setCategory(category);

        split.setAmount(request.getAmount());
        split.setPercentage(request.getPercentage());
        split.setDescription(request.getDescription());

        TransactionSplit saved = transactionSplitRepository.save(split);
        return mapToDto(saved);
    }

    @Override
    public List<TransactionSplitDto> getSplitsByExpenseId(UUID expenseId) {
        return transactionSplitRepository.findByExpenseId(expenseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionSplitDto> getSplitsByIncomeId(UUID incomeId) {
        return transactionSplitRepository.findByIncomeId(incomeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionSplitDto> getSplitsByUserId(UUID userId) {
        return transactionSplitRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSplit(UUID id, UUID userId) {
        transactionSplitRepository.deleteById(id);
    }

    private TransactionSplitDto mapToDto(TransactionSplit split) {
        TransactionSplitDto dto = new TransactionSplitDto();
        dto.setId(split.getId());
        dto.setExpenseId(split.getExpense() != null ? split.getExpense().getId() : null);
        dto.setIncomeId(split.getIncome() != null ? split.getIncome().getId() : null);
        dto.setCategoryId(split.getCategory().getId());
        dto.setCategoryName(split.getCategory().getName());
        dto.setAmount(split.getAmount());
        dto.setPercentage(split.getPercentage());
        dto.setDescription(split.getDescription());
        dto.setUserId(split.getUser().getId());
        dto.setCreatedAt(split.getCreatedAt());
        dto.setUpdatedAt(split.getUpdatedAt());
        return dto;
    }
}
