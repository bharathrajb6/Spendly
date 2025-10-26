package com.example.api_gateway.controller;

import com.example.api_gateway.model.AuthRequest;
import com.example.api_gateway.model.UserRequest;
import com.example.api_gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static com.example.api_gateway.messages.AuthenticationMessages.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final JwtService jwtService;

    private final RestTemplate restTemplate;


    /**
     * Login endpoint to validate user credentials and generate a JWT token.
     *
     * @param authRequest The authentication request containing username and password.
     * @return A ResponseEntity containing a JWT token if the authentication is successful, 401 Unauthorized if not.
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        ResponseEntity<Boolean> validationResponse;
        try {
            validationResponse = restTemplate.postForEntity("http://localhost:8081/api/v1/users/validate", authRequest, Boolean.class);
        } catch (RestClientResponseException exception) {
            String responseBody = exception.getResponseBodyAsString();
            HttpStatusCode statusCode = exception.getStatusCode();
            log.warn(AUTHENTICATION_FAILED, exception.getMessage());
            return ResponseEntity.status(statusCode).contentType(MediaType.APPLICATION_JSON).body(responseBody);
        } catch (ResourceAccessException exception) {
            log.error("Upstream user-service unavailable", exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"User service unavailable\"}");
        }
        if (validationResponse.getBody() != null && validationResponse.getBody()) {
            String token = jwtService.generateToken(authRequest.getUsername());
            log.info(LOGIN_SUCCESSFUL);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } else {
            log.warn(AUTHENTICATION_FAILED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Registers a new user to the system.
     *
     * @param userRequest The user registration request containing user details.
     * @return A ResponseEntity containing a JSON Web Token (JWT) representing the user
     * if the registration is successful, 401 Unauthorized if not.
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody UserRequest userRequest) {
        ResponseEntity<Boolean> validationResponse;
        try {
            validationResponse = restTemplate.postForEntity("http://localhost:8081/api/v1/users/register", userRequest, Boolean.class);
        } catch (RestClientResponseException exception) {
            String responseBody = exception.getResponseBodyAsString();
            HttpStatusCode statusCode = exception.getStatusCode();
            log.warn(REGISTRATION_FAILED, exception.getMessage());
            return ResponseEntity.status(statusCode).contentType(MediaType.APPLICATION_JSON).body(responseBody);
        } catch (ResourceAccessException exception) {
            log.error("Upstream user-service unavailable", exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"User service unavailable\"}");
        }

        if (validationResponse.getBody() != null && validationResponse.getBody()) {
            String token = jwtService.generateToken(userRequest.getUsername());
            log.info(REGISTRATION_SUCCESSFUL);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } else {
            log.error(REGISTRATION_FAILED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
