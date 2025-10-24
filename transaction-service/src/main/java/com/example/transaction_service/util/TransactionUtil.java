package com.example.transaction_service.util;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.model.Transaction;

import java.sql.Timestamp;
import java.util.UUID;

public class TransactionUtil {

    /**
     * This method is used to generate a transaction object from the given request
     *
     * @param request
     * @param username
     * @return
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

        return transaction;
    }


    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();

        response.setTransactionID(transaction.getTransactionID());
        response.setUsername(transaction.getUsername());
        response.setType(transaction.getTransactionType());
        response.setCategory(transaction.getCategory());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setNotes(transaction.getNotes());

        return response;

    }
}
