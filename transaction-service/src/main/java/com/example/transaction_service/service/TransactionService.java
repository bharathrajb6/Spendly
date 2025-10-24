package com.example.transaction_service.service;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.model.ExpenseCategory;
import com.example.transaction_service.model.IncomeCategory;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.repo.TransactionRepo;
import com.example.transaction_service.util.TransactionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.transaction_service.util.TransactionUtil.generateTransaction;
import static com.example.transaction_service.util.TransactionUtil.toTransactionResponse;
import static com.example.transaction_service.validation.TransactionValidation.validateTransaction;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    public TransactionResponse addTransaction(String username, TransactionRequest request) {

        // Validate the transaction request object
        validateTransaction(request);

        // Convert the request object to transaction model
        Transaction transaction = generateTransaction(request, username);

        try {
            // Save the object to database
            transactionRepo.save(transaction);
        } catch (Exception exception) {
            throw new TransactionException(String.format("Unable to save transaction", exception.getMessage()));
        }

        return getTransaction(transaction.getTransactionID());
    }


    public TransactionResponse getTransaction(String transactionID) {
        Optional<Transaction> transaction = transactionRepo.findByTransactionID(transactionID);

        if (transaction.isEmpty()) {
            throw new TransactionException("Transaction not found with this ID.");
        }

        return toTransactionResponse(transaction.get());
    }


    public Page<TransactionResponse> getAllTransactionForUser(String username, Pageable pageable) {

        Page<Transaction> transactions = transactionRepo.findTransactionByUsername(username, pageable);

        if (transactions.isEmpty()) {
            throw new TransactionException("No transactions found for this user");
        }

        return transactions.map(TransactionUtil::toTransactionResponse);
    }


    public TransactionResponse updateTransaction(String transactionID, TransactionRequest request) {

        if (transactionID == null) {
            throw new TransactionException("Transaction ID should not be null");
        }

        Optional<Transaction> transaction = transactionRepo.findByTransactionID(transactionID);

        if (transaction.isEmpty()) {
            throw new TransactionException("Transaction not found with this ID.");
        }

        validateTransaction(request);

        try {
            transactionRepo.updateTransaction(request.getType(), request.getCategory(), request.getAmount(), request.getNotes(), transaction.get().getUsername());
        } catch (Exception exception) {
            throw new TransactionException(exception.getMessage());
        }

        return getTransaction(transactionID);
    }

    public void deleteTransaction(String transactionID) {

        if (transactionID == null) {
            throw new TransactionException("Transaction ID should not be null");
        }

        Optional<Transaction> transaction = transactionRepo.findByTransactionID(transactionID);

        if (transaction.isEmpty()) {
            throw new TransactionException("Transaction not found with this ID.");
        }

        try {
            transactionRepo.delete(transaction.get());
        } catch (Exception exception) {
            throw new TransactionException(exception.getMessage());
        }
    }

    public Page<TransactionResponse> getFilteredTransaction(String username, String start, String end, Pageable pageable) {

        if (start == null || end == null) {
            throw new TransactionException("Start and end date should not be null");
        }

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        LocalDate startDate, endDate;

        try {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        } catch (Exception exception) {
            throw new TransactionException("Invalid date format");
        }

        if (endDate.isBefore(startDate)) {
            throw new TransactionException("End date should be greater than start date");
        }
        if (startDate.isAfter(endDate)) {
            throw new TransactionException("Start date should be less than end date");
        }

        Page<Transaction> transactions = transactionRepo.findTransactionByUsername(username, pageable);

        if (transactions.isEmpty()) {
            throw new TransactionException("No transactions found for this user");
        }

        transactions.getContent().stream().filter(transaction -> {
            LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
            return transactionDate.equals(startDate) || transactionDate.equals(endDate) || (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate));
        });


        return transactions.map(TransactionUtil::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsListByCategory(String username, String value, Pageable pageable) {

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        if (value == null) {
            throw new TransactionException("Category should not be null");
        }

        try {
            IncomeCategory incomeCategory = IncomeCategory.valueOf(value.toUpperCase());
        } catch (Exception exception) {
            try {
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(value.toUpperCase());
            } catch (Exception exception1) {
                throw new TransactionException("Invalid category");
            }
        }

        Page<Transaction> transactions = transactionRepo.findTransactionByUsername(username, pageable);

        transactions.getContent().stream().filter(transaction -> {
            return !transaction.getTransactionType().equals(value);
        });

        return transactions.map(TransactionUtil::toTransactionResponse);


    }

    public double getTransactionAmountByCategory(String username, String value) {

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        if (value == null) {
            throw new TransactionException("Category should not be null");
        }


        try {
            IncomeCategory incomeCategory = IncomeCategory.valueOf(value.toUpperCase());
        } catch (Exception exception) {
            try {
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(value.toUpperCase());
            } catch (Exception exception1) {
                throw new TransactionException("Invalid category");
            }
        }

        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);

        double amount = transactions.stream()
                .filter(transaction -> transaction.getCategory().equalsIgnoreCase(value))
                .mapToDouble(Transaction::getAmount)
                .sum();


        return amount;
    }
}
