package com.example.user_service.util;

import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.model.User;

public class UserUtility {

    public static UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setContactNumber(user.getContactNumber());
        response.setCurrency(user.getCurrency());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }


    public static User createUserObject(UserRequest userRequest) {

        User newUser = new User();
        newUser.setFirstName(userRequest.getFirstName());
        newUser.setLastName(userRequest.getLastName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setUsername(userRequest.getUsername());
        newUser.setContactNumber(userRequest.getContactNumber());
        newUser.setCurrency(userRequest.getCurrency());

        return newUser;
    }
}
