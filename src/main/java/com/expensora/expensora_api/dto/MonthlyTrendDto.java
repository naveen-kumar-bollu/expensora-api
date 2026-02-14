package com.expensora.expensora_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDto {
    private Integer month;
    private Integer year;
    private BigDecimal income;
    private BigDecimal expense;
}
