package com.expensora.expensora_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class QuickAddDto {
    private String type; // EXPENSE or INCOME
    private BigDecimal amount;
    private String description;
    private UUID categoryId;
    private UUID accountId;
    private LocalDate date;

    // getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
