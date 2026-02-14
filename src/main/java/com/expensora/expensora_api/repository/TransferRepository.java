package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    @Query("SELECT t FROM Transfer t WHERE (t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) AND t.transferDate BETWEEN :startDate AND :endDate ORDER BY t.transferDate DESC")
    List<Transfer> findByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transfer t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.transferDate DESC")
    List<Transfer> findByAccountId(UUID accountId);
}
