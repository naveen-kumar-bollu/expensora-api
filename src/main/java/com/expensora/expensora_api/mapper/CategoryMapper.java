package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.CategoryDto;
import com.expensora.expensora_api.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setColor(category.getColor());
        dto.setIcon(category.getIcon());
        dto.setIsDefault(category.getIsDefault());
        dto.setUserId(category.getUser() != null ? category.getUser().getId() : null);
        return dto;
    }
}
