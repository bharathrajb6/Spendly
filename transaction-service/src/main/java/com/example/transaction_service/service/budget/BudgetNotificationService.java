package com.example.transaction_service.service.budget;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BudgetNotificationService {

    public void notifyOverspending(String userId, String category, double amount, double limit) {
        double exceeded = amount - limit;
        log.info("[Notification] User {} exceeded {} budget by {} (limit {}, spent {}).", userId, category, exceeded, limit, amount);
        // TODO: Integrate with centralized EmailService when available
    }
}
