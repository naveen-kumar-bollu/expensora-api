package com.expensora.expensora_api.service;

import com.expensora.expensora_api.dto.AchievementDto;
import com.expensora.expensora_api.dto.UserAchievementDto;

import java.util.List;
import java.util.UUID;

public interface GamificationService {
    List<AchievementDto> getAllAchievements();
    List<UserAchievementDto> getUserAchievements(UUID userId);
    void checkAndAwardAchievements(UUID userId);
    UserAchievementDto awardAchievement(UUID userId, UUID achievementId);
    Integer getUserTotalPoints(UUID userId);
}
