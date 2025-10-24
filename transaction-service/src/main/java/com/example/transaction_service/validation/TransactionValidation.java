package com.example.transaction_service.validation;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.model.ExpenseCategory;
import com.example.transaction_service.model.IncomeCategory;

import java.util.Objects;

public class TransactionValidation {


    public static void validateTransaction(TransactionRequest request) {

        if (request.getType() == null) {
            throw new TransactionException("Type should not be null");
        }

        String type = request.getType();

        if (!Objects.equals(type, "INCOME") && !Objects.equals(type, "EXPENSE")) {
            throw new TransactionException("Type should be INCOME or EXPENSE");
        }

        if (request.getCategory() == null) {
            throw new TransactionException("Category should not be null");
        }

        String category = request.getCategory();

        if (type.equals("INCOME")) {
            try {
                IncomeCategory incomeCategory = IncomeCategory.valueOf(category.toUpperCase());
            } catch (Exception e) {
                throw new TransactionException("Invalid category");
            }
        } else {
            try {
                ExpenseCategory expenseCategory = ExpenseCategory.valueOf(category.toUpperCase());
            } catch (Exception e) {
                throw new TransactionException("Invalid category");
            }
        }


        if (request.getAmount() <= 0) {
            throw new TransactionException("Amount should be greater than 0");
        }

        if (request.getNotes() == null) {
            throw new TransactionException("Notes should not be null");
        }
    }
}
