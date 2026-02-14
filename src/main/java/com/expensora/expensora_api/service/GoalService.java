package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalService {
    Goal createGoal(Goal goal);
    List<Goal> findByUserId(UUID userId);
    List<Goal> findActiveByUserId(UUID userId);
    List<Goal> findCompletedByUserId(UUID userId);
    Optional<Goal> findById(UUID id);
    Goal updateGoal(UUID id, Goal goal);
    void deleteGoal(UUID id);
}
