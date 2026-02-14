package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Household;
import com.expensora.expensora_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {
    List<Household> findByOwner(User owner);
    List<Household> findByIsActiveTrue();
}
