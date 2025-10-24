package com.example.transaction_service.controller;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
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


    @RequestMapping(value = "/transaction", method = RequestMethod.POST)
    public TransactionResponse addTransaction(@RequestHeader("X-Username") String username, @RequestBody TransactionRequest request) {
        return transactionService.addTransaction(username, request);
    }


    /**
     * This method is used to get the transaction details by using its ID
     *
     * @param transactionID
     * @return
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.GET)
    public TransactionResponse getTransaction(@PathVariable String transactionID) {
        return transactionService.getTransaction(transactionID);
    }

    /**
     * This method will return all the transactions for the user
     *
     * @param pageable
     * @return
     */
    @GetMapping("/transaction")
    public Page<TransactionResponse> getAllTransactions(
            @RequestHeader("X-Username") String username,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return transactionService.getAllTransactionForUser(username, pageable);
    }

    /**
     * This method is used to update the transaction by using its ID.
     *
     * @param transactionID
     * @param request
     * @return
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.PUT)
    public TransactionResponse updateTransaction(@PathVariable String transactionID, @RequestBody TransactionRequest request) {
        return transactionService.updateTransaction(transactionID, request);
    }

    /**
     * This method is used to delete the transaction
     *
     * @param transactionID
     */
    @RequestMapping(value = "/transaction/{transactionID}", method = RequestMethod.DELETE)
    public void deleteTransaction(@PathVariable String transactionID) {
        transactionService.deleteTransaction(transactionID);
    }

    /**
     * This method will return the filtered transactions for the user based on given time range
     *
     * @param start
     * @param end
     * @param pageable
     * @return
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
     * This method will return list of transaction filtered by category
     *
     * @param value
     * @param pageable
     * @return
     */
    @GetMapping("/transaction/category/{value}")
    public Page<TransactionResponse> getTransactionByCategory(
            @RequestHeader("X-Username") String username,
            @PathVariable String value,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return transactionService.getTransactionsListByCategory(username, value, pageable);
    }

    /**
     * This method will return amount spent on that category
     *
     * @param value
     * @return
     */
    @RequestMapping(value = "/transaction/category/{value}/total", method = RequestMethod.GET)
    public double getTransactionByCategory(@RequestHeader("X-Username") String username,@PathVariable String value) {
        return transactionService.getTransactionAmountByCategory(username,value);
    }


}
