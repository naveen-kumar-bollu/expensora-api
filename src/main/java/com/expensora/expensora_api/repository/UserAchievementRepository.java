package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID>, JpaSpecificationExecutor<UserAchievement> {
    
    List<UserAchievement> findByUserId(UUID userId);
    
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);
    
    Long countByUserId(UUID userId);
    
}
