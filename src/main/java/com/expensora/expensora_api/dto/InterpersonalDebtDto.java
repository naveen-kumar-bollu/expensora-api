package com.expensora.expensora_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class InterpersonalDebtDto {
    private UUID id;
    private UUID creditorUserId;
    private String creditorUserName;
    private UUID debtorUserId;
    private String debtorUserName;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String description;
    private UUID expenseId;
    private Boolean isSettled;
    private LocalDate settledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCreditorUserId() { return creditorUserId; }
    public void setCreditorUserId(UUID creditorUserId) { this.creditorUserId = creditorUserId; }
    public String getCreditorUserName() { return creditorUserName; }
    public void setCreditorUserName(String creditorUserName) { this.creditorUserName = creditorUserName; }
    public UUID getDebtorUserId() { return debtorUserId; }
    public void setDebtorUserId(UUID debtorUserId) { this.debtorUserId = debtorUserId; }
    public String getDebtorUserName() { return debtorUserName; }
    public void setDebtorUserName(String debtorUserName) { this.debtorUserName = debtorUserName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getExpenseId() { return expenseId; }
    public void setExpenseId(UUID expenseId) { this.expenseId = expenseId; }
    public Boolean getIsSettled() { return isSettled; }
    public void setIsSettled(Boolean isSettled) { this.isSettled = isSettled; }
    public LocalDate getSettledDate() { return settledDate; }
    public void setSettledDate(LocalDate settledDate) { this.settledDate = settledDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
