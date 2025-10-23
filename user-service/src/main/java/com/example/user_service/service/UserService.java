package com.example.user_service.service;

import com.example.user_service.dto.request.AuthRequest;
import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.exceptions.UserException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.user_service.util.UserDataValidation.validateUserRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates user credentials by checking the username and matching the provided password.
     *
     * @param authRequest The authentication request containing username and password.
     * @return True if credentials are valid, false otherwise.
     * @throws UserException if the username is not found.
     */
    public Boolean validateUserCredentials(AuthRequest authRequest) {
        log.info("Validating user credentials for username: {}", authRequest.getUsername());
        Optional<User> user = userRepo.findByUsername(authRequest.getUsername());
        if (!user.isPresent()) {
            log.warn("Username not found: {}", authRequest.getUsername());
            throw new UserException("Username not found");
        }
        boolean isValid = passwordEncoder.matches(authRequest.getPassword(), user.get().getPassword());
        log.info("User credentials validation result for {}: {}", authRequest.getUsername(), isValid);
        return isValid;
    }

    /**
     * Registers a new user in the system.
     * Validates user data, checks for existing usernames, hashes the password, and saves the user.
     *
     * @param user The user registration request containing user details.
     * @return True if the user is successfully registered.
     * @throws UserException if user data is incorrect, username already exists, or unable to save user details.
     */
    public Boolean registerUser(UserRequest user) {
        log.info("Registering new user with username: {}", user.getUsername());
        if (!validateUserRequest(user)) {
            log.error("User data validation failed for user: {}", user.getUsername());
            throw new UserException("User Data's are not correct");
        }
        Optional<User> existingUser = userRepo.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            log.warn("Username already exists: {}", user.getUsername());
            throw new UserException("Already username is exist.");
        }
        User newUser = userMapper.toUser(user);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userRepo.save(newUser);
            log.info("User registered successfully: {}", user.getUsername());
        } catch (Exception exception) {
            log.error("Error saving user details for user: {}", user.getUsername(), exception);
            throw new UserException("Unable to save the user details");
        }
        return true;
    }

    /**
     * Retrieves user details based on the provided username.
     *
     * @param username The username of the user to retrieve.
     * @return A UserResponse object containing the user's details.
     * @throws UserException if the user is not found with the given username.
     */
    public UserResponse getUserDetails(String username) {
        log.info("Fetching user details for username: {}", username);
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent()) {
            UserResponse response = userMapper.toUserResponse(user.get());
            log.info("User details found for username: {}", username);
            return response;
        }
        log.warn("User not found with username: {}", username);
        throw new UserException("User not found with this username");
    }

    /**
     * Updates user details (firstName, lastName, email, contactNumber) for the given username.
     *
     * @param userRequest The user request containing the username and updated details.
     * @return A UserResponse object with the updated user details.
     * @throws UserException if user data is incorrect, unable to update, or user not found.
     */
    public UserResponse updateUserDetails(UserRequest userRequest) {
        log.info("Updating user details for username: {}", userRequest.getUsername());
        if (!validateUserRequest(userRequest)) {
            log.error("User data validation failed for user: {}", userRequest.getUsername());
            throw new UserException("User Data's are not correct");
        }
        Optional<User> userOpt = userRepo.findByUsername(userRequest.getUsername());
        if (userOpt.isPresent()) {
            try {
                userRepo.updateUserDetailsByUsername(userRequest.getUsername(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getContactNumber());
                log.info("User details updated successfully for username: {}", userRequest.getUsername());
            } catch (Exception exception) {
                log.error("Error updating user details for username: {}", userRequest.getUsername(), exception);
                throw new UserException("Unable to update");
            }
            return getUserDetails(userRequest.getUsername());
        }
        log.warn("User not found with username: {}", userRequest.getUsername());
        throw new UserException("User not found with this username");
    }

    /**
     * Updates the password for the given username.
     *
     * @param username    The username of the user whose password is to be updated.
     * @param newPassword The new password to set.
     * @return True if the password was successfully updated.
     * @throws UserException if unable to update the password or user not found.
     */
    public Boolean updatePassword(String username, String newPassword) {
        log.info("Updating password for username: {}", username);
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            try {
                userRepo.updateUserPasswordByUsername(username, passwordEncoder.encode(newPassword));
                log.info("Password updated successfully for username: {}", username);
                return true;
            } catch (Exception exception) {
                log.error("Error updating password for username: {}", username, exception);
                throw new UserException("Unable to update user password");
            }
        }
        log.warn("User not found with username: {}", username);
        throw new UserException("User not found with this username");
    }
}
