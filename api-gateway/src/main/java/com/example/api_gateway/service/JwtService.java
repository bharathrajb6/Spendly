package com.example.api_gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET = "QmBfQdj4+vxDXSrrYfItNTkuszwFaTClm5PC3Y1SVj2HkdIJNzSqLXXL9+zJM76R";

    /**
     * Validate the given JWT token.
     *
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes())).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Generates a JWT token that can be used for authentication.
     * <p>
     * The generated token will contain the username as the subject, and will be valid for 1 hour.
     *
     * @param username The username to include in the token
     * @return a JWT token that can be used for authentication
     */
    public String generateToken(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)).signWith(Keys.hmacShaKeyFor(SECRET.getBytes())).compact();
    }

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token The token to extract the username from
     * @return The username if the token is valid, null otherwise
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
