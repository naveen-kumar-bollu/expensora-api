package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.GoalCreateRequestDto;
import com.expensora.expensora_api.dto.GoalDto;
import com.expensora.expensora_api.entity.Goal;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.GoalMapper;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goals")
@Tag(name = "Goals", description = "Financial goals and savings target management APIs")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalMapper goalMapper;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a goal", description = "Create a new financial goal or savings target")
    public ResponseEntity<GoalDto> create(@RequestBody GoalCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setGoalType(dto.getGoalType());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setTargetDate(dto.getTargetDate());
        goal.setIcon(dto.getIcon());
        goal.setColor(dto.getColor());
        goal.setPriority(dto.getPriority());

        Goal saved = goalService.createGoal(goal);
        return ResponseEntity.ok(goalMapper.toDto(saved));
    }

    @GetMapping
    @Operation(summary = "Get all goals", description = "Get all goals for the authenticated user")
    public ResponseEntity<List<GoalDto>> getAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Goal> goals = goalService.findByUserId(user.getId());
        List<GoalDto> dtos = goals.stream().map(goalMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active goals", description = "Get all active (not completed) goals")
    public ResponseEntity<List<GoalDto>> getActive() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Goal> goals = goalService.findActiveByUserId(user.getId());
        List<GoalDto> dtos = goals.stream().map(goalMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed goals", description = "Get all completed goals")
    public ResponseEntity<List<GoalDto>> getCompleted() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Goal> goals = goalService.findCompletedByUserId(user.getId());
        List<GoalDto> dtos = goals.stream().map(goalMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Get a specific goal by its ID")
    public ResponseEntity<GoalDto> getById(@PathVariable UUID id) {
        Goal goal = goalService.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        return ResponseEntity.ok(goalMapper.toDto(goal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goal", description = "Update an existing goal")
    public ResponseEntity<GoalDto> update(@PathVariable UUID id, @RequestBody GoalCreateRequestDto dto) {
        Goal goal = new Goal();
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setGoalType(dto.getGoalType());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setTargetDate(dto.getTargetDate());
        goal.setIcon(dto.getIcon());
        goal.setColor(dto.getColor());
        goal.setPriority(dto.getPriority());

        Goal updated = goalService.updateGoal(id, goal);
        return ResponseEntity.ok(goalMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goal", description = "Delete a goal")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
