package com.example.transaction_service.controller;

import com.example.transaction_service.dto.response.anomaly.AnomalyResponse;
import com.example.transaction_service.service.anomaly.AnomalyDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;

    /**
     * Gets the spending anomalies for a specific user.
     * 
     * @param userId  the ID of the user
     * @param refresh whether to refresh the anomalies
     * @return a list of anomaly responses for the user
     */
    @Operation(summary = "Get spending anomalies for a user", description = "Calculates anomalies for the current month and returns the latest results.")
    @GetMapping("/{userId}")
    public List<AnomalyResponse> getAnomalies(@PathVariable("userId") String userId,
            @RequestParam(value = "refresh", defaultValue = "true") boolean refresh) {
        if (refresh) {
            return anomalyDetectionService.detectAnomalies(userId);
        }
        return anomalyDetectionService.getAnomalies(userId);
    }
}
