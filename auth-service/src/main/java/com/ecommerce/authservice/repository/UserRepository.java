package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Méthode magique générée par Spring : permet de chercher un utilisateur par son username
    Optional<User> findByUsername(String username);
}