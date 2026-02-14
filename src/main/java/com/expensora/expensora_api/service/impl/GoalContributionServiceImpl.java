package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Goal;
import com.expensora.expensora_api.entity.GoalContribution;
import com.expensora.expensora_api.repository.GoalContributionRepository;
import com.expensora.expensora_api.repository.GoalRepository;
import com.expensora.expensora_api.service.GoalContributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalContributionServiceImpl implements GoalContributionService {

    @Autowired
    private GoalContributionRepository contributionRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Override
    @Transactional
    public GoalContribution createContribution(GoalContribution contribution) {
        GoalContribution saved = contributionRepository.save(contribution);
        
        // Update goal's current amount
        Goal goal = contribution.getGoal();
        goal.setCurrentAmount(goal.getCurrentAmount().add(contribution.getAmount()));
        
        // Check if goal is complete
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        }
        
        goalRepository.save(goal);
        
        return saved;
    }

    @Override
    public List<GoalContribution> findByGoalId(UUID goalId) {
        return contributionRepository.findByGoalIdOrderByContributionDateDesc(goalId);
    }

    @Override
    public Optional<GoalContribution> findById(UUID id) {
        return contributionRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteContribution(UUID id) {
        GoalContribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contribution not found"));
        
        // Update goal's current amount
        Goal goal = contribution.getGoal();
        goal.setCurrentAmount(goal.getCurrentAmount().subtract(contribution.getAmount()));
        goal.setCompleted(false); // Reopen the goal if deleted
        goalRepository.save(goal);
        
        contributionRepository.delete(contribution);
    }
}
