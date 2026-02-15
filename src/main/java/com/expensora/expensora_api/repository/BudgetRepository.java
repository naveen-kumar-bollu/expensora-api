package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserId(UUID userId);

    List<Budget> findByUserIdAndBudgetMonthAndBudgetYear(UUID userId, Integer budgetMonth, Integer budgetYear);

    Optional<Budget> findByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(UUID userId, UUID categoryId, Integer budgetMonth, Integer budgetYear);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId ORDER BY b.budgetYear DESC, b.budgetMonth DESC")
    List<Budget> findBudgetHistory(UUID userId, UUID categoryId);

}
