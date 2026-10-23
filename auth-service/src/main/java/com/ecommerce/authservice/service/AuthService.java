    package com.ecommerce.authservice.service;
    import com.ecommerce.authservice.service.JwtService;
    import com.ecommerce.authservice.dto.LoginRequest;
    import com.ecommerce.authservice.dto.RegisterRequest;
    import com.ecommerce.authservice.entity.User;
    import com.ecommerce.authservice.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.crypto.password.PasswordEncoder; // Si tu utilises BCrypt
    import org.springframework.stereotype.Service;

    @Service
    public class AuthService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtService jwtService; // 👈 Injection de JwtService

        // Optionnel si tu encodes les mots de passe à l'inscription/connexion
        // @Autowired
        // private PasswordEncoder passwordEncoder;

        public String register(RegisterRequest request) {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            // user.setPassword(passwordEncoder.encode(request.getPassword())); // Encodage recommandé
            user.setPassword(request.getPassword());
            user.setRole("USER");

            userRepository.save(user);
            return "Utilisateur enregistré avec succès dans MongoDB !";
        }

        public String login(LoginRequest request) {
            // Recherche de l'utilisateur par son username
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérification du mot de passe (décommente si tu utilises l'encoder)
            // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            //     throw new RuntimeException("Mot de passe incorrect");
            // }

            // 🔑 Génération et retour du token JWT
            return jwtService.generateToken(user.getUsername());
        }
    }