package com.example.transaction_service.service.insights;

import com.example.transaction_service.dto.response.FinancialHealthResponse;
import com.example.transaction_service.dto.response.GoalSummaryResponse;
import com.example.transaction_service.service.SavingsService;
import com.example.transaction_service.service.insights.dto.UserTransactionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialHealthService {

    private final TransactionAnalyticsService transactionAnalyticsService;
    private final SavingsService savingsService;
    private final RestTemplate restTemplate;

    @Value("${services.goal.base-url:http://localhost:8083/api/v1}")
    private String goalServiceBaseUrl;

    @Transactional(readOnly = true)
    public FinancialHealthResponse calculateHealthScore(String userId) {
        UserTransactionSummary summary = transactionAnalyticsService.buildUserTransactionSummary(userId);
        double totalIncome = summary.getTotalIncome();
        double totalSavings = savingsService.getSavingsData(userId);

        double savingsRatio = totalIncome <= 0 ? 0 : clamp(totalSavings / totalIncome);

        List<Double> monthlyExpenses = transactionAnalyticsService.getMonthlyExpenseTotals(userId, 6);
        double expenseStdDev = computeStandardDeviation(monthlyExpenses);
        double averageExpense = monthlyExpenses.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double expenseStabilityNormalized = averageExpense == 0 ? 0 : clamp(expenseStdDev / averageExpense);

        double goalCompletionRate = fetchGoalCompletionRate(userId);

        double score = (savingsRatio * 40) + ((1 - expenseStabilityNormalized) * 30) + (goalCompletionRate * 30);
        score = Math.round(score);

        String feedback = buildFeedbackMessage(score, savingsRatio, goalCompletionRate);

        return FinancialHealthResponse.builder().score(score).savingsRatio(roundTwoDecimals(savingsRatio)).expenseStability(roundTwoDecimals(1 - expenseStabilityNormalized)).goalCompletionRate(roundTwoDecimals(goalCompletionRate)).feedback(feedback).build();
    }

    private double fetchGoalCompletionRate(String userId) {
        try {
            GoalSummaryResponse summaryResponse = restTemplate.getForObject(String.format("%s/goals/%s/summary", goalServiceBaseUrl, userId), GoalSummaryResponse.class);
            if (summaryResponse == null || summaryResponse.getTotalGoals() == 0) {
                return 0;
            }
            return clamp((double) summaryResponse.getAchievedGoals() / summaryResponse.getTotalGoals());
        } catch (Exception exception) {
            log.warn("Unable to fetch goal summary for user {}", userId, exception);
            return 0;
        }
    }

    private double computeStandardDeviation(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream().mapToDouble(value -> Math.pow(value - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private String buildFeedbackMessage(double score, double savingsRatio, double goalCompletionRate) {
        if (score >= 80) {
            return String.format("Your financial health is %.0f — excellent! Maintain your habits to keep momentum.", score);
        }
        if (score >= 60) {
            return String.format("Your financial health is %.0f — good! Try saving %.0f%% more to improve.", score, Math.max(10, (1 - savingsRatio) * 100));
        }
        if (score >= 40) {
            return String.format("Your financial health is %.0f — fair. Focus on stabilizing monthly expenses; completion rate at %.0f%% can improve.", score, goalCompletionRate * 100);
        }
        return String.format("Your financial health is %.0f — needs attention. Increase savings and revisit your goals to get back on track.", score);
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
