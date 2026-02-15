package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, UUID>, JpaSpecificationExecutor<UserChallenge> {
    
    List<UserChallenge> findByUserId(UUID userId);
    
    Optional<UserChallenge> findByUserIdAndChallengeId(UUID userId, UUID challengeId);
    
    List<UserChallenge> findByUserIdAndIsCompleted(UUID userId, Boolean isCompleted);
    
}
