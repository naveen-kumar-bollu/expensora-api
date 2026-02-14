package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.ImportHistory;
import com.expensora.expensora_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, UUID> {
    List<ImportHistory> findByUserOrderByCreatedAtDesc(User user);
}
