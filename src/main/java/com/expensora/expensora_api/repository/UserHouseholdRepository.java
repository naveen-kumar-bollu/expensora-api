package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Household;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.entity.UserHousehold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserHouseholdRepository extends JpaRepository<UserHousehold, UUID> {
    List<UserHousehold> findByUser(User user);
    List<UserHousehold> findByHousehold(Household household);
    Optional<UserHousehold> findByUserAndHousehold(User user, Household household);
    List<UserHousehold> findByUserAndIsActiveTrue(User user);
}
