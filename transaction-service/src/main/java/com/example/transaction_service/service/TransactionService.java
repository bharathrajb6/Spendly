package com.example.transaction_service.service;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.model.ExpenseCategory;
import com.example.transaction_service.model.IncomeCategory;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.repo.TransactionRepo;
import com.example.transaction_service.util.TransactionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.transaction_service.util.TransactionUtil.generateTransaction;
import static com.example.transaction_service.util.TransactionUtil.toTransactionResponse;
import static com.example.transaction_service.validation.TransactionValidation.validateTransaction;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private RedisService redisService;


    /**
     * This method is used to add a new transaction for the given username.
     *
     * @param username The username for which the transaction is being added.
     * @param request  The transaction request object containing the details of the transaction.
     * @return The transaction response object containing the saved transaction's details.
     * @throws TransactionException If the transaction is unable to be saved to the database.
     */
    public TransactionResponse addTransaction(String username, TransactionRequest request) {

        // Validate the transaction request object
        validateTransaction(request);

        // Convert the request object to transaction model
        Transaction transaction = generateTransaction(request, username);

        try {
            // Save the object to database
            transactionRepo.save(transaction);
            redisService.setData(transaction.getTransactionID(), transaction, 3600L);
        } catch (Exception exception) {
            // If unable to save, throw exception
            throw new TransactionException(String.format("Unable to save transaction", exception.getMessage()));
        }

        return getTransaction(transaction.getTransactionID());
    }


    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionID The ID of the transaction to retrieve.
     * @return The transaction response object containing the retrieved transaction's details.
     * @throws TransactionException If no transaction is found with the given ID.
     */
    public TransactionResponse getTransaction(String transactionID) {
        // Check if transactionID is null
        if (transactionID == null) {
            throw new TransactionException("Transaction ID should not be null");
        }

        // Fetch the transaction detail from cache
        Transaction transaction = redisService.getData(transactionID, Transaction.class);

        // If transaction is null, then return from database
        if (transaction == null) {
            // Find the transaction by its ID
            transaction = transactionRepo.findByTransactionID(transactionID).orElseThrow(() -> new TransactionException("Transaction not found with this ID."));
            redisService.setData(transaction.getTransactionID(), transaction, 3600L);
        }

        return toTransactionResponse(transaction);
    }


    /**
     * Retrieves all transactions for a given username.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param pageable The pageable object containing the page and size details.
     * @return A page of transaction response objects containing all transactions for the given user.
     * @throws TransactionException If no transactions are found for the given user.
     */
    public Page<TransactionResponse> getAllTransactionForUser(String username, Pageable pageable) {

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        Object cachedData = redisService.getData(username + "-transactions", List.class);

        List<Transaction> transactions;
        if (cachedData != null) {
            transactions = ((List<?>) cachedData).stream().map(item -> new ObjectMapper().convertValue(item, Transaction.class)).collect(Collectors.toList());
        } else {
            transactions = transactionRepo.findTransactionByUsername(username);
            redisService.setData(username + "-transactions", transactions, 3600L);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), transactions.size());

        List<TransactionResponse> responses = transactions.subList(start, end).stream().map(TransactionUtil::toTransactionResponse).collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, transactions.size());
    }


    /**
     * Updates an existing transaction for the given username.
     *
     * @param transactionID The ID of the transaction to update.
     * @param request       The transaction request object containing the updated details of the transaction.
     * @return The transaction response object containing the updated transaction's details.
     * @throws TransactionException If the transaction is unable to be updated to the database or if no transaction is found with the given ID.
     */
    public TransactionResponse updateTransaction(String transactionID, TransactionRequest request) {

        if (transactionID == null) {
            throw new TransactionException("Transaction ID should not be null");
        }

        // Find the transaction by its ID
        Optional<Transaction> transaction = transactionRepo.findByTransactionID(transactionID);

        if (transaction.isEmpty()) {
            throw new TransactionException("Transaction not found with this ID.");
        }

        // Validate the transaction request object
        validateTransaction(request);

        try {
            // Update the transaction in the database
            transactionRepo.updateTransaction(request.getType(), request.getCategory(), request.getAmount(), request.getNotes(), request.getPaymentType(), request.isRecurring(), transactionID);
            redisService.deleteData(transactionID);
        } catch (Exception exception) {
            // If unable to update, throw exception
            throw new TransactionException(exception.getMessage());
        }

        return getTransaction(transactionID);
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param transactionID The ID of the transaction to delete.
     * @throws TransactionException If the transaction is unable to be deleted from the database or if no transaction is found with the given ID.
     */
    public void deleteTransaction(String transactionID) {

        if (transactionID == null) {
            throw new TransactionException("Transaction ID should not be null");
        }

        // Find the transaction by its ID
        Optional<Transaction> transaction = transactionRepo.findByTransactionID(transactionID);

        if (transaction.isEmpty()) {
            throw new TransactionException("Transaction not found with this ID.");
        }

        try {
            // Delete the transaction from the database
            transactionRepo.delete(transaction.get());
            redisService.deleteData(transactionID);
        } catch (Exception exception) {
            // If unable to delete, throw exception
            throw new TransactionException(exception.getMessage());
        }
    }

    /**
     * Retrieves all transactions for a given username within a given date range.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param start    The start date of the range.
     * @param end      The end date of the range.
     * @param pageable The pageable object containing the page and size details.
     * @return A page of transaction response objects containing all transactions for the given user within the given date range.
     * @throws TransactionException If the start date is after the end date, or if no transactions are found for the given user.
     */
    public Page<TransactionResponse> getFilteredTransaction(String username, String start, String end, Pageable pageable) {

        if (start == null || end == null) {
            throw new TransactionException("Start and end date should not be null");
        }

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        LocalDate startDate, endDate;

        try {
            // Parse the start and end dates
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        } catch (Exception exception) {
            // If unable to parse, throw exception
            throw new TransactionException("Invalid date format");
        }

        if (endDate.isBefore(startDate)) {
            throw new TransactionException("End date should be greater than start date");
        }

        if (startDate.isAfter(endDate)) {
            throw new TransactionException("Start date should be less than end date");
        }

        // Retrieve all transactions for the given username
        Page<Transaction> transactions = transactionRepo.findTransactionByUsername(username, pageable);

        if (transactions.isEmpty()) {
            throw new TransactionException("No transactions found for this user");
        }

        // Filter transactions based on the given date range
        transactions.getContent().stream().filter(transaction -> {
            LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
            return transactionDate.equals(startDate) || transactionDate.equals(endDate) || (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate));
        });

        return transactions.map(TransactionUtil::toTransactionResponse);
    }

    /**
     * Retrieves all transactions for a given username with a given category.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param value    The category of transactions to retrieve.
     * @param pageable The pageable object containing the page and size details.
     * @return A page of transaction response objects containing all transactions for the given user with the given category.
     * @throws TransactionException If the username is null, or if the category is invalid.
     */
    public Page<TransactionResponse> getTransactionsListByCategory(String username, String value, Pageable pageable) {

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        if (value == null) {
            throw new TransactionException("Category should not be null");
        }

        try {
            // Check if the category is a valid income category
            IncomeCategory incomeCategory = IncomeCategory.valueOf(value.toUpperCase());
        } catch (Exception exception) {
            // If not, check if it's a valid expense category
            try {
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(value.toUpperCase());
            } catch (Exception exception1) {
                // If neither, throw exception
                throw new TransactionException("Invalid category");
            }
        }

        // Retrieve all transactions for the given username
        Page<Transaction> transactions = transactionRepo.findTransactionByUsername(username, pageable);

        // Filter transactions based on the given category and collect results
        List<Transaction> filteredTransactions = transactions.getContent().stream().filter(transaction -> transaction.getCategory().equals(value)).collect(Collectors.toList());

        // Convert to Page and then map to response
        return new PageImpl<>(filteredTransactions, pageable, filteredTransactions.size()).map(TransactionUtil::toTransactionResponse);
    }

    /**
     * Retrieves the total amount of transactions for the given user with the given category.
     *
     * @param username The username for which the total amount of transactions is being retrieved.
     * @param value    The category of transactions for which the total amount is being retrieved.
     * @return The total amount of transactions for the given user with the given category.
     * @throws TransactionException If the username is null, or if the category is invalid.
     */
    public double getTransactionAmountByCategory(String username, String value) {

        // Check if username are null
        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        // Check if category is null
        if (value == null) {
            throw new TransactionException("Category should not be null");
        }

        Double amount = redisService.getData(username + value, Double.class);
        if (amount != null) {
            return amount;
        }


        try {
            // Check if the category is a valid income category
            IncomeCategory incomeCategory = IncomeCategory.valueOf(value.toUpperCase());
        } catch (Exception exception) {
            try {
                // If not, check if it's a valid expense category
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(value.toUpperCase());
            } catch (Exception exception1) {
                // If neither, throw exception
                throw new TransactionException("Invalid category");
            }
        }

        // Retrieve all transactions for the given username
        List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);

        // Calculate the total amount for the given category
        amount = transactions.stream().filter(transaction -> transaction.getCategory().equalsIgnoreCase(value)).mapToDouble(Transaction::getAmount).sum();

        redisService.setData(username + value, amount, 3600L);
        return amount;
    }


    /**
     * Retrieves all transactions for a given username in a given month.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param date     The month for which all transactions are being retrieved.
     * @return A page of transaction response objects containing all transactions for the given user in the given month.
     * @throws TransactionException If the username is null, or if the date is invalid.
     */
    public Page<TransactionResponse> getTransactionByMonth(String username, int month, int year, Pageable pageable) {

        if (username == null || month == 0) {
            throw new TransactionException("Username and date should not be null");
        }

        // Retrieve all transactions for the given username
        Page<TransactionResponse> transactionResponses = getAllTransactionForUser(username, pageable);

        List<TransactionResponse> filteredTransactions = new ArrayList<>();

        if (transactionResponses.getContent() != null) {
            filteredTransactions = transactionResponses.getContent().stream().filter(transactionResponse -> {
                LocalDate transactionDate = transactionResponse.getTransactionDate().toLocalDateTime().toLocalDate();
                return transactionDate.getYear() == year && transactionDate.getMonth().getValue() == month;
            }).collect(Collectors.toList());
        }

        return new PageImpl<>(filteredTransactions, pageable, filteredTransactions.size());
    }

    /**
     * Retrieves the total amount of transactions for a given username in a given month.
     *
     * @param username The username for which the total amount of transactions is being retrieved.
     * @param month    The month for which the total amount of transactions is being retrieved.
     * @return The total amount of transactions for the given user in the given month.
     * @throws TransactionException If the username is null, or if the date is invalid.
     */
    public Map<String, Double> getTotalTransactionAmountInTheMonth(String username, int month, int year) {

        Map<String, Double> transactionList = new HashMap<>();

        if (username == null || month == 0) {
            throw new TransactionException("Username and date should not be null");
        }

        Double totalIncomeAmount = redisService.getData(username + month + year + "INCOME", Double.class);
        Double totalExpenseAmount = redisService.getData(username + month + year + "EXPENSE", Double.class);

        if (totalIncomeAmount == null || totalExpenseAmount == null) {
            List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);

            totalIncomeAmount = transactions.stream().filter(transaction -> {
                LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
                return transactionDate.getYear() == year && transactionDate.getMonth().getValue() == month && transaction.getTransactionType().equalsIgnoreCase("INCOME");
            }).mapToDouble(Transaction::getAmount).sum();
            redisService.setData(username + month + year + "INCOME", totalIncomeAmount, 3600L);

            totalExpenseAmount = transactions.stream().filter(transaction -> {
                LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
                return transactionDate.getYear() == year && transactionDate.getMonth().getValue() == month && transaction.getTransactionType().equalsIgnoreCase("EXPENSE");
            }).mapToDouble(Transaction::getAmount).sum();
            redisService.setData(username + month + year + "EXPENSE", totalExpenseAmount, 3600L);
        }

        transactionList.put("INCOME", totalIncomeAmount);
        transactionList.put("EXPENSE", totalExpenseAmount);
        transactionList.put("REMAIN", totalIncomeAmount - totalExpenseAmount);

        return transactionList;
    }

    /**
     * Retrieves the total amount of transactions for a given username in a given year.
     *
     * @param username The username for which the total amount of transactions is being retrieved.
     * @param year     The year for which the total amount of transactions is being retrieved.
     * @return A map containing the total amount of income and expense transactions for the given user in the given year.
     * @throws TransactionException If the username is null.
     */
    public Map<String, Double> getTotalTransactionAmountInYear(String username, int year) {

        if (username == null) {
            throw new TransactionException("Username should not be null");
        }

        Map<String, Double> transactionList = new HashMap<>();

        Double totalIncomeAmount = redisService.getData(username + year + "INCOME", Double.class);
        Double totalExpenseAmount = redisService.getData(username + year + "EXPENSE", Double.class);

        if (totalIncomeAmount == null || totalExpenseAmount == null) {
            List<Transaction> transactions = transactionRepo.findTransactionByUsername(username);

            totalIncomeAmount = transactions.stream().filter(transaction -> {
                LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
                return transactionDate.getYear() == year && transaction.getTransactionType().equalsIgnoreCase("INCOME");
            }).mapToDouble(Transaction::getAmount).sum();
            redisService.setData(username + year + "INCOME", totalIncomeAmount, 3600L);

            totalExpenseAmount = transactions.stream().filter(transaction -> {
                LocalDate transactionDate = transaction.getTransactionDate().toLocalDateTime().toLocalDate();
                return transactionDate.getYear() == year && transaction.getTransactionType().equalsIgnoreCase("EXPENSE");
            }).mapToDouble(Transaction::getAmount).sum();
            redisService.setData(username + year + "EXPENSE", totalExpenseAmount, 3600L);
        }

        transactionList.put("INCOME", totalIncomeAmount);
        transactionList.put("EXPENSE", totalExpenseAmount);
        transactionList.put("REMAIN", totalIncomeAmount - totalExpenseAmount);

        return transactionList;
    }
}
