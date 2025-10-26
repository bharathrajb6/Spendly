package com.example.transaction_service.util;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
public class TransactionUtil {


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
        response.setTransactionDate(transaction.getTransactionDate());
        response.setNotes(transaction.getNotes());
        response.setPaymentType(transaction.getPaymentType());
        response.setRecurring(transaction.isRecurringTransaction());

        log.info("Converted Transaction to TransactionResponse: {}", response);

        return response;

    }
}
