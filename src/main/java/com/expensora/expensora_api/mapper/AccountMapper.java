package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.AccountDto;
import com.expensora.expensora_api.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUserId(account.getUser().getId());
        dto.setName(account.getName());
        dto.setAccountType(account.getAccountType());
        dto.setInitialBalance(account.getInitialBalance());
        dto.setCurrentBalance(account.getCurrentBalance());
        dto.setCurrency(account.getCurrency());
        dto.setIcon(account.getIcon());
        dto.setColor(account.getColor());
        dto.setIsDefault(account.getIsDefault());
        dto.setActive(account.getActive());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}
