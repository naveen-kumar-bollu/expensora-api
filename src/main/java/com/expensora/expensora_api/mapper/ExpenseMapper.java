package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.ExpenseDto;
import com.expensora.expensora_api.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseDto toDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setCategoryId(expense.getCategory().getId());
        dto.setCategoryName(expense.getCategory().getName());
        dto.setUserId(expense.getUser().getId());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setNotes(expense.getNotes());
        dto.setTags(expense.getTags());
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setUpdatedAt(expense.getUpdatedAt());
        return dto;
    }
}