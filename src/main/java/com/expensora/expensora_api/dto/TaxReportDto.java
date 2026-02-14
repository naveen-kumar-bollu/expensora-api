package com.expensora.expensora_api.dto;

import java.math.BigDecimal;

public class TaxReportDto {
    private String taxCategory;
    private BigDecimal totalAmount;
    private Integer transactionCount;

    // getters and setters
    public String getTaxCategory() { return taxCategory; }
    public void setTaxCategory(String taxCategory) { this.taxCategory = taxCategory; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
}
