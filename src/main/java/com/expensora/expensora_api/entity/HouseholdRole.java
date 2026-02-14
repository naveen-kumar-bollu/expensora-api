package com.expensora.expensora_api.entity;

public enum HouseholdRole {
    ADMIN,   // Full access, can manage users
    EDITOR,  // Add/edit/delete transactions
    VIEWER   // Read-only access
}
