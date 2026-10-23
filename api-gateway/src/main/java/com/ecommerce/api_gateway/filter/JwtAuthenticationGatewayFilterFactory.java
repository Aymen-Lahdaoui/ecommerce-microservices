package com.ecommerce.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.nio.charset.StandardCharsets;

@Component("JwtAuthentication")
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    @Value("${jwt.secret}")
    private String secretKey;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println(">>> 1. REQUÊTE REÇUE DANS LE FILTRE JWT !");
            ServerHttpRequest request = exchange.getRequest();

            // 1. Vérifier si le header Authorization est présent
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.out.println(">>> ERREUR : Header Authorization manquant !");
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            System.out.println(">>> Header Authorization trouvé : " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println(">>> ERREUR : Format du header invalide (ne commence pas par Bearer ) !");
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 2. Valider le token JWT avec la clé secrète
                System.out.println(">>> Tentative de validation du token...");
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSignKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                System.out.println(">>> SUCCÈS : Token JWT valide !");

            } catch (Exception e) {
                System.out.println(">>> EXCEPTION JWT CATCHÉE : " + e.getClass().getName() + " -> " + e.getMessage());
                return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
            }

            // 3. Si tout est valide, on laisse passer la requête vers le microservice en aval
            return chain.filter(exchange);
        };
    }

    private Key getSignKey() {
        // Utilisation du décodage Base64 pour correspondre rigoureusement à l'auth-service
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Classe de configuration vide nécessaire pour AbstractGatewayFilterFactory
    }
}