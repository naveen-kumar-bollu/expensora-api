package com.expensora.expensora_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalContributionDto {
    private UUID id;
    private UUID goalId;
    private String goalName;
    private BigDecimal amount;
    private String notes;
    private LocalDate contributionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
