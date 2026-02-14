package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.GoalContributionCreateRequestDto;
import com.expensora.expensora_api.dto.GoalContributionDto;
import com.expensora.expensora_api.entity.Goal;
import com.expensora.expensora_api.entity.GoalContribution;
import com.expensora.expensora_api.mapper.GoalContributionMapper;
import com.expensora.expensora_api.repository.GoalRepository;
import com.expensora.expensora_api.service.GoalContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goal-contributions")
@Tag(name = "Goal Contributions", description = "Goal contribution tracking APIs")
public class GoalContributionController {

    @Autowired
    private GoalContributionService contributionService;

    @Autowired
    private GoalContributionMapper contributionMapper;

    @Autowired
    private GoalRepository goalRepository;

    @PostMapping
    @Operation(summary = "Add a contribution", description = "Add a contribution to a financial goal")
    public ResponseEntity<GoalContributionDto> create(@RequestBody GoalContributionCreateRequestDto dto) {
        Goal goal = goalRepository.findById(dto.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        GoalContribution contribution = new GoalContribution();
        contribution.setGoal(goal);
        contribution.setAmount(dto.getAmount());
        contribution.setNotes(dto.getNotes());
        contribution.setContributionDate(dto.getContributionDate());

        GoalContribution saved = contributionService.createContribution(contribution);
        return ResponseEntity.ok(contributionMapper.toDto(saved));
    }

    @GetMapping("/goal/{goalId}")
    @Operation(summary = "Get contributions by goal", description = "Get all contributions for a specific goal")
    public ResponseEntity<List<GoalContributionDto>> getByGoal(@PathVariable UUID goalId) {
        List<GoalContribution> contributions = contributionService.findByGoalId(goalId);
        List<GoalContributionDto> dtos = contributions.stream()
                .map(contributionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contribution", description = "Delete a goal contribution")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contributionService.deleteContribution(id);
        return ResponseEntity.noContent().build();
    }
}
