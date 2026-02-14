package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.DebtType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class DebtCreateRequestDto {
    private String name;
    private DebtType debtType;
    private BigDecimal principalAmount;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private BigDecimal minimumPayment;
    private LocalDate startDate;
    private LocalDate targetPayoffDate;
    private String notes;
    private UUID accountId;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DebtType getDebtType() { return debtType; }
    public void setDebtType(DebtType debtType) { this.debtType = debtType; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getMinimumPayment() { return minimumPayment; }
    public void setMinimumPayment(BigDecimal minimumPayment) { this.minimumPayment = minimumPayment; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getTargetPayoffDate() { return targetPayoffDate; }
    public void setTargetPayoffDate(LocalDate targetPayoffDate) { this.targetPayoffDate = targetPayoffDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
}
