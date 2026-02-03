package com.example.user_service.util;

import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.model.User;

import java.sql.Timestamp;

public class UserUtility {

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity to convert
     * @return a UserResponse containing the user's public details
     */
    public static UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setContactNumber(user.getContactNumber());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    /**
     * Creates a new User entity from a UserRequest DTO.
     * Automatically sets the creation timestamp.
     *
     * @param userRequest the registration request containing user details
     * @return a new User entity object
     */
    public static User createUserObject(UserRequest userRequest) {

        User newUser = new User();
        newUser.setFirstName(userRequest.getFirstName());
        newUser.setLastName(userRequest.getLastName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setUsername(userRequest.getUsername());
        newUser.setPassword(userRequest.getPassword());
        newUser.setContactNumber(userRequest.getContactNumber());
        newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return newUser;
    }
}
