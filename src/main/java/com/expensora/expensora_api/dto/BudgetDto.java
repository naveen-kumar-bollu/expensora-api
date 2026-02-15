package com.expensora.expensora_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private Integer budgetMonth;
    private Integer budgetYear;
    private BigDecimal spent;
    private Double percentage;
}
