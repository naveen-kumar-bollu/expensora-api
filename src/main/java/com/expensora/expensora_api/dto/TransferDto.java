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
public class TransferDto {
    private UUID id;
    private UUID fromAccountId;
    private String fromAccountName;
    private UUID toAccountId;
    private String toAccountName;
    private BigDecimal amount;
    private String description;
    private LocalDate transferDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
