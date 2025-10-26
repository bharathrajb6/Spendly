package com.example.transaction_service.controller;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;


    /**
     * This method is used to add a new transaction for the given username.
     *
     * @param username The username for which the transaction is being added.
     * @param request  The transaction request object containing the details of the transaction.
     * @return The transaction response object containing the saved transaction's details.
     * @throws TransactionException If the transaction is unable to be saved to the database.
     */
    @RequestMapping(value = "/transaction", method = RequestMethod.POST)
    public TransactionResponse addTransaction(@RequestHeader("X-Username") String username, @RequestBody TransactionRequest request) {
        return transactionService.addTransaction(username, request);
    }

    /**
     * This method will return a transaction by its ID
     *
     * @param transactionID The ID of the transaction to retrieve.
     * @return The transaction response object containing the retrieved transaction's details.
     * @throws TransactionException If no transaction is found with the given ID.
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.GET)
    public TransactionResponse getTransaction(@PathVariable String transactionID) {
        return transactionService.getTransaction(transactionID);
    }


    /**
     * Retrieves all transactions for a given username.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param pageable The pageable object containing the page and size details.
     * @return A page of transaction response objects containing all transactions for the given user.
     * @throws TransactionException If no transactions are found for the given user.
     */
    @GetMapping("/transaction")
    public Page<TransactionResponse> getAllTransactions(
            @RequestHeader("X-Username") String username,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return transactionService.getAllTransactionForUser(username, pageable);
    }


    /**
     * This method is used to update an existing transaction for the given username.
     *
     * @param transactionID The ID of the transaction to update.
     * @param request       The transaction request object containing the updated details of the transaction.
     * @return The transaction response object containing the updated transaction's details.
     * @throws TransactionException If the transaction is unable to be updated to the database or if no transaction is found with the given ID.
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.PUT)
    public TransactionResponse updateTransaction(@PathVariable String transactionID, @RequestBody TransactionRequest request) {
        return transactionService.updateTransaction(transactionID, request);
    }


    /**
     * Deletes a transaction by its ID.
     *
     * @param transactionID The ID of the transaction to delete.
     * @throws TransactionException If the transaction is unable to be deleted from the database or if no transaction is found with the given ID.
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.DELETE)
    public void deleteTransaction(@PathVariable String transactionID) {
        transactionService.deleteTransaction(transactionID);
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
    @GetMapping("/transaction/filter")
    public Page<TransactionResponse> getTransactionFilter(
            @RequestHeader("X-Username") String username,
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return transactionService.getFilteredTransaction(username, start, end, pageable);
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
    @GetMapping("/transaction/category/{value}")
    public Page<TransactionResponse> getTransactionByCategory(
            @RequestHeader("X-Username") String username,
            @PathVariable String value,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return transactionService.getTransactionsListByCategory(username, value, pageable);
    }


    /**
     * Retrieves the total amount of transactions for the given user with the given category.
     *
     * @param username The username for which the total amount of transactions is being retrieved.
     * @param value    The category of transactions for which the total amount is being retrieved.
     * @return The total amount of transactions for the given user with the given category.
     * @throws TransactionException If the username is null, or if the category is invalid.
     */
    @RequestMapping(value = "/transaction/category/{value}/total", method = RequestMethod.GET)
    public double getTransactionByCategory(@RequestHeader("X-Username") String username, @PathVariable String value) {
        return transactionService.getTransactionAmountByCategory(username, value);
    }

}
