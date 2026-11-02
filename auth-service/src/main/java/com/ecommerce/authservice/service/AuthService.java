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
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Le nom d'utilisateur '" + request.getUsername() + "' est déjà pris.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null && !request.getRole().isBlank() ? request.getRole() : "USER");

        userRepository.save(user);
        return "Utilisateur '" + user.getUsername() + "' enregistré avec succès dans MongoDB !";
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + request.getUsername()));

        // Supporte à la fois les mots de passe encodés en BCrypt et ceux en texte brut
        boolean passwordMatches = false;
        if (user.getPassword() != null) {
            if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
                passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            } else {
                passwordMatches = user.getPassword().equals(request.getPassword());
            }
        }

        if (!passwordMatches) {
            throw new RuntimeException("Mot de passe incorrect.");
        }

        // 🔑 Génération et retour du token JWT en passant le username ET le role
        return jwtService.generateToken(user.getUsername(), user.getRole());
    }
}
