package com.example.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private String transactionID;
    private String username;
    private double amount;
    private String category;
    private String type;
    private LocalDate transactionDate;
    private String notes;
    private String paymentType;
    private boolean isRecurring;
}
