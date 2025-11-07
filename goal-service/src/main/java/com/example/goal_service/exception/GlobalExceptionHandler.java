package com.example.goal_service.exception;

import com.example.goal_service.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handles GoalException, returning BAD_REQUEST status with error message
     * <p>
     * This method handles GoalException which may be thrown by the application
     * when there is an error while performing a goal related operation.
     *
     * @param ex the GoalException to handle
     * @return a ResponseEntity with BAD_REQUEST status and error message
     */
    @ExceptionHandler(GoalException.class)
    public ResponseEntity<?> handleGoalException(GoalException ex) {
        // Log the exception
        log.error("Error while performing goal related operation: {}", ex.getMessage());

        // Create an ErrorResponse object to return in the response
        ErrorResponse errorResponse = new ErrorResponse();

        // Return a ResponseEntity with BAD_REQUEST status and error message
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle CacheException, return BAD_REQUEST status with error message
     * <p>
     * This method handles CacheException which may be thrown by the application
     * when there is an error in the cache.
     *
     * @param ex the CacheException to handle
     * @return a ResponseEntity with BAD_REQUEST status and error message
     */
    @ExceptionHandler(CacheException.class)
    public ResponseEntity<?> handleCacheException(CacheException ex) {
        // Log the exception
        log.error("Error while accessing cache: {}", ex.getMessage(), ex);

        // Create an ErrorResponse object to return in the response
        ErrorResponse errorResponse = new ErrorResponse();

        // Return a ResponseEntity with BAD_REQUEST status and error message
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
