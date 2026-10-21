package com.ecommerce.authservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Injecte la clé secrète configurée dans application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Durée de validité du token : 24 heures (en millisecondes)
    private static final long EXPIRATION_TIME = 86400000;

    // Transforme la clé secrète texte en objet Key utilisable par JJWT (HMAC SHA)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Génère un token JWT pour un utilisateur donné
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Ajoute le rôle dans le payload du JWT

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username) // Le "subject" est le nom d'utilisateur
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
}