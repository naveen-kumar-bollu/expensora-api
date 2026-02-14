package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.Frequency;
import com.expensora.expensora_api.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionCreateRequestDto {
    private UUID categoryId;
    private BigDecimal amount;
    private String description;
    private TransactionType transactionType;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
}
