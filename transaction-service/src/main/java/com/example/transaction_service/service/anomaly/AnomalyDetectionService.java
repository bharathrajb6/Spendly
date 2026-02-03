package com.example.transaction_service.service.anomaly;

import com.example.transaction_service.dto.response.anomaly.AnomalyResponse;
import com.example.transaction_service.model.anomaly.Anomaly;
import com.example.transaction_service.repo.AnomalyRepo;
import com.example.transaction_service.service.insights.TransactionAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final TransactionAnalyticsService transactionAnalyticsService;
    private final AnomalyRepo anomalyRepo;

    private static final double DEVIATION_THRESHOLD_PERCENT = 30.0;

    /**
     * Detects anomalies in the given user's transactions.
     * 
     * @param userId The ID of the user for whom to detect anomalies.
     * @return A list of anomalies detected in the given user's transactions.
     */
    @Transactional
    public List<AnomalyResponse> detectAnomalies(String userId) {
        Map<String, Double> averageByCategory = transactionAnalyticsService.calculateAverageExpenseByCategory(userId,
                3);
        Map<String, Double> currentMonthSpend = transactionAnalyticsService.getCurrentMonthExpenseByCategory(userId);
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentPeriod = YearMonth.now();

        List<AnomalyResponse> detected = new ArrayList<>();

        currentMonthSpend.forEach((category, currentAmount) -> {
            double averageAmount = averageByCategory.getOrDefault(category, 0.0);
            if (shouldFlagAnomaly(currentAmount, averageAmount)) {
                double deviationPercent = calculateDeviationPercentage(currentAmount, averageAmount);
                upsertAnomaly(userId, category, currentAmount, averageAmount, deviationPercent, now, currentPeriod)
                        .ifPresent(detected::add);
            }
        });

        // Clean up stale anomalies older than 6 months
        anomalyRepo.deleteByUserIdAndDetectedAtBefore(userId, now.minusMonths(6));

        return detected;
    }

    /**
     * Retrieves a list of anomalies for the given user.
     * 
     * @param userId The ID of the user for whom to retrieve anomalies.
     * @return A list of anomalies for the given user.
     */
    @Transactional(readOnly = true)
    public List<AnomalyResponse> getAnomalies(String userId) {
        return anomalyRepo.findByUserIdOrderByDetectedAtDesc(userId).stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Upserts an anomaly for the given user.
     * 
     * @param userId           The ID of the user for whom to upsert the anomaly.
     * @param category         The category of the anomaly.
     * @param currentAmount    The current amount of the anomaly.
     * @param averageAmount    The average amount of the anomaly.
     * @param deviationPercent The percentage deviation of the anomaly.
     * @param detectedAt       The time at which the anomaly was detected.
     * @param period           The period of the anomaly.
     * @return An optional containing the upserted anomaly response.
     */
    private Optional<AnomalyResponse> upsertAnomaly(String userId, String category, double currentAmount,
            double averageAmount, double deviationPercent, LocalDateTime detectedAt, YearMonth period) {
        Optional<Anomaly> existing = anomalyRepo.findTopByUserIdAndCategoryOrderByDetectedAtDesc(userId, category);
        Anomaly anomaly = existing.filter(item -> isSameMonth(item.getDetectedAt(), period)).map(item -> {
            item.setCurrentAmount(currentAmount);
            item.setAverageAmount(averageAmount);
            item.setPercentageDeviation(deviationPercent);
            item.setDetectedAt(detectedAt);
            return item;
        }).orElseGet(() -> Anomaly.builder().anomalyId(UUID.randomUUID().toString()).userId(userId).category(category)
                .currentAmount(currentAmount).averageAmount(averageAmount).percentageDeviation(deviationPercent)
                .detectedAt(detectedAt).build());

        Anomaly saved = anomalyRepo.save(anomaly);
        return Optional.of(toResponse(saved));
    }

    /**
     * Checks if the given amount should be flagged as an anomaly.
     * 
     * @param currentAmount The current amount to check.
     * @param averageAmount The average amount to compare against.
     * @return True if the amount should be flagged as an anomaly, false otherwise.
     */
    private boolean shouldFlagAnomaly(double currentAmount, double averageAmount) {
        if (currentAmount <= 0) {
            return false;
        }
        if (averageAmount == 0) {
            return currentAmount > 0;
        }
        double threshold = averageAmount * ((100 + DEVIATION_THRESHOLD_PERCENT) / 100);
        return currentAmount > threshold;
    }

    /**
     * Calculates the percentage deviation between the current amount and the
     * average amount.
     * 
     * @param currentAmount The current amount to calculate the deviation for.
     * @param averageAmount The average amount to compare against.
     * @return The percentage deviation between the current amount and the average
     *         amount.
     */
    private double calculateDeviationPercentage(double currentAmount, double averageAmount) {
        if (averageAmount == 0) {
            return 100.0;
        }
        return ((currentAmount - averageAmount) / averageAmount) * 100;
    }

    /**
     * Converts an Anomaly entity to an AnomalyResponse.
     * 
     * @param anomaly The Anomaly entity to convert.
     * @return The AnomalyResponse.
     */
    private AnomalyResponse toResponse(Anomaly anomaly) {
        String message = String.format("Your spending in %s is %.2f%% higher than usual.", anomaly.getCategory(),
                anomaly.getPercentageDeviation());
        return AnomalyResponse.builder().anomalyId(anomaly.getAnomalyId()).category(anomaly.getCategory())
                .currentAmount(anomaly.getCurrentAmount()).averageAmount(anomaly.getAverageAmount())
                .percentageDeviation(anomaly.getPercentageDeviation()).detectedAt(anomaly.getDetectedAt())
                .message(message).build();
    }

    /**
     * Checks if the given detected time is in the same month as the given period.
     * 
     * @param detectedAt The time at which the anomaly was detected.
     * @param period     The period to check against.
     * @return True if the detected time is in the same month as the given period,
     *         false otherwise.
     */
    private boolean isSameMonth(LocalDateTime detectedAt, YearMonth period) {
        return detectedAt.getYear() == period.getYear() && detectedAt.getMonthValue() == period.getMonthValue();
    }
}
