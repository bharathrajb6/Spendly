package com.example.user_service.exceptions;

import com.example.user_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the UserException, which is thrown for user-related errors.
     * This method catches the UserException and returns a ResponseEntity with a
     * custom ErrorResponse object and an HTTP status of BAD_REQUEST.
     *
     * @param ex The UserException that was thrown.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UserException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
