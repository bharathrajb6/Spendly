package com.example.transaction_service.service;

import com.example.transaction_service.kafka.EventProducer;
import com.example.transaction_service.kafka.ReportData;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.util.TransactionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ReportService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generates a financial report for a user based on transaction history and
     * sends it
     * to the appropriate Kafka topic for processing (CSV or PDF).
     *
     * @param username  the username of the user requesting the report
     * @param type      the format of the report ("CSV" or other for PDF)
     * @param startDate the start date of the report period (ISO format)
     * @param endDate   the end date of the report period (ISO format)
     * @throws JsonProcessingException if an error occurs during JSON serialization
     */
    public void generateReport(String username, String type, String startDate, String endDate)
            throws JsonProcessingException {

        List<Transaction> transactions = transactionService.getFilteredTransactionsForUser(username, startDate,
                endDate);

        ReportData reportData = new ReportData();
        reportData.setUsername(username);
        reportData.setTransactions(transactions.stream().map(TransactionUtil::toTransactionResponse).toList());
        reportData.setStartDate(LocalDate.parse(startDate));
        reportData.setEndDate(LocalDate.parse(endDate));

        String jsonValue = objectMapper.writeValueAsString(reportData);

        if (type.equals("CSV")) {
            eventProducer.sendTopic("REPORT-CSV", jsonValue);
        } else {
            eventProducer.sendTopic("REPORT0-PDF", jsonValue);
        }
    }
}
