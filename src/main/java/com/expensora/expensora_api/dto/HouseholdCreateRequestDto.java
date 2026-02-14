package com.expensora.expensora_api.dto;

public class HouseholdCreateRequestDto {
    private String name;
    private String description;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
