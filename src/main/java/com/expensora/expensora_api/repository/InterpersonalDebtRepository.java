package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.InterpersonalDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface InterpersonalDebtRepository extends JpaRepository<InterpersonalDebt, UUID>, JpaSpecificationExecutor<InterpersonalDebt> {
    
    List<InterpersonalDebt> findByCreditorUserId(UUID creditorUserId);
    
    List<InterpersonalDebt> findByDebtorUserId(UUID debtorUserId);
    
    List<InterpersonalDebt> findByCreditorUserIdAndIsSettled(UUID creditorUserId, Boolean isSettled);
    
    List<InterpersonalDebt> findByDebtorUserIdAndIsSettled(UUID debtorUserId, Boolean isSettled);
    
}
