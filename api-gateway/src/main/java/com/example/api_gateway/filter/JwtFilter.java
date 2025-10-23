package com.example.api_gateway.filter;

import com.example.api_gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.example.api_gateway.messages.AuthenticationMessages.AUTHENTICATION_REQUEST;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter implements GlobalFilter {

    private final JwtService jwtService;


    /**
     * Checks if the request has a valid JWT token in the Authorization header.
     * If it does, it extracts the username from the token and adds it as an
     * X-Username header to the request. If the token is invalid, it sets the
     * response status to 401 Unauthorized.
     *
     * @param exchange the exchange object representing the current request
     * @param chain    the filter chain to continue with the next filter
     * @return a Mono that completes the filter chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (path.contains("/login") || path.contains("/register")) {
            log.info(AUTHENTICATION_REQUEST);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            log.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            log.warn("Invalid token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract username from token
        String username = jwtService.extractUsername(token);

        // Mutate request to add X-Username header
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Username", username)
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }
}
