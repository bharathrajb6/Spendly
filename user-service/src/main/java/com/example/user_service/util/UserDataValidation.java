package com.example.user_service.util;

import com.example.user_service.dto.request.UserRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDataValidation {

    /**
     * Validates the first name.
     * The first name should not be null, empty, or start with a digit.
     *
     * @param firstName The first name to validate.
     * @return True if the first name is valid, false otherwise.
     */
    public static boolean isValidFirstName(String firstName) {
        log.info("Validating first name: {}", firstName);
        if (firstName == null || firstName.trim().isEmpty()) {
            log.warn("First name is null or empty.");
            return false;
        }
        char firstChar = firstName.trim().charAt(0);
        if (Character.isDigit(firstChar)) {
            log.warn("First name starts with a digit.");
            return false;
        }
        log.info("First name is valid.");
        return true;
    }

    /**
     * Validates the last name.
     * The last name should not be null, empty, or start with a digit.
     *
     * @param lastName The last name to validate.
     * @return True if the last name is valid, false otherwise.
     */
    public static boolean isValidLastName(String lastName) {
        log.info("Validating last name: {}", lastName);
        if (lastName == null || lastName.trim().isEmpty()) {
            log.warn("Last name is null or empty.");
            return false;
        }
        char firstChar = lastName.trim().charAt(0);
        if (Character.isDigit(firstChar)) {
            log.warn("Last name starts with a digit.");
            return false;
        }
        log.info("Last name is valid.");
        return true;
    }

    /**
     * Validates the email address.
     * The email should not be null or empty and should match a basic email pattern.
     *
     * @param email The email address to validate.
     * @return True if the email is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        log.info("Validating email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email is null or empty.");
            return false;
        }
        // Basic email regex
        boolean isValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!isValid) {
            log.warn("Email format is invalid.");
        } else {
            log.info("Email is valid.");
        }
        return isValid;
    }

    /**
     * Validates the username.
     * The username should not be null or empty.
     *
     * @param username The username to validate.
     * @return True if the username is valid, false otherwise.
     */
    public static boolean isValidUsername(String username) {
        log.info("Validating username: {}", username);
        boolean isValid = username != null && !username.trim().isEmpty();
        if (!isValid) {
            log.warn("Username is null or empty.");
        } else {
            log.info("Username is valid.");
        }
        return isValid;
    }

    /**
     * Validates the password.
     * The password should not be null or empty.
     *
     * @param password The password to validate.
     * @return True if the password is valid, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        log.info("Validating password.");
        boolean isValid = password != null && !password.trim().isEmpty();
        if (!isValid) {
            log.warn("Password is null or empty.");
        } else {
            log.info("Password is valid.");
        }
        return isValid;
    }

    /**
     * Validates the contact number.
     * The contact number should not be null or empty and should contain only digits with a length between 7 and 15.
     *
     * @param contactNumber The contact number to validate.
     * @return True if the contact number is valid, false otherwise.
     */
    public static boolean isValidContactNumber(String contactNumber) {
        log.info("Validating contact number: {}", contactNumber);
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            log.warn("Contact number is null or empty.");
            return false;
        }
        // Basic phone number check: digits only, length 7-15
        boolean isValid = contactNumber.matches("^\\d{7,15}$");
        if (!isValid) {
            log.warn("Contact number format is invalid.");
        } else {
            log.info("Contact number is valid.");
        }
        return isValid;
    }

    /**
     * Validates all attributes of a UserRequest object.
     *
     * @param userRequest The UserRequest object to validate.
     * @return True if all attributes are valid, false otherwise.
     */
    public static boolean validateUserRequest(UserRequest userRequest) {
        log.info("Validating user request for username: {}", userRequest.getUsername());
        boolean isValid = isValidFirstName(userRequest.getFirstName()) &&
                isValidLastName(userRequest.getLastName()) &&
                isValidEmail(userRequest.getEmail()) &&
                isValidUsername(userRequest.getUsername()) &&
                isValidPassword(userRequest.getPassword()) &&
                isValidContactNumber(userRequest.getContactNumber());
        if (isValid) {
            log.info("User request validation successful for username: {}", userRequest.getUsername());
        } else {
            log.warn("User request validation failed for username: {}", userRequest.getUsername());
        }
        return isValid;
    }
}
