package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private UUID id;
    private String name;
    private CategoryType type;
    private String color;
    private String icon;
    private Boolean isDefault;
    private UUID userId;
}
