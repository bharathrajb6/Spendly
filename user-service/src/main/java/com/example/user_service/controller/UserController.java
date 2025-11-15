package com.example.user_service.controller;

import com.example.user_service.dto.request.AuthRequest;
import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * Validates user credentials by checking the username and matching the provided password.
     *
     * @param authRequest The authentication request containing username and password.
     * @return True if credentials are valid, false otherwise.
     * @throws UserException if the username is not found.
     */
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public Boolean validateUserCredentials(@RequestBody AuthRequest authRequest) {
        return userService.validateUserCredentials(authRequest);
    }

    /**
     * Registers a new user in the system.
     * Validates user data, checks for existing usernames, hashes the password, and saves the user.
     *
     * @param userRequest The user registration request containing user details.
     * @return True if the user is successfully registered.
     * @throws UserException if user data is incorrect, username already exists, or unable to save user details.
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Boolean registerUser(@RequestBody UserRequest userRequest) {
        log.info("Received registration request: {}", userRequest);
        Boolean result = userService.registerUser(userRequest);
        return result;
    }

    /**
     * Retrieves user details based on the provided username.
     *
     * @param authHeader The Authorization header containing the JWT token.
     * @param username   The username of the user to retrieve.
     * @return A UserResponse object containing the user's details.
     * @throws UserException if the user is not found with the given username.
     */
    @RequestMapping(value = "/getUserDetails", method = RequestMethod.GET)
    public UserResponse getUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                       @RequestHeader(value = "X-Username", required = false) String username) {
        return userService.getUserDetails(username);
    }

    /**
     * Updates user details (firstName, lastName, email, contactNumber) for the given username.
     *
     * @param username    The username of the user to update.
     * @param userRequest The user request containing the username and updated details.
     * @return A UserResponse object with the updated user details.
     * @throws UserException if user data is incorrect, unable to update, or user not found.
     */
    @RequestMapping(value = "/updateUserDetails", method = RequestMethod.PUT)
    public UserResponse updateUserDetails(@RequestHeader(value = "X-Username", required = false) String username,
                                          @RequestBody UserRequest userRequest) {
        if (!Objects.equals(username, userRequest.getUsername())) {
            return null;
        }
        return userService.updateUserDetails(userRequest);
    }

    /**
     * Updates the password for the given username.
     *
     * @param username    The username of the user to update.
     * @param authRequest The authentication request containing the username and new password.
     * @return True if the password was successfully updated.
     * @throws UserException if unable to update the password or user not found.
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
    public Boolean updatePassword(@RequestHeader(value = "X-Username", required = false) String username,
                                  @RequestBody AuthRequest authRequest) {
        if (!Objects.equals(username, authRequest.getUsername())) {
            return null;
        }
        return userService.updatePassword(username, authRequest.getPassword());
    }


    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMyData(@RequestHeader(value = "X-Username", required = false) String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public String getUserEmail(@PathVariable("username") String username) {
        return userService.getUserDetails(username).getEmail();
    }

}
