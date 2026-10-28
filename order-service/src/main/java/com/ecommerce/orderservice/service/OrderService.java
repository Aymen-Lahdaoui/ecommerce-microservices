package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CartClient;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderHistory;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.repository.OrderHistoryRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private CartClient cartClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Order createOrderAndPay(String userId, OrderRequestDto orderRequest) {
        // 1. Récupérer le panier via Feign
        Map<String, Object> cart = (Map<String, Object>) cartClient.getCart(userId);

        if (cart == null || cart.get("items") == null) {
            throw new RuntimeException("Impossible de passer la commande : le panier est vide.");
        }

        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cart.get("items");
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Le panier ne contient aucun article.");
        }

        // 2. Convertir les items du panier en List<OrderItem> et calculer le prix total
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (Map<String, Object> itemMap : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId((String) itemMap.get("productId"));
            orderItem.setQuantity(Integer.parseInt(itemMap.getOrDefault("quantity", 1).toString()));
            orderItem.setPrice(Double.parseDouble(itemMap.getOrDefault("price", 0.0).toString()));

            orderItems.add(orderItem);
            totalPrice += orderItem.getPrice() * orderItem.getQuantity();
        }

        // 3. Préparer l'objet Commande
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);
        order.setOrderDate(LocalDateTime.now());

        // 4. Traitement du paiement
        boolean isPaymentSuccessful = processPayment(orderRequest);

        if (isPaymentSuccessful) {
            order.setStatus("PAID");
            order.setPaymentTransactionId(UUID.randomUUID().toString());

            // Sauvegarde de la commande classique
            Order savedOrder = orderRepository.save(order);

            // Sauvegarde dédiée dans la table/collection de l'historique (order_history)
            OrderHistory historyEntry = new OrderHistory();
            historyEntry.setUserId(userId);
            historyEntry.setItems(orderItems);
            historyEntry.setTotalPrice(totalPrice);
            historyEntry.setOrderDate(order.getOrderDate());
            historyEntry.setStatus("PAID");
            historyEntry.setPaymentTransactionId(order.getPaymentTransactionId());

            orderHistoryRepository.save(historyEntry);

            // 5. Publier l'événement de commande payée de manière asynchrone via RabbitMQ
            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    userId,
                    orderItems
            );

            rabbitTemplate.convertAndSend("order-events-exchange", "order.created", event);

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

    // 6. Récupérer l'historique des commandes réussies depuis la collection dédiée
    public List<OrderHistory> getUserSuccessfulOrders(String userId) {
        return orderHistoryRepository.findByUserId(userId);
    }
}