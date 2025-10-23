package com.example.user_service.mapper;

import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a User object to a UserResponse object.
     *
     * @param user The User object to map.
     * @return A UserResponse object containing the user's details.
     */
    UserResponse toUserResponse(User user);

    /**
     * Maps a UserRequest object to a User object.
     *
     * @param userRequest The UserRequest object to map.
     * @return A User object containing the user's details.
     */
    User toUser(UserRequest userRequest);
}

