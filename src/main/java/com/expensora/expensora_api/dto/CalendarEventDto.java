package com.expensora.expensora_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CalendarEventDto {
    private UUID id;
    private String title;
    private String type; // EXPENSE, INCOME, RECURRING, BUDGET
    private BigDecimal amount;
    private LocalDate date;
    private String categoryName;
    private String categoryColor;
    private String description;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
