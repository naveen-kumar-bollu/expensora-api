package com.expensora.expensora_api.mapper;

import com.expensora.expensora_api.dto.GoalContributionDto;
import com.expensora.expensora_api.entity.GoalContribution;
import org.springframework.stereotype.Component;

@Component
public class GoalContributionMapper {

    public GoalContributionDto toDto(GoalContribution contribution) {
        GoalContributionDto dto = new GoalContributionDto();
        dto.setId(contribution.getId());
        dto.setGoalId(contribution.getGoal().getId());
        dto.setGoalName(contribution.getGoal().getName());
        dto.setAmount(contribution.getAmount());
        dto.setNotes(contribution.getNotes());
        dto.setContributionDate(contribution.getContributionDate());
        dto.setCreatedAt(contribution.getCreatedAt());
        dto.setUpdatedAt(contribution.getUpdatedAt());
        return dto;
    }
}
