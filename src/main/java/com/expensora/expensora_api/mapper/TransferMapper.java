package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.TransferDto;
import com.expensora.expensora_api.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferDto toDto(Transfer transfer) {
        TransferDto dto = new TransferDto();
        dto.setId(transfer.getId());
        dto.setFromAccountId(transfer.getFromAccount().getId());
        dto.setFromAccountName(transfer.getFromAccount().getName());
        dto.setToAccountId(transfer.getToAccount().getId());
        dto.setToAccountName(transfer.getToAccount().getName());
        dto.setAmount(transfer.getAmount());
        dto.setDescription(transfer.getDescription());
        dto.setTransferDate(transfer.getTransferDate());
        dto.setCreatedAt(transfer.getCreatedAt());
        dto.setUpdatedAt(transfer.getUpdatedAt());
        return dto;
    }
}
