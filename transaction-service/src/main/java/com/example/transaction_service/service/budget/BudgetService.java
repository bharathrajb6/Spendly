package com.example.transaction_service.service.budget;

import com.example.transaction_service.dto.response.budget.BudgetRecommendationResponse;
import com.example.transaction_service.dto.response.budget.BudgetResponse;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.model.budget.Budget;
import com.example.transaction_service.model.budget.BudgetStatus;
import com.example.transaction_service.repo.BudgetRepo;
import com.example.transaction_service.service.insights.TransactionAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private static final Map<String, Double> DEFAULT_CATEGORY_LIMITS = new LinkedHashMap<>() {{
        put("HOUSING", 1500.0);
        put("FOOD", 600.0);
        put("TRANSPORT", 300.0);
        put("ENTERTAINMENT", 250.0);
        put("UTILITIES", 200.0);
    }};

    private final BudgetRepo budgetRepo;
    private final TransactionAnalyticsService transactionAnalyticsService;
    private final BudgetNotificationService budgetNotificationService;

    @Transactional
    public List<BudgetResponse> createDefaultBudgets(String userId) {
        YearMonth current = YearMonth.now();
        List<BudgetResponse> responses = new ArrayList<>();
        DEFAULT_CATEGORY_LIMITS.forEach((category, limit) -> {
            Budget budget = ensureBudget(userId, category, current, limit);
            responses.add(toResponse(budget));
        });
        return responses;
    }

    @Transactional
    public void handleTransactionEvent(Transaction currentState, Transaction previousState) {
        if (currentState != null && currentState.getUsername() != null && "EXPENSE".equalsIgnoreCase(currentState.getTransactionType())) {
            recalculateBudgetForCategory(currentState.getUsername(), normalizeCategory(currentState.getCategory()), resolvePeriod(currentState.getTransactionDate()));
        }

        if (previousState != null && previousState.getUsername() != null && "EXPENSE".equalsIgnoreCase(previousState.getTransactionType())) {
            recalculateBudgetForCategory(previousState.getUsername(), normalizeCategory(previousState.getCategory()), resolvePeriod(previousState.getTransactionDate()));
        }
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsForUser(String userId, YearMonth period) {
        YearMonth target = period != null ? period : YearMonth.now();
        return budgetRepo.findByUserIdAndMonthAndYear(userId, target.getMonthValue(), target.getYear()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<BudgetRecommendationResponse> generateRecommendations(String userId) {
        Map<String, Double> averageByCategory = transactionAnalyticsService.calculateAverageExpenseByCategory(userId, 3);
        YearMonth nextMonth = YearMonth.now().plusMonths(1);

        List<BudgetRecommendationResponse> recommendations = new ArrayList<>();
        for (Map.Entry<String, Double> entry : averageByCategory.entrySet()) {
            String category = entry.getKey();
            double averageSpend = entry.getValue();
            Budget currentBudget = ensureBudget(userId, category, YearMonth.now(), DEFAULT_CATEGORY_LIMITS.getOrDefault(category, 300.0));
            double currentLimit = currentBudget.getLimitAmount();
            double suggestedLimit = currentLimit;
            String note = "No adjustment required";

            if (averageSpend > currentLimit) {
                suggestedLimit = roundTwoDecimals(currentLimit * 1.10);
                note = "Spending trends are above limit. Suggested +10% adjustment.";
            } else if (averageSpend < currentLimit * 0.9) {
                suggestedLimit = roundTwoDecimals(currentLimit * 0.90);
                note = "Spending trends are below limit. Suggested -10% adjustment.";
            }

            Budget upcomingBudget = ensureBudget(userId, category, nextMonth, suggestedLimit);
            upcomingBudget.setLimitAmount(suggestedLimit);
            upcomingBudget.setStatus(BudgetStatus.UPCOMING);
            upcomingBudget.setRecommendedLimitAmount(suggestedLimit);
            upcomingBudget.setUpdatedAt(LocalDateTime.now());
            budgetRepo.save(upcomingBudget);

            recommendations.add(BudgetRecommendationResponse.builder().category(category).currentLimit(currentLimit).suggestedLimit(suggestedLimit).month(nextMonth.getMonthValue()).year(nextMonth.getYear()).recommendationNote(note).build());
        }
        return recommendations;
    }

    @Transactional
    public void recalculateBudgetForCategory(String userId, String category, YearMonth period) {
        if (userId == null || category == null) {
            return;
        }
        Budget budget = ensureBudget(userId, category, period, DEFAULT_CATEGORY_LIMITS.getOrDefault(category, 300.0));
        double currentSpend = transactionAnalyticsService.calculateMonthlyCategoryExpense(userId, category, period);
        BudgetStatus status = currentSpend > budget.getLimitAmount() ? BudgetStatus.OVERSPENT : BudgetStatus.ON_TRACK;
        budget.setStatus(status);
        budget.setUpdatedAt(LocalDateTime.now());
        budgetRepo.save(budget);

        if (status == BudgetStatus.OVERSPENT) {
            log.info("User {} overspent category {} for {}. Spent {}, limit {}", userId, category, period, currentSpend, budget.getLimitAmount());
            budgetNotificationService.notifyOverspending(userId, category, currentSpend, budget.getLimitAmount());
        }
    }

    private Budget ensureBudget(String userId, String category, YearMonth period, double defaultLimit) {
        return budgetRepo.findByUserIdAndCategoryAndMonthAndYear(userId, category, period.getMonthValue(), period.getYear()).orElseGet(() -> budgetRepo.findTopByUserIdAndCategoryOrderByYearDescMonthDesc(userId, category).map(existing -> buildBudget(userId, category, period, existing.getLimitAmount())).orElseGet(() -> buildBudget(userId, category, period, defaultLimit)));
    }

    private Budget buildBudget(String userId, String category, YearMonth period, double limit) {
        Budget budget = Budget.builder().budgetId(UUID.randomUUID().toString()).userId(userId).category(category).limitAmount(roundTwoDecimals(limit)).month(period.getMonthValue()).year(period.getYear()).status(BudgetStatus.ON_TRACK).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return budgetRepo.save(budget);
    }

    private YearMonth resolvePeriod(Timestamp timestamp) {
        if (timestamp == null) {
            return YearMonth.now();
        }
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return YearMonth.of(dateTime.getYear(), dateTime.getMonth());
    }

    private String normalizeCategory(String category) {
        return category == null ? "UNCATEGORIZED" : category.toUpperCase();
    }

    private BudgetResponse toResponse(Budget budget) {
        return BudgetResponse.builder().budgetId(budget.getBudgetId()).userId(budget.getUserId()).category(budget.getCategory()).limitAmount(budget.getLimitAmount()).month(budget.getMonth()).year(budget.getYear()).status(budget.getStatus()).recommendedLimitAmount(budget.getRecommendedLimitAmount()).build();
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
