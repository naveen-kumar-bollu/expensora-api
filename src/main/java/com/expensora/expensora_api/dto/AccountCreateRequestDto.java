package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequestDto {
    private String name;
    private AccountType accountType;
    private BigDecimal initialBalance;
    private String currency;
    private String icon;
    private String color;
    private Boolean isDefault;
}
