package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Achievement;
import com.expensora.expensora_api.entity.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID>, JpaSpecificationExecutor<Achievement> {
    
    List<Achievement> findByIsActive(Boolean isActive);
    
    List<Achievement> findByAchievementType(AchievementType achievementType);
    
}
