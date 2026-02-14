package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.GoalContribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalContributionService {
    GoalContribution createContribution(GoalContribution contribution);
    List<GoalContribution> findByGoalId(UUID goalId);
    Optional<GoalContribution> findById(UUID id);
    void deleteContribution(UUID id);
}
