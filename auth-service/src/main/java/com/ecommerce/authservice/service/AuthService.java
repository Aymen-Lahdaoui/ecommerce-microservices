package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.LoginRequest;
import com.ecommerce.authservice.dto.RegisterRequest;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole("USER");

        userRepository.save(user);
        return "Utilisateur enregistré avec succès dans MongoDB !";
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getPassword().equals(request.getPassword())) {
            return "Connexion réussie !";
        } else {
            throw new RuntimeException("Mot de passe incorrect");
        }
    }
}