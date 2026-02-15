package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.AchievementDto;
import com.expensora.expensora_api.dto.UserAchievementDto;
import com.expensora.expensora_api.entity.*;
import com.expensora.expensora_api.repository.*;
import com.expensora.expensora_api.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GamificationServiceImpl implements GamificationService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public List<AchievementDto> getAllAchievements() {
        return achievementRepository.findByIsActive(true).stream()
                .map(this::mapAchievementToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementDto> getUserAchievements(UUID userId) {
        return userAchievementRepository.findByUserId(userId).stream()
                .map(this::mapUserAchievementToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkAndAwardAchievements(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Achievement> allAchievements = achievementRepository.findByIsActive(true);
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        
        for (Achievement achievement : allAchievements) {
            // Skip if user already has this achievement
            boolean alreadyEarned = userAchievements.stream()
                .anyMatch(ua -> ua.getAchievement().getId().equals(achievement.getId()));
            if (alreadyEarned) continue;

            boolean shouldAward = false;

            switch (achievement.getCriteriaType()) {
                case CONSECUTIVE_DAYS:
                    shouldAward = checkConsecutiveDays(userId, achievement.getCriteriaValue());
                    break;
                case TRANSACTION_COUNT:
                    shouldAward = checkTransactionCount(userId, achievement.getCriteriaValue());
                    break;
                case BUDGET_ADHERENCE:
                    shouldAward = checkBudgetAdherence(userId, achievement.getCriteriaValue());
                    break;
                case SAVINGS_RATE:
                    shouldAward = checkSavingsRate(userId, achievement.getCriteriaValue());
                    break;
                case DEBT_PAYOFF:
                    shouldAward = checkDebtPayoff(userId);
                    break;
                case GOAL_COMPLETION:
                    shouldAward = checkGoalCompletion(userId, achievement.getCriteriaValue());
                    break;
                case AMOUNT_SAVED:
                    shouldAward = checkAmountSaved(userId, achievement.getCriteriaValue());
                    break;
            }

            if (shouldAward) {
                awardAchievement(userId, achievement.getId());
            }
        }
    }

    private boolean checkConsecutiveDays(UUID userId, BigDecimal requiredDays) {
        // Check if user has transactions for consecutive days
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        if (expenses.isEmpty()) return false;

        // Get unique dates with transactions
        List<LocalDate> transactionDates = expenses.stream()
            .map(e -> e.getExpenseDate())
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        if (transactionDates.isEmpty()) return false;

        // Find longest consecutive streak
        int longestStreak = 1;
        int currentStreak = 1;
        
        for (int i = 1; i < transactionDates.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(transactionDates.get(i-1), transactionDates.get(i));
            if (daysBetween == 1) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak >= requiredDays.intValue();
    }

    private boolean checkTransactionCount(UUID userId, BigDecimal requiredCount) {
        long expenseCount = expenseRepository.countByUserId(userId);
        long incomeCount = incomeRepository.countByUserId(userId);
        return (expenseCount + incomeCount) >= requiredCount.intValue();
    }

    private boolean checkBudgetAdherence(UUID userId, BigDecimal requiredMonths) {
        // Check budget adherence for required number of months
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        if (budgets.isEmpty()) return false;

        int adherentMonths = 0;
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < requiredMonths.intValue(); i++) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            
            boolean adherentForMonth = budgets.stream().anyMatch(budget -> {
                BigDecimal spent = expenseRepository.findByUserIdAndCategoryIdAndExpenseDateBetween(
                    userId, 
                    budget.getCategory().getId(), 
                    monthStart.atStartOfDay(), 
                    monthEnd.atTime(23, 59, 59)
                ).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return spent.compareTo(budget.getAmount()) <= 0;
            });
            
            if (adherentForMonth) adherentMonths++;
        }

        return adherentMonths >= requiredMonths.intValue();
    }

    private boolean checkSavingsRate(UUID userId, BigDecimal requiredRate) {
        // Calculate savings rate for the last month
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        
        BigDecimal totalIncome = incomeRepository.findByUserIdAndIncomeDateBetween(
            userId, 
            monthStart.atStartOfDay(), 
            now.atTime(23, 59, 59)
        ).stream()
        .map(Income::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseRepository.findByUserIdAndExpenseDateBetween(
            userId, 
            monthStart.atStartOfDay(), 
            now.atTime(23, 59, 59)
        ).stream()
        .map(Expense::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) return false;

        BigDecimal savingsRate = totalIncome.subtract(totalExpenses)
            .divide(totalIncome, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return savingsRate.compareTo(requiredRate) >= 0;
    }

    private boolean checkDebtPayoff(UUID userId) {
        // Check if user has paid off at least one debt
        List<Debt> debts = debtRepository.findByUserId(userId);
        return debts.stream().anyMatch(debt -> 
            debt.getCurrentBalance().compareTo(BigDecimal.ZERO) == 0
        );
    }

    private boolean checkGoalCompletion(UUID userId, BigDecimal requiredCount) {
        // Check if user has completed required number of goals
        List<Goal> goals = goalRepository.findByUserId(userId);
        long completedGoals = goals.stream()
            .filter(goal -> goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0)
            .count();
        return completedGoals >= requiredCount.intValue();
    }

    private boolean checkAmountSaved(UUID userId, BigDecimal requiredAmount) {
        // Check total savings across all goals
        List<Goal> goals = goalRepository.findByUserId(userId);
        BigDecimal totalSaved = goals.stream()
            .map(Goal::getCurrentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalSaved.compareTo(requiredAmount) >= 0;
    }

    @Override
    public UserAchievementDto awardAchievement(UUID userId, UUID achievementId) {
        // Check if user already has this achievement
        Optional<UserAchievement> existing = userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId);
        
        if (existing.isPresent()) {
            return mapUserAchievementToDto(existing.get());
        }

        User user = userRepository.findById(userId).orElseThrow();
        Achievement achievement = achievementRepository.findById(achievementId).orElseThrow();

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        userAchievement.setEarnedDate(LocalDateTime.now());
        userAchievement.setProgress(BigDecimal.valueOf(100.00));
        userAchievement.setIsNotified(false);

        UserAchievement saved = userAchievementRepository.save(userAchievement);
        return mapUserAchievementToDto(saved);
    }

    @Override
    public Integer getUserTotalPoints(UUID userId) {
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        return userAchievements.stream()
                .mapToInt(ua -> ua.getAchievement().getPoints())
                .sum();
    }

    private AchievementDto mapAchievementToDto(Achievement achievement) {
        AchievementDto dto = new AchievementDto();
        dto.setId(achievement.getId());
        dto.setName(achievement.getName());
        dto.setDescription(achievement.getDescription());
        dto.setIcon(achievement.getIcon());
        dto.setBadgeColor(achievement.getBadgeColor());
        dto.setPoints(achievement.getPoints());
        dto.setAchievementType(achievement.getAchievementType().name());
        dto.setCriteriaType(achievement.getCriteriaType().name());
        dto.setCriteriaValue(achievement.getCriteriaValue());
        dto.setIsActive(achievement.getIsActive());
        dto.setCreatedAt(achievement.getCreatedAt());
        dto.setUpdatedAt(achievement.getUpdatedAt());
        return dto;
    }

    private UserAchievementDto mapUserAchievementToDto(UserAchievement userAchievement) {
        UserAchievementDto dto = new UserAchievementDto();
        dto.setId(userAchievement.getId());
        dto.setUserId(userAchievement.getUser().getId());
        dto.setAchievementId(userAchievement.getAchievement().getId());
        dto.setAchievementName(userAchievement.getAchievement().getName());
        dto.setAchievementDescription(userAchievement.getAchievement().getDescription());
        dto.setIcon(userAchievement.getAchievement().getIcon());
        dto.setBadgeColor(userAchievement.getAchievement().getBadgeColor());
        dto.setPoints(userAchievement.getAchievement().getPoints());
        dto.setEarnedDate(userAchievement.getEarnedDate());
        dto.setProgress(userAchievement.getProgress());
        dto.setIsNotified(userAchievement.getIsNotified());
        dto.setCreatedAt(userAchievement.getCreatedAt());
        dto.setUpdatedAt(userAchievement.getUpdatedAt());
        return dto;
    }
}
