package com.example.transaction_service.util;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.kafka.TransactionDto;
import com.example.transaction_service.kafka.TransactionEventType;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.service.insights.dto.UserTransactionSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionUtil {

    private final ObjectMapper objectMapper;

    /**
     * Generates a Transaction object based on the given TransactionRequest object and username.
     *
     * @param request  The TransactionRequest object containing the details of the transaction.
     * @param username The username of the user for which the transaction is being generated.
     * @return A Transaction object containing the generated transaction's details.
     */
    public static Transaction generateTransaction(TransactionRequest request, String username) {
        Transaction transaction = new Transaction();

        transaction.setTransactionID(UUID.randomUUID().toString());
        transaction.setTransactionType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(new Timestamp(System.currentTimeMillis()));
        transaction.setNotes(request.getNotes());
        transaction.setUsername(username);
        transaction.setCategory(request.getCategory());
        transaction.setPaymentType(request.getPaymentType());
        transaction.setRecurringTransaction(request.isRecurring());
        if (transaction.isRecurringTransaction()) {
            transaction.setFrequency(request.getFrequency());
        } else {
            transaction.setFrequency(0);
        }

        log.info("Generated transaction: {}", transaction);
        return transaction;
    }


    /**
     * Converts a Transaction object into a TransactionResponse object.
     *
     * @param transaction The Transaction object to be converted.
     * @return A TransactionResponse object containing the converted transaction's details.
     */
    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();

        response.setTransactionID(transaction.getTransactionID());
        response.setUsername(transaction.getUsername());
        response.setType(transaction.getTransactionType());
        response.setCategory(transaction.getCategory());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate().toLocalDateTime().toLocalDate());
        response.setNotes(transaction.getNotes());
        response.setPaymentType(transaction.getPaymentType());
        response.setRecurring(transaction.isRecurringTransaction());

        log.info("Converted Transaction to TransactionResponse: {}", response);

        return response;

    }

    public String generateTransactionData(Transaction transaction, double savingAmount, UserTransactionSummary summary, TransactionEventType eventType) throws JsonProcessingException {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUsername(transaction.getUsername());
        transactionDto.setSavingsAmount(savingAmount);
        transactionDto.setTotalIncome(summary.getTotalIncome());
        transactionDto.setTotalExpense(summary.getTotalExpense());
        transactionDto.setRemainingBalance(summary.getRemainingBalance());
        transactionDto.setTransactionId(transaction.getTransactionID());
        transactionDto.setTransactionType(transaction.getTransactionType());
        transactionDto.setCategory(transaction.getCategory());
        transactionDto.setTransactionAmount(transaction.getAmount());
        transactionDto.setTransactionTimestamp(transaction.getTransactionDate() != null ? transaction.getTransactionDate().getTime() : System.currentTimeMillis());
        if (transaction.getTransactionDate() != null) {
            var dateTime = transaction.getTransactionDate().toLocalDateTime();
            transactionDto.setTransactionMonth(dateTime.getMonthValue());
            transactionDto.setTransactionYear(dateTime.getYear());
        } else {
            var now = java.time.LocalDate.now();
            transactionDto.setTransactionMonth(now.getMonthValue());
            transactionDto.setTransactionYear(now.getYear());
        }
        transactionDto.setEventType(eventType);

        return objectMapper.writeValueAsString(transactionDto);
    }
}
