package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequestDto {
    private String name;
    private CategoryType type;
    private String color;
    private String icon;
}
