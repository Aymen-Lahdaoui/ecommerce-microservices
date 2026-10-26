package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {

    // Récupérer l'historique des commandes pour un utilisateur donné
    List<OrderHistory> findByUserId(String userId);
}