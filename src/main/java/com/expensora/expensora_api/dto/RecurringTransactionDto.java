package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.Frequency;
import com.expensora.expensora_api.entity.TransactionType;
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
public class RecurringTransactionDto {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private TransactionType transactionType;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastExecutionDate;
    private Boolean active;
    private LocalDateTime createdAt;
}
