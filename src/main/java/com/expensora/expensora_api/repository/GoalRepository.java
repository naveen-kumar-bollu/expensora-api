package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserIdOrderByPriorityAscCreatedAtDesc(UUID userId);
    List<Goal> findByUserIdAndCompletedFalseOrderByPriorityAscCreatedAtDesc(UUID userId);
    List<Goal> findByUserIdAndCompletedTrueOrderByUpdatedAtDesc(UUID userId);
}
