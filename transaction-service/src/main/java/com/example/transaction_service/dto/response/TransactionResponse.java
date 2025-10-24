package com.example.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
    private Timestamp transactionDate;
    private String notes;
}
