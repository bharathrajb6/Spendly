package com.example.transaction_service.service.insights;

import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.repo.TransactionRepo;
import com.example.transaction_service.service.insights.dto.UserTransactionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalyticsService {

    private final TransactionRepo transactionRepo;

    @Transactional(readOnly = true)
    public UserTransactionSummary buildUserTransactionSummary(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must be provided to build transaction summary");
        }
        log.debug("Building transaction summary for user {}", username);
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);
        double income = transactions.stream()
                .filter(transaction -> "INCOME".equalsIgnoreCase(transaction.getTransactionType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double expense = transactions.stream()
                .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double remaining = income - expense;
        return UserTransactionSummary.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .remainingBalance(remaining)
                .build();
    }

    @Transactional(readOnly = true)
    public double calculateMonthlyCategoryExpense(String username, String category, YearMonth period) {
        if (period == null) {
            period = YearMonth.from(LocalDate.now());
        }
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);
        YearMonth finalPeriod = period;
        return transactions.stream()
                .filter(transaction -> transaction.getTransactionDate() != null)
                .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()))
                .filter(transaction -> transaction.getCategory() != null && transaction.getCategory().equalsIgnoreCase(category))
                .filter(transaction -> YearMonth.from(transaction.getTransactionDate().toLocalDateTime()).equals(finalPeriod))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getCurrentMonthExpenseByCategory(String username) {
        YearMonth current = YearMonth.now();
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);
        return transactions.stream()
                .filter(transaction -> transaction.getTransactionDate() != null)
                .filter(transaction -> transaction.getCategory() != null)
                .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()))
                .filter(transaction -> YearMonth.from(transaction.getTransactionDate().toLocalDateTime()).equals(current))
                .collect(Collectors.groupingBy(transaction -> transaction.getCategory().toUpperCase(), Collectors.summingDouble(Transaction::getAmount)));
    }

    @Transactional(readOnly = true)
    public Map<String, Double> calculateAverageExpenseByCategory(String username, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be greater than zero");
        }
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);
        Map<String, List<Double>> categoryBuckets = new HashMap<>();
        YearMonth current = YearMonth.now();

        for (int i = 0; i < months; i++) {
            YearMonth targetMonth = current.minusMonths(i);
            Map<String, Double> monthTotals = transactions.stream()
                    .filter(transaction -> transaction.getTransactionDate() != null)
                    .filter(transaction -> transaction.getCategory() != null)
                    .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()))
                    .filter(transaction -> YearMonth.from(transaction.getTransactionDate().toLocalDateTime()).equals(targetMonth))
                    .collect(Collectors.groupingBy(transaction -> transaction.getCategory().toUpperCase(), Collectors.summingDouble(Transaction::getAmount)));

            monthTotals.forEach((category, total) ->
                    categoryBuckets.computeIfAbsent(category, key -> new ArrayList<>()).add(total));
        }

        return categoryBuckets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)));
    }

    @Transactional(readOnly = true)
    public List<Double> getMonthlyExpenseTotals(String username, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be greater than zero");
        }
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);
        List<Double> totals = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 0; i < months; i++) {
            YearMonth targetMonth = current.minusMonths(i);
            double total = transactions.stream()
                    .filter(transaction -> transaction.getTransactionDate() != null)
                    .filter(transaction -> "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()))
                    .filter(transaction -> YearMonth.from(transaction.getTransactionDate().toLocalDateTime()).equals(targetMonth))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            totals.add(total);
        }
        // reverse to chronological order (oldest first)
        java.util.Collections.reverse(totals);
        return totals;
    }
}

