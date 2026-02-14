package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.IncomeDto;
import com.expensora.expensora_api.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    public IncomeDto toDto(Income income) {
        IncomeDto dto = new IncomeDto();
        dto.setId(income.getId());
        dto.setAmount(income.getAmount());
        dto.setDescription(income.getDescription());
        dto.setCategoryId(income.getCategory().getId());
        dto.setCategoryName(income.getCategory().getName());
        dto.setUserId(income.getUser().getId());
        dto.setIncomeDate(income.getIncomeDate());
        dto.setNotes(income.getNotes());
        dto.setTags(income.getTags());
        dto.setCreatedAt(income.getCreatedAt());
        dto.setUpdatedAt(income.getUpdatedAt());
        return dto;
    }
}
