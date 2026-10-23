package com.ecommerce.authservice;

import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin_user").isEmpty()) {
                User user = new User();
                user.setUsername("admin_user");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole("ADMIN");
                userRepository.save(user);
                System.out.println(">>> Utilisateur admin_user créé avec succès !");
            }
        };
    }

}