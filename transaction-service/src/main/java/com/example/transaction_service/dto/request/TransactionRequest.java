package com.example.transaction_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {
    private double amount;
    private String category;
    private String type;
    private String notes;
    private String paymentType;
    private boolean recurring;
}
