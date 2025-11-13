package com.example.transaction_service.service.budget;

import com.example.transaction_service.repo.BudgetRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetScheduler {

    private final BudgetRepo budgetRepo;
    private final BudgetService budgetService;

    @Scheduled(cron = "0 0 2 1 * ?")
    public void adjustBudgetsForUpcomingMonth() {
        List<String> userIds = budgetRepo.findDistinctUserIds();
        userIds.forEach(userId -> {
            log.info("Running scheduled budget adjustments for user {}", userId);
            budgetService.generateRecommendations(userId);
        });
    }
}
