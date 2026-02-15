package com.expensora.expensora_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionSplitCreateRequestDto {
    private UUID expenseId;
    private UUID incomeId;
    private UUID categoryId;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String description;

    // getters and setters
    public UUID getExpenseId() { return expenseId; }
    public void setExpenseId(UUID expenseId) { this.expenseId = expenseId; }
    public UUID getIncomeId() { return incomeId; }
    public void setIncomeId(UUID incomeId) { this.incomeId = incomeId; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
