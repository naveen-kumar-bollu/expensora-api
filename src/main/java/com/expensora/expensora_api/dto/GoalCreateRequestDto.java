package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.GoalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalCreateRequestDto {
    private String name;
    private String description;
    private GoalType goalType;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private String icon;
    private String color;
    private Integer priority;
}
