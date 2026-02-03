package com.example.export_report_service.kafka;

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

