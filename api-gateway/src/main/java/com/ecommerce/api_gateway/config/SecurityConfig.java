package com.ecommerce.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()) // Désactive le CSRF sur la passerelle
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**").permitAll() // Laisse passer l'auth à travers la passerelle
                        .anyExchange().authenticated()
                );
        return http.build();
    }
}