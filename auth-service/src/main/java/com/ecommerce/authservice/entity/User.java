package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identifiant unique de l'utilisateur (généré automatiquement)

    @Column(unique = true, nullable = false)
    private String username; // Nom d'utilisateur (doit être unique)

    @Column(nullable = false)
    private String email; // Adresse e-mail de l'utilisateur

    @Column(nullable = false)
    private String password; // Le mot de passe (qui sera stocké sous forme hachée avec BCrypt pour la sécurité)

    private String role; // Le rôle de l'utilisateur (ex: "USER" ou "ADMIN")
}