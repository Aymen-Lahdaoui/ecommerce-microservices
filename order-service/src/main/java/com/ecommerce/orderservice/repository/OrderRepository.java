package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    // Récupère uniquement l'historique des commandes payées pour un utilisateur
    List<Order> findByUserIdAndStatus(String userId, String status);
}