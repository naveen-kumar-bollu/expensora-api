package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Goal;
import com.expensora.expensora_api.repository.GoalRepository;
import com.expensora.expensora_api.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Override
    @Transactional
    public Goal createGoal(Goal goal) {
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        if (goal.getCompleted() == null) {
            goal.setCompleted(false);
        }
        if (goal.getPriority() == null) {
            goal.setPriority(3); // Default medium priority
        }
        return goalRepository.save(goal);
    }

    @Override
    public List<Goal> findByUserId(UUID userId) {
        return goalRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId);
    }

    @Override
    public List<Goal> findActiveByUserId(UUID userId) {
        return goalRepository.findByUserIdAndCompletedFalseOrderByPriorityAscCreatedAtDesc(userId);
    }

    @Override
    public List<Goal> findCompletedByUserId(UUID userId) {
        return goalRepository.findByUserIdAndCompletedTrueOrderByUpdatedAtDesc(userId);
    }

    @Override
    public Optional<Goal> findById(UUID id) {
        return goalRepository.findById(id);
    }

    @Override
    @Transactional
    public Goal updateGoal(UUID id, Goal goal) {
        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        existing.setName(goal.getName());
        existing.setDescription(goal.getDescription());
        existing.setGoalType(goal.getGoalType());
        existing.setTargetAmount(goal.getTargetAmount());
        existing.setTargetDate(goal.getTargetDate());
        existing.setIcon(goal.getIcon());
        existing.setColor(goal.getColor());
        existing.setPriority(goal.getPriority());
        
        // Check if goal is complete
        if (existing.getCurrentAmount().compareTo(existing.getTargetAmount()) >= 0) {
            existing.setCompleted(true);
        }
        
        return goalRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID id) {
        goalRepository.deleteById(id);
    }
}
