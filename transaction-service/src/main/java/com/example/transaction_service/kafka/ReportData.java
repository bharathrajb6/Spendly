package com.example.transaction_service.kafka;

import com.example.transaction_service.dto.response.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportData {
    private String username;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TransactionResponse> transactions;
}
