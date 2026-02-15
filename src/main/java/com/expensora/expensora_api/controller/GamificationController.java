package com.expensora.expensora_api.controller;

import com.expensora.expensora_api.dto.AchievementDto;
import com.expensora.expensora_api.dto.UserAchievementDto;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/gamification")
@Tag(name = "Gamification", description = "Achievements and gamification APIs")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/achievements")
    @Operation(summary = "Get all achievements", description = "Get all available achievements")
    public ResponseEntity<List<AchievementDto>> getAllAchievements() {
        List<AchievementDto> achievements = gamificationService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/my-achievements")
    @Operation(summary = "Get my achievements", description = "Get all achievements earned by the current user")
    public ResponseEntity<List<UserAchievementDto>> getMyAchievements() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<UserAchievementDto> achievements = gamificationService.getUserAchievements(user.getId());
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/my-points")
    @Operation(summary = "Get my total points",description = "Get total points earned by the current user")
    public ResponseEntity<Map<String, Integer>> getMyPoints() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Integer totalPoints = gamificationService.getUserTotalPoints(user.getId());
        Map<String, Integer> response = new HashMap<>();
        response.put("totalPoints", totalPoints);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/award/{achievementId}")
    @Operation(summary = "Award achievement", description = "Award an achievement to the current user")
    public ResponseEntity<UserAchievementDto> awardAchievement(@PathVariable UUID achievementId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        UserAchievementDto awarded = gamificationService.awardAchievement(user.getId(), achievementId);
        return ResponseEntity.ok(awarded);
    }
}
