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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    // ── Commande & Paiement protégés par Circuit Breaker et Retry ────────────
    @CircuitBreaker(name = "cartService", fallbackMethod = "createOrderFallback")
    @Retry(name = "cartService")
    public Order createOrderAndPay(String userId, OrderRequestDto orderRequest) {
        // 1. Récupérer le panier via Feign
        System.out.println(">>> OrderService: Récupération du panier pour userId : [" + userId + "]");
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

            // Sauvegarde de la commande
            Order savedOrder = orderRepository.save(order);

            // Sauvegarde dans l'historique
            OrderHistory historyEntry = new OrderHistory();
            historyEntry.setUserId(userId);
            historyEntry.setItems(orderItems);
            historyEntry.setTotalPrice(totalPrice);
            historyEntry.setOrderDate(order.getOrderDate());
            historyEntry.setStatus("PAID");
            historyEntry.setPaymentTransactionId(order.getPaymentTransactionId());
            orderHistoryRepository.save(historyEntry);

            // 5. Vider le panier après une commande réussie via Feign
            try {
                System.out.println(">>> OrderService: Appel direct cartClient.clearCart pour userId : [" + userId + "]");
                cartClient.clearCart(userId);
                System.out.println(">>> OrderService: cartClient.clearCart réussi !");
            } catch (Exception e) {
                System.err.println(">>> Avertissement : échec vidage panier via Feign : " + e.getMessage());
                e.printStackTrace();
            }

            // 6. Publier l'événement RabbitMQ (décrémentation du stock et vidage async panier)
            List<OrderCreatedEvent.OrderItemDto> eventItems = new ArrayList<>();
            for (OrderItem oi : orderItems) {
                eventItems.add(new OrderCreatedEvent.OrderItemDto(
                        oi.getProductId(),
                        oi.getQuantity(),
                        oi.getPrice()
                ));
            }
            OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), userId, eventItems);
            System.out.println(">>> OrderService: Publication événement order.created sur RabbitMQ...");
            rabbitTemplate.convertAndSend("order-events-exchange", "order.created", event);

            return savedOrder;

        } else {
            // Paiement échoué → panier conservé intact
            order.setStatus("PAYMENT_FAILED");
            return orderRepository.save(order);
        }
    }

    /**
     * Méthode de Fallback exécutée automatiquement par Resilience4j quand :
     * - cart-service est éteint ou ne répond pas
     * - le circuit breaker passe en statut OUVERT (Open State)
     */
    public Order createOrderFallback(String userId, OrderRequestDto orderRequest, Throwable throwable) {
        System.err.println(">>> [CIRCUIT BREAKER - order-service] Échec d'accès au cart-service : " + throwable.getMessage());
        throw new RuntimeException("Le service de panier est temporairement inaccessible. Votre commande n'a pas été débitée. Veuillez réessayer dans quelques instants.");
    }

    private boolean processPayment(OrderRequestDto request) {
        if (request == null || request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            return false;
        }
        return request.getCardNumber().trim().length() == 16;
    }

    // Récupérer l'historique des commandes réussies depuis la collection dédiée
    public List<OrderHistory> getUserSuccessfulOrders(String userId) {
        return orderHistoryRepository.findByUserId(userId);
    }
}
