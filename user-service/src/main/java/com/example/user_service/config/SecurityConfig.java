package com.example.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Defines the security configuration for the application.
     * <p>
     * This configuration is very minimal. It disables CSRF protection and allows
     * any exchange to be permitted. This is suitable for a simple service that
     * doesn't handle sensitive data.
     *
     * @param http the server HTTP security configuration
     * @return the security web filter chain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(exchanges -> exchanges.anyExchange().permitAll()).build();
    }


    /**
     * Defines the password encoder bean for the application.
     * <p>
     * This bean is required for the user service to hash passwords properly.
     * <p>
     * The bean is configured to use the BCrypt password encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
