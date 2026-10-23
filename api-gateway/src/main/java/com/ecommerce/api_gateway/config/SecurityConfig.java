package com.ecommerce.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // 📌 Important car l'API Gateway tourne sur WebFlux (Reactive) et non Web MVC
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()) // Désactive le CSRF
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll() // 🔓 Laisse passer tout à travers le Gateway (le filtrage JWT se fait via ton GatewayFilter)
                );
        return http.build();
    }
}