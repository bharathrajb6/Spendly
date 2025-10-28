package com.example.transaction_service.exception;

import com.example.transaction_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type {@link TransactionException}.
     *
     * @param ex The exception to be handled.
     * @return A ResponseEntity containing an ErrorResponse with the status code set to BAD_REQUEST (400) and the error message set to the exception message.
     */
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<?> handleTransactionException(TransactionException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Transaction Error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions of type {@link CacheException}.
     *
     * @param ex The exception to be handled.
     * @return A ResponseEntity containing an ErrorResponse with the status code set to BAD_REQUEST (400) and the error message set to the exception message.
     */
    @ExceptionHandler(CacheException.class)
    public ResponseEntity<?> handleCacheException(CacheException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Cache Error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
