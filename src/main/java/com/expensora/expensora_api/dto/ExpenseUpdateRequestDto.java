package com.expensora.expensora_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseUpdateRequestDto {
    private BigDecimal amount;
    private String description;
    private java.util.UUID categoryId;
    private LocalDate expenseDate;
    private String notes;
    private String tags;

    // getters and setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.util.UUID getCategoryId() { return categoryId; }
    public void setCategoryId(java.util.UUID categoryId) { this.categoryId = categoryId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}