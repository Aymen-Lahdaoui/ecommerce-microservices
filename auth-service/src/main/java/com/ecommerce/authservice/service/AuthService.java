package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.LoginRequest;
import com.ecommerce.authservice.dto.RegisterRequest;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Inscription d'un nouvel utilisateur
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Erreur : Ce nom d'utilisateur est déjà pris !");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Hachage sécurisé du mot de passe avec BCrypt
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");

        userRepository.save(user);
        return "Utilisateur enregistré avec succès !";
    }

    // Connexion d'un utilisateur et génération du JWT
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Erreur : Utilisateur introuvable !"));

        // Vérification du mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Erreur : Mot de passe incorrect !");
        }

        // Génération du token JWT si les identifiants sont correct
        return jwtService.generateToken(user.getUsername(), user.getRole());
    }
}