package com.example.transaction_service.validation;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.model.ExpenseCategory;
import com.example.transaction_service.model.IncomeCategory;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class TransactionValidation {


    /**
     * Validates a transaction request object.
     *
     * @param request the transaction request object to validate.
     * @throws TransactionException if the transaction request object is invalid.
     */
    public static void validateTransaction(TransactionRequest request) {

        if (request.getType() == null) {
            log.error("Transaction type is null");
            throw new TransactionException("Type should not be null");
        }

        String type = request.getType();

        if (!Objects.equals(type, "INCOME") && !Objects.equals(type, "EXPENSE")) {
            log.error("Invalid transaction type: {}", type);
            throw new TransactionException("Type should be INCOME or EXPENSE");
        }

        if (request.getCategory() == null) {
            log.error("Transaction category is null");
            throw new TransactionException("Category should not be null");
        }

        String category = request.getCategory();

        if (type.equals("INCOME")) {
            try {
                IncomeCategory incomeCategory = IncomeCategory.valueOf(category.toUpperCase());
            } catch (Exception e) {
                log.error("Invalid income category: {}", category);
                throw new TransactionException("Invalid category");
            }
        } else {
            try {
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(category.toUpperCase());
            } catch (Exception e) {
                log.error("Invalid expense category: {}", category);
                throw new TransactionException("Invalid category");
            }
        }


        if (request.getAmount() <= 0) {
            log.error("Invalid transaction amount: {}", request.getAmount());
            throw new TransactionException("Amount should be greater than 0");
        }
    }
}
