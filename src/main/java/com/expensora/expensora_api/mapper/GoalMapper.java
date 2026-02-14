package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.GoalDto;
import com.expensora.expensora_api.entity.Goal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GoalMapper {

    public GoalDto toDto(Goal goal) {
        GoalDto dto = new GoalDto();
        dto.setId(goal.getId());
        dto.setUserId(goal.getUser().getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setGoalType(goal.getGoalType());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setTargetDate(goal.getTargetDate());
        dto.setIcon(goal.getIcon());
        dto.setColor(goal.getColor());
        dto.setPriority(goal.getPriority());
        dto.setCompleted(goal.getCompleted());
        
        // Calculate progress percentage
        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dto.setProgressPercentage(percentage.doubleValue());
        } else {
            dto.setProgressPercentage(0.0);
        }
        
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());
        return dto;
    }
}
