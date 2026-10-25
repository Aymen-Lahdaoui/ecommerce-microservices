package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CartClient;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartClient cartClient;

    @Autowired
    private ProductClient productClient; // 👈 Injecte le client pour gérer le stock

    public Order createOrderAndPay(String userId, OrderRequestDto orderRequest) {
        // 1. Récupérer le panier via Feign
        Map<String, Object> cart = (Map<String, Object>) cartClient.getCart(userId);

        if (cart == null || cart.get("items") == null) {
            throw new RuntimeException("Impossible de passer la commande : le panier est vide.");
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        if (items.isEmpty()) {
            throw new RuntimeException("Le panier ne contient aucun article.");
        }

        // 2. Calculer le prix total dynamiquement
        double totalPrice = 0.0;
        for (Map<String, Object> item : items) {
            double price = Double.parseDouble(item.getOrDefault("price", 0.0).toString());
            int quantity = Integer.parseInt(item.getOrDefault("quantity", 1).toString());
            totalPrice += price * quantity;
        }

        // 3. Préparer l'objet Commande
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(items);
        order.setTotalPrice(totalPrice);
        order.setOrderDate(LocalDateTime.now());

        // 4. Traitement du paiement
        boolean isPaymentSuccessful = processPayment(orderRequest);

        if (isPaymentSuccessful) {
            order.setStatus("PAID");
            order.setPaymentTransactionId(UUID.randomUUID().toString());

            // Sauvegarde de la commande
            Order savedOrder = orderRepository.save(order);

            // 5. Réduire le stock de chaque produit acheté via Feign
            for (Map<String, Object> item : items) {
                String productId = (String) item.get("productId");
                int quantity = Integer.parseInt(item.get("quantity").toString());

                productClient.reduceStock(productId, quantity);
            }

            // 6. Vider le panier via Feign si le paiement réussit
            cartClient.clearCart(userId);

            return savedOrder;
        } else {
            order.setStatus("PAYMENT_FAILED");
            return orderRepository.save(order);
        }
    }

    private boolean processPayment(OrderRequestDto request) {
        if (request == null || request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            return false;
        }
        return request.getCardNumber().trim().length() == 16;
    }
}