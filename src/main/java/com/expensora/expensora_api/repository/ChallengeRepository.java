package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Challenge;
import com.expensora.expensora_api.entity.ChallengeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID>, JpaSpecificationExecutor<Challenge> {
    
    List<Challenge> findByIsActive(Boolean isActive);
    
    List<Challenge> findByIsGlobal(Boolean isGlobal);
    
    List<Challenge> findByChallengeType(ChallengeType challengeType);
    
}
